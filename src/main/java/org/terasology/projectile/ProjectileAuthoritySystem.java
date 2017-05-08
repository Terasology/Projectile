/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.projectile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.registry.In;

/**
 * Created by nikhil on 28/3/17.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ProjectileAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    final int TERMINAL_VELOCITY = 40;
    final float G = 20f;
    private static final Logger logger = LoggerFactory.getLogger(ProjectileAuthoritySystem.class);

    @In
    private InventoryManager inventoryManager;

    @In
    private Physics physicsRenderer;

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    private CollisionGroup filter = StandardCollisionGroup.ALL;
    private float lastTime;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity, ProjectileActionComponent projectileActionComponent) {

        if (time.getGameTime() > lastTime + 1.0f / projectileActionComponent.projectilesPerSecond) {
            int slot = InventoryUtils.getSlotWithItem(event.getInstigator(), entity);
            entity = inventoryManager.removeItem(event.getInstigator(), event.getInstigator(), slot, false, 1);
            projectileActionComponent = entity.getComponent(ProjectileActionComponent.class);
            projectileActionComponent.direction = new Vector3f(event.getDirection());
            projectileActionComponent.currentVelocity = new Vector3f(event.getDirection()).mul(projectileActionComponent.velocity);
            Vector3f pos = event.getOrigin();
            LocationComponent location = new LocationComponent(pos.add(projectileActionComponent.direction));
            location.setWorldScale(projectileActionComponent.iconScale);
            location.setWorldRotation(getRotationQuaternion(projectileActionComponent.initialOrientation, new Vector3f(event.getDirection())));
            entity.addOrSaveComponent(location);
            entity.saveComponent(projectileActionComponent);
            lastTime = time.getGameTime();
        }
    }

    /*
     * Rotates the projectile in the direction of motion
     */
    private Quat4f getRotationQuaternion(Vector3f initialDir, Vector3f finalDir){
        // rotates the entity to face in the direction of pointer
        Quat4f rotation = new Quat4f();
        Vector3f crossProduct = new Vector3f();
        crossProduct.cross(initialDir, finalDir);
        rotation.x = crossProduct.x;
        rotation.y = crossProduct.y;
        rotation.z = crossProduct.z;
        rotation.w = (float) (Math.sqrt((initialDir.lengthSquared())*(finalDir.lengthSquared())) +
                initialDir.dot(finalDir));
        rotation.normalize();
        return rotation;
    }

    /*
     * Deactivates the projectile ( currently not used )
     */
    @ReceiveEvent
    public void onDeactivate(DeactivateProjectileEvent event, EntityRef entity, ProjectileActionComponent projectile){
        projectile.direction = null;
        projectile.currentVelocity = null;
        projectile.distanceTravelled = 0;
        entity.saveComponent(projectile);
        entity.send(new DropItemEvent(entity.getComponent(LocationComponent.class).getWorldPosition()));
    }
    /*
     * Updates the state of fired projectiles
     */
    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(ProjectileActionComponent.class)) {
            ProjectileActionComponent projectile = entity.getComponent(ProjectileActionComponent.class);
            if(projectile.direction == null || entity.getComponent(LocationComponent.class) == null) // not been fired
                continue;
            if(projectile.distanceTravelled >= projectile.maxDistance && projectile.maxDistance != -1) {
                if(projectile.reusable)
                    entity.send(new DeactivateProjectileEvent());
                else
                    entity.destroy();
                continue;
            }

            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f position = location.getWorldPosition();
            projectile.direction = new Vector3f(projectile.currentVelocity).normalize();
            HitResult result;
            float displacement = projectile.currentVelocity.length();
            result = physicsRenderer.rayTrace(position, projectile.direction, displacement, filter);

            if(result.isHit()) {
                EntityRef blockEntity = result.getEntity();
                if(!blockEntity.hasComponent(HealthComponent.class)){
                    // a hack to induce a HealthComponent in the blockEntity
                    blockEntity.send(new DoDamageEvent(0, projectile.damageType));
                    if(!blockEntity.hasComponent(HealthComponent.class)) {
                        // if it still doesn't have a health component, it's indestructible
                        // so destroy our projectile
                        if(projectile.reusable)
                            entity.send(new DeactivateProjectileEvent());
                        else
                            entity.destroy();
                        continue;
                    }
                }
                entity.send(new HitTargetEvent(blockEntity, entity, new Vector3f(),
                        projectile.direction, result.getHitPoint(), result.getHitNormal()));
                if(!entity.exists() || entity.getComponent(ProjectileActionComponent.class).direction == null) {
                    continue;
                }
            }

            position.add(projectile.currentVelocity);
            location.setWorldPosition(position);
            location.setWorldRotation(getRotationQuaternion(projectile.initialOrientation, projectile.currentVelocity));
            projectile.distanceTravelled += displacement;

            if(projectile.affectedByGravity && Math.abs(projectile.currentVelocity.getY()) < TERMINAL_VELOCITY) {
                float update = G * (float) Math.pow(delta, 2);
                projectile.currentVelocity.subY(update);
            }


            entity.addOrSaveComponent(location);
            entity.addOrSaveComponent(projectile);
        }

    }
}

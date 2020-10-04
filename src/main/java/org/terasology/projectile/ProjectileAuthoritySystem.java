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

import org.joml.Quaternionf;
import org.joml.Vector3f;
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
import org.terasology.logic.health.event.DoDamageEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ProjectileAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(ProjectileAuthoritySystem.class);
    public static final int TERMINAL_VELOCITY = 40;
    public static final float G = 1f;
    @In
    private InventoryManager inventoryManager;

    @In
    private Physics physicsRenderer;

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD, StandardCollisionGroup.CHARACTER};
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
            EntityRef inventoryEntity = inventoryManager.removeItem(event.getInstigator(), event.getInstigator(),
                slot, false, 1);
            lastTime = time.getGameTime();
            inventoryEntity.send(new FireProjectileEvent(event.getOrigin(), event.getDirection()));
        }
    }

    @ReceiveEvent
    public void onFire(FireProjectileEvent event, EntityRef entity, ProjectileActionComponent projectileActionComponent) {
        ProjectileMotionComponent projectileMotionComponent = new ProjectileMotionComponent();
        projectileMotionComponent.direction = new Vector3f(event.getDirection());
        projectileMotionComponent.currentVelocity = new Vector3f(event.getDirection()).mul(projectileActionComponent.initialVelocity);
        Vector3f pos = event.getOrigin();
        LocationComponent location = new LocationComponent(JomlUtil.from(pos));
        location.setWorldScale(projectileActionComponent.iconScale);
        location.setWorldRotation(getRotationQuaternion(projectileActionComponent.initialOrientation, new Vector3f(event.getDirection())));
        entity.addOrSaveComponent(location);
        entity.addComponent(projectileMotionComponent);
        entity.saveComponent(projectileActionComponent);
    }

    /**
     * Rotates the projectile in the direction of motion
     */
    private Quaternionf getRotationQuaternion(Vector3f initialDir, Vector3f finalDir) {
        // rotates the entity to face in the direction of pointer
        Quaternionf rotation = new Quaternionf();
        Vector3f crossProduct = new Vector3f();
        initialDir.cross(finalDir, crossProduct);
        rotation.x = crossProduct.x;
        rotation.y = crossProduct.y;
        rotation.z = crossProduct.z;
        rotation.w = (float) (Math.sqrt((initialDir.lengthSquared()) * (finalDir.lengthSquared())) +
            initialDir.dot(finalDir));
        rotation.normalize();
        return rotation;
    }

    /**
     * Deactivates the projectile and drops it as an item
     */
    @ReceiveEvent
    public void onDeactivate(DeactivateProjectileEvent event, EntityRef entity, ProjectileMotionComponent projectileMotion) {
        entity.removeComponent(ProjectileMotionComponent.class);
        entity.send(new DropItemEvent(entity.getComponent(LocationComponent.class).getWorldPosition()));
    }

    /**
     * Updates the state of fired projectiles
     */
    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(ProjectileMotionComponent.class)) {
            ProjectileActionComponent projectile = entity.getComponent(ProjectileActionComponent.class);
            ProjectileMotionComponent projectileMotion = entity.getComponent(ProjectileMotionComponent.class);

            if (projectileMotion.distanceTravelled >= projectile.maxDistance && projectile.maxDistance != -1) {
                if (projectile.reusable) {
                    entity.send(new DeactivateProjectileEvent());
                } else {
                    entity.destroy();
                }
                continue;
            }

            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f position = location.getWorldPosition(new Vector3f());
            projectileMotion.direction = new Vector3f(projectileMotion.currentVelocity).normalize();
            HitResult result;
            float displacement = Math.min(projectileMotion.currentVelocity.length() * delta,
                    projectile.maxDistance - projectileMotion.distanceTravelled);
            // 0.1 is added so that raytraces are inclusive of the endpoint
            result = physicsRenderer.rayTrace(position, projectileMotion.direction, displacement + .01f, filter);

            if (result.isHit()) {
                EntityRef targetEntity = result.getEntity();
                if (!targetEntity.hasComponent(HealthComponent.class)) {
                    // a hack to induce a HealthComponent in the blockEntity
                    targetEntity.send(new DoDamageEvent(0, projectile.damageType));
                    if (!targetEntity.hasComponent(HealthComponent.class)) {
                        // if it still doesn't have a health component, it's indestructible
                        // so destroy our projectile
                        if (projectile.reusable) {
                            entity.send(new DeactivateProjectileEvent());
                        } else {
                            entity.destroy();
                        }
                        continue;
                    }
                }
                location.setWorldPosition(JomlUtil.from(result.getHitPoint()));
                entity.saveComponent(location);
                entity.send(new HitTargetEvent(targetEntity, entity, new Vector3f(),
                        projectileMotion.direction, result.getHitPoint(), result.getHitNormal()));
                if (!entity.exists() || !entity.hasComponent(ProjectileMotionComponent.class)) {
                    continue;
                }
            }

            position.add(new Vector3f(projectileMotion.currentVelocity).mul(delta));
            location.setWorldPosition(position);
            location.setWorldRotation(getRotationQuaternion(projectile.initialOrientation, projectileMotion.currentVelocity));
            projectileMotion.distanceTravelled += displacement;

            if (projectile.affectedByGravity && Math.abs(projectileMotion.currentVelocity.y()) < TERMINAL_VELOCITY) {
                float update = G * delta;
                projectileMotion.currentVelocity.y -= update;
            }


            entity.addOrSaveComponent(location);
            entity.addOrSaveComponent(projectile);

            entity.send(new ProjectileUpdateEvent());
        }

    }
}

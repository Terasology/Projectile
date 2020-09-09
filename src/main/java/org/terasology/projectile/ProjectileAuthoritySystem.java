// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.inventory.events.DropItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.HitResult;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.registry.In;
import org.terasology.health.logic.HealthComponent;
import org.terasology.health.logic.event.DoDamageEvent;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.inventory.logic.InventoryUtils;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ProjectileAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(ProjectileAuthoritySystem.class);
    final int TERMINAL_VELOCITY = 40;
    final float G = 1f;
    private final CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD,
            StandardCollisionGroup.CHARACTER};
    @In
    private InventoryManager inventoryManager;
    @In
    private Physics physicsRenderer;
    @In
    private EntityManager entityManager;
    @In
    private Time time;
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
            lastTime = time.getGameTime();
            entity.send(new FireProjectileEvent(event.getOrigin(), event.getDirection()));
        }
    }

    @ReceiveEvent
    public void onFire(FireProjectileEvent event, EntityRef entity,
                       ProjectileActionComponent projectileActionComponent) {
        ProjectileMotionComponent projectileMotionComponent = new ProjectileMotionComponent();
        projectileMotionComponent.direction = new Vector3f(event.getDirection());
        projectileMotionComponent.currentVelocity =
                new Vector3f(event.getDirection()).mul(projectileActionComponent.initialVelocity);
        Vector3f pos = event.getOrigin();
        LocationComponent location = new LocationComponent(pos);
        location.setWorldScale(projectileActionComponent.iconScale);
        location.setWorldRotation(getRotationQuaternion(projectileActionComponent.initialOrientation,
                new Vector3f(event.getDirection())));
        entity.addOrSaveComponent(location);
        entity.addComponent(projectileMotionComponent);
        entity.saveComponent(projectileActionComponent);
    }

    /**
     * Rotates the projectile in the direction of motion
     */
    private Quat4f getRotationQuaternion(Vector3f initialDir, Vector3f finalDir) {
        // rotates the entity to face in the direction of pointer
        Quat4f rotation = new Quat4f();
        Vector3f crossProduct = new Vector3f();
        crossProduct.cross(initialDir, finalDir);
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
    public void onDeactivate(DeactivateProjectileEvent event, EntityRef entity,
                             ProjectileMotionComponent projectileMotion) {
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
            Vector3f position = location.getWorldPosition();
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
                        if (projectile.reusable)
                            entity.send(new DeactivateProjectileEvent());
                        else
                            entity.destroy();
                        continue;
                    }
                }
                location.setWorldPosition(result.getHitPoint());
                entity.saveComponent(location);
                entity.send(new HitTargetEvent(targetEntity, entity, new Vector3f(),
                        projectileMotion.direction, result.getHitPoint(), result.getHitNormal()));
                if (!entity.exists() || !entity.hasComponent(ProjectileMotionComponent.class)) {
                    continue;
                }
            }

            position.add(new Vector3f(projectileMotion.currentVelocity).mul(delta));
            location.setWorldPosition(position);
            location.setWorldRotation(getRotationQuaternion(projectile.initialOrientation,
                    projectileMotion.currentVelocity));
            projectileMotion.distanceTravelled += displacement;

            if (projectile.affectedByGravity && Math.abs(projectileMotion.currentVelocity.getY()) < TERMINAL_VELOCITY) {
                float update = G * delta;
                projectileMotion.currentVelocity.subY(update);
            }


            entity.addOrSaveComponent(location);
            entity.addOrSaveComponent(projectile);

            entity.send(new ProjectileUpdateEvent());
        }

    }
}

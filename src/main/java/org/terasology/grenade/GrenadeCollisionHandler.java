// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.grenade;

import com.google.common.collect.Lists;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.explosives.logic.ExplosionActionComponent;
import org.terasology.health.logic.event.DoDamageEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.projectile.HitTargetEvent;
import org.terasology.projectile.ProjectileActionComponent;

import java.util.List;
import java.util.Optional;

/*
 * TODO : Almost exactly copied from ExplosionAuthoritySystem. Unable to use that as ActivateEvent
 * is tightly coupled to localPlayer, has to be a better way to do this.
 */

@RegisterSystem(RegisterMode.AUTHORITY)
public class GrenadeCollisionHandler extends BaseComponentSystem {

    private final Random random = new FastRandom();
    private final List<Optional<StaticSound>> explosionSounds = Lists.newArrayList();
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;
    @In
    private BlockManager blockManager;

    @Override
    public void initialise() {
        explosionSounds.add(Assets.getSound("CoreAssets:explode1"));
        explosionSounds.add(Assets.getSound("CoreAssets:explode2"));
        explosionSounds.add(Assets.getSound("CoreAssets:explode3"));
        explosionSounds.add(Assets.getSound("CoreAssets:explode4"));
        explosionSounds.add(Assets.getSound("CoreAssets:explode5"));
    }

    @ReceiveEvent(components = {GrenadeComponent.class})
    public void onCollision(HitTargetEvent event, EntityRef entity, ProjectileActionComponent projectile) {
        EntityRef blockEntity = event.getTarget();
        ExplosionActionComponent explosionActionComponent = new ExplosionActionComponent();
        doExplosion(explosionActionComponent, event.getTargetLocation(), EntityRef.NULL);
        entity.destroy();
        event.consume();
    }

    private StaticSound getRandomExplosionSound() {
        return explosionSounds.get(random.nextInt(0, explosionSounds.size() - 1)).get();
    }

    void doExplosion(ExplosionActionComponent explosionComp, Vector3f origin, EntityRef instigatingBlockEntity) {
        EntityBuilder builder = entityManager.newBuilder("CoreAssets:smokeExplosion");
        builder.getComponent(LocationComponent.class).setWorldPosition(origin);
        EntityRef smokeEntity = builder.build();

        smokeEntity.send(new PlaySoundEvent(getRandomExplosionSound(), 1f));

        Vector3i blockPos = new Vector3i();
        for (int i = 0; i < explosionComp.maxRange; i++) {
            Vector3f direction = random.nextVector3f(1.0f);

            for (int j = 0; j < 4; j++) {
                Vector3f target = new Vector3f(origin);

                target.x += direction.x * j;
                target.y += direction.y * j;
                target.z += direction.z * j;
                blockPos.set((int) target.x, (int) target.y, (int) target.z);
                Block currentBlock = worldProvider.getBlock(blockPos);

                /* PHYSICS */
                if (currentBlock.isDestructible()) {
                    EntityRef blockEntity = blockEntityRegistry.getEntityAt(blockPos);
                    // allow explosions to chain together,  but do not chain on the instigating block
                    if (!blockEntity.equals(instigatingBlockEntity) && blockEntity.hasComponent(ExplosionActionComponent.class)) {
                        doExplosion(blockEntity.getComponent(ExplosionActionComponent.class), blockPos.toVector3f(),
                                blockEntity);
                    } else {
                        blockEntity.send(new DoDamageEvent(explosionComp.damageAmount, explosionComp.damageType));
                    }
                }
            }
        }
    }
}

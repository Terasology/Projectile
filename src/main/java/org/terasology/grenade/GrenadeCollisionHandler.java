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
package org.terasology.grenade;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.event.DoDamageEvent;
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

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private BlockManager blockManager;

    private Random random = new FastRandom();
    private List<Optional<StaticSound>> explosionSounds = Lists.newArrayList();


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
            Vector3f direction = random.nextVector3f(1.0f, new Vector3f());

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
                        doExplosion(blockEntity.getComponent(ExplosionActionComponent.class), new Vector3f(blockPos),
                            blockEntity);
                    } else {
                        blockEntity.send(new DoDamageEvent(explosionComp.damageAmount, explosionComp.damageType));
                    }
                }
            }
        }
    }
}

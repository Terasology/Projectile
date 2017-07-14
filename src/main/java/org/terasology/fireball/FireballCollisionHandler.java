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
package org.terasology.fireball;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.projectile.HitTargetEvent;
import org.terasology.projectile.ProjectileActionComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class FireballCollisionHandler extends BaseComponentSystem {
    @ReceiveEvent(components = {FireballComponent.class})
    public void onCollision(HitTargetEvent event, EntityRef entity, ProjectileActionComponent projectile) {
        EntityRef targetEntity = event.getTarget();
        HealthComponent health = entity.getComponent(HealthComponent.class);
        int oldTargetHealth = targetEntity.getComponent(HealthComponent.class).currentHealth;
        targetEntity.send(new DoDamageEvent(health.currentHealth, projectile.damageType));
        int newTargetHealth = 0;
        if (targetEntity.exists())
            newTargetHealth = targetEntity.getComponent(HealthComponent.class).currentHealth;
        // inflict same amount of damage on fireball as on the target
        entity.send(new DoDamageEvent(oldTargetHealth - newTargetHealth, projectile.damageType));

        if (entity.exists()) {
            ParticleEmitterComponent particleEmitter = entity.getComponent(ParticleEmitterComponent.class);
            particleEmitter.maxParticles -= (oldTargetHealth - newTargetHealth) / health.maxHealth
                    * particleEmitter.spawnRateMax;
        }

        event.consume();
    }
}

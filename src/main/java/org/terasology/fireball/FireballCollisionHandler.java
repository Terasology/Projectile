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
import org.terasology.projectile.HitTargetEvent;
import org.terasology.projectile.ProjectileActionComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class FireballCollisionHandler extends BaseComponentSystem {
    @ReceiveEvent(components = {FireballComponent.class})
    public void onCollision(HitTargetEvent event, EntityRef entity, ProjectileActionComponent projectile) {
        int oldFireballHealth = entity.getComponent(HealthComponent.class).currentHealth;
        // The fireball takes as much damage as it can inflict to the collision target
        // Maximum damage a fireball can inflict is its health
        entity.send(new DoDamageEvent(projectile.damageAmount, projectile.damageType));
        int newFireballHealth = 0;
        if(entity.exists())
            newFireballHealth = entity.getComponent(HealthComponent.class).currentHealth;
        EntityRef blockEntity = event.getTarget();
        // Inflict as much damage to the target as taken by fireball
        blockEntity.send(new DoDamageEvent(oldFireballHealth - newFireballHealth, projectile.damageType));
        event.consume();
    }
}

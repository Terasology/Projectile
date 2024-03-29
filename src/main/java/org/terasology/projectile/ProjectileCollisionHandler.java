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

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.health.events.DoDamageEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ProjectileCollisionHandler extends BaseComponentSystem {
    // Set handler to low priority as it contains override-able default behaviour
    @Priority(EventPriority.PRIORITY_LOW)
    @ReceiveEvent
    public void onCollision(HitTargetEvent event, EntityRef entity, ProjectileActionComponent projectile) {
        event.getTarget().send(new DoDamageEvent(projectile.damageAmount, projectile.damageType));
        //reset ProjectileActionComponent to defaults and drop item
        entity.send(new DeactivateProjectileEvent());
    }
}

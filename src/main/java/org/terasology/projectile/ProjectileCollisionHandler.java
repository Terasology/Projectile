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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.location.LocationComponent;

/**
 * Created by nikhil on 1/4/17.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ProjectileCollisionHandler extends BaseComponentSystem {
    // Set handler to low priority as it contains override-able default behaviour
    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void onCollision(HitTargetEvent event, EntityRef entity, ProjectileActionComponent projectile) {
        event.getTarget().send(new DoDamageEvent(projectile.damageAmount, projectile.damageType));
        //reset ProjectileActionComponent to defaults and drop item
        projectile.direction = null;
        projectile.currentVelocity = null;
        projectile.distanceTravelled = 0;
        entity.saveComponent(projectile);
        entity.send(new DropItemEvent(entity.getComponent(LocationComponent.class).getWorldPosition()));
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.health.logic.event.DoDamageEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ProjectileCollisionHandler extends BaseComponentSystem {
    // Set handler to low priority as it contains override-able default behaviour
    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void onCollision(HitTargetEvent event, EntityRef entity, ProjectileActionComponent projectile) {
        event.getTarget().send(new DoDamageEvent(projectile.damageAmount, projectile.damageType));
        //reset ProjectileActionComponent to defaults and drop item
        entity.send(new DeactivateProjectileEvent());
    }
}

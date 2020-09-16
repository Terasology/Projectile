// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile.fireball;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.particles.ParticleSystemManager;
import org.terasology.engine.particles.components.ParticleEmitterComponent;
import org.terasology.engine.particles.events.ParticleSystemUpdateEvent;
import org.terasology.engine.particles.functions.RegisterParticleSystemFunction;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.projectile.FireProjectileEvent;
import org.terasology.projectile.ProjectileActionComponent;
import org.terasology.projectile.particleAffectors.AttractorAffectorComponent;

@RegisterSystem(RegisterMode.CLIENT)
@RegisterParticleSystemFunction()
public class FireballParticleHandlerSystem extends BaseComponentSystem {

    @In
    ParticleSystemManager particleSystemManager;


    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW, components = {FireballComponent.class})
    public void onFire(FireProjectileEvent event, EntityRef entity,
                       ProjectileActionComponent projectileActionComponent) {
        ParticleEmitterComponent particleEmitterComponent = entity.getComponent(ParticleEmitterComponent.class);
        particleEmitterComponent.enabled = true;
        Vector3f negDirection = new Vector3f(event.getDirection()).normalize().negate();

        AttractorAffectorComponent attractorAffector = new AttractorAffectorComponent();
        attractorAffector.origin = entity.getComponent(LocationComponent.class);
        attractorAffector.attractors.put(new Vector3f(0, 0, 0), -.1f);
        attractorAffector.attractors.put(new Vector3f(negDirection).scale(.1f), -.3f);

        entity.addComponent(attractorAffector);

        entity.removeComponent(ParticleEmitterComponent.class);
        entity.addComponent(particleEmitterComponent);
        entity.send(new ParticleSystemUpdateEvent());
        entity.removeComponent(MeshComponent.class);
    }


}

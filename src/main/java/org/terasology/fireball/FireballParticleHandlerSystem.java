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

import org.joml.Vector3f;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.particles.ParticleSystemManager;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.particles.events.ParticleSystemUpdateEvent;
import org.terasology.particles.functions.RegisterParticleSystemFunction;
import org.terasology.projectile.FireProjectileEvent;
import org.terasology.projectile.ProjectileActionComponent;
import org.terasology.projectile.particleAffectors.AttractorAffectorComponent;
import org.terasology.registry.In;
import org.terasology.rendering.logic.MeshComponent;

@RegisterSystem(RegisterMode.CLIENT)
@RegisterParticleSystemFunction()
public class FireballParticleHandlerSystem extends BaseComponentSystem {

    @In
    ParticleSystemManager particleSystemManager;


    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW, components = {FireballComponent.class})
    public void onFire(FireProjectileEvent event, EntityRef entity, ProjectileActionComponent projectileActionComponent) {
        ParticleEmitterComponent particleEmitterComponent = entity.getComponent(ParticleEmitterComponent.class);
        particleEmitterComponent.enabled = true;
        Vector3f negDirection = new Vector3f(event.getDirection()).normalize().negate();

        AttractorAffectorComponent attractorAffector = new AttractorAffectorComponent();
        attractorAffector.origin = entity.getComponent(LocationComponent.class);
        attractorAffector.attractors.put(new Vector3f(0, 0, 0), -.1f);
        attractorAffector.attractors.put(new Vector3f(negDirection).mul(.1f), -.3f);

        entity.addComponent(attractorAffector);

        entity.removeComponent(ParticleEmitterComponent.class);
        entity.addComponent(particleEmitterComponent);
        entity.send(new ParticleSystemUpdateEvent());
        entity.removeComponent(MeshComponent.class);
    }


}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile.particleAffectors;

import org.terasology.engine.math.JomlUtil;
import org.terasology.engine.particles.ParticleData;
import org.terasology.engine.particles.ParticleDataMask;
import org.terasology.engine.particles.functions.affectors.AffectorFunction;
import org.terasology.engine.utilities.random.Random;
import org.terasology.math.geom.Vector3f;

import java.util.Map;

public class AttractorAffectorFunction extends AffectorFunction<AttractorAffectorComponent> {
    static final float EPS = 1e-2f;

    public AttractorAffectorFunction() {
        super(ParticleDataMask.VELOCITY, ParticleDataMask.ENERGY);
    }

    @Override
    public void update(final AttractorAffectorComponent component,
                       final ParticleData particleData,
                       final Random random,
                       final float delta
    ) {
        if (component.origin == null) {
            return;
        }

        Vector3f particlePos = JomlUtil.from(particleData.position);
        for (Map.Entry<Vector3f, Float> attractor : component.attractors.entrySet()) {
            final Vector3f attractorOffset = attractor.getKey();
            float strength = attractor.getValue();
            Vector3f attractorPos = new Vector3f(attractorOffset).add(component.origin.getWorldPosition());
            Vector3f displacementVector = attractorPos.sub(particlePos);
            float displacementSquared = displacementVector.lengthSquared();
            Vector3f acceleration = new Vector3f();

            if (strength > 0) {
                if (displacementSquared < EPS) {
                    particleData.energy = 0;
                    continue;
                }
                acceleration = new Vector3f(displacementVector).normalize().div(displacementSquared);
            } else if (strength < 0) {
                strength = -strength;
                if (displacementSquared == 0) {
                    displacementVector = new Vector3f(random.nextFloat(-.1f, .1f), random.nextFloat(-.1f, .1f),
                            random.nextFloat(-.1f, .1f));
                    displacementSquared = displacementVector.lengthSquared();
                }
                acceleration = new Vector3f(displacementVector).normalize().div(displacementSquared).invert();
            }

            acceleration.mul(strength);
            particleData.velocity.add(
                    JomlUtil.from(acceleration.mul(delta))
            );
        }
    }
}

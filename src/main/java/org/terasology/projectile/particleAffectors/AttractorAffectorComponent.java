// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile.particleAffectors;

import org.joml.Vector3f;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashMap;
import java.util.Map;

public class AttractorAffectorComponent implements Component<AttractorAffectorComponent> {

    @Replicate
    public Map<Vector3f, Float> attractors;

    @Replicate
    public LocationComponent origin;

    public AttractorAffectorComponent() {
        this.attractors = new HashMap<>();
    }

    @Override
    public void copy(AttractorAffectorComponent other) {
        this.attractors.clear();
        for (Map.Entry<Vector3f, Float> entry : other.attractors.entrySet()) {
            this.attractors.put(new Vector3f(entry.getKey()), entry.getValue());
        }

        this.origin.copy(other.origin);
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile.particleAffectors;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.Replicate;
import org.terasology.math.geom.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class AttractorAffectorComponent implements Component {

    @Replicate
    public Map<Vector3f, Float> attractors;

    @Replicate
    public LocationComponent origin;

    public AttractorAffectorComponent() {
        this.attractors = new HashMap<>();
    }

}

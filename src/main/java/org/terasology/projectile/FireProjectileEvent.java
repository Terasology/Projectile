// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.math.geom.Vector3f;

public class FireProjectileEvent implements Event {
    private final Vector3f origin;
    private final Vector3f direction;

    public FireProjectileEvent(Vector3f origin, Vector3f direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getDirection() {
        return direction;
    }
}

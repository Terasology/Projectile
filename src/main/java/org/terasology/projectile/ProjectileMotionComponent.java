// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile;

import org.joml.Vector3f;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Component attached to projectiles which are in state of motion ,i.e have been fired.
 */
public class ProjectileMotionComponent implements Component<ProjectileMotionComponent> {

    /**
     * The current velocity of projectile.
     */
    public Vector3f currentVelocity = null;

    /**
     * The direction of the projectile.
     */
    public Vector3f direction = null;

    /**
     * The distance travelled by the projectile.
     */
    public float distanceTravelled = 0;

    @Override
    public void copy(ProjectileMotionComponent other) {
        this.currentVelocity = new Vector3f(other.currentVelocity);
        this.direction = new Vector3f(other.direction);
        this.distanceTravelled = other.distanceTravelled;
    }
}

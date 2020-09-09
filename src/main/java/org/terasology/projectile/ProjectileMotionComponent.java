// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile;

import org.terasology.engine.entitySystem.Component;
import org.terasology.math.geom.Vector3f;

/**
 * Component attached to projectiles which are in state of motion ,i.e have been fired.
 */
public class ProjectileMotionComponent implements Component {

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

}

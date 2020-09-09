// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.destruction.EngineDamageTypes;
import org.terasology.math.geom.Vector3f;

public class ProjectileActionComponent implements Component {
    /**
     * scaling the projectile icon
     */
    public float iconScale = 1f;

    /**
     * the initial orientation of projectile (must be final) for eg. for the spear projectile, the icon faces up, so the
     * initial orientation will be (0,1,0)
     */
    public Vector3f initialOrientation = new Vector3f(0, 1, 0);

    /**
     * is reusable?
     */
    public boolean reusable = false;

    /**
     * Whether projectile is affected by gravity.
     */
    public boolean affectedByGravity = false;

    /**
     * The resistance against movement of projectile (proportional to surface area and how aerodynamic the projectile
     * is).
     */
    public float frictionCoefficient = 0.1f;

    /**
     * The velocity of the projectile.
     */
    public float initialVelocity = 20;

    /**
     * The max distance the projectile will fly.
     */
    public int maxDistance = 24;

    /**
     * The damage the projectile does
     */
    public int damageAmount = 3;

    /**
     * How many projectiles can be fired per second
     */
    public float projectilesPerSecond = 1.0f;

    public Prefab damageType = EngineDamageTypes.PHYSICAL.get();
}

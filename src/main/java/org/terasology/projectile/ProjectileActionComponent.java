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
package org.terasology.projectile;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.math.geom.Vector3f;

public class ProjectileActionComponent implements Component{
    /**
     * scaling the projectile icon
     */
    public float iconScale = 1f;

    /**
     * the initial orientation of projectile (must be final)
     * for eg. for the spear projectile, the icon faces up, so
     * the initial orientation will be (0,1,0)
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
     * The resistance against movement of projectile (proportional to surface area
     * and how aerodynamic the projectile is).
     */
    public float frictionCoefficient = 0.1f;

    /**
     * The velocity of the projectile.
     */
    public float initialVelocity = 1;

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

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

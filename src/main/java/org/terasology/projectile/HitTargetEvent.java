// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.projectile;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;

public class HitTargetEvent extends AbstractConsumableEvent {
    private final EntityRef instigator;
    private final EntityRef target;
    private final Vector3f origin;
    private final Vector3f direction;
    private final Vector3f hitPosition;
    private final Vector3f hitNormal;
    private int activationId;

    public HitTargetEvent(EntityRef target, EntityRef instigator, Vector3f origin, Vector3f direction,
                          Vector3f hitPosition, Vector3f hitNormal) {
        this.instigator = instigator;
        this.target = target;
        this.direction = direction;
        this.hitPosition = hitPosition;
        this.hitNormal = hitNormal;
        this.origin = origin;
        this.activationId = activationId;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getTarget() {
        return target;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getHitPosition() {
        return hitPosition;
    }

    public Vector3f getHitNormal() {
        return hitNormal;
    }

    public int getActivationId() {
        return activationId;
    }

    public Vector3f getTargetLocation() {
        LocationComponent loc = target.getComponent(LocationComponent.class);
        if (loc != null) {
            return loc.getWorldPosition();
        }
        return null;
    }

    public Vector3f getInstigatorLocation() {
        LocationComponent loc = instigator.getComponent(LocationComponent.class);
        if (loc != null) {
            return loc.getWorldPosition();
        }
        return new Vector3f();
    }
}

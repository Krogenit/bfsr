package org.jbox2d.callbacks;

import org.jbox2d.common.Vector2;

public interface ParticleRaycastCallback {
    /**
     * Called for each particle found in the query. See
     * {@link RayCastCallback#reportFixture(org.jbox2d.dynamics.Fixture, Vector2, Vector2, float)} for
     * argument info.
     */
    float reportParticle(int index, Vector2 point, Vector2 normal, float fraction);
}

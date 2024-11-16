package org.jbox2d.particle;

import org.jbox2d.common.Vector2;

public class ParticleDef {
    /**
     * Specifies the type of particle. A particle may be more than one type. Multiple types are
     * chained by logical sums, for example: pd.flags = ParticleType.b2_elasticParticle |
     * ParticleType.b2_viscousParticle.
     */
    int flags;

    /**
     * The world position of the particle.
     */
    public final Vector2 position = new Vector2();

    /**
     * The linear velocity of the particle in world co-ordinates.
     */
    public final Vector2 velocity = new Vector2();

    /**
     * Use this to store application-specific body data.
     */
    public Object userData;
}

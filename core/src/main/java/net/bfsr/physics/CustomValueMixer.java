package net.bfsr.physics;

import org.dyn4j.world.ValueMixer;

public class CustomValueMixer implements ValueMixer {
    @Override
    public double mixFriction(double friction1, double friction2) {
        return (friction1 + friction2) / 2.0;
    }

    @Override
    public double mixRestitution(double restitution1, double restitution2) {
        return Math.max(restitution1, restitution2);
    }

    @Override
    public double mixRestitutionVelocity(double restitutionVelocity1, double restitutionVelocity2) {
        return Math.min(restitutionVelocity1, restitutionVelocity2);
    }
}

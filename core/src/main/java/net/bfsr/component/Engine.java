package net.bfsr.component;

public class Engine {
    private final float forwardSpeed, backwardSpeed, sideSpeed;
    private final float maxForwardSpeed;
    private final float maneuverability;
    private final float rotationSpeed;

    public Engine(float forwardSpeed, float backwardSpeed, float sideSpeed,
                  float maxForwardSpeed,
                  float maneuverability, float rotationSpeed) {
        this.forwardSpeed = forwardSpeed;
        this.backwardSpeed = backwardSpeed;
        this.sideSpeed = sideSpeed;
        this.maxForwardSpeed = maxForwardSpeed;
        this.maneuverability = maneuverability;
        this.rotationSpeed = rotationSpeed;
    }

    public float getForwardSpeed() {
        return forwardSpeed;
    }

    public float getBackwardSpeed() {
        return backwardSpeed;
    }

    public float getSideSpeed() {
        return sideSpeed;
    }

    public float getMaxForwardSpeed() {
        return maxForwardSpeed;
    }

    public float getManeuverability() {
        return maneuverability;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }
}

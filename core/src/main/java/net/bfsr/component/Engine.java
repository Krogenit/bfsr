package net.bfsr.component;

import lombok.Getter;

public class Engine {
    @Getter
    private final float forwardSpeed, backwardSpeed, sideSpeed;
    @Getter
    private final float maxForwardSpeed;
    @Getter
    private final float maneuverability;
    @Getter
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
}

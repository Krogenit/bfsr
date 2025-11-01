package net.bfsr.server.ai.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.Engine;
import net.bfsr.engine.ai.task.AiTask;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RigidBodyUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Modules;
import org.jbox2d.common.Vector2;
import org.joml.Math;
import org.joml.Vector2f;

@Log4j2
@RequiredArgsConstructor
public class AiJumpTask extends AiTask {
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final Vector2f destination;
    private final Vector2f warpVelocity = new Vector2f();

    private Ship ship;
    private Modules modules;

    private boolean executing = true;
    private final float maxWarpSpeed = 5000.0f;
    private final float maxAcceleration = 100.0f;
    private final float brakingDuration = 10.0f;

    private float currentSpeed;

    private float speedAtBrakeDistance;
    private float brakingElapseTime;

    private Runnable warpSpeedRunnable;

    @Override
    public void init(RigidBody rigidBody) {
        super.init(rigidBody);
        this.ship = ((Ship) rigidBody);
        this.modules = ship.getModules();
    }

    @Override
    public void execute() {
        if (!ship.isWarpDrive()) {
            alignForWarpDrive();
        } else {
            warpDrive();
        }
    }

    private void warpDrive() {
        warpSpeedRunnable.run();

        RotationHelper.angleToVelocity(ship.getSin(), ship.getCos(), currentSpeed, warpVelocity);
        ship.getLinearVelocity().set(warpVelocity.x, warpVelocity.y);

        if (currentSpeed <= 0.025f) {
            ship.setWarpDrive(false);
            executing = false;
            return;
        }

        float deltaTime = Engine.getUpdateDeltaTimeInSeconds();
        ship.setPosition(ship.getX() + warpVelocity.x * deltaTime, ship.getY() + warpVelocity.y * deltaTime);
    }

    private void acceleration() {
        float dx = destination.x - ship.getX();
        float dy = destination.y - ship.getY();
        float distance = Math.sqrt(dx * dx + dy * dy);
        float optimalStopDistance = calculateFullStopDistance();
        if (distance <= optimalStopDistance) {
            speedAtBrakeDistance = currentSpeed;
            warpSpeedRunnable = this::deceleration;
            warpSpeedRunnable.run();
            return;
        }

        if (currentSpeed < maxWarpSpeed) {
            currentSpeed += maxAcceleration * Engine.getUpdateDeltaTimeInSeconds();
            if (currentSpeed >= maxWarpSpeed) {
                currentSpeed = maxWarpSpeed;
            }
        }
    }

    private void deceleration() {
        float progress = brakingElapseTime / brakingDuration;
        if (progress > 1.0f) {
            progress = 1.0f;
        }

        float speedFactor = easeOutCubic(progress);
        float newSpeed = speedAtBrakeDistance * speedFactor;

        int ticksAmount = (int) ((brakingDuration - brakingElapseTime) * Engine.UPDATES_PER_SECOND);
        float expectedTravel = ticksAmount * currentSpeed * easeOutCubicIntegral() * Engine.getUpdateDeltaTimeInSeconds();

        float dx = destination.x - ship.getX();
        float dy = destination.y - ship.getY();
        float distance = Math.sqrt(dx * dx + dy * dy);

        if (expectedTravel > distance) {
            brakingElapseTime += Engine.getUpdateDeltaTimeInSeconds();
            newSpeed = speedAtBrakeDistance * easeOutCubic(brakingElapseTime / brakingDuration);
        } else {
            // Need for dead loop fix
            float amountTravelByTick = currentSpeed * Engine.getUpdateDeltaTimeInSeconds();
            if (amountTravelByTick > 0.01f && distance - expectedTravel > amountTravelByTick) {
                brakingElapseTime -= Engine.getUpdateDeltaTimeInSeconds();
                newSpeed = speedAtBrakeDistance * easeOutCubic(brakingElapseTime / brakingDuration);
            }
        }

        currentSpeed = newSpeed;
        brakingElapseTime += Engine.getUpdateDeltaTimeInSeconds();
    }

    private float easeOutCubic(float t) {
        return (float) (java.lang.Math.pow(1 - t, 3));
    }

    private float easeOutCubicIntegral() {
        return 1.0f / 4.0f;
    }

    private float calculateFullStopDistance() {
        return currentSpeed * easeOutCubicIntegral() * brakingDuration;
    }

    private void alignForWarpDrive() {
        rigidBodyUtils.rotateToVector(ship, destination.x, destination.y, 0.01f);

        float diffRad = rigidBodyUtils.getRotationDifference(ship, destination.x, destination.y);
        float diffAbs = Math.abs(diffRad);

        float forwardAcceleration = modules.getEngines().getForwardAcceleration();
        float diff = (MathUtils.PI - diffAbs) / MathUtils.PI;
        if (diff < 0.9f) {
            ship.addForce(warpVelocity.set(ship.getCos(), ship.getSin()), 0.1f * diff * forwardAcceleration);
            ship.addMoveDirection(Direction.FORWARD);
        } else {
            ship.addForce(warpVelocity.set(ship.getCos(), ship.getSin()), diff * diff * forwardAcceleration);
            ship.addMoveDirection(Direction.FORWARD);
        }

        Vector2 linearVelocity = ship.getLinearVelocity();
        float velocitySquared = linearVelocity.lengthSquared();
        float maxForwardSpeed = modules.getEngines().getMaxForwardVelocity();
        float maxForwardSpeedSquared = maxForwardSpeed * maxForwardSpeed;
        float maxSpeedPercent = velocitySquared / maxForwardSpeedSquared;

        if (diffAbs < 0.01f && maxSpeedPercent > 0.99f &&
                Math.abs(MathUtils.getAngle(linearVelocity.x, linearVelocity.y, destination.x - ship.getX(), destination.y - ship.getY())) <
                        0.03f) {
            ship.setWarpDrive(true);
            float angle = Math.atan2(destination.y - ship.getY(), destination.x - ship.getX());
            ship.setRotation(Math.sin(angle), Math.cos(angle));
            ship.removeMoveDirection(Direction.FORWARD);
            currentSpeed = maxForwardSpeed;
            warpSpeedRunnable = this::acceleration;
        }
    }

    @Override
    public boolean shouldExecute() {
        return executing;
    }
}

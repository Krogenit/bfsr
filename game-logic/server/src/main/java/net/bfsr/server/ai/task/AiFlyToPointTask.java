package net.bfsr.server.ai.task;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.ai.task.AiTask;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RigidBodyUtils;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Modules;
import org.jbox2d.common.Vector2;
import org.joml.Math;
import org.joml.Vector2f;

@RequiredArgsConstructor
public class AiFlyToPointTask extends AiTask {
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final Vector2f point;
    private Ship ship;
    private Modules modules;

    private boolean executing = true;
    private boolean acceleration = true;

    @Override
    public void init(RigidBody rigidBody) {
        super.init(rigidBody);
        ship = ((Ship) rigidBody);
        modules = ship.getModules();
        ship.removeAllMoveDirections();
    }

    @Override
    public void execute() {
        float minSpeedToFinish = 0.002f;
        float shipSpeed = ship.getSpeed();
        if (shipSpeed <= minSpeedToFinish && !acceleration) {
            executing = false;
            ship.removeAllMoveDirections();
            return;
        }

        float diffRad = rigidBodyUtils.getRotationDifference(ship, point.x, point.y);
        float diffAbs = org.joml.Math.abs(diffRad);
        float dt = Engine.getUpdateDeltaTimeInSeconds();
        float dx = point.x - ship.getX();
        float dy = point.y - ship.getY();
        float distance = Math.sqrt(dx * dx + dy * dy);
        float brakingStrength = 1.0f / (1.0f + ship.getBrakingStrength() * dt);
        int framesToFullStop = getFramesToFullStop(minSpeedToFinish, shipSpeed, brakingStrength);
        float optimalStopDistance = calculateFullStopDistance(framesToFullStop, shipSpeed * dt, brakingStrength);
        if (diffAbs < 0.1f && distance <= optimalStopDistance || !acceleration) {
            if (acceleration) {
                ship.removeAllMoveDirections();
                acceleration = false;
                ship.addMoveDirection(Direction.STOP);
            }

            return;
        }

        rigidBodyUtils.rotateToVector(ship, point.x, point.y, modules.getEngines().getAngularVelocity());

        float forwardAcceleration = modules.getEngines().getForwardAcceleration();
        float diff = (MathUtils.PI - diffAbs) / MathUtils.PI;
        float force = diff * forwardAcceleration;
        ship.addMoveDirection(Direction.FORWARD);
        ship.addForce(ship.getCos() * force, ship.getSin() * force);

        Vector2 linearVelocity = ship.getLinearVelocity();
        float forwardX = ship.getCos();
        float forwardY = ship.getSin();
        float rightX = -forwardY;
        float rightY = forwardX;
        float forwardSpeed = linearVelocity.x * forwardX + linearVelocity.y * forwardY;
        float sideSpeed = linearVelocity.x * rightX + linearVelocity.y * rightY;
        sideSpeed *= 0.9f;

        linearVelocity.set(forwardX * forwardSpeed + rightX * sideSpeed, forwardY * forwardSpeed + rightY * sideSpeed);
    }

    private int getFramesToFullStop(float minSpeedToFinish, float speed, float brakingStrength) {
        return (int) Math.round(java.lang.Math.log(minSpeedToFinish / speed) / java.lang.Math.log(brakingStrength));
    }

    private float calculateFullStopDistance(int frames, float speed, float brakingStrength) {
        return (float) (speed * (1 - java.lang.Math.pow(brakingStrength, frames)) / (1 - brakingStrength));
    }

    @Override
    public boolean shouldExecute() {
        return executing;
    }
}

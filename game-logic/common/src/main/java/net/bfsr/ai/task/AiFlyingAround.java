package net.bfsr.ai.task;

import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;

public class AiFlyingAround extends AiTask {
    private Vector2f point;
    private Vector2f currentPoint;
    private RigidBody<?> obj;
    private final float size;
    private final Vector2f angleToVelocity = new Vector2f();

    private static final float UPDATE_LENGTH = 25;

    public AiFlyingAround(Ship ship, Vector2f point, float size) {
        super(ship);
        this.point = point;
        this.size = size;
    }

    public AiFlyingAround(Ship ship, RigidBody<?> obj, float size) {
        super(ship);
        this.obj = obj;
        this.size = size;
    }

    @Override
    public void execute() {
        if (obj != null) {
            if (currentPoint == null || ship.getPosition().distance(currentPoint.x, currentPoint.y) <= UPDATE_LENGTH) {
                calculateNewPoint(obj.getPosition());
            }
        } else if (point != null) {
            if (currentPoint == null || ship.getPosition().distance(currentPoint.x, currentPoint.y) <= UPDATE_LENGTH) {
                calculateNewPoint(point);
            }
        }
    }

    private void calculateNewPoint(Vector2f pos) {
        RotationHelper.angleToVelocity(MathUtils.TWO_PI * obj.getWorld().getRand().nextFloat(), size, angleToVelocity);
        currentPoint = new Vector2f(pos.x + angleToVelocity.x, pos.y + angleToVelocity.y);
    }

    @Override
    public boolean shouldExecute() {
        return ship.getTarget() == null;
    }
}
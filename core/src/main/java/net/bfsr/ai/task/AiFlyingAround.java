package net.bfsr.ai.task;

import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;

public class AiFlyingAround extends AiTask {
    private Vector2f point;
    private Vector2f currentPoint;
    private CollisionObject obj;
    private final float size;

    private static final float UPDATE_LENGTH = 25;

    public AiFlyingAround(Ship ship, Vector2f point, float size) {
        super(ship);
        this.point = point;
        this.size = size;
    }

    public AiFlyingAround(Ship ship, CollisionObject obj, float size) {
        super(ship);
        this.obj = obj;
        this.size = size;
    }

    @Override
    public void execute(double delta) {
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
        Vector2f addPos = RotationHelper.angleToVelocity(RotationHelper.TWOPI * obj.getWorld().getRand().nextFloat(), size);
        currentPoint = new Vector2f(pos.x + addPos.x, pos.y + addPos.y);
    }

    @Override
    public boolean shouldExecute() {
        return ship.getTarget() == null;
    }
}

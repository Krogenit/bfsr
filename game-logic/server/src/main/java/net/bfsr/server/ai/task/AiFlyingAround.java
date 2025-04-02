package net.bfsr.server.ai.task;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.ai.task.AiTask;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.world.entity.RigidBody;
import org.joml.Vector2f;

public class AiFlyingAround extends AiTask {
    private Vector2f point;
    private Vector2f currentPoint;
    private RigidBody obj;
    private final float size;
    private final Vector2f angleToVelocity = new Vector2f();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();

    private static final float UPDATE_LENGTH = 25;

    public AiFlyingAround(Vector2f point, float size) {
        this.point = point;
        this.size = size;
    }

    public AiFlyingAround(RigidBody obj, float size) {
        this.obj = obj;
        this.size = size;
    }

    @Override
    public void execute() {
        if (obj != null) {
            if (currentPoint == null || currentPoint.distance(ship.getX(), ship.getY()) <= UPDATE_LENGTH) {
                calculateNewPoint(obj.getX(), obj.getY());
            }
        } else if (point != null) {
            if (currentPoint == null || currentPoint.distance(ship.getX(), ship.getY()) <= UPDATE_LENGTH) {
                calculateNewPoint(point.x, point.y);
            }
        }
    }

    private void calculateNewPoint(float x, float y) {
        RotationHelper.angleToVelocity(MathUtils.TWO_PI * random.nextFloat(), size, angleToVelocity);
        currentPoint = new Vector2f(x + angleToVelocity.x, y + angleToVelocity.y);
    }

    @Override
    public boolean shouldExecute() {
        return ship.getTarget() == null;
    }
}
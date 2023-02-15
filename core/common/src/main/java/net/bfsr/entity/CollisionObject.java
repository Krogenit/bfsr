package net.bfsr.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.math.Direction;
import net.bfsr.math.RotationHelper;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.util.List;

@NoArgsConstructor
public abstract class CollisionObject extends TextureObject {
    private static final Transform IDENTITY_TRANSFORM = new Transform();
    private static final AABB CACHED_AABB_0 = new AABB(0, 0, 0, 0);
    private static final AABB CACHED_AABB_1 = new AABB(0, 0, 0, 0);
    protected static final Vector2f rotateToVector = new Vector2f();
    protected static final Vector2f angleToVelocity = new Vector2f();

    @Getter
    protected World<?> world;
    @Getter
    protected final Body body = new Body();
    @Getter
    @Setter
    protected boolean isDead;
    @Getter
    @Setter
    protected int id;
    protected Vector2f velocity = new Vector2f();
    protected AxisAlignedBoundingBox aabb;
    @Getter
    protected AxisAlignedBoundingBox worldAABB;
    protected float aliveTimer;
    @Getter
    protected float lastSin, lastCos;
    @Getter
    protected float sin, cos;

    protected CollisionObject(World world, int id, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a) {
        super(x, y, rotation, scaleX, scaleY, r, g, b, a);
        this.world = world;
        this.id = id;
        createBody(x, y);
        createAABB();
    }

    protected CollisionObject(World world, int id, float x, float y, float rotation, float scaleX, float scaleY) {
        this(world, id, x, y, rotation, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected CollisionObject(World world, int id, float x, float y, float sin, float cos, float scaleX, float scaleY, float r, float g, float b, float a) {
        this(world, id, x, y, 0, scaleX, scaleY, r, g, b, a);
        this.sin = sin;
        this.cos = cos;
    }

    protected CollisionObject(World world, int id, float x, float y, float scaleX, float scaleY, float r, float g, float b, float a) {
        this(world, id, x, y, 0, scaleX, scaleY, r, g, b, a);
    }

    protected CollisionObject(World world, int id, float x, float y, float scaleX, float scaleY) {
        this(world, id, x, y, 0, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected CollisionObject(World world) {
        this.world = world;
    }

    protected abstract void createBody(float x, float y);

    protected void createAABB() {
        AABB aabb = computeAABB();

        if (this.aabb != null) {
            this.aabb.set((float) aabb.getMinX(), (float) aabb.getMinY(), (float) aabb.getMaxX(), (float) aabb.getMaxY());
        } else {
            this.aabb = new AxisAlignedBoundingBox((float) aabb.getMinX(), (float) aabb.getMinY(), (float) aabb.getMaxX(), (float) aabb.getMaxY());
            this.worldAABB = new AxisAlignedBoundingBox(this.aabb);
        }
    }

    protected AABB computeAABB() {
        List<BodyFixture> fixtures = body.getFixtures();
        int size = fixtures.size();
        fixtures.get(0).getShape().computeAABB(IDENTITY_TRANSFORM, CACHED_AABB_0);
        for (int i = 1; i < size; i++) {
            fixtures.get(i).getShape().computeAABB(IDENTITY_TRANSFORM, CACHED_AABB_1);
            CACHED_AABB_0.union(CACHED_AABB_1);
        }

        return CACHED_AABB_0;
    }

    protected void updateWorldAABB() {
        Vector2f position = getPosition();
        worldAABB.set(aabb.getMin().x + position.x, aabb.getMin().y + position.y, aabb.getMax().x + position.x, aabb.getMax().y + position.y);
    }

    @Override
    public void update() {
        if (world.isRemote()) {
            lastSin = sin;
            lastCos = cos;
            aliveTimer += 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (aliveTimer > 120) {
                setDead(true);
                aliveTimer = 0;
            }
        }
    }

    public void postPhysicsUpdate() {
        sin = (float) body.getTransform().getSint();
        cos = (float) body.getTransform().getCost();
        updateWorldAABB();
    }

    public float getRotationDifference(Vector2f vector) {
        Transform transform = body.getTransform();
        rotateToVector.x = (float) (vector.x - transform.getTranslationX());
        rotateToVector.y = (float) (vector.y - transform.getTranslationY());

        RotationHelper.angleToVelocity(getRotation(), 1.0f, angleToVelocity);
        return angleToVelocity.angle(rotateToVector);
    }

    public void rotateToVector(Vector2f vector, float rotateSpeed) {
        Transform transform = body.getTransform();
        float rot = (float) transform.getRotationAngle();
        rotateToVector.x = (float) (vector.x - transform.getTranslationX());
        rotateToVector.y = (float) (vector.y - transform.getTranslationY());

        RotationHelper.angleToVelocity(rot, 1.0f, angleToVelocity);
        float diffRad = angleToVelocity.angle(rotateToVector);
        double diff = Math.toDegrees(diffRad);
        double diffAbs = Math.abs(diff);
        double addRot = 0;

        if (diff > 0) {
            if (diffAbs <= 5) {
                if (diffAbs <= 1) {
                    body.setAngularVelocity(0);
                } else addRot = rotateSpeed / 4.0 * TimeUtils.UPDATE_DELTA_TIME;
            } else addRot = rotateSpeed * TimeUtils.UPDATE_DELTA_TIME;

            if (addRot >= diffRad) {
                transform.setRotation(Math.atan2(rotateToVector.x, -rotateToVector.y) - Math.PI / 2.0);
                body.setAngularVelocity(0);
            } else {
                transform.setRotation(rot + addRot);
                body.setAngularVelocity(body.getAngularVelocity() * 0.99f);
            }
        } else {
            if (diffAbs <= 5) {
                if (diffAbs <= 1) {
                    body.setAngularVelocity(0);
                } else addRot = -rotateSpeed / 4.0 * TimeUtils.UPDATE_DELTA_TIME;
            } else addRot = -rotateSpeed * TimeUtils.UPDATE_DELTA_TIME;

            if (addRot <= diffRad) {
                transform.setRotation(Math.atan2(rotateToVector.x, -rotateToVector.y) - Math.PI / 2.0);
                body.setAngularVelocity(0);
            } else {
                transform.setRotation(rot + addRot);
                body.setAngularVelocity(body.getAngularVelocity() * 0.99f);
            }
        }
    }

    private static final Direction[] directions = new Direction[2];

    public Direction[] calculateDirectionsToOtherObject(float x, float y) {
        directions[0] = directions[1] = null;
        Vector2f pos = getPosition();
        rotateToVector.x = x - pos.x;
        rotateToVector.y = y - pos.y;
        RotationHelper.angleToVelocity(getRotation(), 1.0f, angleToVelocity);
        double diff = Math.toDegrees(angleToVelocity.angle(rotateToVector));
        double diffAbs = Math.abs(diff);
        if (diffAbs > 112.5f) {
            directions[0] = Direction.BACKWARD;
        } else if (diffAbs <= 67.5f) {
            directions[0] = Direction.FORWARD;
        }


        if (diff < -22.5f && diff >= -157.5f) {
            directions[1] = Direction.LEFT;
        } else if (diff > 22.5f && diff <= 157.5f) {
            directions[1] = Direction.RIGHT;
        }

        return directions;
    }

    public Direction calculateDirectionToOtherObject(float x, float y) {
        Vector2f pos = getPosition();
        rotateToVector.x = x - pos.x;
        rotateToVector.y = y - pos.y;
        RotationHelper.angleToVelocity(getRotation(), 1.0f, angleToVelocity);
        double diff = Math.toDegrees(angleToVelocity.angle(rotateToVector));
        double diffAbs = Math.abs(diff);
        if (diffAbs > 135) {
            return Direction.BACKWARD;
        } else if (diff < -45 && diff >= -135) {
            return Direction.LEFT;
        } else if (diffAbs <= 45) {
            return Direction.FORWARD;
        } else if (diff > 45 && diff <= 135) {
            return Direction.RIGHT;
        }

        return null;
    }

    public void updateClientPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        aliveTimer = 0;
        body.setAtRest(false);
        updatePos(pos);
        updateRot(rot);
        updateVelocity(velocity);
        updateAngularVelocity(angularVelocity);
    }

    public void updateServerPositionFromPacket(Vector2f pos, float rot, Vector2f velocity, float angularVelocity) {
        aliveTimer = 0;
        body.setAtRest(false);
        body.getTransform().setTranslation(pos.x, pos.y);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.getTransform().setRotation(rot);
        body.setAngularVelocity(angularVelocity);
    }

    private void updateVelocity(Vector2f velocity) {
        body.setLinearVelocity(velocity.x, velocity.y);
    }

    private void updatePos(Vector2f newPos) {
        Vector2f pos = getPosition();

        float dist = pos.distance(newPos);

        float alpha = dist > 10 ? 1.0f : 0.05f;
        double x = pos.x + alpha * (newPos.x - pos.x);
        double y = pos.y + alpha * (newPos.y - pos.y);

        body.getTransform().setTranslation(x, y);
    }

    private void updateRot(float re) {
        double rs = body.getTransform().getRotationAngle();

        double diff = re - rs;
        if (diff < -Math.PI) diff += Geometry.TWO_PI;
        if (diff > Math.PI) diff -= Geometry.TWO_PI;

        float alpha = (float) (Math.max(Math.abs(diff) / 10.0f, 0.1f));

        double a = diff * alpha + rs;

        body.getTransform().setRotation(a);
    }

    private void updateAngularVelocity(float re) {
        body.setAngularVelocity(re);
    }

    @Override
    public float getRotation() {
        return (float) body.getTransform().getRotationAngle();
    }

    @Override
    public Vector2f getPosition() {
        Vector2 pos = body.getTransform().getTranslation();
        position.x = (float) pos.x;
        position.y = (float) pos.y;
        return position;
    }

    public Vector2f getVelocity() {
        Vector2 vel = body.getLinearVelocity();
        velocity.x = (float) vel.x;
        velocity.y = (float) vel.y;
        return velocity;
    }

    public float getAngularVelocity() {
        return (float) body.getAngularVelocity();
    }
}

package net.bfsr.entity;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.component.Engine;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.common.PacketShipEngine;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class CollisionObject extends TextureObject {
    private static final Transform IDENTITY_TRANSFORM = new Transform();
    private static final AABB CACHED_AABB_0 = new AABB(0, 0, 0, 0);
    private static final AABB CACHED_AABB_1 = new AABB(0, 0, 0, 0);
    protected static final Vector2f rotateToVector = new Vector2f();
    protected static final Vector2f angleToVelocity = new Vector2f();

    @Getter
    protected World world;
    @Getter
    protected Body body;
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
    private Direction lastMoveDir;
    @Getter
    protected float lastSin, lastCos;
    @Getter
    protected float sin, cos;

    public CollisionObject(World world, int id, TextureRegister texture, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a) {
        super(TextureLoader.getTexture(texture), x, y, rotation, scaleX, scaleY, r, g, b, a);
        this.world = world;
        this.id = id;
        createBody(x, y);
        createAABB();
    }

    public CollisionObject(World world, int id, TextureRegister texture, float x, float y, float rotation, float scaleX, float scaleY) {
        this(world, id, texture, x, y, rotation, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public CollisionObject(World world, int id, TextureRegister texture, float x, float y, float scaleX, float scaleY, float r, float g, float b, float a) {
        this(world, id, texture, x, y, 0, scaleX, scaleY, r, g, b, a);
    }

    public CollisionObject(World world, int id, TextureRegister texture, float x, float y, float scaleX, float scaleY) {
        this(world, id, texture, x, y, 0, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public CollisionObject(World world, int id, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a) {
        this(world, id, null, x, y, rotation, scaleX, scaleY, r, g, b, a);
    }

    public CollisionObject(World world, int id, float x, float y, float scaleX, float scaleY, float r, float g, float b, float a) {
        this(world, id, null, x, y, 0, scaleX, scaleY, r, g, b, a);
    }

    public CollisionObject(World world, int id, float x, float y, float scaleX, float scaleY) {
        this(world, id, null, x, y, 0, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public CollisionObject(World world) {
        this.world = world;
    }

    public CollisionObject() {}

    protected void createBody(float x, float y) {
        body = new Body();
    }

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

    private void move(Vector2 r, float speed) {
        Vector2 f = r.product(speed);
        body.applyForce(f);
    }

    public void move(Ship ship, Direction dir) {
        Engine engine = ship.getEngine();
        Vector2 r = new Vector2(body.getTransform().getRotationAngle());
        Vector2f pos = getPosition();

        switch (dir) {
            case FORWARD:
                move(r, engine.getForwardSpeed());
                break;
            case BACKWARD:
                r.negate();
                move(r, engine.getBackwardSpeed());
                break;
            case LEFT:
                r.left();
                move(r, engine.getSideSpeed());
                break;
            case RIGHT:
                r.right();
                move(r, engine.getSideSpeed());
                break;
            case STOP:
                body.getLinearVelocity().multiply(engine.getManeuverability() / 1.02f);

                float x = -(float) body.getLinearVelocity().x;
                float y = -(float) body.getLinearVelocity().y;

                if (Math.abs(x) > 10) {
                    dir = calculateDirectionToOtherObject(x + pos.x, pos.y);
                    if (world.isRemote()) {
                        ship.spawnEngineParticles(dir);
                        if (ship == world.getPlayerShip()) {
                            Core.getCore().sendPacket(new PacketShipEngine(id, dir.ordinal()));
                        }
                    } else MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipEngine(id, dir.ordinal()), pos, WorldServer.PACKET_UPDATE_DISTANCE);
                }

                if (Math.abs(y) > 10) {
                    dir = calculateDirectionToOtherObject(pos.x, y + pos.y);
                    if (world.isRemote()) {
                        ship.spawnEngineParticles(dir);
                        if (ship == world.getPlayerShip()) {
                            Core.getCore().sendPacket(new PacketShipEngine(id, dir.ordinal()));
                        }
                    } else MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipEngine(id, dir.ordinal()), pos, WorldServer.PACKET_UPDATE_DISTANCE);
                }

                return;
        }

        if (world.isRemote()) {
            ship.spawnEngineParticles(dir);
            if (ship == world.getPlayerShip()) Core.getCore().sendPacket(new PacketShipEngine(id, dir.ordinal()));
        } else {
            if (lastMoveDir != null && lastMoveDir != dir)
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipEngine(id, dir.ordinal()), pos, WorldServer.PACKET_UPDATE_DISTANCE);
            lastMoveDir = dir;
        }
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
        double rot = transform.getRotationAngle();
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

    public void renderDebug() {
        org.dyn4j.geometry.AABB aabb = body.createAABB();
        Vector2f center = getPosition();
        double rot = Math.toDegrees(body.getTransform().getRotationAngle());

        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2d(aabb.getMinX(), aabb.getMinY());
        GL11.glVertex2d(aabb.getMinX(), aabb.getMaxY());
        GL11.glVertex2d(aabb.getMaxX(), aabb.getMaxY());
        GL11.glVertex2d(aabb.getMaxX(), aabb.getMinY());
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_LOOP);
        float x = (float) body.getLinearVelocity().x;
        float y = (float) body.getLinearVelocity().y;
        GL11.glVertex2d(center.x, center.y);
        GL11.glVertex2d(x / 5.0f + center.x, y / 5.0f + center.y);
        GL11.glEnd();

        List<BodyFixture> fixtures = body.getFixtures();
        for (BodyFixture bodyFixture : fixtures) {
            Convex convex = bodyFixture.getShape();
            if (convex instanceof Rectangle rect) {
                GL11.glPushMatrix();
                GL11.glTranslated(center.x, center.y, 0);
                GL11.glRotated(rot, 0, 0, 1);
                GL11.glBegin(GL11.GL_LINE_LOOP);
                for (int i = 0; i < rect.getVertices().length; i++) {
                    Vector2 vertex = rect.getVertices()[i];
                    GL11.glVertex2d(vertex.x, vertex.y);
                }
                GL11.glEnd();
                GL11.glPopMatrix();
            } else if (convex instanceof Polygon polygon) {
                GL11.glPushMatrix();
                GL11.glTranslated(center.x, center.y, 0);
                GL11.glRotated(rot, 0, 0, 1);
                GL11.glBegin(GL11.GL_LINE_LOOP);
                for (int i = 0; i < polygon.getVertices().length; i++) {
                    Vector2 vect1 = polygon.getVertices()[i];
                    GL11.glVertex2d(vect1.x, vect1.y);
                }
                GL11.glEnd();
                GL11.glPopMatrix();
            } else if (convex instanceof Circle circle) {
                GL11.glPushMatrix();
                GL11.glTranslated(center.x, center.y, 0);
                GL11.glBegin(GL11.GL_LINE_LOOP);
                float count = 10.0f;
                float angleAdd = RotationHelper.TWOPI / count;
                float startAngle = 0.0f;
                for (int i = 0; i < count; i++) {
                    Vector2f pos = RotationHelper.angleToVelocity(startAngle, (float) circle.getRadius());
                    GL11.glVertex2f(pos.x, pos.y);
                    startAngle += angleAdd;
                }
                GL11.glEnd();
                GL11.glPopMatrix();
            }
        }
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

package net.bfsr.client.particle;

import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.joml.Vector2f;

public class Particle extends CollisionObject {
    protected float sizeVelocity, alphaVelocity, angularVelocity, maxAlpha;
    protected boolean canCollide, isAlphaFromZero, zeroVelocity;
    protected EnumParticlePositionType positionType;
    protected EnumParticleRenderType renderType;
    protected float greater;

    public Particle init(World world, int id, TextureRegister texture, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, float greater, boolean isAlphaFromZero, boolean canCollide,
                         EnumParticlePositionType positionType, EnumParticleRenderType renderType) {
        this.world = world;
        this.id = id;
        this.texture = TextureLoader.getTexture(texture);
        this.position.set(x, y);
        this.lastPosition.set(position);
        this.velocity.set(velocityX, velocityY);
        this.rotation = rotation;
        this.angularVelocity = angularVelocity;
        this.scale.set(scaleX, scaleY);
        this.sizeVelocity = sizeVelocity;
        this.color.set(r, g, b, a);
        this.alphaVelocity = alphaVelocity;
        this.greater = greater;
        this.isAlphaFromZero = isAlphaFromZero;
        this.canCollide = canCollide;
        this.positionType = positionType;
        this.renderType = renderType;
        this.zeroVelocity = velocity.length() != 0;
        this.isDead = false;

        if (isAlphaFromZero) {
            this.maxAlpha = a;
            this.color.w = 0.0f;
        }

        if (canCollide) {
            createBody(position.x, position.y);
        }

        if (world.isRemote()) {
            addParticle();
        }

        return this;
    }

    public Particle init(int id, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY, float sizeVelocity,
                         float r, float g, float b, float a, float alphaVelocity, float greater, boolean isAlphaFromZero, boolean canCollide, EnumParticlePositionType positionType,
                         EnumParticleRenderType renderType) {
        return init(MainServer.getInstance().getWorld(), id, null, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity, greater,
                isAlphaFromZero, canCollide, positionType, renderType);
    }

    public Particle init(int id, TextureRegister texture, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, float greater, boolean isAlphaFromZero, boolean canCollide,
                         EnumParticlePositionType positionType, EnumParticleRenderType renderType) {
        return init(Core.getCore().getWorld(), id, texture, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity, greater,
                isAlphaFromZero, canCollide, positionType, renderType);
    }

    public Particle init(TextureRegister texture, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY, float sizeVelocity,
                         float r, float g, float b, float a, float alphaVelocity, float greater, boolean isAlphaFromZero, boolean canCollide, EnumParticlePositionType positionType,
                         EnumParticleRenderType renderType) {
        return init(Core.getCore().getWorld(), -1, texture, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity, greater,
                isAlphaFromZero, canCollide, positionType, renderType);
    }

    protected void addParticle() {
        ParticleRenderer.getInstance().addParticle(this);
    }

    @Override
    protected void createAABB() {
        if (canCollide) {
            AABB aabb = body.createAABB(new Transform());

            if (this.aabb != null) {
                this.aabb.set((float) aabb.getMinX(), (float) aabb.getMinY(), (float) aabb.getMaxX(), (float) aabb.getMaxY());
            } else {
                this.aabb = new AxisAlignedBoundingBox(new Vector2f((float) aabb.getMinX(), (float) aabb.getMinY()), new Vector2f((float) aabb.getMaxX(), (float) aabb.getMaxY()));
            }
        }
    }

    protected void createFixtures() {
        body.removeFixture(0);
        Vector2f size = getScale();
        Convex convex;
        if (size.x == size.y) {
            convex = Geometry.createCircle(size.x / 2.0f);
        } else {
            convex = Geometry.createRectangle(size.x, size.y);
        }

        BodyFixture bodyFixture = new BodyFixture(convex);
        body.addFixture(bodyFixture);
    }

    @Override
    protected void createBody(float x, float y) {
        if (canCollide) {
            if (body == null) super.createBody(x, y);

            createFixtures();
            body.translate(x, y);
            body.setMass(MassType.NORMAL);
            body.setUserData(this);
            body.setLinearVelocity(velocity.x, velocity.y);
            body.getTransform().setRotation(rotation);
            body.setAngularVelocity(angularVelocity);
            body.setLinearDamping(0.05f);
            body.setAngularDamping(0.005f);
            world.addDynamicParticle(this);
        }
    }

    @Override
    public void update() {
        lastPosition.set(getPosition());

        if (!canCollide) {
            position.x += velocity.x * TimeUtils.UPDATE_DELTA_TIME;
            position.y += velocity.y * TimeUtils.UPDATE_DELTA_TIME;
            rotation += angularVelocity * TimeUtils.UPDATE_DELTA_TIME;

            if (!zeroVelocity) {
                velocity.x *= 0.99f * TimeUtils.UPDATE_DELTA_TIME;
                velocity.y *= 0.99f * TimeUtils.UPDATE_DELTA_TIME;
            }
        }

        if (sizeVelocity != 0) {
            float sizeVel = sizeVelocity * TimeUtils.UPDATE_DELTA_TIME;
            scale.add(sizeVel, sizeVel);

            if (scale.x <= 0.0f || scale.y <= 0.0f)
                setDead(true);
        }

        if (alphaVelocity != 0) {
            if (isAlphaFromZero) {
                if (maxAlpha != 0) {
                    color.w += alphaVelocity * TimeUtils.UPDATE_DELTA_TIME;

                    if (color.w >= maxAlpha * 2.0f)
                        setDead(true);

                    if (color.w >= maxAlpha) {
                        maxAlpha = 0.0f;
                    }
                } else {
                    color.w -= alphaVelocity * TimeUtils.UPDATE_DELTA_TIME;
                    if (color.w <= 0)
                        setDead(true);
                }
            } else {
                color.w -= alphaVelocity * TimeUtils.UPDATE_DELTA_TIME;
                if (color.w <= 0)
                    setDead(true);
            }
        }
    }

    public void setVelocity(Vector2f velocity) {
        if (canCollide) body.setLinearVelocity(velocity.x, velocity.y);
        else this.velocity = velocity;
    }

    public EnumParticlePositionType getPositionType() {
        return positionType;
    }

    public EnumParticleRenderType getRenderType() {
        return renderType;
    }

    @Override
    public Vector2f getPosition() {
        if (canCollide) return super.getPosition();
        else return position;
    }

    @Override
    public float getRotation() {
        if (canCollide) return super.getRotation();
        else return rotation;
    }

    public float getAlphaVelocity() {
        return alphaVelocity;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void returnToPool() {
        ParticleSpawner.PARTICLE_POOL.returnBack(this);
    }
}

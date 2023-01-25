package net.bfsr.client.particle;

import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.ParticleShader;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.World;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class Particle extends CollisionObject {

    protected float sizeVelocity, alphaVelocity, rotationSpeed, maxAlpha;
    protected boolean canCollide, isAlphaFromZero, zeroVelocity;
    protected Vector2f velocity;
    protected EnumParticlePositionType positionType;
    protected EnumParticleRenderType renderType;
    protected float greater;

    public Particle(World world, int id, TextureRegister text, Vector2f pos, Vector2f velocity, float rot, float rotSpeed,
                    Vector2f size, float sizeSpeed, Vector4f color, float alphaSpeed, float greater, boolean isAlphaFromZero, boolean canCollide, EnumParticlePositionType positionType, EnumParticleRenderType renderType) {
        super(world, id, text, pos, size);
        this.canCollide = canCollide;
        this.isAlphaFromZero = isAlphaFromZero;
        this.positionType = positionType;
        this.renderType = renderType;

        alphaVelocity = alphaSpeed;
        sizeVelocity = sizeSpeed;
        this.color = color;
        this.greater = greater;
        this.velocity = velocity;
        rotate = rot;
        rotationSpeed = rotSpeed;
        zeroVelocity = velocity.length() != 0;

        if (isAlphaFromZero) {
            maxAlpha = color.w;
            this.color.w = 0.0f;
        }

        if (canCollide) {
            createBody(pos);
        }

        if (world.isRemote()) addParticle();
    }

    public Particle(int id, Vector2f pos, Vector2f velocity, float rot, float rotSpeed,
                    Vector2f size, float sizeSpeed, Vector4f color, float alphaSpeed, float greater, boolean isAlphaFromZero, boolean canCollide, EnumParticlePositionType positionType, EnumParticleRenderType renderType) {
        this(MainServer.getInstance().getWorld(), id, null, pos, velocity, rot, rotSpeed, size, sizeSpeed, color, alphaSpeed, greater, isAlphaFromZero, canCollide, positionType, renderType);
    }

    public Particle(int id, TextureRegister text, Vector2f pos, Vector2f velocity, float rot, float rotSpeed,
                    Vector2f size, float sizeSpeed, Vector4f color, float alphaSpeed, float greater, boolean isAlphaFromZero, boolean canCollide, EnumParticlePositionType positionType, EnumParticleRenderType renderType) {
        this(Core.getCore().getWorld(), id, text, pos, velocity, rot, rotSpeed, size, sizeSpeed, color, alphaSpeed, greater, isAlphaFromZero, canCollide, positionType, renderType);
    }

    public Particle(TextureRegister text, Vector2f pos, Vector2f velocity, float rot, float rotSpeed,
                    Vector2f size, float sizeSpeed, Vector4f color, float alphaSpeed, float greater, boolean isAlphaFromZero, boolean canCollide, EnumParticlePositionType positionType, EnumParticleRenderType renderType) {
        this(Core.getCore().getWorld(), -1, text, pos, velocity, rot, rotSpeed, size, sizeSpeed, color, alphaSpeed, greater, isAlphaFromZero, canCollide, positionType, renderType);
    }

    protected void addParticle() {
        ParticleRenderer.getInstance().addParticle(this);
    }

    public void setCanCollide(boolean canCollide) {
        this.canCollide = canCollide;
        updateBody();
    }

    private void updateBody() {}

    @Override
    protected void createAABB() {
        if (canCollide) {
            super.createAABB();
        }
    }

    protected void createFixtures() {
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
    protected void createBody(Vector2f pos) {
        if (canCollide) {
            super.createBody(pos);

            createFixtures();
            body.translate(pos.x, pos.y);
            body.setMass(MassType.NORMAL);
            body.setUserData(this);
            body.setLinearVelocity(velocity.x, velocity.y);
            body.getTransform().setRotation(rotate);
            body.setAngularVelocity(rotationSpeed);
            body.setLinearDamping(0.05f);
            body.setAngularDamping(0.005f);
            world.addDynamicParticle(this);
        }
    }

    @Override
    public void update() {
        if (!canCollide) {
            position.x += velocity.x * TimeUtils.UPDATE_DELTA_TIME;
            position.y += velocity.y * TimeUtils.UPDATE_DELTA_TIME;
            rotate += rotationSpeed * TimeUtils.UPDATE_DELTA_TIME;

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

    public void renderParticle(ParticleShader shader) {
        render(shader);
    }

    public void setSizeVelocity(float sizeVelocity) {
        this.sizeVelocity = sizeVelocity;
    }

    public void setAlphaVelocity(float alphaVelocity) {
        this.alphaVelocity = alphaVelocity;
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
        else return rotate;
    }

    public float getGreater() {
        return greater;
    }

    public float getAlphaVelocity() {
        return alphaVelocity;
    }

    public float getSizeVelocity() {
        return sizeVelocity;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }
}

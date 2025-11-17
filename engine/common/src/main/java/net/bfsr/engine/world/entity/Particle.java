package net.bfsr.engine.world.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.renderer.particle.ParticleRender;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.particle.ParticleType;
import org.joml.Vector2f;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class Particle extends GameObject {
    private final ParticleManager particleManager;
    private final ParticleRenderer particleRenderer = Engine.getRenderer().getParticleRenderer();

    @Getter
    protected float sin, cos;
    @Getter
    protected float sizeVelocity;
    private float angularVelocitySin, angularVelocityCos;
    @Getter
    protected boolean zeroVelocity;
    @Getter
    private final Vector2f velocity = new Vector2f();
    @Getter
    private ParticleRender render;
    @Getter
    private final Vector2f localPosition = new Vector2f();
    private Consumer<Particle> updateLogic;
    @Getter
    private ParticleType particleType;

    public Particle init(long textureHandle, float worldX, float worldY, float z, float velocityX, float velocityY, float sin,
                         float cos, float angularVelocity, float scaleX, float scaleY, float sizeVelocity, float r, float g, float b,
                         float a, float alphaVelocity, boolean isAlphaFromZero, ParticleType particleType) {
        return init(textureHandle, worldX, worldY, 0, 0, z,
                velocityX, velocityY, sin, cos, angularVelocity, scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity,
                isAlphaFromZero, particleType, Particle::defaultUpdateLogic, ParticleRender::defaultUpdateLastValues);
    }

    public Particle init(long textureHandle, float worldX, float worldY, float localX, float localY, float z, float velocityX,
                         float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY, float sizeVelocity,
                         float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero, ParticleType particleType) {
        return init(textureHandle, worldX, worldY, localX, localY, z, velocityX, velocityY, sin, cos, angularVelocity,
                scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity, isAlphaFromZero, particleType,
                Particle::defaultUpdateLogic, ParticleRender::defaultUpdateLastValues);
    }

    public Particle init(long textureHandle, float worldX, float worldY, float localX, float localY, float z, float velocityX,
                         float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY, float sizeVelocity,
                         float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero, ParticleType particleType,
                         Consumer<Particle> updateLogic, Consumer<ParticleRender> lastValuesUpdateConsumer) {
        this.particleType = particleType;
        render = particleRenderer.newRender(particleType).init(this, worldX, worldY, z, sin, cos, scaleX, scaleY,
                textureHandle, r, g, b, a, isAlphaFromZero,
                Engine.convertToDeltaTime(alphaVelocity), particleRenderer.getBuffersHolder(particleType), lastValuesUpdateConsumer);
        super.setPosition(worldX, worldY);
        this.localPosition.set(localX, localY);
        this.velocity.set(Engine.convertToDeltaTime(velocityX), Engine.convertToDeltaTime(velocityY));
        this.sin = sin;
        this.cos = cos;
        float angularVelocityInFrames = Engine.convertToDeltaTime(angularVelocity);
        this.angularVelocitySin = LUT.sin(angularVelocityInFrames);
        this.angularVelocityCos = LUT.cos(angularVelocityInFrames);
        super.setSize(scaleX, scaleY);
        this.sizeVelocity = Engine.convertToDeltaTime(sizeVelocity);
        this.zeroVelocity = velocity.lengthSquared() == 0.0f;
        this.isDead = false;
        this.updateLogic = updateLogic;
        addParticle();
        return this;
    }

    private void addParticle() {
        particleManager.addParticle(this);
        particleRenderer.addParticle(render, particleType);
    }

    @Override
    public void update() {
        updateLogic.accept(this);
        render.postWorldUpdate();
    }

    public void defaultUpdateLogic() {
        setRotation(this.cos * angularVelocitySin + this.sin * angularVelocityCos,
                this.cos * angularVelocityCos - this.sin * angularVelocitySin);

        if (!zeroVelocity) {
            addPosition(velocity.x, velocity.y);
            velocity.x *= 0.99f;
            velocity.y *= 0.99f;
        }

        if (sizeVelocity != 0) {
            addSize(sizeVelocity, sizeVelocity);

            if (getSizeX() <= 0.0f || getSizeY() <= 0.0f)
                setDead();
        }
    }

    @Override
    protected void addPosition(float x, float y) {
        super.addPosition(x, y);
        render.setPosition(getX(), getY());
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        render.setPosition(x, y);
    }

    public void setRotation(float sin, float cos) {
        this.sin = sin;
        this.cos = cos;
        render.setRotation(sin, cos);
    }

    @Override
    public void addSize(float x, float y) {
        super.addSize(x, y);
        render.setSize(getSizeX(), getSizeY());
    }

    @Override
    public void setSize(float x, float y) {
        super.setSize(x, y);
        render.setSize(x, y);
    }

    public void clear() {
        particleManager.remove(this);
        particleRenderer.remove(particleType, render);
    }
}
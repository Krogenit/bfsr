package net.bfsr.client.particle;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.client.renderer.particle.ParticleRender;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.particle.RenderLayer;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.RigidBody;
import org.joml.Vector2f;

import java.util.function.Consumer;

public class Particle extends GameObject {
    private static final ObjectPool<ParticleRender> RENDER_POOL = new ObjectPool<>(ParticleRender::new);
    static final ParticleManager PARTICLE_MANAGER = Core.get().getParticleManager();
    static final ParticleRenderer PARTICLE_RENDERER = Core.get().getGlobalRenderer().getParticleRenderer();

    @Getter
    @Setter
    protected float sin, cos;
    protected float localSin, localCos;
    @Getter
    protected float sizeVelocity, alphaVelocity;
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

    public Particle init(TextureRegister texture, float worldX, float worldY, float velocityX,
                         float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero,
                         RenderLayer renderLayer) {
        return init(Engine.assetsManager.getTexture(texture).getTextureHandle(), worldX, worldY, 0, 0, velocityX,
                velocityY, sin, cos, angularVelocity, scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity, isAlphaFromZero,
                renderLayer, Particle::defaultUpdateLogic);
    }

    public Particle init(long textureHandle, float worldX, float worldY, float localX, float localY, float velocityX,
                         float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero,
                         RenderLayer renderLayer) {
        return init(textureHandle, worldX, worldY, localX, localY, velocityX, velocityY, sin, cos, angularVelocity, scaleX,
                scaleY, sizeVelocity, r, g, b, a, alphaVelocity, isAlphaFromZero, renderLayer, Particle::defaultUpdateLogic);
    }

    public Particle init(long textureHandle, float worldX, float worldY, float localX, float localY, float velocityX,
                         float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero,
                         RenderLayer renderLayer, Consumer<Particle> updateLogic) {
        this.position.set(worldX, worldY);
        this.localPosition.set(localX, localY);
        Core core = Core.get();
        this.velocity.set(core.convertToDeltaTime(velocityX), core.convertToDeltaTime(velocityY));
        this.sin = sin;
        this.cos = cos;
        this.localSin = 0;
        this.localCos = 1;
        float angularVelocityInTick = core.convertToDeltaTime(angularVelocity);
        this.angularVelocitySin = LUT.sin(angularVelocityInTick);
        this.angularVelocityCos = LUT.cos(angularVelocityInTick);
        this.size.set(scaleX, scaleY);
        this.sizeVelocity = core.convertToDeltaTime(sizeVelocity);
        this.alphaVelocity = core.convertToDeltaTime(alphaVelocity);
        this.zeroVelocity = velocity.lengthSquared() <= 0.01f;
        this.isDead = false;
        this.updateLogic = updateLogic;
        addParticle(textureHandle, r, g, b, a, isAlphaFromZero, renderLayer);
        return this;
    }

    protected void addParticle(long textureHandle, float r, float g, float b, float a, boolean isAlphaFromZero,
                               RenderLayer renderLayer) {
        PARTICLE_MANAGER.addParticle(this);
        render = RENDER_POOL.get().init(this, textureHandle, r, g, b, a, isAlphaFromZero);
        PARTICLE_RENDERER.addParticleToRenderLayer(render, renderLayer);
    }

    @Override
    public void update() {
        updateLogic.accept(this);
    }

    public void defaultUpdateLogic() {
        float cos = this.cos * angularVelocityCos - this.sin * angularVelocitySin;
        float sin = this.cos * angularVelocitySin + this.sin * angularVelocityCos;
        this.cos = cos;
        this.sin = sin;

        if (!zeroVelocity) {
            position.x += velocity.x;
            position.y += velocity.y;
            velocity.x *= 0.99f;
            velocity.y *= 0.99f;
        }

        if (sizeVelocity != 0) {
            size.add(sizeVelocity, sizeVelocity);

            if (size.x <= 0.0f || size.y <= 0.0f)
                setDead();
        }
    }

    public void connectedToObjectUpdateLogic(RigidBody<?> object) {
        float cos = this.localCos * angularVelocityCos - this.localSin * angularVelocitySin;
        float sin = this.localCos * angularVelocitySin + this.localSin * angularVelocityCos;
        this.cos = cos * object.getCos() - sin * object.getSin();
        this.sin = cos * object.getSin() + sin * object.getCos();
        Vector2f objectPosition = object.getPosition();

        if (!zeroVelocity) {
            localPosition.x += velocity.x;
            localPosition.y += velocity.y;
            velocity.x *= 0.99f;
            velocity.y *= 0.99f;
        }

        if (sizeVelocity != 0) {
            size.add(sizeVelocity, sizeVelocity);

            if (size.x <= 0.0f || size.y <= 0.0f)
                setDead();
        }

        this.position.x = objectPosition.x + localPosition.x;
        this.position.y = objectPosition.y + localPosition.y;
    }

    public void onRemoved() {
        ParticleManager.PARTICLE_POOL.returnBack(this);
        RENDER_POOL.returnBack(render);
    }
}
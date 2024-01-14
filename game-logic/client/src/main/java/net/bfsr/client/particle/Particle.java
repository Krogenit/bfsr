package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.renderer.particle.ParticleRender;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.particle.RenderLayer;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.entity.GameObject;
import org.joml.Vector2f;

public class Particle extends GameObject {
    private static final ObjectPool<ParticleRender> RENDER_POOL = new ObjectPool<>(ParticleRender::new);
    static final ParticleManager PARTICLE_MANAGER = Core.get().getParticleManager();
    static final ParticleRenderer PARTICLE_RENDERER = Core.get().getGlobalRenderer().getParticleRenderer();

    @Getter
    protected float sin, cos;
    @Getter
    protected float sizeVelocity, alphaVelocity;
    private float angularVelocitySin, angularVelocityCos;
    @Getter
    protected boolean zeroVelocity;
    private final Vector2f velocity = new Vector2f();
    private ParticleRender render;

    public Particle init(TextureRegister texture, float x, float y, float velocityX, float velocityY, float sin, float cos,
                         float angularVelocity,
                         float scaleX, float scaleY, float sizeVelocity, float r, float g, float b, float a, float alphaVelocity,
                         boolean isAlphaFromZero,
                         RenderLayer renderLayer) {
        return init(Engine.assetsManager.getTexture(texture).getTextureHandle(), x, y, velocityX, velocityY, sin, cos,
                angularVelocity,
                scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity, isAlphaFromZero, renderLayer);
    }

    public Particle init(long textureHandle, float x, float y, float velocityX, float velocityY, float sin, float cos,
                         float angularVelocity,
                         float scaleX, float scaleY, float sizeVelocity, float r, float g, float b, float a, float alphaVelocity,
                         boolean isAlphaFromZero,
                         RenderLayer renderLayer) {
        this.position.set(x, y);
        Core core = Core.get();
        this.velocity.set(core.convertToDeltaTime(velocityX), core.convertToDeltaTime(velocityY));
        this.sin = sin;
        this.cos = cos;
        float angularVelocityInTick = core.convertToDeltaTime(angularVelocity);
        this.angularVelocitySin = LUT.sin(angularVelocityInTick);
        this.angularVelocityCos = LUT.cos(angularVelocityInTick);
        this.size.set(scaleX, scaleY);
        this.sizeVelocity = core.convertToDeltaTime(sizeVelocity);
        this.alphaVelocity = core.convertToDeltaTime(alphaVelocity);
        this.zeroVelocity = velocity.lengthSquared() <= 0.01f;
        this.isDead = false;
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

    public void onRemoved() {
        ParticleManager.PARTICLE_POOL.returnBack(this);
        RENDER_POOL.returnBack(render);
    }
}
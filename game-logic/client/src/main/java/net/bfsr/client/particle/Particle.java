package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.Client;
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
    private static final ObjectPool<ParticleRender>[] RENDER_POOL = new ObjectPool[4];

    static {
        RENDER_POOL[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()] = new ObjectPool<>(ParticleRender::new);
        RENDER_POOL[RenderLayer.BACKGROUND_ADDITIVE.ordinal()] = new ObjectPool<>(ParticleRender::new);
        RENDER_POOL[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()] = new ObjectPool<>(ParticleRender::new);
        RENDER_POOL[RenderLayer.DEFAULT_ADDITIVE.ordinal()] = new ObjectPool<>(ParticleRender::new);
    }

    private static final ParticleManager PARTICLE_MANAGER = Client.get().getParticleManager();
    private static final ParticleRenderer PARTICLE_RENDERER = Client.get().getGlobalRenderer().getParticleRenderer();

    @Getter
    protected float sin, cos;
    private float localSin, localCos;
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
    private RenderLayer renderLayer;

    public Particle init(TextureRegister texture, float worldX, float worldY, float velocityX,
                         float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero,
                         RenderLayer renderLayer) {
        return init(Engine.assetsManager.getTexture(texture).getTextureHandle(), worldX, worldY, 0, 0, velocityX,
                velocityY, sin, cos, angularVelocity, scaleX, scaleY, sizeVelocity, r, g, b, a, alphaVelocity, isAlphaFromZero,
                renderLayer, Particle::defaultUpdateLogic, ParticleRender::defaultUpdateLastValues);
    }

    public Particle init(long textureHandle, float worldX, float worldY, float localX, float localY, float velocityX,
                         float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero,
                         RenderLayer renderLayer) {
        return init(textureHandle, worldX, worldY, localX, localY, velocityX, velocityY, sin, cos, angularVelocity, scaleX,
                scaleY, sizeVelocity, r, g, b, a, alphaVelocity, isAlphaFromZero, renderLayer, Particle::defaultUpdateLogic,
                ParticleRender::defaultUpdateLastValues);
    }

    public Particle init(long textureHandle, float worldX, float worldY, float localX, float localY, float velocityX,
                         float velocityY, float sin, float cos, float angularVelocity, float scaleX, float scaleY,
                         float sizeVelocity, float r, float g, float b, float a, float alphaVelocity, boolean isAlphaFromZero,
                         RenderLayer renderLayer, Consumer<Particle> updateLogic,
                         Consumer<ParticleRender> lastValuesUpdateConsumer) {
        this.renderLayer = renderLayer;
        Client client = Client.get();
        render = RENDER_POOL[renderLayer.ordinal()].get().init(this, worldX, worldY, sin, cos, scaleX, scaleY, textureHandle,
                r, g, b, a, isAlphaFromZero, client.convertToDeltaTime(alphaVelocity),
                PARTICLE_RENDERER.getBuffersHolder(renderLayer), lastValuesUpdateConsumer);
        super.setPosition(worldX, worldY);
        this.localPosition.set(localX, localY);
        this.velocity.set(client.convertToDeltaTime(velocityX), client.convertToDeltaTime(velocityY));
        this.sin = sin;
        this.cos = cos;
        this.localSin = 0;
        this.localCos = 1;
        float angularVelocityInTick = client.convertToDeltaTime(angularVelocity);
        this.angularVelocitySin = LUT.sin(angularVelocityInTick);
        this.angularVelocityCos = LUT.cos(angularVelocityInTick);
        super.setSize(scaleX, scaleY);
        this.sizeVelocity = client.convertToDeltaTime(sizeVelocity);
        this.zeroVelocity = velocity.lengthSquared() <= 0.01f;
        this.isDead = false;
        this.updateLogic = updateLogic;
        addParticle();
        return this;
    }

    protected void addParticle() {
        PARTICLE_MANAGER.addParticle(this);
        PARTICLE_RENDERER.addParticleToRenderLayer(render, renderLayer);
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

    public void connectedToObjectUpdateLogic(RigidBody object) {
        float cos = this.localCos * angularVelocityCos - this.localSin * angularVelocitySin;
        float sin = this.localCos * angularVelocitySin + this.localSin * angularVelocityCos;
        this.cos = cos * object.getCos() - sin * object.getSin();
        this.sin = cos * object.getSin() + sin * object.getCos();

        if (!zeroVelocity) {
            localPosition.x += velocity.x;
            localPosition.y += velocity.y;
            velocity.x *= 0.99f;
            velocity.y *= 0.99f;
        }

        if (sizeVelocity != 0) {
            addSize(sizeVelocity, sizeVelocity);

            if (getSizeX() <= 0.0f || getSizeY() <= 0.0f)
                setDead();
        }

        addPosition(localPosition.x, localPosition.y);
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
        ParticleManager.PARTICLE_POOL.returnBack(this);
        RENDER_POOL[renderLayer.ordinal()].returnBack(render);
    }
}
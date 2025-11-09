package net.bfsr.client.renderer;

import lombok.Getter;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.event.world.WorldInitEvent;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.constant.BlendFactor;
import net.bfsr.engine.renderer.culling.AbstractGPUFrustumCullingSystem;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class WorldRenderer {
    private final AbstractRenderer renderer = Engine.getRenderer();
    private final AbstractGPUFrustumCullingSystem cullingSystem = renderer.getCullingSystem();
    private final AbstractSpriteRenderer spriteRenderer = renderer.getSpriteRenderer();
    @Getter
    private final ParticleRenderer particleRenderer = renderer.getParticleRenderer();

    private final Profiler profiler;
    private final EntityRenderer entityRenderer;
    private final BackgroundRenderer backgroundRenderer;

    public WorldRenderer(Layers layers, Profiler profiler, EntityRenderer entityRenderer, EventBus eventBus) {
        this.profiler = profiler;
        this.backgroundRenderer = new BackgroundRenderer(layers, renderer);
        this.entityRenderer = entityRenderer;

        Engine.getAssetsManager().getTexture(TextureRegister.damageFire).bind();
        eventBus.register(this);
    }

    void prepareRender(int totalParticlesCount) {
        particleRenderer.putParticlesToBuffers(totalParticlesCount);
        backgroundRenderer.render();
        entityRenderer.render();
    }

    public void render() {
        profiler.start("background");
        spriteRenderer.render(BufferType.BACKGROUND);

        profiler.endStart("entitiesAlpha");

        if (renderer.isEntitiesGPUFrustumCulling()) {
            renderer.blendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
            AbstractBuffersHolder buffersHolder = spriteRenderer.getBuffersHolder(BufferType.ENTITIES_ALPHA);
            cullingSystem.renderFrustumCulled(buffersHolder.getRenderObjects(), buffersHolder);
            buffersHolder.setRenderObjects(0);
            profiler.endStart("entitiesAdditive");
            renderer.blendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE);
            buffersHolder = spriteRenderer.getBuffersHolder(BufferType.ENTITIES_ADDITIVE);
            cullingSystem.renderFrustumCulled(buffersHolder.getRenderObjects(), buffersHolder);
            buffersHolder.setRenderObjects(0);
        } else {
            renderer.blendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
            spriteRenderer.render(BufferType.ENTITIES_ALPHA);
            profiler.endStart("entitiesAdditive");
            renderer.blendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE);
            spriteRenderer.render(BufferType.ENTITIES_ADDITIVE);
        }

        profiler.endStart("particles");
        profiler.start("waitTasks");
        particleRenderer.waitTasks();
        profiler.end();
        particleRenderer.render();
        renderer.blendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
        profiler.end();
    }

    @EventHandler
    public EventListener<WorldInitEvent> event() {
        return event -> backgroundRenderer.createBackgroundTexture(event.getWorld().getSeed());
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> {
            particleRenderer.removeAllRenders();
            backgroundRenderer.clear();
        };
    }

    public void clear() {
        particleRenderer.clear();
        backgroundRenderer.clear();
    }
}
package net.bfsr.client.renderer;

import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class WorldRenderer {
    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractSpriteRenderer spriteRenderer = renderer.spriteRenderer;

    private final Profiler profiler;
    private final RenderManager renderManager;
    @Getter
    private final ParticleRenderer particleRenderer;
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();

    public WorldRenderer(Profiler profiler, RenderManager renderManager) {
        this.profiler = profiler;
        this.renderManager = renderManager;
        this.particleRenderer = new ParticleRenderer(profiler);
    }

    public void init() {
        if (ClientSettings.IS_DEBUG.getBoolean()) {
            renderer.setDebugWindow();
        }

        Engine.assetsManager.getTexture(TextureRegister.damageFire).bind();

        backgroundRenderer.init();
        particleRenderer.init();

        Client.get().getEventBus().register(this);
    }

    public void update() {
        particleRenderer.update();
    }

    void prepareRender(int totalParticlesCount) {
        particleRenderer.putBackgroundParticlesToBuffers(totalParticlesCount);
        backgroundRenderer.render();
        renderManager.renderAlpha();
        renderManager.renderAdditive();
        particleRenderer.putParticlesToBuffers();
    }

    public void render() {
        profiler.start("background");
        spriteRenderer.render(BufferType.BACKGROUND);
        profiler.endStart("particlesBackground");
        particleRenderer.renderBackground();
        profiler.endStart("entitiesAlpha");
        renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        spriteRenderer.syncAndRender(BufferType.ENTITIES_ALPHA);
        profiler.endStart("entitiesAdditive");
        renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
        spriteRenderer.syncAndRender(BufferType.ENTITIES_ADDITIVE);
        profiler.endStart("particles");
        particleRenderer.render();
        renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        profiler.end();
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> {
            particleRenderer.clear();
            backgroundRenderer.clear();
        };
    }
}
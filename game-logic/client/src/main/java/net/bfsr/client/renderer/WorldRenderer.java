package net.bfsr.client.renderer;

import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureLoader;
import net.bfsr.engine.renderer.texture.TextureRegister;

import java.util.Random;

public class WorldRenderer {
    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractCamera camera = renderer.camera;
    private final AbstractSpriteRenderer spriteRenderer = renderer.spriteRenderer;

    private final Profiler profiler;
    private final RenderManager renderManager;
    @Getter
    private final ParticleRenderer particleRenderer;

    private AbstractTexture backgroundTexture = AbstractTextureLoader.dummyTexture;

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

        particleRenderer.init();

        Core.get().getEventBus().register(this);
    }

    public void update() {
        particleRenderer.update();
    }

    void prepareRender(int totalParticlesCount, float interpolation) {
        particleRenderer.putBackgroundParticlesToBuffers(totalParticlesCount);
        prepareAmbient(interpolation);
        spriteRenderer.addTask(renderManager::renderAlpha, BufferType.ENTITIES_ALPHA);
        spriteRenderer.addTask(renderManager::renderAdditive, BufferType.ENTITIES_ADDITIVE);
        particleRenderer.putParticlesToBuffers();
    }

    private void prepareAmbient(float interpolation) {
        float moveFactor = 0.005f;
        float cameraZoom = camera.getLastZoom() + (camera.getZoom() - camera.getLastZoom()) * interpolation;
        float lastX = (camera.getLastPosition().x - camera.getLastPosition().x * moveFactor / cameraZoom);
        float lastY = (camera.getLastPosition().y - camera.getLastPosition().y * moveFactor / cameraZoom);
        float x = (camera.getPosition().x - camera.getPosition().x * moveFactor / cameraZoom);
        float y = (camera.getPosition().y - camera.getPosition().y * moveFactor / cameraZoom);
        float zoom = (float) (0.5f + Math.log(cameraZoom) * 0.01f);
        float scaleX = backgroundTexture.getWidth() / cameraZoom * zoom;
        float scaleY = backgroundTexture.getHeight() / cameraZoom * zoom;
        spriteRenderer.add(lastX, lastY, x, y, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f, backgroundTexture, BufferType.BACKGROUND);
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

    void createBackgroundTexture(long seed) {
        backgroundTexture = Engine.renderer.textureGenerator.generateNebulaTexture(5120, 5120, new Random(seed));
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> {
            particleRenderer.clear();
            backgroundTexture.delete();
            backgroundTexture = AbstractTextureLoader.dummyTexture;
        };
    }
}
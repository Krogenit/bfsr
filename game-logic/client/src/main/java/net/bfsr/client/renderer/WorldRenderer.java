package net.bfsr.client.renderer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.client.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.renderer.particle.ParticleRenderer;
import net.bfsr.client.settings.Option;
import net.bfsr.client.world.WorldClient;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.shader.AbstractShaderProgram;
import net.bfsr.engine.renderer.texture.AbstractTexture;

import java.util.Random;

@RequiredArgsConstructor
public class WorldRenderer {
    private final Core core;

    @Getter
    private final RenderManager renderManager = new RenderManager();
    private final net.bfsr.engine.renderer.AbstractRenderer renderer = Engine.renderer;
    private final AbstractShaderProgram shader = renderer.shader;
    private final AbstractCamera camera = renderer.camera;
    @Getter
    private final ParticleRenderer particleRenderer = new ParticleRenderer();
    private final AbstractSpriteRenderer spriteRenderer = renderer.spriteRenderer;
    private final AbstractDebugRenderer debugRenderer = renderer.debugRenderer;

    private AbstractTexture backgroundTexture;

    public void init() {
        Engine.renderer.setVSync(Option.V_SYNC.getBoolean());
        backgroundTexture = Engine.assetsManager.textureLoader.createTexture(2560 << 1, 2560 << 1);

        if (Option.IS_DEBUG.getBoolean()) {
            Engine.renderer.setDebugWindow();
        }

        particleRenderer.init();
    }

    public void update() {
        renderManager.update();

        if (!Engine.isPaused()) {
            particleRenderer.update();
        }
    }

    public void postUpdate() {
        renderManager.postUpdate();
    }

    public void prepareRender(float interpolation) {
        WorldClient world = core.getWorld();
        if (world != null) {
            particleRenderer.putBackgroundParticlesToBuffers(world.getParticlesCount());
            prepareAmbient(interpolation);
            spriteRenderer.addTask(renderManager::renderAlpha, BufferType.ENTITIES_ALPHA);
            spriteRenderer.addTask(renderManager::renderAdditive, BufferType.ENTITIES_ADDITIVE);
            particleRenderer.putParticlesToBuffers();
        }
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

    public void render(float interpolation) {
        renderer.resetDrawCalls();
        renderer.glClear();
        camera.calculateInterpolatedViewMatrix(interpolation);
        camera.bindInterpolatedWorldViewMatrix();
        spriteRenderer.bind();
        shader.enable();

        WorldClient world = core.getWorld();
        if (world != null) {
            spriteRenderer.render(BufferType.BACKGROUND);
            particleRenderer.renderBackground();
            renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            spriteRenderer.syncAndRender(BufferType.ENTITIES_ALPHA);
            renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
            spriteRenderer.syncAndRender(BufferType.ENTITIES_ADDITIVE);
            particleRenderer.render();

            renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            if (Option.SHOW_DEBUG_BOXES.getBoolean()) {
                debugRenderer.clear();
                debugRenderer.bind();
                camera.bindWorldViewMatrix();
                renderManager.renderDebug();
                debugRenderer.render(GL.GL_LINE_LOOP);
                spriteRenderer.bind();
                shader.enable();
            }
        }

        camera.bindGUI();

        if (world != null) {
            core.getGuiManager().getGuiInGame().render();
        }

        Gui gui = core.getGuiManager().getCurrentGui();
        if (gui != null) {
            gui.render();
        }

        spriteRenderer.render(BufferType.GUI);
    }

    public void createBackgroundTexture(long seed) {
        backgroundTexture.delete();
        backgroundTexture = Engine.renderer.textureGenerator.generateNebulaTexture(backgroundTexture.getWidth(), backgroundTexture.getHeight(), new Random(seed));
    }

    public void reloadShaders() {
        shader.delete();
        shader.load();
        shader.init();
    }

    public void onExitToMainMenu() {
        particleRenderer.onExitToMainMenu();
        renderManager.onExitToMainMenu();
    }
}
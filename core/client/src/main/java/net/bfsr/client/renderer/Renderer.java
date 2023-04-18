package net.bfsr.client.renderer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.renderer.buffer.BufferType;
import net.bfsr.client.renderer.debug.DebugRenderer;
import net.bfsr.client.renderer.font.string.StringGeometryBuilder;
import net.bfsr.client.renderer.font.string.StringRenderer;
import net.bfsr.client.renderer.gui.GUIRenderer;
import net.bfsr.client.renderer.particle.ParticleRenderer;
import net.bfsr.client.renderer.shader.BaseShader;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureGenerator;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.settings.Option;
import net.bfsr.client.world.WorldClient;
import net.bfsr.texture.TextureRegister;

import java.util.Random;

import static net.bfsr.client.renderer.debug.OpenGLDebugUtils.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_OTHER;
import static org.lwjgl.opengl.GL43C.glDebugMessageCallback;
import static org.lwjgl.opengl.GLDebugMessageCallback.getMessage;
import static org.lwjgl.system.MemoryUtil.NULL;

@Log4j2
public class Renderer {
    private final Core core;
    @Getter
    private final Camera camera = new Camera();
    @Getter
    private final BaseShader shader = new BaseShader();
    @Getter
    private final StringGeometryBuilder stringGeometryBuilder = new StringGeometryBuilder();
    @Getter
    private final StringRenderer stringRenderer = new StringRenderer();
    @Getter
    private final ParticleRenderer particleRenderer = new ParticleRenderer();
    @Getter
    private final SpriteRenderer spriteRenderer = new SpriteRenderer();
    @Getter
    private final GUIRenderer guiRenderer = new GUIRenderer();
    private final DebugRenderer debugRenderer = new DebugRenderer();
    @Getter
    private int drawCalls;
    @Getter
    private int lastFrameDrawCalls;
    @Setter
    @Getter
    private int fps;
    @Getter
    private float interpolation;

    public Renderer(Core core) {
        this.core = core;
    }

    public void init(long window, int width, int height) {
        setupOpenGL(core.getScreenWidth(), core.getScreenHeight());
        setVSync(Option.V_SYNC.getBoolean());

        TextureLoader.init();
        TextureGenerator.init();

        camera.init(core.getScreenWidth(), core.getScreenHeight());
        spriteRenderer.init();
        stringRenderer.init(stringGeometryBuilder, spriteRenderer);
        guiRenderer.init(spriteRenderer);
        particleRenderer.init(spriteRenderer);
        debugRenderer.init();
        shader.load();
        shader.init();

        if (Option.IS_DEBUG.getBoolean()) {
            glfwRestoreWindow(window);
            glfwSetWindowSize(window, 1280, 720);
            glfwSetWindowPos(window, (width - 1280) / 2, (height - 720) / 2);
        }

        glfwShowWindow(window);
    }

    private void setupOpenGL(int width, int height) {
        glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
            if (type != GL_DEBUG_TYPE_OTHER) {
                log.info("GLDebug {} {}, {}, {}, {}", getDebugSeverity(severity), String.format("0x%X", id),
                        getDebugSource(source), getDebugType(type), getMessage(length, message));
            }
        }, NULL);

        glViewport(0, 0, width, height);

        glEnable(GL_ALPHA_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glAlphaFunc(GL_GREATER, 0.0001f);
        TextureLoader.getTexture(TextureRegister.damageFire, GL_REPEAT, GL_LINEAR).bind();

        glClearColor(0.05F, 0.1F, 0.2F, 1.0F);
    }

    public void updateCamera() {
        camera.update();
    }

    public void prepareRender(float interpolation) {
        if (Core.get().isPaused()) {
            interpolation = 1.0f;
        }

        this.interpolation = interpolation;

        WorldClient world = core.getWorld();
        if (world != null) {
            particleRenderer.putBackgroundParticlesToBuffers();
            world.prepareAmbient();
            world.prepareEntities();
            particleRenderer.putParticlesToBuffers();
        }
    }

    public void render() {
        resetDrawCalls();
        glClear(GL_COLOR_BUFFER_BIT);
        camera.calculateInterpolatedViewMatrix(interpolation);
        camera.bindInterpolatedWorldViewMatrix();
        spriteRenderer.bind();
        shader.enable();

        WorldClient world = core.getWorld();
        if (world != null) {
            world.renderAmbient();
            particleRenderer.renderBackground();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            world.renderEntitiesAlpha();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            world.renderEntitiesAdditive();
            particleRenderer.render();

            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            if (Option.SHOW_DEBUG_BOXES.getBoolean()) {
                debugRenderer.clear();
                debugRenderer.bind();
                camera.bindWorldViewMatrix();
                world.renderDebug(debugRenderer);
                debugRenderer.render(GL_LINE_LOOP);
                spriteRenderer.bind();
                shader.enable();
            }
        }

        camera.bindGUI();

        if (world != null) {
            core.getGuiInGame().render();
        }

        Gui gui = core.getCurrentGui();
        if (gui != null) {
            gui.render();
        }

        spriteRenderer.render(BufferType.GUI);
    }

    public void resize(int width, int height) {
        glViewport(0, 0, width, height);
        camera.resize(width, height);
    }

    public Texture createBackgroundTexture(long seed, int sizeX, int sizeY) {
        return TextureGenerator.generateNebulaTexture(sizeX, sizeY, new Random(seed));
    }

    public void onExitToMainMenu() {
        camera.onExitToMainMenu();
        particleRenderer.onExitToMainMenu();
    }

    private void resetDrawCalls() {
        lastFrameDrawCalls = drawCalls;
        drawCalls = 0;
    }

    public void increaseDrawCalls() {
        drawCalls++;
    }

    public void setVSync(boolean value) {
        glfwSwapInterval(value ? 1 : 0);
    }

    public void reloadShaders() {
        shader.delete();
        shader.load();
        shader.init();
    }

    public void clear() {
        spriteRenderer.clear();
    }
}
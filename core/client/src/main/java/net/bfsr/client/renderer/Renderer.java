package net.bfsr.client.renderer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.ingame.GuiInGame;
import net.bfsr.client.renderer.debug.OpenGLDebugUtils;
import net.bfsr.client.renderer.font.StringGeometryBuilder;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.instanced.StringRenderer;
import net.bfsr.client.renderer.particle.ParticleRenderer;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.settings.Option;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.world.WorldClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_OTHER;
import static org.lwjgl.opengl.GL43C.glDebugMessageCallback;

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
    private final SpriteRenderer spriteRenderer = new SpriteRenderer();
    @Getter
    private GuiInGame guiInGame;
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

        camera.init(core.getScreenWidth(), core.getScreenHeight());
        spriteRenderer.init();
        stringRenderer.init(stringGeometryBuilder);
        shader.load();
        shader.init();

        guiInGame = new GuiInGame();
        guiInGame.init();

        if (Option.IS_DEBUG.getBoolean()) {
            GLFW.glfwRestoreWindow(window);
            GLFW.glfwSetWindowSize(window, 1280, 720);
            GLFW.glfwSetWindowPos(window, (width - 1280) / 2, (height - 720) / 2);
        }

        GLFW.glfwShowWindow(window);
    }

    private void setupOpenGL(int width, int height) {
        glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
            if (type != GL_DEBUG_TYPE_OTHER) {
                log.info("GLDebug {} {}, {}, {}, {}", OpenGLDebugUtils.getDebugSeverity(severity), String.format("0x%X", id),
                        OpenGLDebugUtils.getDebugSource(source), OpenGLDebugUtils.getDebugType(type), GLDebugMessageCallback.getMessage(length, message));
            }
        }, MemoryUtil.NULL);

        GL11.glViewport(0, 0, width, height);

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        GL11.glClearColor(0.05F, 0.1F, 0.2F, 1.0F);
    }

    public void updateCamera() {
        camera.update();
    }

    public void update() {
        guiInGame.update();
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
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        camera.calculateInterpolatedViewMatrix(interpolation);
        camera.bind();
        spriteRenderer.bind();
        shader.enable();
        OpenGLHelper.alphaGreater(0.0001f);

        WorldClient world = core.getWorld();
        if (world != null) {
            world.renderAmbient();
            particleRenderer.renderBackground();
            world.renderEntities();
            particleRenderer.render();
            OpenGLHelper.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (Option.SHOW_DEBUG_BOXES.getBoolean()) {
                shader.disable();
                world.renderDebug();
                shader.enable();
            }
        }

        camera.bindGUI();

        if (world != null) {
            guiInGame.render(interpolation);
        }

        Gui gui = core.getCurrentGui();
        if (gui != null) {
            gui.render(interpolation);
        }

        SpriteRenderer.INSTANCE.render(BufferType.GUI);
    }

    private void resetDrawCalls() {
        lastFrameDrawCalls = drawCalls;
        drawCalls = 0;
    }

    public void resize(int width, int height) {
        GL11.glViewport(0, 0, width, height);
        camera.resize(width, height);
        guiInGame.resize(width, height);
    }

    public void setVSync(boolean value) {
        GLFW.glfwSwapInterval(value ? 1 : 0);
    }

    public void onExitToMainMenu() {
        camera.onExitToMainMenu();
        particleRenderer.onExitToMainMenu();
    }

    public void increaseDrawCalls() {
        drawCalls++;
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
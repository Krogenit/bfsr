package net.bfsr.client.render;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.ingame.GuiInGame;
import net.bfsr.client.model.TexturedQuad;
import net.bfsr.client.render.debug.OpenGLDebugUtils;
import net.bfsr.client.render.font.StringRenderer;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.primitive.PrimitiveShaders;
import net.bfsr.core.Core;
import net.bfsr.math.Transformation;
import net.bfsr.settings.EnumOption;
import net.bfsr.world.WorldClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_OTHER;
import static org.lwjgl.opengl.GL43C.glDebugMessageCallback;

@Log4j2
public class Renderer {
    public static TexturedQuad quad, centeredQuad, counterClockWiseCnteredQuad;

    private final Core core;
    @Getter
    private final Camera camera;
    @Getter
    private final StringRenderer stringRenderer = new StringRenderer();
    @Getter
    private final BaseShader shader = new BaseShader();
    @Getter
    private GuiInGame guiInGame;
    @Getter
    private int drawCalls;
    @Getter
    private int lastFrameDrawCalls;
    @Setter
    @Getter
    private int fps;

    public Renderer(Core core) {
        this.core = core;
        camera = new Camera();
    }

    public void init(long window, int width, int height) {
        setupOpenGL(core.getWidth(), core.getHeight());
        setVSync(EnumOption.V_SYNC.getBoolean());

        TextureLoader.init();

        quad = TexturedQuad.createQuad();
        centeredQuad = TexturedQuad.createCenteredQuad();
        counterClockWiseCnteredQuad = TexturedQuad.createCounterClockWiseCenteredQuad();

        camera.init(core.getWidth(), core.getHeight());
        stringRenderer.init();
        shader.load();
        shader.init();

        guiInGame = new GuiInGame();
        guiInGame.init();

        if (EnumOption.IS_DEBUG.getBoolean()) {
            GLFW.glfwRestoreWindow(window);
            GLFW.glfwSetWindowSize(window, 1280, 720);
            GLFW.glfwSetWindowPos(window, (width - 1280) / 2, (height - 720) / 2);
        }

        GLFW.glfwShowWindow(window);

        PrimitiveShaders.INSTANCE.init();
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
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);

        GL11.glClearColor(0.05F, 0.1F, 0.2F, 1.0F);
    }

    public void update() {
        camera.update();
        guiInGame.update();
    }

    public void render(float interpolation) {
        resetDrawCalls();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        camera.bind();
        shader.enable();
        shader.enableTexture();
        shader.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        Transformation.updateViewMatrix(camera, interpolation);
        OpenGLHelper.alphaGreater(0.5f);

        WorldClient world = core.getWorld();
        if (world != null) {
            world.renderAmbient(shader, interpolation);
            world.renderBackParticles(interpolation);
            OpenGLHelper.alphaGreater(0.75f);
            world.renderEntities(shader, interpolation);
            world.renderParticles(interpolation);
            if (EnumOption.SHOW_DEBUG_BOXES.getBoolean()) {
                GL20.glUseProgram(0);
                world.renderDebug(null);
                shader.enable();
            }
        }

        shader.enable();
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, Camera.VIEW_MATRIX_UBO, camera.getGUIViewMatrixUBO());

        if (world != null) {
            guiInGame.render(shader);
        }

        shader.enable();
        Gui gui = core.getCurrentGui();
        if (gui != null) {
            OpenGLHelper.alphaGreater(0.01f);
            gui.render(shader);
        }
    }

    private void resetDrawCalls() {
        lastFrameDrawCalls = drawCalls;
        drawCalls = 0;
    }

    public void resize(int width, int height) {
        GL11.glViewport(0, 0, width, height);
        camera.resize(width, height);
        Transformation.resize(width, height);
        guiInGame.resize(width, height);
    }

    public void setVSync(boolean value) {
        GLFW.glfwSwapInterval(value ? 1 : 0);
    }

    public void checkGlError(String name) {
        int i = GL11.glGetError();

        if (i != 0) {
            log.error("########## GL ERROR ##########");
            log.error("Erorr number {}", i);
            log.error("Error in {}", name);
        }
    }

    public void clear() {
        guiInGame.clearByExit();
        camera.clear();
    }

    public void increaseDrawCalls() {
        drawCalls++;
    }
}

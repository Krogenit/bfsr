package net.bfsr.client.render;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.font.FontRenderer;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.ingame.GuiInGame;
import net.bfsr.client.model.TexturedQuad;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.primitive.PrimitiveShaders;
import net.bfsr.client.shader.primitive.VertexColorShader;
import net.bfsr.core.Core;
import net.bfsr.math.Transformation;
import net.bfsr.settings.ClientSettings;
import net.bfsr.world.WorldClient;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;

@Log4j2
public class Renderer {
    public static TexturedQuad quad;

    private final Core core;
    @Getter
    private final Camera camera;
    private final FontRenderer fontRenderer = new FontRenderer();
    @Getter
    private final BaseShader shader = new BaseShader();
    @Getter
    private GuiInGame guiInGame;
    @Setter
    @Getter
    private int drawCalls;
    private int viewDataUBO;
    private float[] viewBuffer = new float[16 * 3];
    @Setter
    @Getter
    private int fps;

    public Renderer(Core core) {
        this.core = core;
        camera = new Camera(core.getWidth(), core.getHeight());
    }

    public void init(long window, GLFWVidMode vidMode) {
        setupOpenGL(core.getWidth(), core.getHeight());
        ClientSettings settings = core.getSettings();
        setVSync(settings.isVSync());

        quad = new TexturedQuad();

        fontRenderer.init();
        shader.load();
        shader.init();

        guiInGame = new GuiInGame();
        guiInGame.init();

        viewDataUBO = GL45.glCreateBuffers();
        GL45.glNamedBufferData(viewDataUBO, 16 * 3 * 4, GL15.GL_DYNAMIC_DRAW);

        if (settings.isDebug()) {
            GLFW.glfwSetWindowSize(window, 1280, 720);
            GLFW.glfwSetWindowPos(window, (vidMode.width() - 1280) / 2, (vidMode.height() - 720) / 2);
        }

        PrimitiveShaders.INSTANCE.init();
    }

    private void setupOpenGL(int width, int height) {
        GL.createCapabilities();

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
        updateViewUBO();
        guiInGame.update();
    }

    private void updateViewUBO() {
        camera.getOrthographicMatrix().get(viewBuffer);
        camera.getViewMatrix().get(viewBuffer, 16);
        GL45.glNamedBufferSubData(viewDataUBO, 0, viewBuffer);
    }

    private float[] modelMatrixFloatArray = new float[16];

    public void setModelMatrix(Matrix4f modelMatrix) {
        GL45.glNamedBufferSubData(viewDataUBO, 32 * 4, modelMatrix.get(modelMatrixFloatArray));
    }

    public void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        fontRenderer.updateOrthographicMatrix(camera.getOrthographicMatrix());
        shader.enable();
        shader.enableTexture();
        shader.setOrthoMatrix(camera.getOrthographicMatrix());
        shader.setColor(new Vector4f(1, 1, 1, 1));
        checkGlError("init shaders");
        Transformation.updateViewMatrix(camera);
        OpenGLHelper.alphaGreater(0.5f);

        WorldClient world = core.getWorld();
        if (world != null) {
            world.renderAmbient(shader);
            world.renderBackParticles();
            OpenGLHelper.alphaGreater(0.75f);
            world.renderEntities(shader);
            checkGlError("entities");
            fontRenderer.render(EnumParticlePositionType.Default);
            world.renderParticles();
            checkGlError("particles");
            if (core.getSettings().isDebug()) {
                VertexColorShader vertexColorShader = PrimitiveShaders.INSTANCE.getVertexColorShader();
                vertexColorShader.enable();
                world.renderDebug(vertexColorShader);
                checkGlError("debug");
                shader.enable();
            }
            shader.enable();
            guiInGame.render(shader);
            checkGlError("gui in game");
            fontRenderer.render(EnumParticlePositionType.GuiInGame);
            shader.enable();
        }

        Gui gui = core.getCurrentGui();
        if (gui != null) {
            OpenGLHelper.alphaGreater(0.01f);
            gui.render(shader);
            fontRenderer.render(EnumParticlePositionType.Gui);
        }

        OpenGLHelper.alphaGreater(0.01f);
        fontRenderer.render(EnumParticlePositionType.Last);
        checkGlError("FINISH");
    }

    private void checkGlError(String name) {
        int i = GL11.glGetError();

        if (i != 0) {
            log.error("########## GL ERROR ##########");
            log.error("Erorr number {}", i);
            log.error("Error in {}", name);
        }
    }

    public void resize(int width, int height) {
        GL11.glViewport(0, 0, width, height);
        camera.resize(width, height);
        guiInGame.resize(width, height);
    }

    public void setVSync(boolean value) {
        GLFW.glfwSwapInterval(value ? 1 : 0);
    }

    public void clear() {
        guiInGame.clearByExit();
        camera.clear();
    }

    public void increaseDrawCalls() {
        drawCalls++;
    }
}

package net.bfsr.client.render;

import lombok.Getter;
import lombok.Setter;
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
import net.bfsr.core.Main;
import net.bfsr.math.Transformation;
import net.bfsr.world.WorldClient;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;

public class Renderer {
    public static TexturedQuad quad = new TexturedQuad();

    private final Core core;
    private final Camera camera;
    private final FontRenderer fontRenderer;
    private final BaseShader shader;
    private final GuiInGame guiInGame;
    private int drawCalls;
    private int viewDataUBO;
    private float[] viewBuffer = new float[16 * 3];
    @Setter
    @Getter
    private int fps;

    public Renderer(Core core) {
        this.core = core;
        camera = new Camera(core.getWidth(), core.getHeight());
        fontRenderer = new FontRenderer();
        shader = new BaseShader();
        shader.init();
        guiInGame = new GuiInGame();
        guiInGame.init();
        viewDataUBO = GL45.glCreateBuffers();
        GL45.glNamedBufferData(viewDataUBO, 16 * 3 * 4, GL15.GL_DYNAMIC_DRAW);
    }

    public void update(double delta) {
        camera.update(delta);
        updateViewUBO();
        guiInGame.update(delta);
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

    public void input() {
        if (core.getWorld() != null) guiInGame.input();
    }

    public void render() {
        fontRenderer.updateOrthographicMatrix(camera.getOrthographicMatrix());
        shader.enable();
        shader.enableTexture();
        shader.setOrthoMatrix(camera.getOrthographicMatrix());
        shader.setColor(new Vector4f(1, 1, 1, 1));
        Main.checkGlError("init shaders");
        Transformation.updateViewMatrix(camera);
        OpenGLHelper.alphaGreater(0.5f);

        WorldClient world = core.getWorld();
        if (world != null) {
            world.renderAmbient(shader);
            world.renderBackParticles();
            OpenGLHelper.alphaGreater(0.75f);
            world.renderEntities(shader);
            Main.checkGlError("entities");
            fontRenderer.render(EnumParticlePositionType.Default);
            world.renderParticles();
            Main.checkGlError("particles");
            if (core.getSettings().isDebug()) {
                VertexColorShader vertexColorShader = PrimitiveShaders.INSTANCE.getVertexColorShader();
                vertexColorShader.enable();
                world.renderDebug(vertexColorShader);
                Main.checkGlError("debug");
                shader.enable();
            }
            shader.enable();
            guiInGame.render(shader);
            Main.checkGlError("gui in game");
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
    }

    public Camera getCamera() {
        return camera;
    }

    public BaseShader getShader() {
        return shader;
    }

    public void resize(int width, int height) {
        camera.resize(width, height);
        guiInGame.resize(width, height);
    }

    public void clear() {
        guiInGame.clearByExit();
        camera.clear();
    }

    public GuiInGame getGuiInGame() {
        return guiInGame;
    }

    public void setDrawCalls(int drawCalls) {
        this.drawCalls = drawCalls;
    }

    public int getDrawCalls() {
        return drawCalls;
    }

    public void increaseDrawCalls() {
        drawCalls++;
    }
}

package net.bfsr.engine.renderer;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.renderer.camera.Camera;
import net.bfsr.engine.renderer.debug.DebugRenderer;
import net.bfsr.engine.renderer.debug.OpenGLDebugUtils;
import net.bfsr.engine.renderer.font.StringGeometryBuilder;
import net.bfsr.engine.renderer.font.StringRenderer;
import net.bfsr.engine.renderer.shader.BaseShader;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureGenerator;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_OTHER;
import static org.lwjgl.opengl.GL43C.glDebugMessageCallback;
import static org.lwjgl.opengl.GLDebugMessageCallback.getMessage;
import static org.lwjgl.system.MemoryUtil.NULL;

@Log4j2
public class Renderer extends AbstractRenderer {
    public Renderer() {
        super(new Camera(), new BaseShader(), new StringGeometryBuilder(), new StringRenderer(), new SpriteRenderer(),
                new GUIRenderer(),
                new DebugRenderer(), new TextureGenerator());
    }

    @Override
    public void setupOpenGL() {
        glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
            if (type != GL_DEBUG_TYPE_OTHER) {
                log.info("GLDebug {} {}, {}, {}, {}", OpenGLDebugUtils.getDebugSeverity(severity), String.format("0x%X", id),
                        OpenGLDebugUtils.getDebugSource(source), OpenGLDebugUtils.getDebugType(type),
                        getMessage(length, message));
            }
        }, NULL);

        glViewport(0, 0, screenWidth, screenHeight);

        glEnable(GL_ALPHA_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glAlphaFunc(GL_GREATER, 0.0001f);
        glClearColor(0.05F, 0.1F, 0.2F, 1.0F);
    }

    @Override
    public void update() {
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        glViewport(0, 0, width, height);
        camera.resize(width, height);
    }

    @Override
    public ByteBuffer createByteBuffer(int size) {
        return BufferUtils.createByteBuffer(size);
    }

    @Override
    public FloatBuffer createFloatBuffer(int size) {
        return BufferUtils.createFloatBuffer(size);
    }

    @Override
    public String glGetString(int name) {
        return GL11C.glGetString(name);
    }

    @Override
    public void glEnable(int target) {
        GL11C.glEnable(target);
    }

    @Override
    public void glDisable(int target) {
        GL11C.glDisable(target);
    }

    @Override
    public void glScissor(int x, int y, int width, int height) {
        GL11C.glScissor(x, y, width, height);
    }

    @Override
    public void glBlendFunc(int sFactor, int dFactor) {
        GL11C.glBlendFunc(sFactor, dFactor);
    }

    @Override
    public void lineWidth(float value) {
        GL11C.glLineWidth(value);
    }

    @Override
    public void glClear() {
        GL11C.glClear(GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void subImage2D(int id, int x, int y, int width, int height, int format, ByteBuffer byteBuffer) {
        GL45C.glTextureSubImage2D(id, 0, x, y, width, height, format, GL11.GL_UNSIGNED_BYTE, byteBuffer);
    }

    @Override
    public void subImage2D(int id, int x, int y, int width, int height, int format, IntBuffer buffer) {
        GL45C.glTextureSubImage2D(id, 0, x, y, width, height, format, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    @Override
    public void uploadTexture(AbstractTexture texture, int internalFormat, int format, int wrap, int filter,
                              ByteBuffer byteBuffer) {
        int id = texture.getId();
        int width = texture.getWidth();
        int height = texture.getHeight();
        GL45C.glTextureStorage2D(id, 1, internalFormat, width, height);
        GL45C.glTextureSubImage2D(id, 0, 0, 0, width, height, format, GL11.GL_UNSIGNED_BYTE, byteBuffer);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_S, wrap);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_T, wrap);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_MAG_FILTER, filter);
        long textureHandle = ARBBindlessTexture.glGetTextureHandleARB(id);
        texture.setTextureHandle(textureHandle);
        ARBBindlessTexture.glMakeTextureHandleResidentARB(textureHandle);
    }

    @Override
    public void uploadTexture(AbstractTexture texture, int internalFormat, int format, int wrap, int filter, IntBuffer buffer) {
        int id = texture.getId();
        int width = texture.getWidth();
        int height = texture.getHeight();
        GL45C.glTextureStorage2D(id, 1, internalFormat, width, height);
        GL45C.glTextureSubImage2D(id, 0, 0, 0, width, height, format, GL11.GL_UNSIGNED_BYTE, buffer);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_S, wrap);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_T, wrap);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_MAG_FILTER, filter);
        long textureHandle = ARBBindlessTexture.glGetTextureHandleARB(id);
        texture.setTextureHandle(textureHandle);
        ARBBindlessTexture.glMakeTextureHandleResidentARB(textureHandle);
    }

    @Override
    public void uploadEmpty(AbstractTexture texture, int internalFormat, int format) {
        int id = texture.getId();
        int width = texture.getWidth();
        int height = texture.getHeight();
        GL45C.glTextureStorage2D(id, 1, internalFormat, width, height);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL45C.glTextureParameteri(id, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL44C.glClearTexImage(id, 0, format, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        long textureHandle = ARBBindlessTexture.glGetTextureHandleARB(id);
        texture.setTextureHandle(textureHandle);
        ARBBindlessTexture.glMakeTextureHandleResidentARB(textureHandle);
    }

    @Override
    public void setVSync(boolean value) {
        glfwSwapInterval(value ? 1 : 0);
    }

    @Override
    public void setDebugWindow() {
        glfwRestoreWindow(window);
        int width = screenWidth;
        int height = screenHeight;
        glfwSetWindowSize(window, 1280, 720);
        glfwSetWindowPos(window, (width - 1280) / 2, (height - 720) / 2);
    }

    @Override
    public void clear() {
        spriteRenderer.clear();

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
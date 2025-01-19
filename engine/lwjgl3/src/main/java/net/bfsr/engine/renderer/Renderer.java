package net.bfsr.engine.renderer;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.camera.Camera;
import net.bfsr.engine.renderer.culling.GPUFrustumCullingSystem;
import net.bfsr.engine.renderer.debug.DebugRenderer;
import net.bfsr.engine.renderer.debug.OpenGLDebugUtils;
import net.bfsr.engine.renderer.font.StringGeometryBuilder;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.font.stb.STBTrueTypeGlyphsBuilder;
import net.bfsr.engine.renderer.font.truetype.TrueTypeGlyphsBuilder;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.shader.BaseShader;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureGenerator;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.opengl.ARBBindlessTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwRestoreWindow;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11C.GL_BACK;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_GREATER;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glCullFace;
import static org.lwjgl.opengl.GL11C.glLineWidth;
import static org.lwjgl.opengl.GL11C.glPixelStorei;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.opengl.GL45C.GL_DEBUG_TYPE_OTHER;
import static org.lwjgl.opengl.GL45C.glClearTexImage;
import static org.lwjgl.opengl.GL45C.glDebugMessageCallback;
import static org.lwjgl.opengl.GL45C.glTextureParameteri;
import static org.lwjgl.opengl.GL45C.glTextureStorage2D;
import static org.lwjgl.opengl.GL45C.glTextureSubImage2D;
import static org.lwjgl.opengl.GLDebugMessageCallback.getMessage;
import static org.lwjgl.system.MemoryUtil.NULL;

@Log4j2
public class Renderer extends AbstractRenderer {
    public static final int UBO_PROJECTION_MATRIX = 0;
    private static final int UBO_INTERPOLATION = 1;
    public static final int UBO_VIEW_DATA = 2;

    private final Profiler profiler;

    private int interpolationUBO;
    private FloatBuffer interpolationBuffer;
    private long interpolationBufferAddress;

    public Renderer(Profiler profiler) {
        super(new Camera(), new BaseShader(), new StringGeometryBuilder(), new SpriteRenderer(), new GuiRenderer(),
                new DebugRenderer(), new TextureGenerator(), new GPUFrustumCullingSystem(), new ParticleRenderer());
        this.profiler = profiler;
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

        interpolationUBO = GL45.glCreateBuffers();
        interpolationBuffer = createFloatBuffer(1);
        interpolationBufferAddress = MemoryUtil.memAddress(interpolationBuffer);

        GL45C.nglNamedBufferStorage(interpolationUBO, 4, interpolationBufferAddress, GL44.GL_DYNAMIC_STORAGE_BIT);
    }

    @Override
    public void setInterpolation(float interpolation) {
        super.setInterpolation(interpolation);
        putValue(interpolationBufferAddress, interpolation);
        GL45C.nglNamedBufferSubData(interpolationUBO, 0, 4, interpolationBufferAddress);
        GL30C.glBindBufferBase(GL31C.GL_UNIFORM_BUFFER, UBO_INTERPOLATION, interpolationUBO);
    }

    @Override
    public void update() {
        profiler.start("camera");
        camera.update();
        profiler.endStart("particleRenderer");
        particleRenderer.update();
        profiler.end();
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
        return MemoryUtil.memAlloc(size);
    }

    @Override
    public void memFree(ByteBuffer byteBuffer) {
        MemoryUtil.memFree(byteBuffer);
    }

    @Override
    public void memFree(IntBuffer intBuffer) {
        MemoryUtil.memFree(intBuffer);
    }

    @Override
    public long getAddress(Buffer buffer) {
        return MemoryUtil.memAddress(buffer);
    }

    @Override
    public FloatBuffer createFloatBuffer(int size) {
        return MemoryUtil.memAllocFloat(size);
    }

    @Override
    public IntBuffer createIntBuffer(int size) {
        return MemoryUtil.memAllocInt(size);
    }

    @Override
    public void putValue(long address, int value) {
        MemoryUtil.memPutInt(address, value);
    }

    @Override
    public void putValue(long address, float value) {
        MemoryUtil.memPutFloat(address, value);
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
        glLineWidth(value);
    }

    @Override
    public void glClear() {
        GL11C.glClear(GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void subImage2D(int id, int x, int y, int width, int height, int format, ByteBuffer byteBuffer) {
        glTextureSubImage2D(id, 0, x, y, width, height, format, GL11.GL_UNSIGNED_BYTE, byteBuffer);
    }

    @Override
    public void subImage2D(int id, int x, int y, int width, int height, int format, IntBuffer buffer) {
        glTextureSubImage2D(id, 0, x, y, width, height, format, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    @Override
    public void uploadTexture(AbstractTexture texture, int internalFormat, int format, int wrap, int filter,
                              ByteBuffer byteBuffer) {
        int id = texture.getId();
        int width = texture.getWidth();
        int height = texture.getHeight();
        glTextureStorage2D(id, 1, internalFormat, width, height);
        glTextureSubImage2D(id, 0, 0, 0, width, height, format, GL11.GL_UNSIGNED_BYTE, byteBuffer);
        glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_S, wrap);
        glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_T, wrap);
        glTextureParameteri(id, GL11.GL_TEXTURE_MIN_FILTER, filter);
        glTextureParameteri(id, GL11.GL_TEXTURE_MAG_FILTER, filter);
        long textureHandle = ARBBindlessTexture.glGetTextureHandleARB(id);
        texture.setTextureHandle(textureHandle);
        ARBBindlessTexture.glMakeTextureHandleResidentARB(textureHandle);
    }

    @Override
    public void uploadTexture(AbstractTexture texture, int internalFormat, int format, int wrap, int filter, IntBuffer buffer) {
        int id = texture.getId();
        int width = texture.getWidth();
        int height = texture.getHeight();
        glTextureStorage2D(id, 1, internalFormat, width, height);
        glTextureSubImage2D(id, 0, 0, 0, width, height, format, GL11.GL_UNSIGNED_BYTE, buffer);
        glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_S, wrap);
        glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_T, wrap);
        glTextureParameteri(id, GL11.GL_TEXTURE_MIN_FILTER, filter);
        glTextureParameteri(id, GL11.GL_TEXTURE_MAG_FILTER, filter);
        long textureHandle = ARBBindlessTexture.glGetTextureHandleARB(id);
        texture.setTextureHandle(textureHandle);
        ARBBindlessTexture.glMakeTextureHandleResidentARB(textureHandle);
    }

    @Override
    public void uploadFilledTexture(AbstractTexture texture, int internalFormat, int format, ByteBuffer value) {
        int id = texture.getId();
        int width = texture.getWidth();
        int height = texture.getHeight();
        glTextureStorage2D(id, 1, internalFormat, width, height);
        glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTextureParameteri(id, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        glTextureParameteri(id, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        glTextureParameteri(id, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        glClearTexImage(id, 0, format, GL11.GL_UNSIGNED_BYTE, value);
        long textureHandle = ARBBindlessTexture.glGetTextureHandleARB(id);
        texture.setTextureHandle(textureHandle);
        ARBBindlessTexture.glMakeTextureHandleResidentARB(textureHandle);
    }

    @Override
    public void fullTexture(AbstractTexture texture, int internalFormat, int format, ByteBuffer value) {
        glClearTexImage(texture.getId(), 0, format, GL11.GL_UNSIGNED_BYTE, value);
    }

    @Override
    public GlyphsBuilder createSTBTrueTypeGlyphsBuilder(String fontFile) {
        return new STBTrueTypeGlyphsBuilder(fontFile);
    }

    @Override
    public GlyphsBuilder createTrueTypeGlyphsBuilder(String fontFile) {
        return new TrueTypeGlyphsBuilder(fontFile);
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
        super.clear();

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
package net.bfsr.engine.renderer;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.culling.AbstractGPUFrustumCullingSystem;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import net.bfsr.engine.renderer.font.string.StringGeometryBuilder;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.shader.AbstractShaderProgram;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureGenerator;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@Getter
public abstract class AbstractRenderer {
    protected final long window;
    @Setter
    protected int screenWidth, screenHeight;
    private int drawCalls;
    private int lastFrameDrawCalls;
    @Setter
    private int fps;
    @Setter
    protected float interpolation;
    protected boolean persistentMappedBuffers;
    protected boolean particlesGPUFrustumCulling;
    @Setter
    protected boolean entitiesGPUFrustumCulling;

    protected final AbstractTexture dummyTexture;
    protected final AbstractCamera camera;
    protected final AbstractShaderProgram shader;
    protected final StringGeometryBuilder stringGeometryBuilder;
    protected final AbstractSpriteRenderer spriteRenderer;
    protected final AbstractGUIRenderer guiRenderer;
    protected final AbstractDebugRenderer debugRenderer;
    protected final AbstractTextureGenerator textureGenerator;
    protected final AbstractGPUFrustumCullingSystem cullingSystem;
    protected final ParticleRenderer particleRenderer;

    protected AbstractRenderer(long window, int screenWidth, int screenHeight, AbstractTexture dummyTexture, AbstractCamera camera,
                               AbstractShaderProgram shader, StringGeometryBuilder stringGeometryBuilder,
                               AbstractSpriteRenderer spriteRenderer, AbstractGUIRenderer guiRenderer, AbstractDebugRenderer debugRenderer,
                               AbstractTextureGenerator textureGenerator, AbstractGPUFrustumCullingSystem cullingSystem,
                               ParticleRenderer particleRenderer) {
        this.window = window;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.dummyTexture = dummyTexture;
        this.camera = camera;
        this.shader = shader;
        this.stringGeometryBuilder = stringGeometryBuilder;
        this.spriteRenderer = spriteRenderer;
        this.guiRenderer = guiRenderer;
        this.debugRenderer = debugRenderer;
        this.textureGenerator = textureGenerator;
        this.cullingSystem = cullingSystem;
        this.particleRenderer = particleRenderer;

        setupOpenGL();

        camera.init(screenWidth, screenHeight, this);
        spriteRenderer.init(this);
        guiRenderer.init(this);
        debugRenderer.init();
        particleRenderer.init(this);
        shader.load();
        shader.init();
        cullingSystem.init(shader, this);
    }

    public abstract void setupOpenGL();

    public abstract void update();

    public abstract void resize(int width, int height);

    public abstract ByteBuffer createByteBuffer(int size);
    public abstract FloatBuffer createFloatBuffer(int size);
    public abstract IntBuffer createIntBuffer(int size);
    public abstract void putValue(long address, int value);
    public abstract void putValue(long address, float value);
    public abstract void memFree(ByteBuffer byteBuffer);
    public abstract void memFree(IntBuffer intBuffer);
    public abstract long getAddress(Buffer buffer);

    public abstract void glClear();
    public abstract String glGetString(int name);
    public abstract void glEnable(int target);
    public abstract void glDisable(int target);
    public abstract void glScissor(int x, int y, int width, int height);
    public abstract void glBlendFunc(int sFactor, int dFactor);
    public abstract void lineWidth(float value);
    public abstract void subImage2D(int id, int x, int y, int width, int height, int format, ByteBuffer byteBuffer);
    public abstract void subImage2D(int id, int x, int y, int width, int height, int format, IntBuffer buffer);
    public abstract void uploadTexture(AbstractTexture texture, int internalFormat, int format, int wrap, int filter,
                                       ByteBuffer byteBuffer);
    public abstract void uploadTexture(AbstractTexture texture, int internalFormat, int format, int wrap, int filter,
                                       IntBuffer buffer);
    public abstract void uploadFilledTexture(AbstractTexture texture, int internalFormat, int format, ByteBuffer value);
    public abstract void fullTexture(AbstractTexture texture, int internalFormat, int format, ByteBuffer value);

    public void resetDrawCalls() {
        lastFrameDrawCalls = drawCalls;
        drawCalls = 0;
    }

    public void increaseDrawCalls() {
        drawCalls++;
    }

    public abstract void setVSync(boolean value);
    public abstract void setDebugWindow();

    public void setPersistentMappedBuffers(boolean value) {
        persistentMappedBuffers = value;
        spriteRenderer.setPersistentMappedBuffers(value);
        particleRenderer.setPersistentMappedBuffers(value);
    }

    public void setParticlesGPUFrustumCulling(boolean value) {
        particlesGPUFrustumCulling = value;
        particleRenderer.onParticlesGPUOcclusionCullingChangeValue();
    }

    public void reloadShaders() {
        shader.delete();
        shader.load();
        shader.init();
        cullingSystem.reloadShaders();
        debugRenderer.reloadShaders();
    }

    public void clear() {
        dummyTexture.delete();
        camera.clear();
        shader.delete();
        spriteRenderer.clear();
        debugRenderer.clear();
        cullingSystem.clear();
    }
}
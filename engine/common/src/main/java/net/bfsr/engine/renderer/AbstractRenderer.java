package net.bfsr.engine.renderer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import net.bfsr.engine.renderer.font.string.AbstractStringGeometryBuilder;
import net.bfsr.engine.renderer.font.string.AbstractStringRenderer;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.shader.AbstractShaderProgram;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureGenerator;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@RequiredArgsConstructor
public abstract class AbstractRenderer {
    protected long window;
    @Getter
    @Setter
    protected int screenWidth, screenHeight;
    @Getter
    private int drawCalls;
    @Getter
    private int lastFrameDrawCalls;
    @Getter
    @Setter
    private int fps;
    @Getter
    @Setter
    protected float interpolation;

    public final AbstractCamera camera;
    public final AbstractShaderProgram shader;
    public final AbstractStringGeometryBuilder stringGeometryBuilder;
    public final AbstractStringRenderer stringRenderer;
    public final AbstractSpriteRenderer spriteRenderer;
    public final AbstractGUIRenderer guiRenderer;
    public final AbstractDebugRenderer debugRenderer;
    public final AbstractTextureGenerator textureGenerator;

    public void init(long window, int width, int height) {
        this.window = window;
        this.screenWidth = width;
        this.screenHeight = height;

        setupOpenGL();

        camera.init(width, height);
        spriteRenderer.init();
        stringRenderer.init();
        guiRenderer.init();
        debugRenderer.init();
        shader.load();
        shader.init();
        textureGenerator.init();
    }

    public abstract void setupOpenGL();

    public abstract void update();

    public void resetDrawCalls() {
        lastFrameDrawCalls = drawCalls;
        drawCalls = 0;
    }

    public void increaseDrawCalls() {
        drawCalls++;
    }

    public abstract void setVSync(boolean value);
    public abstract void setDebugWindow();
    public abstract void resize(int width, int height);
    public abstract void clear();

    public abstract ByteBuffer createByteBuffer(int size);
    public abstract FloatBuffer createFloatBuffer(int size);

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
    public abstract void fillTexture(AbstractTexture texture, int internalFormat, int format, ByteBuffer value);
}
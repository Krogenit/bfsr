package net.bfsr.engine;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.dialog.AbstractSystemDialogs;
import net.bfsr.engine.dialog.SystemDialogs;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.input.Keyboard;
import net.bfsr.engine.input.Mouse;
import net.bfsr.engine.loop.AbstractGameLoop;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.Renderer;
import net.bfsr.engine.renderer.texture.TextureLoader;
import net.bfsr.engine.sound.AbstractSoundManager;
import net.bfsr.engine.sound.SoundLoader;
import net.bfsr.engine.sound.SoundManager;
import net.bfsr.engine.util.FPSSync;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwShowWindow;

@RequiredArgsConstructor
public class LWJGL3Engine extends AbstractGameLoop implements EngineConfiguration {
    private long window;
    private GLFWVidMode vidMode;
    private final FPSSync fpsSync = new FPSSync();

    private final Class<? extends ClientGameLogic> gameLogicClass;
    private ClientGameLogic gameLogic;
    private Profiler profiler;
    private AbstractRenderer renderer;

    @Override
    public void run() {
        Thread.currentThread().setName("Client Thread");
        init();
        super.run();
    }

    protected void init() {
        Vector2i windowSize = initGLFW();

        create();
        Engine.renderer.init(window, windowSize.x, windowSize.y);
        Engine.soundManager.init();
        Engine.assetsManager.init();
        Engine.mouse.init(window);
        Engine.keyboard.init(window);
        fpsSync.init();
        renderer = Engine.renderer;

        try {
            gameLogic = gameLogicClass.getConstructor().newInstance();
            gameLogic.init();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't create game logic instance", e);
        }

        profiler = gameLogic.getProfiler();

        glfwShowWindow(window);
    }

    private Vector2i initGLFW() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);

        vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (vidMode == null) throw new IllegalStateException("Failed to get GLFW video mode");
        window = GLFW.glfwCreateWindow(vidMode.width(), vidMode.height(), "Battle For Space Resources", MemoryUtil.NULL,
                MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) throw new IllegalStateException("Failed to create the GLFW window");

        Vector2i size = new Vector2i();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(window, width, height);
            size.set(width.get(), height.get());
        }

        GLFW.glfwSetWindowSizeCallback(window, (window1, width1, height1) -> {
            renderer.resize(width1, height1);
            gameLogic.resize(width1, height1);
        });

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        return size;
    }

    @Override
    public void update(double time) {
        profiler.startSection("update");
        renderer.update();
        gameLogic.update(time);
        profiler.endSection("update");
    }

    @Override
    public void render(float interpolation) {
        profiler.startSection("render");
        if (gameLogic.isPaused()) {
            interpolation = 1.0f;
        }

        renderer.setInterpolation(interpolation);
        gameLogic.render(interpolation);
        GLFW.glfwSwapBuffers(window);
        profiler.endStartSection("pollEvents");
        GLFW.glfwPollEvents();
        profiler.endSection("pollEvents");
    }

    @Override
    public boolean isRunning() {
        return gameLogic.isRunning() && !GLFW.glfwWindowShouldClose(window);
    }

    @Override
    public void setFps(int fps) {
        renderer.setFps(fps);
    }

    @Override
    public void clear() {
        gameLogic.clear();
        Engine.clear();
    }

    private boolean isVSync() {
        return gameLogic.isVSync();
    }

    @Override
    protected void sync(long now, double lastUpdateTime) {
        if (!isVSync()) {
            int fps = gameLogic.getTargetFPS();
            if (gameLogic.needSync()) {
                fpsSync.sync(fps);
            }
        }
    }

    @Override
    public AbstractSystemDialogs createSystemDialogs() {
        return new SystemDialogs();
    }

    @Override
    public AbstractKeyboard createKeyboard() {
        return new Keyboard();
    }

    @Override
    public AbstractMouse createMouse() {
        return new Mouse();
    }

    @Override
    public AbstractSoundManager createSoundManager() {
        return new SoundManager();
    }

    @Override
    public AbstractRenderer createRenderer() {
        return new Renderer();
    }

    @Override
    public AssetsManager createAssetManager() {
        return new AssetsManager(new TextureLoader(), new SoundLoader());
    }

    @Override
    protected int getUpdatesPerSecond() {
        return gameLogic.getUpdatesPerSecond();
    }

    @Override
    protected float getUpdateDeltaTime() {
        return gameLogic.getUpdateDeltaTime();
    }

    @Override
    protected double getTimeBetweenUpdates() {
        return gameLogic.getTimeBetweenUpdates();
    }
}
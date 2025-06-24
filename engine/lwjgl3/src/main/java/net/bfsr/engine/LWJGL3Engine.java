package net.bfsr.engine;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.dialog.AbstractSystemDialogs;
import net.bfsr.engine.dialog.SystemDialogs;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.input.Keyboard;
import net.bfsr.engine.input.Mouse;
import net.bfsr.engine.logic.ClientGameLogic;
import net.bfsr.engine.loop.AbstractGameLoop;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.Renderer;
import net.bfsr.engine.renderer.font.AbstractFontManager;
import net.bfsr.engine.renderer.font.FontManager;
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
public class LWJGL3Engine extends AbstractGameLoop {
    private long window;
    private GLFWVidMode vidMode;
    private final FPSSync fpsSync = new FPSSync();
    private final Profiler profiler = new Profiler();
    private final Class<? extends ClientGameLogic> gameLogicClass;
    private ClientGameLogic gameLogic;
    private AbstractRenderer renderer;

    @Override
    public void run() {
        Thread.currentThread().setName("Client Thread");
        init();
        super.run();
    }

    protected void init() {
        Vector2i windowSize = initGLFW();
        fpsSync.init();

        EventBus eventBus = new EventBus();
        AssetsManager assetsManager = new AssetsManager(new TextureLoader(), new SoundLoader());
        renderer = new Renderer(profiler, window, windowSize.x, windowSize.y, assetsManager.createDummyTexture());
        AbstractFontManager fontManager = new FontManager();
        AbstractSoundManager soundManager = new SoundManager();
        AbstractKeyboard keyboard = new Keyboard(window);
        AbstractMouse mouse = new Mouse(window, renderer);
        AbstractSystemDialogs systemDialogs = new SystemDialogs();

        Engine.setRenderer(renderer);
        Engine.setFontManager(fontManager);
        Engine.setSoundManager(soundManager);
        Engine.setAssetsManager(assetsManager);
        Engine.setKeyboard(keyboard);
        Engine.setMouse(mouse);
        Engine.setSystemDialogs(systemDialogs);
        Engine.setGuiManager(new GuiManager(eventBus));

        try {
            gameLogic = gameLogicClass.getConstructor(AbstractGameLoop.class, Profiler.class, EventBus.class)
                    .newInstance(this, profiler, eventBus);
            gameLogic.init();
            gameLogic.setRunning(true);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't create game logic instance", e);
        }

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
        profiler.start("update");
        renderer.update();
        gameLogic.update(time);
        profiler.end();
    }

    @Override
    public void render(float interpolation) {
        profiler.start("render");
        if (gameLogic.isPaused()) {
            interpolation = 1.0f;
        }

        renderer.setInterpolation(interpolation);
        gameLogic.render(interpolation);
        profiler.start("swapBuffers");
        GLFW.glfwSwapBuffers(window);
        profiler.endStart("pollEvents");
        GLFW.glfwPollEvents();
        profiler.end();
        profiler.end();
    }

    @Override
    public void setFps(int fps) {
        renderer.setFps(fps);
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
    public boolean isRunning() {
        return gameLogic.isRunning() && !GLFW.glfwWindowShouldClose(window);
    }

    @Override
    public void clear() {
        gameLogic.clear();
        Engine.clear();
    }
}
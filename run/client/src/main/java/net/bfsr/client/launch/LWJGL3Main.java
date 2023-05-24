package net.bfsr.client.launch;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.ingame.GuiInGame;
import net.bfsr.client.gui.menu.GuiMainMenu;
import net.bfsr.engine.AssetsManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.dialog.SystemDialogs;
import net.bfsr.engine.input.Keyboard;
import net.bfsr.engine.input.Mouse;
import net.bfsr.engine.loop.AbstractLoop;
import net.bfsr.engine.renderer.Renderer;
import net.bfsr.engine.renderer.texture.TextureLoader;
import net.bfsr.engine.sound.SoundLoader;
import net.bfsr.engine.sound.SoundManager;
import net.bfsr.engine.util.FPSSync;
import net.bfsr.engine.util.Side;
import org.joml.Vector2i;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwShowWindow;

@Log4j2
public class LWJGL3Main extends AbstractLoop {
    private Core core;
    private long window;
    private GLFWVidMode vidMode;
    private final FPSSync fpsSync = new FPSSync();

    @Override
    public void run() {
        Thread.currentThread().setName("Client Thread");
        init();
        loop();

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    protected void init() {
        Vector2i windowSize = initGLFW();

        createEngine(windowSize);

        core = new Core();
        core.init(getStartGui(), getGuiInGame());
        Engine.setGameLogic(Side.CLIENT, core);

        glfwShowWindow(window);

        fpsSync.init();
    }

    private void createEngine(Vector2i windowSize) {
        Engine.assetsManager = new AssetsManager(new TextureLoader(), new SoundLoader());
        Engine.renderer = new Renderer();
        Engine.soundManager = new SoundManager();
        Engine.mouse = new Mouse();
        Engine.keyboard = new Keyboard();
        Engine.systemDialogs = new SystemDialogs();
        Engine.init(window, windowSize.x, windowSize.y);
    }

    protected Gui getStartGui() {
        return new GuiMainMenu();
    }

    protected GuiInGame getGuiInGame() {
        return new GuiInGame();
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
        window = GLFW.glfwCreateWindow(vidMode.width(), vidMode.height(), "Battle For Space Resources", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) throw new IllegalStateException("Failed to create the GLFW window");

        Vector2i size = new Vector2i();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(window, width, height);
            size.set(width.get(), height.get());
        }

        GLFW.glfwSetWindowSizeCallback(window, (window1, width1, height1) -> {
            Engine.renderer.resize(width1, height1);
            core.resize(width1, height1);
        });

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        return size;
    }

    @Override
    public void update() {
        core.getProfiler().startSection("update");
        Engine.renderer.update();
        core.update();
        core.getProfiler().endSection("update");
    }

    @Override
    public void render(float interpolation) {
        core.getProfiler().startSection("render");
        if (Engine.isPaused()) {
            interpolation = 1.0f;
        }

        Engine.renderer.setInterpolation(interpolation);

        core.render(interpolation);
        core.getProfiler().endSection("render");
    }

    @Override
    public boolean isRunning() {
        return core.isRunning() && !GLFW.glfwWindowShouldClose(window);
    }

    @Override
    public void setFps(int fps) {
        Engine.renderer.setFps(fps);
    }

    @Override
    public void onPostRender() {
        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }

    @Override
    public void clear() {
        core.clear();
        Engine.clear();
    }

    private boolean isVSync() {
        return core.isVSync();
    }

    @Override
    protected void sync(long now, double lastUpdateTime) {
        if (!isVSync()) {
            int fps = core.getTargetFPS();
            if (core.needSync()) {
                fpsSync.sync(fps);
            }
        }
    }

    public static void main(String[] args) {
        new LWJGL3Main().run();
    }
}
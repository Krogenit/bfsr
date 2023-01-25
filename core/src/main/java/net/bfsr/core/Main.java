package net.bfsr.core;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.settings.EnumOption;
import org.joml.Vector2i;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

@Log4j2
public class Main extends Loop {
    private final Core core = new Core();
    private long window;
    private GLFWVidMode vidMode;

    @Override
    public void run() {
        super.run();

        init();
        loop();

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    private void init() {
        Vector2i windowSize = initGLFW();
        initInput();
        core.init(window, windowSize.x, windowSize.y);
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

        GLFW.glfwSetWindowSizeCallback(window, (window1, width1, height1) -> core.resize(width1, height1));

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        return size;
    }

    private void initInput() {
        Mouse.init(window);
        Keyboard.init(window);
    }

    @Override
    protected void update() {
        core.update();
    }

    @Override
    protected void render(float interpolation) {
        core.render(interpolation);
    }

    @Override
    public boolean isRunning() {
        return !GLFW.glfwWindowShouldClose(window) && super.isRunning();
    }

    @Override
    protected void setFps(int fps) {
        core.getRenderer().setFps(fps);
    }

    @Override
    protected void onPostRender() {
        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }

    @Override
    protected void clear() {
        core.clear();
    }

    @Override
    protected boolean isVSync() {
        return EnumOption.V_SYNC.getBoolean();
    }

    @Override
    protected boolean shouldWait(double now, double lastUpdateTime) {
        int maxFps = EnumOption.MAX_FPS.getInteger();
        return maxFps < 240 && now - lastUpdateTime < 1_000_000_000.0f / maxFps;
    }

    public static void main(String[] args) {
        new Main().run();
    }
}

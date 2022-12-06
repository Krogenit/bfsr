package net.bfsr.core;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

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
        initGLFW();
        initInput();
        core.init(window, vidMode);
    }

    private void initGLFW() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        window = GLFW.glfwCreateWindow(vidMode.width(), vidMode.height(), "Battle For Space Resources", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) throw new IllegalStateException("Failed to create the GLFW window");

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwShowWindow(window);

        GLFW.glfwSetWindowSizeCallback(window, (window1, width1, height1) -> core.resize(width1, height1));
        GLFW.glfwSetWindowFocusCallback(window, (window1, focused) -> core.setFocused(focused));
    }

    private void initInput() {
        Mouse.init(window);
        Keyboard.init(window);
    }

    @Override
    protected void input() {
        Mouse.updateState();
        core.input();
    }

    @Override
    protected void postInputUpdate() {
        Keyboard.update();
        Mouse.postUpdateState();
    }

    @Override
    protected void update(double delta) {
        core.update(delta);
    }

    @Override
    protected void render(float interpolation) {
        core.render();
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
        return core.getSettings().isVSync();
    }

    @Override
    protected boolean shouldWait(double now, double lastUpdateTime) {
        int maxFps = core.getSettings().getMaxFps();
        return maxFps < 240 && now - lastUpdateTime < 1_000_000_000.0f / maxFps;
    }

    public static void main(String[] args) {
        new Main().run();
    }
}

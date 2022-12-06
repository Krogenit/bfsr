package net.bfsr.core;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.settings.ClientSettings;
import net.bfsr.util.Timer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

@Log4j2
public class Main extends Loop {
    private int windowWidth, windowHeight;
    private Core core;
    private long window;
    private Timer timer;
    private GLFWVidMode vidMode;

    @Override
    public void run() {
        super.run();
        timer = new Timer();
        initGLFW();
        core = new Core(this, windowWidth, windowHeight);
        init();
        loop();

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    public static void setVSync(boolean value) {
        if (value) {
            GLFW.glfwSwapInterval(1);
        } else {
            GLFW.glfwSwapInterval(0);
        }
    }

    private void initGLFW() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        windowHeight = 720;
        windowWidth = 1280;

        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, "BFSR Client", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) throw new IllegalStateException("Failed to create the GLFW window");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            GLFW.glfwGetWindowSize(window, pWidth, pHeight);
            GLFW.glfwSetWindowPos(window, (vidMode.width() - pWidth.get(0)) / 2, (vidMode.height() - pHeight.get(0)) / 2);
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwShowWindow(window);

        initInput();

        GL.createCapabilities();

        GLFW.glfwSetWindowSizeCallback(window, (window1, width1, height1) -> resize(width1, height1));

        GLFW.glfwSetWindowFocusCallback(window, (window1, focused) -> setFocused(focused));
    }

    private void setupOpenGL(int width, int height) {
        GL11.glViewport(0, 0, width, height);

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);

        GL11.glClearColor(0.05F, 0.1F, 0.2F, 1.0F);
    }

    private void init() {
        timer.init();
        core.init();
        setupOpenGL(core.getWidth(), core.getHeight());
        ClientSettings settings = core.getSettings();
        setVSync(settings.isVSync());
        if (settings.isDebug()) {
            GLFW.glfwSetWindowSize(window, 1280, 720);
            GLFW.glfwSetWindowPos(window, (vidMode.width() - 1280) / 2, (vidMode.height() - 720) / 2);
        }
    }

    private void initInput() {
        Mouse.init(window);
        Keyboard.init(window);
    }

    private void resize(int width, int height) {
        GL11.glViewport(0, 0, width, height);
        core.resize(width, height);
    }

    protected void input() {
        Mouse.updateState();
        core.input();
    }

    protected void postInputUpdate() {
        Keyboard.update();
        Mouse.postUpdateState();
    }

    protected void update(double delta) {
        core.update(delta);
    }

    protected void render(float interpolation) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        core.render();

        checkGlError("FINISH");
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
        return maxFps < 240 && now - lastUpdateTime < 1_000_000_000 / (float) maxFps;
    }

    public static void checkGlError(String name) {
        int i = GL11.glGetError();

        if (i != 0) {
            log.error("########## GL ERROR ##########");
            log.error("Erorr number {}", i);
            log.error("Error in {}", name);
        }
    }

    private void setFocused(boolean focused) {
        core.setFocused(focused);
    }

    public static void main(String[] args) {
        new Main().run();
    }
}

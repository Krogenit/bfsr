package ru.krogenit.bfsr.core;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import ru.krogenit.bfsr.util.Timer;
import ru.krogenit.bfsr.client.input.Keyboard;
import ru.krogenit.bfsr.client.input.Mouse;
import ru.krogenit.bfsr.log.LoggingSystem;
import ru.krogenit.bfsr.settings.ClientSettings;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main extends Loop {
	
	static {
		LoggingSystem.initClient();
	}
	
	private int windowWidth, windowHeight;
	private Core core;
	private long window;
	private Timer timer;
	public static boolean isRunning = true;

	public static int fps;

	public void run() {
		timer = new Timer();
		initGLFW();
		core = new Core(windowWidth, windowHeight);
		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	public static void setVSync(boolean value) {
		if(value) {
			glfwSwapInterval(1);
		} else {
			glfwSwapInterval(0);
		}
	}

	private void initGLFW() {
		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		windowWidth = vidmode.width();
		windowHeight = vidmode.height();

		window = glfwCreateWindow(windowWidth, windowHeight, "BFSR Client", NULL, NULL);
		if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);

			glfwGetWindowSize(window, pWidth, pHeight);
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		}

		glfwMakeContextCurrent(window);
		glfwShowWindow(window);

		initInput();

		GL.createCapabilities();

		glfwSetWindowSizeCallback(window, (window, width1, height1) -> {
			if(width1 == 0) width1 = 1;
			if(height1 == 0) height1= 1;
			resize(width1, height1);
		});

		glfwSetWindowFocusCallback(window, (window, focused) -> {
			setActive(focused);
		});
	}

	private void setupOpenGL(int width, int height) {
		glViewport(0, 0, width, height);

//		glEnable(GL_DEPTH_TEST);
//		glDepthFunc(GL_LEQUAL);
//		glEnable(GL_TEXTURE_2D);
		glEnable(GL_ALPHA_TEST);
		glAlphaFunc(GL_GREATER, 0.1F);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);

		glClearColor(0.25F, 0.5F, 1.0F, 1.0F);
	}

	private void init() {
		timer.init();
//		SoundLoader.loadSounds();
		core.init();
		setupOpenGL(core.getWidth(), core.getHeight());
		ClientSettings settings = core.getSettings();
		setVSync(settings.isVSync());
		if(settings.isDebug()) {
			glfwSetWindowSize(window, 1280, 720);
			glfwSetWindowPos(window, (windowWidth - 1280) / 2, (windowHeight - 720) / 2);
		}
	}

	private void initInput() {
		Mouse.init(window);
		Keyboard.init(window);
	}

	private void resize(int width, int height) {
		glViewport(0, 0, width, height);
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
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		core.render();

		checkGlError("FINISH");
	}

	@Override
	protected boolean isRunning() {
		return !glfwWindowShouldClose(window) && isRunning;
	}

	@Override
	protected void setFps(int fps) {
		Main.fps = fps;
	}

	@Override
	protected void last() {
		glfwSwapBuffers(window);
		glfwPollEvents();
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
	protected boolean shouldWaitBeforeNextFrame(double now, double lastUpdateTime) {
		int maxFps = core.getSettings().getMaxFps();
		return maxFps < 240 && now - lastUpdateTime < 1_000_000_000 / (float) maxFps;
	}

	public static void checkGlError(String name) {
		int i = glGetError();

		if (i != 0) {
			System.err.println("########## GL ERROR ##########");
			System.err.println("Erorr number " + i);
			System.err.println("Error in " + name);
		}
	}

	private void setActive(boolean isActive) {
		core.setActive(isActive);
	}

	public static void main(String[] args) {
		new Main().run();
	}
}

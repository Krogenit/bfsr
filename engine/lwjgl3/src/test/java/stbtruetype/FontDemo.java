package stbtruetype;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;

import java.nio.FloatBuffer;
import java.util.Objects;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_0;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_END;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetMonitorContentScale;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static stbtruetype.GLFWUtil.glfwInvoke;

/**
 * STB Easy Font demo.
 */
abstract class FontDemo {

    protected final String text;
    private final int lineCount;

    private long window;
    private int ww = 800;
    private int wh = 600;

    private float
            contentScaleX,
            contentScaleY;

    private boolean ctrlDown;

    private int fontHeight;

    private int scale;
    private int lineOffset;
    private float lineHeight;

    private boolean kerningEnabled = true;
    private boolean lineBBEnabled;

    private Callback debugProc;

    protected FontDemo(int fontHeight, String filePath) {
        this.fontHeight = fontHeight;
        this.lineHeight = fontHeight;
        text = "Test text for font rendering\nwith one line";
        lineCount = 2;
    }

    public String getText() {
        return text;
    }

    public long getWindow() {
        return window;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public int getScale() {
        return scale;
    }

    public float getContentScaleX() {
        return contentScaleX;
    }

    public float getContentScaleY() {
        return contentScaleY;
    }

    public int getLineOffset() {
        return lineOffset;
    }

    public boolean isKerningEnabled() {
        return kerningEnabled;
    }

    public boolean isLineBBEnabled() {
        return lineBBEnabled;
    }

    protected void run(String title) {
        try {
            init(title);

            loop();
        } finally {
            try {
                destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void windowSizeChanged(long window, int width, int height) {
        if (Platform.get() != Platform.MACOSX) {
            width /= contentScaleX;
            height /= contentScaleY;
        }

        this.ww = width;
        this.wh = height;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, width, height, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);

        FontDemo.this.setLineOffset(lineOffset);
    }

    private static void framebufferSizeChanged(long window, int width, int height) {
        glViewport(0, 0, width, height);
    }

    private void init(String title) {
        GLFWErrorCallback.createPrint().set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

        long monitor = glfwGetPrimaryMonitor();

        int framebufferW;
        int framebufferH;
        try (MemoryStack s = stackPush()) {
            FloatBuffer px = s.mallocFloat(1);
            FloatBuffer py = s.mallocFloat(1);

            glfwGetMonitorContentScale(monitor, px, py);

            contentScaleX = px.get(0);
            contentScaleY = py.get(0);

            if (Platform.get() == Platform.MACOSX) {
                framebufferW = ww;
                framebufferH = wh;
            } else {
                framebufferW = round(ww * contentScaleX);
                framebufferH = round(wh * contentScaleY);
            }
        }

        this.window = glfwCreateWindow(framebufferW, framebufferH, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetWindowSizeCallback(window, this::windowSizeChanged);
        glfwSetFramebufferSizeCallback(window, FontDemo::framebufferSizeChanged);

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            switch (key) {
                case GLFW_KEY_LEFT_CONTROL:
                case GLFW_KEY_RIGHT_CONTROL:
                    ctrlDown = action != GLFW_RELEASE;
            }

            if (action == GLFW_RELEASE) {
                return;
            }

            switch (key) {
                case GLFW_KEY_ESCAPE:
                    glfwSetWindowShouldClose(window, true);
                    break;
                case GLFW_KEY_PAGE_UP:
                    setLineOffset(lineOffset - wh / FontDemo.this.lineHeight);
                    break;
                case GLFW_KEY_PAGE_DOWN:
                    setLineOffset(lineOffset + wh / FontDemo.this.lineHeight);
                    break;
                case GLFW_KEY_HOME:
                    setLineOffset(0);
                    break;
                case GLFW_KEY_END:
                    setLineOffset(lineCount - wh / FontDemo.this.lineHeight);
                    break;
                case GLFW_KEY_KP_ADD:
                case GLFW_KEY_EQUAL:
                    setScale(scale + 1);
                    break;
                case GLFW_KEY_KP_SUBTRACT:
                case GLFW_KEY_MINUS:
                    setScale(scale - 1);
                    break;
                case GLFW_KEY_0:
                case GLFW_KEY_KP_0:
                    if (ctrlDown) {
                        setScale(0);
                    }
                    break;
                case GLFW_KEY_B:
                    lineBBEnabled = !lineBBEnabled;
                    break;
                case GLFW_KEY_K:
                    kerningEnabled = !kerningEnabled;
                    break;
            }
        });

        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            if (ctrlDown) {
                setScale(scale + (int) round(yoffset));
            } else {
                setLineOffset(lineOffset - (int) round(yoffset) * 3);
            }
        });

        // Center window
        GLFWVidMode vidmode = Objects.requireNonNull(glfwGetVideoMode(monitor));

        glfwSetWindowPos(
                window,
                (vidmode.width() - framebufferW) / 2,
                (vidmode.height() - framebufferH) / 2
        );

        // Create context
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        debugProc = GLUtil.setupDebugMessageCallback();

        glfwSwapInterval(1);
        glfwShowWindow(window);

        glfwInvoke(window, this::windowSizeChanged, FontDemo::framebufferSizeChanged);
    }

    private void setScale(int scale) {
        this.scale = max(-3, scale);
        this.lineHeight = fontHeight * (1.0f + this.scale * 0.25f);
        setLineOffset(lineOffset);
    }

    private void setLineOffset(float offset) {
        setLineOffset(round(offset));
    }

    private void setLineOffset(int offset) {
        lineOffset = max(0, min(offset, lineCount - (int) (wh / lineHeight)));
    }

    protected abstract void loop();

    private void destroy() {
        GL.setCapabilities(null);

        if (debugProc != null) {
            debugProc.free();
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}
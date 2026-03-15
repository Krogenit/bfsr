import net.bfsr.engine.util.MatrixBufferUtils;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_BACK;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_GREATER;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11C.glViewport;

public class DepthTest2DMain {
    public static void main(String[] args) {
        new Test();
    }

    private static class Test {
        GLFWVidMode vidMode;
        long window;
        int screenWidth = 1920, screenHeight = 1080;

        public Test() {
            initGLFW();
            glfwShowWindow(window);
            glfwSwapInterval(1);

            while (true) {
                render();
                GLFW.glfwSwapBuffers(window);
                GLFW.glfwPollEvents();
            }
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
                this.screenWidth = width1;
                this.screenHeight = height1;
//                renderer.resize(width1, height1);
//                gameLogic.resize(width1, height1);
            });

            GLFW.glfwMakeContextCurrent(window);
            GL.createCapabilities();
            return size;
        }

        public void render() {
            GL11.glClearColor(0, 0, 0, 1);
            GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glViewport(0, 0, screenWidth, screenHeight);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            float zNear = -1.0f;
            float zFar = 1.0f;
            int width = screenWidth;
            int height = screenHeight;
//            float aspect = 1.0f;
            float left = 0, right = width, bottom = 0, top = height;
            float[] proj = new float[]{
                    2 / (right - left), 0, 0, 0,
                    0, 2 / (top - bottom), 0, 0,
                    0, 0, -2 / (zFar - zNear), 0,
                    -(right + left) / (right - left), -(top + bottom) / (top - bottom), -(zFar + zNear) / (zFar - zNear), 1
            };

            float Z_NEAR = -1.0f;
            float Z_FAR = 1.0f;
            FloatBuffer floatBuffer = new Matrix4f().setOrtho(0.0f, width, 0.0f, height, Z_NEAR, Z_FAR)
                    .get(MatrixBufferUtils.MATRIX_BUFFER);

            FloatBuffer floatBuffer1 = BufferUtils.createFloatBuffer(proj.length);
            floatBuffer1.put(proj).flip();

            for (int i = 0; i < 16; i++) {
                float v = floatBuffer.get(i);
                float v1 = floatBuffer1.get(i);
                if (v != v1)
                    System.out.println("index " + i + " value " + v + " " + v1);
            }

            GL11.glLoadMatrixf(floatBuffer);

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            GL11.glEnable(GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthMask(true);

            GL11.glEnable(GL_ALPHA_TEST);
            GL11.glEnable(GL_BLEND);
            GL11.glEnable(GL_CULL_FACE);
            GL11.glCullFace(GL_BACK);
            GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GL11.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            GL11.glAlphaFunc(GL_GREATER, 0.0001f);

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor3f(1f, 1f, 1f);
            GL11.glVertex3f(-100.5f, -100.5f, 0.0f);
            GL11.glVertex3f(100.5f, -100.5f, 0.0f);
            GL11.glVertex3f(100.5f, 100.5f, 0.0f);
            GL11.glVertex3f(-100.5f, 100.5f, 0.0f);
            GL11.glEnd();
        }
    }
}

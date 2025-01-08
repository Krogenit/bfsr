package gl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL43.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL43.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL43.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL43.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL43.GL_FALSE;
import static org.lwjgl.opengl.GL43.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL43.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL43.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL43.GL_TRIANGLES;
import static org.lwjgl.opengl.GL43.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL43.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL43.glAttachShader;
import static org.lwjgl.opengl.GL43.glBindBuffer;
import static org.lwjgl.opengl.GL43.glBindVertexArray;
import static org.lwjgl.opengl.GL43.glBufferData;
import static org.lwjgl.opengl.GL43.glClear;
import static org.lwjgl.opengl.GL43.glCompileShader;
import static org.lwjgl.opengl.GL43.glCreateProgram;
import static org.lwjgl.opengl.GL43.glCreateShader;
import static org.lwjgl.opengl.GL43.glDeleteBuffers;
import static org.lwjgl.opengl.GL43.glDeleteProgram;
import static org.lwjgl.opengl.GL43.glDeleteShader;
import static org.lwjgl.opengl.GL43.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL43.glGenBuffers;
import static org.lwjgl.opengl.GL43.glGenVertexArrays;
import static org.lwjgl.opengl.GL43.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL43.glGetProgrami;
import static org.lwjgl.opengl.GL43.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL43.glGetShaderi;
import static org.lwjgl.opengl.GL43.glLinkProgram;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;
import static org.lwjgl.opengl.GL43.glShaderSource;
import static org.lwjgl.opengl.GL43.glUseProgram;
import static org.lwjgl.opengl.GL43C.glVertexBindingDivisor;
import static org.lwjgl.opengl.GL45C.glVertexArrayVertexBuffer;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GlMultiDrawElementsIndirect {
    private long window;
    private int vao, vbo, ebo, indirectBuffer, shaderProgram;

    private static final float[] VERTICES = {
            0.5f, -0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.0f, 0.0f
    };

    private static final int[] INDICES = {
            0, 1, 3, 3, 1, 2
    };

    private static class DrawElementsIndirectCommand {
        int count, instanceCount, firstIndex, baseVertex, baseInstance;
    }

    private static final DrawElementsIndirectCommand[] COMMANDS = {
            new DrawElementsIndirectCommand() {{
                count = 6;
                instanceCount = 1;
                firstIndex = 0;
                baseVertex = 0;
                baseInstance = 0;
            }},
            new DrawElementsIndirectCommand() {{
                count = 6;
                instanceCount = 1;
                firstIndex = 0;
                baseVertex = 0;
                baseInstance = 1;
            }}
    };

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(800, 600, "LWJGL MultiDrawElementsIndirect", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        setupShaders();
        setupBuffers();
    }

    private void setupShaders() {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, """
                    #version 430 core
                    layout(location = 0) in vec4 position;
                    layout(location = 1) in uint index;
                    void main() {
                        float x = position.x * 0.25 + index;
                        float y = position.y * 0.25 + index;
                        gl_Position = vec4(x, y, 0.0, 1.0);
                    }
                """);
        glCompileShader(vertexShader);
        checkCompileErrors(vertexShader, "VERTEX");

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, """
                    #version 430 core
                    out vec4 FragColor;
                    void main() {
                        FragColor = vec4(0.2, 0.7, 0.3, 1.0);
                    }
                """);
        glCompileShader(fragmentShader);
        checkCompileErrors(fragmentShader, "FRAGMENT");

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        checkCompileErrors(shaderProgram, "PROGRAM");

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    int indexBuffer;

    private void setupBuffers() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Vertex Buffer
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);
        glVertexArrayVertexBuffer(vao, 0, vbo, 0, 16);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Element Buffer
        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);

        // index buffer
        indexBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, indexBuffer);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer intBuffer = stack.mallocInt(128);
            for (int i = 0; i < 128; i++) {
                intBuffer.put(i);
            }
            intBuffer.flip();
            glBufferData(GL_ARRAY_BUFFER, intBuffer, GL_STATIC_DRAW);
            glVertexArrayVertexBuffer(vao, 1, indexBuffer, 0, 4);
        }

        glVertexBindingDivisor(1, 1);
        glEnableVertexAttribArray(1);

        // Indirect Command Buffer
        indirectBuffer = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectBuffer);
        ByteBuffer buffer = BufferUtils.createByteBuffer(COMMANDS.length * 20);
        for (DrawElementsIndirectCommand cmd : COMMANDS) {
            buffer.putInt(cmd.count)
                    .putInt(cmd.instanceCount)
                    .putInt(cmd.firstIndex)
                    .putInt(cmd.baseVertex)
                    .putInt(cmd.baseInstance);
        }
        buffer.flip();
        glBufferData(GL_DRAW_INDIRECT_BUFFER, buffer, GL_STATIC_DRAW);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            glUseProgram(shaderProgram);
            glBindVertexArray(vao);
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectBuffer);
            glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, COMMANDS.length, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteBuffers(indirectBuffer);
        glDeleteVertexArrays(vao);
        glDeleteProgram(shaderProgram);

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void checkCompileErrors(int shader, String type) {
        if (type.equals("PROGRAM")) {
            if (glGetProgrami(shader, GL_LINK_STATUS) == GL_FALSE) {
                System.err.println("ERROR::PROGRAM_LINKING_ERROR: " + glGetProgramInfoLog(shader));
            }
        } else {
            if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
                System.err.println("ERROR::SHADER_COMPILATION_ERROR: " + glGetShaderInfoLog(shader));
            }
        }
    }

    public static void main(String[] args) {
        new GlMultiDrawElementsIndirect().run();
    }
}

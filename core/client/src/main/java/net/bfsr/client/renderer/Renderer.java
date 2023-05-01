package net.bfsr.client.renderer;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.camera.Camera;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.renderer.buffer.BufferType;
import net.bfsr.client.renderer.debug.DebugRenderer;
import net.bfsr.client.renderer.font.string.StringGeometryBuilder;
import net.bfsr.client.renderer.font.string.StringRenderer;
import net.bfsr.client.renderer.gui.GUIRenderer;
import net.bfsr.client.renderer.particle.ParticleRenderer;
import net.bfsr.client.renderer.render.Render;
import net.bfsr.client.renderer.shader.BaseShader;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureGenerator;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.settings.Option;
import net.bfsr.client.world.WorldClient;
import net.bfsr.texture.TextureRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.bfsr.client.renderer.debug.OpenGLDebugUtils.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL43C.GL_DEBUG_TYPE_OTHER;
import static org.lwjgl.opengl.GL43C.glDebugMessageCallback;
import static org.lwjgl.opengl.GLDebugMessageCallback.getMessage;
import static org.lwjgl.system.MemoryUtil.NULL;

@Log4j2
public class Renderer {
    private final Core core;
    @Setter
    @Getter
    private int screenWidth, screenHeight;
    @Getter
    private final Camera camera = new Camera();
    @Getter
    private final BaseShader shader = new BaseShader();
    @Getter
    private final StringGeometryBuilder stringGeometryBuilder = new StringGeometryBuilder();
    @Getter
    private final StringRenderer stringRenderer = new StringRenderer();
    @Getter
    private final ParticleRenderer particleRenderer = new ParticleRenderer();
    @Getter
    private final SpriteRenderer spriteRenderer = new SpriteRenderer();
    @Getter
    private final GUIRenderer guiRenderer = new GUIRenderer();
    @Getter
    private final DebugRenderer debugRenderer = new DebugRenderer();
    @Getter
    private int drawCalls;
    @Getter
    private int lastFrameDrawCalls;
    @Setter
    @Getter
    private int fps;
    @Getter
    private float interpolation;
    private final List<Render<?>> renderList = new ArrayList<>();
    private final TIntObjectMap<Render<?>> renders = new TIntObjectHashMap<>();
    private Texture backgroundTexture;

    public Renderer(Core core) {
        this.core = core;
    }

    public void init(long window, int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        setupOpenGL(width, height);
        setVSync(Option.V_SYNC.getBoolean());

        TextureLoader.init();
        TextureGenerator.init();

        camera.init(width, height);
        spriteRenderer.init();
        stringRenderer.init(stringGeometryBuilder, spriteRenderer);
        guiRenderer.init(spriteRenderer);
        particleRenderer.init(spriteRenderer);
        debugRenderer.init();
        shader.load();
        shader.init();

        if (Option.IS_DEBUG.getBoolean()) {
            glfwRestoreWindow(window);
            glfwSetWindowSize(window, 1280, 720);
            glfwSetWindowPos(window, (width - 1280) / 2, (height - 720) / 2);
        }

        glfwShowWindow(window);
    }

    private void setupOpenGL(int width, int height) {
        glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
            if (type != GL_DEBUG_TYPE_OTHER) {
                log.info("GLDebug {} {}, {}, {}, {}", getDebugSeverity(severity), String.format("0x%X", id),
                        getDebugSource(source), getDebugType(type), getMessage(length, message));
            }
        }, NULL);

        glViewport(0, 0, width, height);

        glEnable(GL_ALPHA_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glAlphaFunc(GL_GREATER, 0.0001f);
        TextureLoader.getTexture(TextureRegister.damageFire, GL_REPEAT, GL_LINEAR).bind();
        backgroundTexture = new Texture(2560 << 1, 2560 << 1).create();

        glClearColor(0.05F, 0.1F, 0.2F, 1.0F);
    }

    public void update() {
        camera.update();

        if (!core.isPaused()) {
            for (int i = 0; i < renderList.size(); i++) {
                Render<?> render = renderList.get(i);
                if (render.isDead()) {
                    render.clear();
                    renderList.remove(i--);
                    renders.remove(render.getObject().getId());
                } else {
                    render.update();
                }
            }

            particleRenderer.update();
        }
    }

    public void postUpdate() {
        for (int i = 0; i < renderList.size(); i++) {
            Render<?> render = renderList.get(i);
            render.postWorldUpdate();
        }
    }

    public void prepareRender(float interpolation) {
        if (Core.get().isPaused()) {
            interpolation = 1.0f;
        }

        this.interpolation = interpolation;

        WorldClient world = core.getWorld();
        if (world != null) {
            particleRenderer.putBackgroundParticlesToBuffers();
            prepareAmbient();
            spriteRenderer.addTask(this::renderAlpha, BufferType.ENTITIES_ALPHA);
            spriteRenderer.addTask(this::renderAdditive, BufferType.ENTITIES_ADDITIVE);
            particleRenderer.putParticlesToBuffers();
        }
    }

    private void prepareAmbient() {
        float moveFactor = 0.005f;
        float cameraZoom = camera.getLastZoom() + (camera.getZoom() - camera.getLastZoom()) * interpolation;
        float lastX = (camera.getLastPosition().x - camera.getLastPosition().x * moveFactor / cameraZoom);
        float lastY = (camera.getLastPosition().y - camera.getLastPosition().y * moveFactor / cameraZoom);
        float x = (camera.getPosition().x - camera.getPosition().x * moveFactor / cameraZoom);
        float y = (camera.getPosition().y - camera.getPosition().y * moveFactor / cameraZoom);
        float zoom = (float) (0.5f + Math.log(cameraZoom) * 0.01f);
        float scaleX = backgroundTexture.getWidth() / cameraZoom * zoom;
        float scaleY = backgroundTexture.getHeight() / cameraZoom * zoom;
        spriteRenderer.add(lastX, lastY, x, y, scaleX, scaleY, 1.0f, 1.0f, 1.0f, 1.0f, backgroundTexture, BufferType.BACKGROUND);
    }

    public void renderAlpha() {
        for (int i = 0; i < renderList.size(); i++) {
            Render<?> render = renderList.get(i);
            if (render.getAabb().overlaps(camera.getBoundingBox())) {
                render.renderAlpha();
            }
        }
    }

    public void renderAdditive() {
        for (int i = 0; i < renderList.size(); i++) {
            Render<?> render = renderList.get(i);
            if (render.getAabb().overlaps(camera.getBoundingBox())) {
                render.renderAdditive();
            }
        }
    }

    public void render() {
        resetDrawCalls();
        glClear(GL_COLOR_BUFFER_BIT);
        camera.calculateInterpolatedViewMatrix(interpolation);
        camera.bindInterpolatedWorldViewMatrix();
        spriteRenderer.bind();
        shader.enable();

        WorldClient world = core.getWorld();
        if (world != null) {
            SpriteRenderer.get().render(BufferType.BACKGROUND);
            particleRenderer.renderBackground();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            spriteRenderer.syncAndRender(BufferType.ENTITIES_ALPHA);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            spriteRenderer.syncAndRender(BufferType.ENTITIES_ADDITIVE);
            particleRenderer.render();

            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            if (Option.SHOW_DEBUG_BOXES.getBoolean()) {
                debugRenderer.clear();
                debugRenderer.bind();
                camera.bindWorldViewMatrix();
                for (int i = 0; i < renderList.size(); i++) {
                    Render<?> render = renderList.get(i);
                    if (render.getAabb().overlaps(camera.getBoundingBox())) {
                        render.renderDebug();
                    }
                }
                debugRenderer.render(GL_LINE_LOOP);
                spriteRenderer.bind();
                shader.enable();
            }
        }

        camera.bindGUI();

        if (world != null) {
            core.getGuiManager().getGuiInGame().render();
        }

        Gui gui = core.getGuiManager().getCurrentGui();
        if (gui != null) {
            gui.render();
        }

        spriteRenderer.render(BufferType.GUI);
    }

    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        glViewport(0, 0, width, height);
        camera.resize(width, height);
    }

    public void createBackgroundTexture(long seed) {
        if (backgroundTexture != null) backgroundTexture.delete();
        backgroundTexture = TextureGenerator.generateNebulaTexture(backgroundTexture.getWidth(), backgroundTexture.getHeight(), new Random(seed));
    }

    public void onExitToMainMenu() {
        particleRenderer.onExitToMainMenu();
        renderList.clear();
    }

    private void resetDrawCalls() {
        lastFrameDrawCalls = drawCalls;
        drawCalls = 0;
    }

    public void increaseDrawCalls() {
        drawCalls++;
    }

    public void setVSync(boolean value) {
        glfwSwapInterval(value ? 1 : 0);
    }

    public void reloadShaders() {
        shader.delete();
        shader.load();
        shader.init();
    }

    public void addRender(Render<?> render) {
        renderList.add(render);
        renders.put(render.getObject().getId(), render);
    }

    public Render<?> getRender(int id) {
        return renders.get(id);
    }

    public void clear() {
        spriteRenderer.clear();

        for (int i = 0; i < renderList.size(); i++) {
            renderList.get(i).clear();
        }

        renderList.clear();
        renders.clear();
    }
}
package net.bfsr.core;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFutureTask;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.ingame.GuiInGame;
import net.bfsr.client.gui.menu.GuiMainMenu;
import net.bfsr.client.language.Lang;
import net.bfsr.client.renderer.Renderer;
import net.bfsr.client.sound.SoundListener;
import net.bfsr.client.sound.SoundManager;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.network.Packet;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.profiler.Profiler;
import net.bfsr.server.ThreadLocalServer;
import net.bfsr.settings.ClientSettings;
import net.bfsr.settings.EnumOption;
import net.bfsr.world.WorldClient;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.openal.AL11;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class Core {
    private static Core instance;

    @Setter
    @Getter
    private int screenWidth, screenHeight;
    private long window;
    @Setter
    @Getter
    private boolean paused;

    @Getter
    private final SoundManager soundManager;
    @Setter
    @Getter
    private WorldClient world;
    @Getter
    private Gui currentGui;
    @Getter
    private final Renderer renderer;
    @Getter
    private final ClientSettings settings;
    @Getter
    private final Profiler profiler;

    private ThreadLocalServer localServer;
    @Getter
    @Setter
    private NetworkManagerClient networkManager;

    @Getter
    private String playerName;

    private final Queue<ListenableFutureTask<?>> futureTasks = Queues.newArrayDeque();

    public Core() {
        instance = this;

        this.settings = new ClientSettings();
        this.soundManager = new SoundManager();
        this.renderer = new Renderer(this);
        this.profiler = new Profiler();
    }

    public void init(long window, int width, int height) {
        this.window = window;
        this.screenWidth = width;
        this.screenHeight = height;

        Lang.load();
        this.settings.readSettings();
        this.soundManager.init();
        this.soundManager.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        this.soundManager.setListener(new SoundListener(new Vector3f(0, 0, 0)));
        this.renderer.init(window, width, height);
        this.currentGui = new GuiMainMenu();
        this.currentGui.init();
        profiler.setEnable(EnumOption.IS_PROFILING.getBoolean());
        WreckRegistry.INSTANCE.init();
    }

    public void update() {
        profiler.endStartSection("tasks");
        synchronized (this.futureTasks) {
            while (!this.futureTasks.isEmpty()) {
                this.futureTasks.poll().run();
            }
        }

        profiler.endStartSection("update");
        if (world != null) {
            renderer.updateCamera();
            soundManager.updateListenerPosition(renderer.getCamera());
            if (!paused) world.update();
            renderer.update();
        }

        if (currentGui != null) currentGui.update();

        profiler.endStartSection("network");
        if (networkManager != null) {
            if (networkManager.isChannelOpen()) {
                networkManager.processReceivedPackets();
            } else if (networkManager.getExitMessage() != null) {
                networkManager.onDisconnect(networkManager.getExitMessage());
                clearNetwork();
            } else {
                networkManager.onDisconnect("Disconnected from server");
                clearNetwork();
            }
        }
    }

    public void render(float interpolation) {
        profiler.endStartSection("prepareRender");
        renderer.prepareRender(interpolation);
        profiler.endStartSection("render");
        renderer.render();
        profiler.endSection();
    }

    public void startSinglePlayer() {
        startLocalServer();
        localServer.connectToLocalServer();
        setCurrentGui(null);
    }

    private void startLocalServer() {
        playerName = "Local Player";
        localServer = new ThreadLocalServer();
        localServer.setName("Local Server");
        localServer.start();
    }

    private void stopServer() {
        if (localServer != null) {
            localServer.stopServer();
            localServer = null;
        }
    }

    public void quitToMainMenu() {
        renderer.onExitToMainMenu();
        clearNetwork();
        stopServer();
        if (world != null) {
            world.clear();
            world = null;
        }

        setCurrentGui(new GuiMainMenu());
    }

    public void clearNetwork() {
        if (networkManager != null) {
            networkManager.closeChannel("Quitting");
            networkManager.stop();
            networkManager = null;
        }
    }

    void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.renderer.resize(width, height);

        if (currentGui != null) currentGui.resize(width, height);
    }

    public void setCurrentGui(Gui gui) {
        if (currentGui != null) currentGui.clear();
        this.currentGui = gui;
        if (currentGui != null) currentGui.init();
    }

    public void sendPacket(Packet packet) {
        if (networkManager != null) networkManager.scheduleOutboundPacket(packet);
    }

    public void addFutureTask(Runnable runnable) {
        addFutureTask(Executors.callable(runnable));
    }

    private void addFutureTask(Callable<?> callable) {
        ListenableFutureTask<?> futureTask = ListenableFutureTask.create(callable);

        synchronized (futureTasks) {
            futureTasks.add(futureTask);
        }
    }

    public GuiInGame getGuiInGame() {
        return renderer.getGuiInGame();
    }

    public static Core get() {
        return instance;
    }

    public boolean canControlShip() {
        return !renderer.getGuiInGame().isActive() && currentGui == null;
    }

    public void stop() {
        GLFW.glfwSetWindowShouldClose(window, true);
    }

    public void clear() {
        clearNetwork();
        if (localServer != null) {
            localServer.stopServer();
        }

        soundManager.cleanup();
        renderer.clear();
    }
}

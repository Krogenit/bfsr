package net.bfsr.client.core;

import com.google.common.util.concurrent.ListenableFutureTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.LocalServer;
import net.bfsr.client.ThreadLocalServer;
import net.bfsr.client.camera.CameraController;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.gui.ingame.GuiInGame;
import net.bfsr.client.gui.menu.GuiMainMenu;
import net.bfsr.client.input.InputHandler;
import net.bfsr.client.language.Lang;
import net.bfsr.client.listener.Listeners;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.network.packet.client.PacketHandshake;
import net.bfsr.client.network.packet.client.PacketLoginTCP;
import net.bfsr.client.network.packet.client.PacketLoginUDP;
import net.bfsr.client.particle.config.ParticleEffectsRegistry;
import net.bfsr.client.renderer.Renderer;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.client.settings.Option;
import net.bfsr.client.sound.SoundListener;
import net.bfsr.client.sound.SoundManager;
import net.bfsr.client.world.WorldClient;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.network.PacketOut;
import net.bfsr.profiler.Profiler;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.openal.AL11;

import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Log4j2
public class Core {
    private static Core instance;

    private long window;
    @Setter
    @Getter
    private boolean paused;

    @Getter
    private final SoundManager soundManager = new SoundManager();
    @Getter
    private final Renderer renderer;
    @Getter
    private final NetworkSystem networkSystem = new NetworkSystem();
    @Getter
    private final Profiler profiler = new Profiler();
    @Getter
    private final InputHandler inputHandler = new InputHandler();
    @Getter
    private final GuiManager guiManager = new GuiManager();
    @Getter
    private final CameraController cameraController = new CameraController();

    @Getter
    private final ClientSettings settings = new ClientSettings();

    @Setter
    @Getter
    private WorldClient world;
    @Getter
    private String playerName;
    @Getter
    private LocalServer localServer;

    private final Queue<ListenableFutureTask<?>> futureTasks = new ConcurrentLinkedQueue<>();
    @Setter
    private Supplier<WorldClient> worldSupplier = WorldClient::new;

    public Core() {
        this.renderer = new Renderer(this);
        instance = this;
    }

    public void init(long window, int width, int height, Gui startGui, GuiInGame guiInGame) {
        this.window = window;
        Lang.load();
        Listeners.init();
        this.inputHandler.init(window);
        this.settings.readSettings();
        this.networkSystem.init();
        this.soundManager.init();
        this.soundManager.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        this.soundManager.setListener(new SoundListener(new Vector3f(0, 0, 0)));
        this.renderer.init(window, width, height);
        this.guiManager.init(startGui, guiInGame);
        this.cameraController.init();
        this.profiler.setEnable(Option.IS_PROFILING.getBoolean());
        ParticleEffectsRegistry.INSTANCE.init();
        ConfigConverterManager.INSTANCE.init();
        Listeners.registerListeners();
    }

    public void update() {
        profiler.endStartSection("tasks");
        while (!futureTasks.isEmpty()) {
            futureTasks.poll().run();
        }

        profiler.endStartSection("renderer");
        renderer.update();
        profiler.endStartSection("inputHandler");
        inputHandler.update();
        profiler.endStartSection("cameraController");
        cameraController.update();
        if (world != null) {
            profiler.endStartSection("soundManager");
            soundManager.updateListenerPosition(renderer.getCamera());
            profiler.endStartSection("world");
            if (!paused) world.update();
        }

        profiler.endStartSection("guiManager");
        guiManager.update();
        profiler.endStartSection("network");
        networkSystem.update();
        profiler.endStartSection("renderer.postUpdate");
        renderer.postUpdate();
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
        connectToLocalServerTCP();
        setCurrentGui(null);
    }

    private void startLocalServer() {
        playerName = "Local Player";
        localServer = new LocalServer();
        ThreadLocalServer threadLocalServer = new ThreadLocalServer(localServer);
        threadLocalServer.setName("Local Server");
        threadLocalServer.start();
        waitServerStart();
    }

    private void waitServerStart() {
        while (!localServer.isRunning()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void connectToLocalServerTCP() {
        try {
            InetAddress inetaddress = InetAddress.getByName("127.0.0.1");
            connectToServer(inetaddress, 34000);
        } catch (Exception e) {
            log.error("Couldn't connect to local TCP server", e);
        }
    }

    public void connectToServer(InetAddress inetaddress, int port) {
        networkSystem.connectTCP(inetaddress, port);
        networkSystem.setHandshakeTime(System.nanoTime());
        networkSystem.sendPacketTCP(new PacketHandshake(5, networkSystem.getHandshakeTime()));
        networkSystem.sendPacketTCP(new PacketLoginTCP("Local Player"));
    }

    public void establishUDPConnection(byte[] digest) {
        try {
            InetAddress inetaddress = InetAddress.getByName("127.0.0.1");
            networkSystem.connectUDP(inetaddress, 34000);
            networkSystem.sendPacketUDP(new PacketLoginUDP("Local Player", digest));
        } catch (Exception e) {
            log.error("Couldn't connect to local UDP server", e);
        }
    }

    private void stopServer() {
        if (localServer != null) {
            localServer.stop();
            localServer = null;
        }
    }

    public void quitToMainMenu() {
        cameraController.onExitToMainMenu();
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
        networkSystem.closeChannels();
        networkSystem.shutdown();
        networkSystem.clear();
    }

    void resize(int width, int height) {
        renderer.resize(width, height);
        guiManager.resize(width, height);
    }

    public WorldClient createWorld() {
        this.world = worldSupplier.get();
        return world;
    }

    public void setCurrentGui(Gui gui) {
        guiManager.setCurrentGui(gui);
    }

    public void sendTCPPacket(PacketOut packet) {
        networkSystem.sendPacketTCP(packet);
    }

    public void sendUDPPacket(PacketOut packet) {
        networkSystem.sendPacketUDP(packet);
    }

    public void addFutureTask(Runnable runnable) {
        addFutureTask(Executors.callable(runnable));
    }

    private void addFutureTask(Callable<?> callable) {
        futureTasks.add(ListenableFutureTask.create(callable));
    }

    public void stop() {
        GLFW.glfwSetWindowShouldClose(window, true);
    }

    public void clear() {
        clearNetwork();
        stopServer();

        soundManager.cleanup();
        renderer.clear();
    }

    public static Core get() {
        return instance;
    }
}
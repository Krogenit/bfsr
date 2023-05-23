package net.bfsr.client;

import com.google.common.util.concurrent.ListenableFutureTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
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
import net.bfsr.client.renderer.WorldRenderer;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.client.settings.Option;
import net.bfsr.client.world.WorldClient;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.sound.AbstractSoundManager;
import net.bfsr.network.PacketOut;
import net.bfsr.profiler.Profiler;

import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

@Log4j2
public class Core {
    private static Core instance;

    @Getter
    private final AbstractSoundManager soundManager = Engine.soundManager;
    @Getter
    private final WorldRenderer worldRenderer;
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

    public Core() {
        this.worldRenderer = new WorldRenderer(this);
        instance = this;
    }

    public void init(Gui startGui, GuiInGame guiInGame) {
        Lang.load();
        Listeners.init();
        this.inputHandler.init();
        Engine.setInputHandler(inputHandler);
        this.settings.readSettings();
        this.networkSystem.init();
        this.worldRenderer.init();
        this.guiManager.init(startGui, guiInGame);
        this.cameraController.init();
        this.profiler.setEnable(Option.IS_PROFILING.getBoolean());
        this.soundManager.setGain(Option.SOUND_VOLUME.getFloat());
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
        worldRenderer.update();
        profiler.endStartSection("inputHandler");
        inputHandler.update();
        profiler.endStartSection("cameraController");
        cameraController.update();
        if (world != null) {
            profiler.endStartSection("soundManager");
            soundManager.updateListenerPosition(Engine.renderer.camera.getPosition());
            soundManager.updateGain(Option.SOUND_VOLUME.getFloat());
            profiler.endStartSection("world");
            if (!Engine.isPaused()) world.update();
        }

        profiler.endStartSection("guiManager");
        guiManager.update();
        profiler.endStartSection("network");
        networkSystem.update();
        profiler.endStartSection("renderer.postUpdate");
        worldRenderer.postUpdate();
    }

    public void render(float interpolation) {
        profiler.endStartSection("prepareRender");
        worldRenderer.prepareRender(interpolation);
        profiler.endStartSection("render");
        worldRenderer.render(interpolation);
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

    public void stopServer() {
        if (localServer != null) {
            localServer.stop();
            localServer = null;
        }
    }

    public void quitToMainMenu() {
        cameraController.onExitToMainMenu();
        worldRenderer.onExitToMainMenu();
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

    public void resize(int width, int height) {
        guiManager.resize(width, height);
    }

    public WorldClient createWorld() {
        this.world = new WorldClient();
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
        Engine.stop();
    }

    public void clear() {
        clearNetwork();
        stopServer();
    }

    public static Core get() {
        return instance;
    }

    public boolean isVSync() {
        return Option.V_SYNC.getBoolean() && Option.MAX_FPS.getMaxValue() - Option.MAX_FPS.getInteger() <= 0;
    }

    public int getTargetFPS() {
        return Option.MAX_FPS.getInteger();
    }

    public boolean needSync() {
        return Option.MAX_FPS.getInteger() < Option.MAX_FPS.getMaxValue();
    }
}
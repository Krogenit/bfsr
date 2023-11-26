package net.bfsr.client;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.gui.main.GuiMainMenu;
import net.bfsr.client.input.InputHandler;
import net.bfsr.client.language.Lang;
import net.bfsr.client.listener.entity.ship.ShipEventListener;
import net.bfsr.client.listener.entity.wreck.WreckEventListener;
import net.bfsr.client.listener.module.shield.ShieldEventListener;
import net.bfsr.client.listener.module.weapon.BeamEventListener;
import net.bfsr.client.listener.module.weapon.WeaponEventListener;
import net.bfsr.client.listener.world.WorldEventListener;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.client.renderer.GlobalRenderer;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.WorldRenderer;
import net.bfsr.client.server.LocalServer;
import net.bfsr.client.server.LocalServerGameLogic;
import net.bfsr.client.server.ThreadLocalServer;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.client.settings.ConfigSettings;
import net.bfsr.client.world.BlankWorld;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.engine.ClientGameLogic;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.sound.AbstractSoundManager;
import net.bfsr.engine.util.Side;
import net.bfsr.network.packet.Packet;
import net.bfsr.world.World;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

@Log4j2
@Getter
public class Core extends ClientGameLogic {
    private static Core instance;

    private final AbstractSoundManager soundManager = Engine.soundManager;
    private final NetworkSystem networkSystem = new NetworkSystem();
    private final InputHandler inputHandler = new InputHandler();
    private final GuiManager guiManager = new GuiManager();
    private final ParticleManager particleManager = new ParticleManager();
    private final RenderManager renderManager = new RenderManager();
    private final DamageHandler damageHandler = new DamageHandler(renderManager);
    private final ConfigSettings settings = new ConfigSettings();
    private final GlobalRenderer globalRenderer = new GlobalRenderer(
            guiManager, profiler, renderManager, particleManager, new WorldRenderer(renderManager)
    );

    @Setter
    private World world = BlankWorld.get();
    private String playerName;
    private LocalServer localServer;

    public Core() {
        instance = this;
    }

    @Override
    public void init() {
        Lang.load();
        this.inputHandler.init();
        this.settings.load();
        this.networkSystem.init();
        this.globalRenderer.init();
        this.renderManager.init();
        this.guiManager.init();
        this.profiler.setEnable(ClientSettings.IS_PROFILING.getBoolean());
        this.soundManager.setGain(ClientSettings.SOUND_VOLUME.getFloat());
        this.particleManager.init();
        ConfigConverterManager.INSTANCE.init();
        ConfigConverterManager.INSTANCE.registerConfigRegistry(ParticleEffectsRegistry.INSTANCE);
        registerListeners();
        super.init();
        this.guiManager.openGui(new GuiMainMenu());
    }

    private void registerListeners() {
        eventBus.subscribe(new ShipEventListener());
        eventBus.subscribe(new ShieldEventListener());
        eventBus.subscribe(new WeaponEventListener());
        eventBus.subscribe(new WreckEventListener());
        eventBus.subscribe(new BeamEventListener());
        eventBus.subscribe(new WorldEventListener());
    }

    @Override
    public void update() {
        super.update();
        profiler.startSection("renderer");

        if (!isPaused()) {
            renderManager.update();
            globalRenderer.update();
        }

        profiler.endStartSection("inputHandler");
        inputHandler.update();
        profiler.endStartSection("soundManager");
        soundManager.updateListenerPosition(Engine.renderer.camera.getPosition());
        soundManager.updateGain(ClientSettings.SOUND_VOLUME.getFloat());

        if (!isPaused()) {
            profiler.endStartSection("world");
            world.update();
            profiler.endStartSection("particles");
            particleManager.update();
        }

        profiler.endStartSection("guiManager");
        guiManager.update();
        profiler.endStartSection("network");
        networkSystem.update();
        profiler.endStartSection("renderManager.postUpdate");
        renderManager.postUpdate();
        profiler.endSection();
    }

    @Override
    public void render(float interpolation) {
        globalRenderer.render(interpolation);
    }

    public void startSinglePlayer() {
        startLocalServer();
        connectToLocalServerTCP();
        closeGui();
    }

    private void startLocalServer() {
        playerName = "Local Player";
        localServer = new LocalServer(new LocalServerGameLogic());
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

    private void connectToLocalServerTCP() {
        try {
            InetAddress inetaddress = InetAddress.getByName("127.0.0.1");
            connectToServer(inetaddress, 34000);
        } catch (Exception e) {
            log.error("Couldn't connect to local server", e);
        }
    }

    public void connectToServer(InetAddress inetaddress, int port) {
        networkSystem.connect(inetaddress, port);
    }

    public void stopServer() {
        if (localServer != null) {
            localServer.stop();
            localServer = null;
        }
    }

    public void quitToMainMenu() {
        eventBus.publish(new ExitToMainMenuEvent());
        clearNetwork();
        stopServer();
        world.clear();
        world = BlankWorld.get();

        openGui(new GuiMainMenu());
    }

    public void clearNetwork() {
        networkSystem.closeChannels();
        networkSystem.shutdown();
        networkSystem.clear();
    }

    @Override
    public void resize(int width, int height) {
        guiManager.resize(width, height);
    }

    public void createWorld(long seed) {
        this.world = new World(profiler, Side.CLIENT, seed, eventBus);
        globalRenderer.createBackgroundTexture(seed);
    }

    public void closeGui() {
        guiManager.closeGui();
    }

    public void openGui(@NotNull Gui gui) {
        guiManager.openGui(gui);
    }

    public void sendTCPPacket(Packet packet) {
        networkSystem.sendPacketTCP(packet);
    }

    public void sendUDPPacket(Packet packet) {
        networkSystem.sendPacketUDP(packet);
    }

    @Override
    public void clear() {
        clearNetwork();
        stopServer();
    }

    public static Core get() {
        return instance;
    }

    @Override
    public boolean isVSync() {
        return ClientSettings.V_SYNC.getBoolean() &&
                ClientSettings.MAX_FPS.getMaxValue() - ClientSettings.MAX_FPS.getInteger() <= 0;
    }

    @Override
    public int getTargetFPS() {
        return ClientSettings.MAX_FPS.getInteger();
    }

    @Override
    public boolean needSync() {
        return ClientSettings.MAX_FPS.getInteger() < ClientSettings.MAX_FPS.getMaxValue();
    }

    public int getParticlesCount() {
        return particleManager.getParticlesCount();
    }

    public boolean isInWorld() {
        return world != BlankWorld.get();
    }
}
package net.bfsr.client;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.client.gui.main.GuiMainMenu;
import net.bfsr.client.input.InputHandler;
import net.bfsr.client.language.Lang;
import net.bfsr.client.listener.entity.ShipEventListener;
import net.bfsr.client.listener.gui.GuiEventListener;
import net.bfsr.client.listener.gui.HUDEventListener;
import net.bfsr.client.listener.world.WorldEventListener;
import net.bfsr.client.module.ShieldLogic;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.client.physics.CollisionHandler;
import net.bfsr.client.renderer.EntityRenderer;
import net.bfsr.client.renderer.GlobalRenderer;
import net.bfsr.client.renderer.WorldRenderer;
import net.bfsr.client.server.LocalServer;
import net.bfsr.client.server.LocalServerGameLogic;
import net.bfsr.client.server.ThreadLocalServer;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.client.settings.ConfigSettings;
import net.bfsr.client.world.BlankWorld;
import net.bfsr.client.world.entity.ClientEntityIdManager;
import net.bfsr.client.world.entity.EntitySpawnLoginRegistry;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.logic.ClientGameLogic;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.sound.AbstractSoundManager;
import net.bfsr.engine.util.Side;
import net.bfsr.entity.CommonEntityManager;
import net.bfsr.logic.LogicType;
import net.bfsr.network.packet.Packet;
import net.bfsr.world.World;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

@Log4j2
@Getter
public class Client extends ClientGameLogic {
    public static final String GAME_VERSION = "Dev 0.1.6";
    private static Client instance;

    private final ConfigConverterManager configConverterManager = new ConfigConverterManager();
    private final AbstractSoundManager soundManager = Engine.soundManager;
    private final NetworkSystem networkSystem = new NetworkSystem();
    private final EntitySpawnLoginRegistry entitySpawnLoginRegistry = new EntitySpawnLoginRegistry();
    private final GuiManager guiManager = Engine.guiManager;
    private final InputHandler inputHandler = new InputHandler();
    private final ParticleManager particleManager = new ParticleManager();
    private final EntityRenderer entityRenderer = new EntityRenderer();
    private final DamageHandler damageHandler = new DamageHandler(entityRenderer);
    private final ConfigSettings settings = new ConfigSettings();
    private final GlobalRenderer globalRenderer = new GlobalRenderer(
            guiManager, profiler, entityRenderer, particleManager, new WorldRenderer(profiler, entityRenderer)
    );

    private World world = BlankWorld.get();
    private String playerName;
    private LocalServer localServer;
    private ThreadLocalServer threadLocalServer;

    @Setter
    private double clientToServerTimeDiff;
    @Getter
    private double renderTime;
    @Getter
    private final double clientRenderDelay = Engine.getClientRenderDelayInMills() * 1_000_000.0;

    public Client(Profiler profiler) {
        super(profiler);
        instance = this;
    }

    @Override
    public void init() {
        Lang.load();
        inputHandler.init();
        settings.load();
        configConverterManager.init();
        configConverterManager.registerConfigRegistry(ParticleEffectsRegistry.INSTANCE);
        networkSystem.init();
        entitySpawnLoginRegistry.init(configConverterManager);
        globalRenderer.init();
        entityRenderer.init();
        guiManager.init(eventBus);
        profiler.setEnable(ClientSettings.IS_PROFILING.getBoolean());
        soundManager.setGain(ClientSettings.SOUND_VOLUME.getFloat());
        particleManager.init();
        registerListeners();
        super.init();
        guiManager.openGui(new GuiMainMenu());
        registerLogic(LogicType.SHIELD_UPDATE.ordinal(), new ShieldLogic());
    }

    private void registerListeners() {
        eventBus.register(new ShipEventListener());
        eventBus.register(new WorldEventListener());
        eventBus.register(new GuiEventListener());
        eventBus.register(new HUDEventListener());
    }

    @Override
    public void update(double time) {
        super.update(time);
        profiler.start("renderManager");

        if (!isPaused()) {
            entityRenderer.update();
        }

        profiler.endStart("inputHandler");
        inputHandler.update();
        profiler.endStart("soundManager");
        soundManager.updateListenerPosition(Engine.renderer.camera.getPosition());
        soundManager.updateGain(ClientSettings.SOUND_VOLUME.getFloat());
        profiler.end();

        renderTime = time - clientToServerTimeDiff - clientRenderDelay;

        profiler.start("network");
        networkSystem.update(renderTime);

        if (!isPaused()) {
            profiler.endStart("world");
            world.update(renderTime);
            profiler.endStart("particles");
            particleManager.update();
        }

        profiler.endStart("guiManager");
        guiManager.update();
        profiler.endStart("renderManager.postUpdate");

        if (!isPaused()) {
            entityRenderer.postWorldUpdate();
        }

        profiler.end();
    }

    @Override
    public void render(float interpolation) {
        profiler.start("globalRenderer");
        globalRenderer.render(interpolation);
        profiler.end();
    }

    public void startSinglePlayer() {
        startLocalServer();
        connectToLocalServerTCP();
        closeGui();
    }

    private void startLocalServer() {
        playerName = "Local Player";
        localServer = new LocalServer(new LocalServerGameLogic(new Profiler()));
        threadLocalServer = new ThreadLocalServer(localServer);
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
            connectToServer(inetaddress, 34000, "Local Player");
        } catch (Exception e) {
            log.error("Couldn't connect to local server", e);
        }
    }

    public void connectToServer(InetAddress inetaddress, int port, String login) {
        networkSystem.connect(inetaddress, port, login);
    }

    public void stopLocalServer() {
        if (localServer != null) {
            localServer.stop();
            localServer = null;
        }
    }

    public void quitToMainMenu() {
        eventBus.publish(new ExitToMainMenuEvent());
        clearNetwork();
        stopLocalServer();
        waitServerStop();
        setBlankWorld();

        openGui(new GuiMainMenu());
    }

    public void clearNetwork() {
        networkSystem.closeChannels();
        networkSystem.shutdown();
        networkSystem.clear();
    }

    private void waitServerStop() {
        if (threadLocalServer != null) {
            while (threadLocalServer.isAlive()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        guiManager.resize(width, height);
    }

    public void createWorld(long seed) {
        world = new World(profiler, Side.CLIENT, seed, eventBus, new CommonEntityManager(), new ClientEntityIdManager(), this,
                new CollisionHandler(eventBus));
        world.init();
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
        stopLocalServer();
    }

    private void setBlankWorld() {
        world.clear();
        world = BlankWorld.get();
    }

    public static Client get() {
        return instance;
    }

    public HUD createHUD() {
        return new HUD();
    }

    @Override
    public boolean isVSync() {
        return ClientSettings.V_SYNC.getBoolean() && ClientSettings.MAX_FPS.getMaxValue() - ClientSettings.MAX_FPS.getInteger()
                <= 0;
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
package net.bfsr.client;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.font.FontType;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.client.gui.main.GuiMainMenu;
import net.bfsr.client.input.InputHandler;
import net.bfsr.client.language.LanguageManager;
import net.bfsr.client.listener.entity.ShipEventListener;
import net.bfsr.client.listener.gui.GuiEventListener;
import net.bfsr.client.listener.gui.HUDEventListener;
import net.bfsr.client.listener.world.WorldEventListener;
import net.bfsr.client.module.ShieldLogic;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.particle.effect.ParticleEffects;
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
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.entity.ParticleManager;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.logic.ClientGameLogic;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.sound.AbstractSoundManager;
import net.bfsr.entity.CommonEntityManager;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.logic.LogicType;
import net.bfsr.network.packet.Packet;
import net.bfsr.world.World;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

@Log4j2
@Getter
public class Client extends ClientGameLogic {
    public static final String GAME_VERSION = "Dev 0.1.7";
    private static Client instance;

    private final LanguageManager languageManager = new LanguageManager().load();

    private final ConfigSettings settings = new ConfigSettings();

    private final InputHandler inputHandler = new InputHandler(this);

    private final GuiManager guiManager = Engine.getGuiManager();

    private final AbstractSoundManager soundManager = Engine.getSoundManager();

    private final ParticleManager particleManager = new ParticleManager();
    private final ParticleEffectsRegistry particleEffectsRegistry = new ParticleEffectsRegistry(particleManager);
    private final ConfigConverterManager configConverterManager = new ConfigConverterManager(particleEffectsRegistry);
    private final ParticleEffects particleEffects = new ParticleEffects(particleManager, particleEffectsRegistry);

    private final ShipFactory shipFactory = new ShipFactory(configConverterManager.getConverter(ShipRegistry.class),
            new ShipOutfitter(configConverterManager));

    private final EntityRenderer entityRenderer = new EntityRenderer(this);
    private final GlobalRenderer globalRenderer = new GlobalRenderer(profiler, entityRenderer, particleManager,
            new WorldRenderer(profiler, entityRenderer, eventBus));
    private final AbstractCamera camera = Engine.getRenderer().getCamera();

    private final DamageHandler damageHandler = new DamageHandler(entityRenderer);

    private final EntitySpawnLoginRegistry entitySpawnLoginRegistry = new EntitySpawnLoginRegistry(configConverterManager, shipFactory,
            damageHandler);

    private final NetworkSystem networkSystem = new NetworkSystem(this);

    private World world = BlankWorld.get();
    private String playerName;

    @Getter
    private LocalServer localServer;
    private ThreadLocalServer threadLocalServer;

    @Setter
    private double clientToServerTimeDiff;
    @Getter
    private double renderTime;
    @Getter
    private final double clientRenderDelay = Engine.getClientRenderDelayInMills() * 1_000_000.0;

    public Client(Profiler profiler, EventBus eventBus) {
        super(profiler, eventBus);
        instance = this;
    }

    @Override
    public void init() {
        settings.load();
        applySettings();
        networkSystem.init();
        registerListeners();
        registerFonts();
        registerLogic(LogicType.SHIELD_UPDATE.ordinal(), new ShieldLogic());
        guiManager.openGui(new GuiMainMenu());
    }

    private void applySettings() {
        if (ClientSettings.IS_DEBUG.getBoolean()) {
            Engine.getRenderer().setDebugWindow();
        }

        profiler.setEnable(ClientSettings.IS_PROFILING.getBoolean());
        soundManager.setGain(ClientSettings.SOUND_VOLUME.getFloat());
    }

    private void registerListeners() {
        eventBus.register(new ShipEventListener());
        eventBus.register(new WorldEventListener());
        eventBus.register(new GuiEventListener());
        eventBus.register(new HUDEventListener());
    }

    private void registerFonts() {
        FontType[] fonts = FontType.values();
        for (int i = 0; i < fonts.length; i++) {
            FontType font = fonts[i];
            Engine.getFontManager().registerFont(this, font.getFontName(), font.getFontFile());
        }
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
        soundManager.updateListenerPosition(camera.getPosition());
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
        localServer = new LocalServer(new LocalServerGameLogic(new Profiler(), new EventBus()));
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
        particleManager.clear();
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
        world = new World(profiler, seed, eventBus, new CommonEntityManager(), new ClientEntityIdManager(), this,
                new CollisionHandler(this));
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
        super.clear();
        clearNetwork();
        stopLocalServer();
    }

    private void setBlankWorld() {
        world.clear();
        world = BlankWorld.get();
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

    public static Client get() {
        return instance;
    }
}
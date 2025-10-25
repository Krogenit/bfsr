package net.bfsr.client;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.font.FontType;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.client.gui.main.GuiMainMenu;
import net.bfsr.client.input.CameraInputController;
import net.bfsr.client.input.DebugInputController;
import net.bfsr.client.input.GuiInputController;
import net.bfsr.client.input.InputHandler;
import net.bfsr.client.input.PlayerInputController;
import net.bfsr.client.language.LanguageManager;
import net.bfsr.client.listener.entity.ShipEventListener;
import net.bfsr.client.listener.gui.GuiEventListener;
import net.bfsr.client.listener.world.WorldEventListener;
import net.bfsr.client.module.ShieldLogic;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.particle.effect.ParticleEffects;
import net.bfsr.client.physics.CollisionHandler;
import net.bfsr.client.renderer.EntityRenderer;
import net.bfsr.client.renderer.GlobalRenderer;
import net.bfsr.client.renderer.WorldRenderer;
import net.bfsr.client.server.LocalServer;
import net.bfsr.client.server.ThreadLocalServer;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.client.settings.ConfigSettings;
import net.bfsr.client.world.BlankWorld;
import net.bfsr.client.world.entity.ClientEntityIdManager;
import net.bfsr.client.world.entity.EntityManager;
import net.bfsr.client.world.entity.EntitySpawnDataRegistry;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.ConfigConverterManager;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.logic.ClientGameLogic;
import net.bfsr.engine.loop.AbstractGameLoop;
import net.bfsr.engine.network.RenderDelayManager;
import net.bfsr.engine.network.packet.Packet;
import net.bfsr.engine.network.packet.common.world.entity.spawn.EntityPacketSpawnData;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.sound.AbstractSoundManager;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.ParticleManager;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.logic.LogicType;
import net.bfsr.physics.collision.CollisionMatrix;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

@Log4j2
@Getter
public class Client extends ClientGameLogic {
    public static final String GAME_VERSION = "Dev 0.1.8";
    private static Client instance;

    @Getter
    private double renderTime;
    @Getter
    private int renderFrame;

    private final LanguageManager languageManager = new LanguageManager().load();

    private final ConfigSettings settings = new ConfigSettings();

    private final PlayerInputController playerInputController = new PlayerInputController(this);
    private final InputHandler inputHandler = new InputHandler(new GuiInputController(),
            new CameraInputController(this, playerInputController), playerInputController, new DebugInputController(this));

    private final GuiManager guiManager = Engine.getGuiManager();

    private final AbstractSoundManager soundManager = Engine.getSoundManager();

    private final ParticleManager particleManager = new ParticleManager();
    private final ParticleEffectsRegistry particleEffectsRegistry = new ParticleEffectsRegistry(particleManager);
    private final ConfigConverterManager configConverterManager = new ConfigConverterManager(particleEffectsRegistry);
    private final ParticleEffects particleEffects = new ParticleEffects(particleManager, particleEffectsRegistry);

    private final ShipOutfitter shipOutfitter = new ShipOutfitter(configConverterManager);
    private final ShipFactory shipFactory = new ShipFactory(configConverterManager.getConverter(ShipRegistry.class), shipOutfitter);

    private final EntityRenderer entityRenderer = new EntityRenderer(this);
    private final GlobalRenderer globalRenderer = new GlobalRenderer(profiler, entityRenderer, particleManager,
            new WorldRenderer(profiler, entityRenderer, eventBus));
    private final AbstractCamera camera = Engine.getRenderer().getCamera();

    private final DamageHandler damageHandler = new DamageHandler(entityRenderer);

    private final EntitySpawnDataRegistry entitySpawnDataRegistry = new EntitySpawnDataRegistry(configConverterManager, shipFactory,
            damageHandler, this);

    private final ClientEntityIdManager entityIdManager = new ClientEntityIdManager(this);

    private final RenderDelayManager renderDelayManager = new RenderDelayManager();
    private final NetworkSystem networkSystem = new NetworkSystem(this, renderDelayManager);

    protected HUD hud;

    private World world = BlankWorld.get();
    private String playerName;

    @Getter
    private LocalServer localServer;
    private ThreadLocalServer threadLocalServer;

    public Client(AbstractGameLoop gameLoop, Profiler profiler, EventBus eventBus) {
        super(gameLoop, profiler, eventBus);
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
    }

    private void registerListeners() {
        eventBus.register(new ShipEventListener());
        eventBus.register(new WorldEventListener());
        eventBus.register(new GuiEventListener());
    }

    private void registerFonts() {
        FontType[] fonts = FontType.values();
        for (int i = 0; i < fonts.length; i++) {
            FontType font = fonts[i];
            Engine.getFontManager().registerFont(this, font.getFontName(), font.getFontFile());
        }
    }

    @Override
    public void update(int frame, double time) {
        super.update(frame, time);
        renderTime = time
                + networkSystem.getAveragePing() * 1_000_000.0
                - renderDelayManager.getRenderDelayInNanos();

        renderFrame = frame - renderDelayManager.getRenderDelayInFrames() + networkSystem.getAveragePingInFrames();

        profiler.start("renderManager");

        if (!isPaused()) {
            entityRenderer.update();
        }

        profiler.endStart("inputHandler");
        inputHandler.update(frame);
        profiler.endStart("soundManager");
        soundManager.updateListenerPosition(camera.getPosition());

        if (!isPaused()) {
            profiler.endStart("world");
            world.update(renderTime, renderFrame);
            profiler.endStart("particles");
            particleManager.update();
        }

        profiler.endStart("guiManager");
        guiManager.update();
        profiler.endStart("renderManager.postUpdate");

        if (!isPaused()) {
            entityRenderer.postWorldUpdate();
        }

        profiler.endStart("network");
        networkSystem.update(renderFrame);
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
        localServer = new LocalServer();
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
            networkSystem.connect(inetaddress, 34000, "Local Player");
        } catch (Exception e) {
            log.error("Couldn't connect to local server", e);
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
        renderDelayManager.reset();
    }

    public void stopLocalServer() {
        if (localServer != null) {
            localServer.stop();
            localServer = null;
        }
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
        world = new World(profiler, seed, eventBus, new EntityManager(), entityIdManager, this,
                new CollisionMatrix(new CollisionHandler(this)));
        world.init();
    }

    public void openGui(@NotNull Gui gui) {
        guiManager.openGui(gui);
    }

    public void closeGui() {
        guiManager.closeGui();
    }

    public void sendTCPPacket(Packet packet) {
        networkSystem.sendPacketTCP(packet);
    }

    public void sendUDPPacket(Packet packet) {
        networkSystem.sendPacketUDP(packet);
    }

    private void setBlankWorld() {
        world.clear();
        world = BlankWorld.get();
    }

    public HUD createHUD() {
        return hud = new HUD();
    }

    @Override
    public boolean isVSync() {
        return ClientSettings.V_SYNC.getBoolean() && ClientSettings.MAX_FPS.getMaxValue() - ClientSettings.MAX_FPS.getInteger() <= 0;
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

    @Override
    public EntityPacketSpawnData<?> getEntitySpawnData(int entityId) {
        return entitySpawnDataRegistry.createSpawnData(entityId);
    }

    public static Client get() {
        return instance;
    }

    @Override
    public void clear() {
        super.clear();
        clearNetwork();
        stopLocalServer();
    }
}
package net.bfsr.server;

import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.util.Side;
import net.bfsr.entity.EntityIdManager;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.logic.LogicType;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.database.PlayerRepository;
import net.bfsr.server.entity.EntityManager;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.entity.ship.ShipSpawner;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.event.listener.entity.ShipEventListener;
import net.bfsr.server.event.listener.module.ModuleEventListener;
import net.bfsr.server.event.listener.module.weapon.WeaponEventListener;
import net.bfsr.server.module.ShieldLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.physics.CollisionHandler;
import net.bfsr.server.player.PlayerManager;
import net.bfsr.world.World;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Getter
@Log4j2
public abstract class ServerGameLogic extends GameLogic {
    private static ServerGameLogic instance;

    private final ConfigConverterManager configConverterManager = new ConfigConverterManager();
    private final ServerSettings settings = createSettings();
    private final DamageSystem damageSystem = new DamageSystem();
    private final ShipFactory shipFactory = new ShipFactory(configConverterManager.getConverter(ShipRegistry.class),
            new ShipOutfitter(configConverterManager));
    private final PlayerManager playerManager = new PlayerManager(shipFactory);
    private final NetworkSystem networkSystem = new NetworkSystem(playerManager);
    private final EntityTrackingManager entityTrackingManager = new EntityTrackingManager(eventBus, networkSystem);
    private final AiFactory aiFactory = new AiFactory(entityTrackingManager);
    private final ShipSpawner shipSpawner = new ShipSpawner(shipFactory, aiFactory);
    private final ShipOutfitter shipOutfitter = new ShipOutfitter(configConverterManager);
    private final WreckSpawner wreckSpawner = new WreckSpawner(configConverterManager.getConverter(WreckRegistry.class));
    private final CollisionHandler collisionHandler = new CollisionHandler(eventBus, damageSystem, entityTrackingManager, wreckSpawner);

    private int ups;
    private World world;

    protected ServerGameLogic(Profiler profiler, EventBus eventBus) {
        super(Side.SERVER, profiler, eventBus);
        instance = this;
    }

    public void init() {
        playerManager.init(createPlayerRepository(settings));
        long seed = new XoRoShiRo128PlusPlusRandom().nextLong();
        log.info("Creating world with seed {}", seed);
        world = new World(profiler, seed, eventBus, new EntityManager(), new EntityIdManager(), this, collisionHandler);
        world.init();
        profiler.setEnable(true);
        networkSystem.init();
        startupNetworkSystem();
        initListeners();
        registerLogic(LogicType.SHIELD_UPDATE.ordinal(), new ShieldLogic(entityTrackingManager));
    }

    private void initListeners() {
        eventBus.register(new ShipEventListener());
        eventBus.register(new WeaponEventListener(entityTrackingManager, world));
        eventBus.register(new ModuleEventListener(entityTrackingManager));
    }

    protected ServerSettings createSettings() {
        return new ServerSettings();
    }

    protected abstract PlayerRepository createPlayerRepository(ServerSettings settings);

    private void startupNetworkSystem() {
        InetAddress inetaddress;
        try {
            inetaddress = InetAddress.getByName(settings.getHostName());
            networkSystem.startup(this, inetaddress, settings.getPort());
            log.info("Set server address {}:{}", settings.getHostName(), settings.getPort());
        } catch (UnknownHostException e) {
            throw new IllegalStateException(
                    "Can't start server on address " + settings.getHostName() + ":" + settings.getPort(), e);
        }
    }

    @Override
    public void update(double time) {
        super.update(time);
        profiler.start("playerManager");
        playerManager.update();
        profiler.endStart("world");
        updateWorld(time);
        profiler.endStart("network");
        networkSystem.update();
        profiler.end();
    }

    protected void updateWorld(double time) {
        world.update(time);
        shipSpawner.update(world);
        entityTrackingManager.update(time, world.getEntities());
    }

    public PlayerNetworkHandler createPlayerNetworkHandler(int connectionId, SocketChannel socketChannel, DatagramChannel datagramChannel,
                                                           boolean singlePlayer) {
        return new PlayerNetworkHandler(connectionId, socketChannel, datagramChannel, singlePlayer, world, playerManager,
                entityTrackingManager, aiFactory, eventBus, networkSystem.getPacketRegistry(), shipOutfitter);
    }

    void setFps(int fps) {
        ups = fps;
    }

    public static ServerGameLogic get() {
        return instance;
    }

    @Override
    public void clear() {
        log.info("Saving database...");
        playerManager.saveAllSync();
        log.info("Clearing world...");
        world.clear();
        log.info("Terminating network...");
        networkSystem.shutdown();
        playerManager.clear();
        log.info("Stopped");
    }
}
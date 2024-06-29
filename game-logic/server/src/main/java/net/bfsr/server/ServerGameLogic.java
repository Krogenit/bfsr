package net.bfsr.server;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.util.Side;
import net.bfsr.entity.EntityIdManager;
import net.bfsr.entity.ship.Ship;
import net.bfsr.logic.LogicType;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.entity.EntityManager;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.entity.ship.ShipSpawner;
import net.bfsr.server.event.PlayerDisconnectEvent;
import net.bfsr.server.event.listener.entity.ShipEventListener;
import net.bfsr.server.event.listener.module.ModuleEventListener;
import net.bfsr.server.event.listener.module.weapon.WeaponEventListener;
import net.bfsr.server.module.ShieldLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.physics.CollisionHandler;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;
import net.bfsr.world.World;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

@Log4j2
public class ServerGameLogic extends GameLogic {
    @Getter
    private static ServerGameLogic instance;

    @Getter
    private int ups;

    @Getter
    private net.bfsr.world.World world;

    @Getter
    private final PlayerManager playerManager = new PlayerManager();
    @Getter
    private final NetworkSystem networkSystem = new NetworkSystem(playerManager);
    private ServerSettings settings;
    private final ShipSpawner shipSpawner = new ShipSpawner();
    @Getter
    private final DamageSystem damageSystem = new DamageSystem();
    @Getter
    private final EntityTrackingManager entityTrackingManager = new EntityTrackingManager(eventBus, networkSystem);

    protected ServerGameLogic(Profiler profiler) {
        super(profiler);
        instance = this;
    }

    @Override
    public void init() {
        world = new World(profiler, Side.SERVER, new Random().nextLong(), eventBus, new EntityManager(), new EntityIdManager(),
                this, new CollisionHandler(eventBus));
        world.init();
        profiler.setEnable(true);
        networkSystem.init();
        loadConfigs();
        settings = createSettings();
        startupNetworkSystem(settings);
        initListeners();
        super.init();
        registerLogic(LogicType.SHIELD_UPDATE.ordinal(), new ShieldLogic());
    }

    private void initListeners() {
        eventBus.register(new ShipEventListener());
        eventBus.register(new WeaponEventListener());
        eventBus.register(new ModuleEventListener());
    }

    protected void loadConfigs() {
        ConfigConverterManager.INSTANCE.init();
    }

    protected ServerSettings createSettings() {
        return new ServerSettings();
    }

    private void startupNetworkSystem(ServerSettings serverSettings) {
        InetAddress inetaddress;
        try {
            inetaddress = InetAddress.getByName(serverSettings.getHostName());
            networkSystem.startup(inetaddress, serverSettings.getPort());
            log.info("Set server address {}:{}", serverSettings.getHostName(), serverSettings.getPort());
        } catch (UnknownHostException e) {
            throw new IllegalStateException(
                    "Can't start server on address " + serverSettings.getHostName() + ":" + serverSettings.getPort(), e);
        }

        playerManager.connect(serverSettings.getDataBaseServiceHost(), serverSettings.getDatabaseServicePort());
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

    public void onPlayerDisconnected(Player player) {
        playerManager.removePlayer(player);
        playerManager.save(player);
        List<Ship> ships = player.getShips();
        for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
            ships.get(i).setDead();
        }

        eventBus.publish(new PlayerDisconnectEvent(player));
    }

    void setFps(int fps) {
        ups = fps;
    }

    public static NetworkSystem getNetwork() {
        return instance.networkSystem;
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
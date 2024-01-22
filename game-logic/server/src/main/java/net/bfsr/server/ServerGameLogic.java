package net.bfsr.server;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.GameLogic;
import net.bfsr.engine.util.Side;
import net.bfsr.entity.EntityIdManager;
import net.bfsr.entity.ship.Ship;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.entity.ship.ShipSpawner;
import net.bfsr.server.event.listener.damage.DamageEventListener;
import net.bfsr.server.event.listener.entity.BulletEventListener;
import net.bfsr.server.event.listener.entity.ShipEventListener;
import net.bfsr.server.event.listener.entity.WreckEventListener;
import net.bfsr.server.event.listener.module.ModuleEventListener;
import net.bfsr.server.event.listener.module.shield.ShieldEventListener;
import net.bfsr.server.event.listener.module.weapon.WeaponEventListener;
import net.bfsr.server.event.listener.world.WorldEventListener;
import net.bfsr.server.network.EntitySyncManager;
import net.bfsr.server.network.NetworkSystem;
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
    private NetworkSystem networkSystem;
    private ServerSettings settings;
    @Getter
    private PlayerManager playerManager;
    private ShipSpawner shipSpawner;
    @Getter
    private final DamageSystem damageSystem = new DamageSystem();
    private final EntitySyncManager entitySyncManager = new EntitySyncManager();

    protected ServerGameLogic() {
        instance = this;
    }

    @Override
    public void init() {
        world = new World(profiler, Side.SERVER, new Random().nextLong(), eventBus, new EntityIdManager(), this);
        world.init();
        playerManager = new PlayerManager(world);
        networkSystem = new NetworkSystem(playerManager);
        shipSpawner = new ShipSpawner(world);

        profiler.setEnable(true);
        networkSystem.init();
        loadConfigs();
        settings = createSettings();
        startupNetworkSystem(settings);
        initListeners();
        super.init();
    }

    private void initListeners() {
        eventBus.register(new ShieldEventListener());
        eventBus.register(new ShipEventListener());
        eventBus.register(new WeaponEventListener());
        eventBus.register(new BulletEventListener());
        eventBus.register(new WreckEventListener());
        eventBus.register(new DamageEventListener());
        eventBus.register(new WorldEventListener());
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
        profiler.startSection("playerManager");
        playerManager.update();
        profiler.endStartSection("updateWorld");
        updateWorld(time);
        profiler.endStartSection("network");
        networkSystem.update();
        profiler.endSection();
    }

    protected void updateWorld(double time) {
        world.update(time);
        shipSpawner.update();
        entitySyncManager.sendEntitiesToClients(world.getEntities(), time);
    }

    public void onPlayerDisconnected(Player player) {
        playerManager.removePlayer(player);
        playerManager.save(player);
        List<Ship> ships = player.getShips();
        for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
            ships.get(i).setDead();
        }
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
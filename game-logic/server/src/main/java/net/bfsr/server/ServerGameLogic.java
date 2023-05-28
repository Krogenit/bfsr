package net.bfsr.server;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.GameLogic;
import net.bfsr.engine.util.Side;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.server.config.ServerSettings;
import net.bfsr.server.entity.ship.ShipSpawner;
import net.bfsr.server.event.listener.damage.DamageEventListener;
import net.bfsr.server.event.listener.entity.bullet.BulletEventListener;
import net.bfsr.server.event.listener.entity.ship.ShipEventListener;
import net.bfsr.server.event.listener.entity.wreck.WreckEventListener;
import net.bfsr.server.event.listener.module.shield.ShieldEventListener;
import net.bfsr.server.event.listener.module.weapon.WeaponEventListener;
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
    private final net.bfsr.world.World world;
    @Getter
    private final NetworkSystem networkSystem;
    private ServerSettings settings;
    @Getter
    private final PlayerManager playerManager;
    private final ShipSpawner shipSpawner;
    @Getter
    private final DamageSystem damageSystem = new DamageSystem();

    protected ServerGameLogic() {
        this.world = new World(profiler, Side.SERVER, new Random().nextLong(), eventBus);
        this.playerManager = new PlayerManager(world);
        this.networkSystem = new NetworkSystem(playerManager);
        this.shipSpawner = new ShipSpawner(world);

        instance = this;
    }

    @Override
    public void init() {
        profiler.setEnable(true);
        networkSystem.init();
        loadConfigs();
        settings = createSettings();
        startupNetworkSystem(settings);
        initListeners();
        super.init();
    }

    private void initListeners() {
        eventBus.subscribe(new ShieldEventListener());
        eventBus.subscribe(new ShipEventListener());
        eventBus.subscribe(new WeaponEventListener());
        eventBus.subscribe(new BulletEventListener());
        eventBus.subscribe(new WreckEventListener());
        eventBus.subscribe(new DamageEventListener());
    }

    protected void loadConfigs() {
        ConfigConverterManager.INSTANCE.init();
    }

    protected ServerSettings createSettings() {
        return new ServerSettings();
    }

    protected void startupNetworkSystem(ServerSettings serverSettings) {
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
    public void update() {
        super.update();
        profiler.startSection("playerManager");
        playerManager.update();
        profiler.endStartSection("updateWorld");
        updateWorld();
        profiler.endStartSection("network");
        networkSystem.update();
        profiler.endSection();
    }

    protected void updateWorld() {
        world.update();
        shipSpawner.update();
    }

    public void onPlayerDisconnected(Player player) {
        playerManager.removePlayer(player);
        playerManager.save(player);
        List<Ship> ships = player.getShips();
        for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
            Ship s = ships.get(i);
            s.setOwner(null);
            s.setDead();
            networkSystem.sendTCPPacketToAll(new PacketRemoveObject(s));
        }
    }

    protected void setFps(int fps) {
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
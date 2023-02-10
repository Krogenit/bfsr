package net.bfsr.world;

import lombok.Getter;
import net.bfsr.client.particle.Wreck;
import net.bfsr.component.weapon.small.WeaponBeamSmall;
import net.bfsr.component.weapon.small.WeaponGausSmall;
import net.bfsr.component.weapon.small.WeaponLaserSmall;
import net.bfsr.component.weapon.small.WeaponPlasmSmall;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.engi.ShipEngiSmall0;
import net.bfsr.entity.ship.human.ShipHumanSmall0;
import net.bfsr.entity.ship.saimon.ShipSaimonSmall0;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.network.EnumGui;
import net.bfsr.network.packet.server.PacketOpenGui;
import net.bfsr.profiler.Profiler;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorldServer extends World {
    public static final float PACKET_SPAWN_DISTANCE = 600;
    public static final float PACKET_UPDATE_DISTANCE = 400;

    @Getter
    private final List<PlayerServer> players = new ArrayList<>();
    private final HashMap<String, PlayerServer> playersByName = new HashMap<>();
    @Getter
    private final long seed;
    @Getter
    private final List<Wreck> particles = new ArrayList<>();

    private float timer;

    public WorldServer(Profiler profiler) {
        super(false, profiler);
        this.seed = rand.nextLong();
    }

    public void spawnShips() {
        boolean sameFaction = true;
        int botCount = 0;
        Faction lastFaction = null;
        for (Ship s : ships) {
            if (s.isBot()) botCount++;

            if (lastFaction != null && lastFaction != s.getFaction()) {
                sameFaction = false;
            }

            lastFaction = s.getFaction();
        }

        if (botCount < 2 || sameFaction
//				|| --timer <= 0
        ) {
            timer = 600;
            int maxCount = 20;
            int count = maxCount;

            Vector2f pos = new Vector2f(-150, 0);
            Ship ship;
            float angle = 0.1f;
            if (sameFaction && lastFaction == Faction.HUMAN) count = count - botCount;
            for (int i = 0; i < count; i++) {
                pos.x = rand.nextInt(1) - 250;
                pos.y = rand.nextInt(100) - 50;
//				pos = RotationHelper.rotate(angle, pos.x, pos.y);
                ship = new ShipHumanSmall0(this, pos.x, pos.y, rand.nextFloat() * MathUtils.TWO_PI, false);
                ship.init();
                ship.setFaction(Faction.HUMAN);
                if (rand.nextInt(2) == 0) {
                    ship.addWeaponToSlot(0, new WeaponBeamSmall(ship));
                    ship.addWeaponToSlot(1, new WeaponBeamSmall(ship));
                } else {
                    ship.addWeaponToSlot(0, new WeaponPlasmSmall(ship));
                    ship.addWeaponToSlot(1, new WeaponPlasmSmall(ship));
                }
                ship.setName("[BOT] " + ship.getFaction().toString());
                pos.y += ship.getScale().x * 1f;
            }

//			pos = RotationHelper.rotate((float) (Math.PI * 2f / 3f), pos.x, pos.y);
            pos = new Vector2f(0, 0);
            count = maxCount;
            if (sameFaction && lastFaction == Faction.SAIMON) count = count - botCount;
            for (int i = 0; i < count; i++) {
                pos.y = rand.nextInt(1) - 350;
                pos.x = rand.nextInt(100) - 50;
//				pos = RotationHelper.rotate(angle, pos.x, pos.y);
                ship = new ShipSaimonSmall0(this, pos.x, pos.y, rand.nextFloat() * MathUtils.TWO_PI, false);
                ship.init();
                ship.setFaction(Faction.SAIMON);
                if (rand.nextInt(2) == 0) {
                    ship.addWeaponToSlot(0, new WeaponBeamSmall(ship));
                    ship.addWeaponToSlot(1, new WeaponBeamSmall(ship));
                } else {
                    ship.addWeaponToSlot(0, new WeaponLaserSmall(ship));
                    ship.addWeaponToSlot(1, new WeaponLaserSmall(ship));
                }
                ship.setName("[BOT] " + ship.getFaction().toString());
                pos.y += ship.getScale().x * 1f;
            }

//			pos = RotationHelper.rotate((float) (Math.PI * 2f / 3f), pos.x, pos.y);
            pos = new Vector2f(150, 0);
            count = maxCount;
            if (sameFaction && lastFaction == Faction.ENGI) count = count - botCount;
            for (int i = 0; i < count; i++) {
                pos.x = rand.nextInt(1) + 250;
                pos.y = rand.nextInt(100) - 50;
//				pos = RotationHelper.rotate(angle, pos.x, pos.y);
                ship = new ShipEngiSmall0(this, pos.x, pos.y, rand.nextFloat() * MathUtils.TWO_PI, false);
                ship.init();
                ship.setFaction(Faction.ENGI);
                if (rand.nextInt(2) == 0) {
                    ship.addWeaponToSlot(0, new WeaponBeamSmall(ship));
                    ship.addWeaponToSlot(1, new WeaponBeamSmall(ship));
                } else {
                    ship.addWeaponToSlot(0, new WeaponGausSmall(ship));
                    ship.addWeaponToSlot(1, new WeaponGausSmall(ship));
                }
                ship.setName("[BOT] " + ship.getFaction().toString());
                pos.y += ship.getScale().x * 1f;
            }
        }
    }

    @Override
    public void update() {
        super.update();
        spawnShips();
    }

    @Override
    protected void updateParticles() {
        for (int i = 0; i < particles.size(); i++) {
            Wreck wreck = particles.get(i);
            wreck.update();
            if (wreck.isDead()) {
                removePhysicObject(wreck);
                particles.remove(i);
                i--;
            }
        }
    }

    @Override
    protected void removeShip(Ship ship, int index) {
        super.removeShip(ship, index);
        if (ship.getOwner() != null) {
            PlayerServer player = ship.getOwner();
            player.removeShip(ship);
            if (player.getShips().size() == 0) {
                CollisionObject lastAttacker = ship.getLastAttacker();
                String attacker = "";
                if (lastAttacker != null) {
                    if (lastAttacker instanceof Ship) {
                        Ship attackerShip = (Ship) lastAttacker;
                        attacker = attackerShip.getName();
                    }
                }
                player.getNetworkManager().scheduleOutboundPacket(new PacketOpenGui(EnumGui.Destroyed, attacker));
            }
        }
    }

    public void addWreck(Wreck wreck) {
        particles.add(wreck);
    }

    public void removePlayer(PlayerServer player) {
        this.players.remove(player);
        this.playersByName.remove(player.getUserName());
    }

    public void addNewPlayer(PlayerServer player) {
        this.players.add(player);
        this.playersByName.put(player.getUserName(), player);
    }

    public PlayerServer getPlayer(String name) {
        return playersByName.get(name);
    }

    public boolean canJoin(PlayerServer player) {
        return !playersByName.containsKey(player.getUserName());
    }

    @Override
    public void clear() {
        super.clear();
        players.clear();
        playersByName.clear();
        particles.clear();
    }
}

package net.bfsr.server.world;

import lombok.Getter;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.network.GuiType;
import net.bfsr.profiler.Profiler;
import net.bfsr.server.component.weapon.WeaponBeamSmall;
import net.bfsr.server.component.weapon.WeaponGausSmall;
import net.bfsr.server.component.weapon.WeaponLaserSmall;
import net.bfsr.server.component.weapon.WeaponPlasmSmall;
import net.bfsr.server.entity.CollisionObject;
import net.bfsr.server.entity.bullet.Bullet;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.entity.ship.ShipEngiSmall0;
import net.bfsr.server.entity.ship.ShipHumanSmall0;
import net.bfsr.server.entity.ship.ShipSaimonSmall0;
import net.bfsr.server.entity.wreck.ShipWreckDamagable;
import net.bfsr.server.entity.wreck.Wreck;
import net.bfsr.server.network.packet.server.gui.PacketOpenGui;
import net.bfsr.server.player.Player;
import net.bfsr.world.World;
import org.dyn4j.dynamics.BodyFixture;
import org.joml.Vector2f;

import java.util.*;

public class WorldServer extends World<Ship, Bullet> {
    public static final float PACKET_SPAWN_DISTANCE = 600;
    public static final float PACKET_UPDATE_DISTANCE = 400;

    @Getter
    private final List<Player> players = new ArrayList<>();
    private final HashMap<String, Player> playersByName = new HashMap<>();
    @Getter
    private final long seed;
    @Getter
    private final List<Wreck> particles = new ArrayList<>();
    private final List<ShipWreckDamagable> shipWrecks = new ArrayList<>();
    private final Queue<ShipWreckDamagable> damagesToAdd = new LinkedList<>();

    private float timer;

    public WorldServer(Profiler profiler) {
        super(profiler);
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

        if (botCount < 25 || sameFaction
//				|| --timer <= 0
        ) {
            timer = 600;
            int maxCount = 1;
            int count = maxCount;

            Vector2f pos = new Vector2f(-50, 0);
            Ship ship;
            if (sameFaction && lastFaction == Faction.HUMAN) count = count - botCount;
            for (int i = 0; i < count; i++) {
                pos.x = rand.nextInt(1) - 150;
                pos.y = rand.nextInt(100) - 50;
                ship = new ShipHumanSmall0();
                ship.setPosition(pos.x, pos.y);
                ship.setRotation(rand.nextFloat() * MathUtils.TWO_PI);
                ship.init(this);
                ship.setFaction(Faction.HUMAN);
                if (rand.nextInt(2) == 0) {
                    ship.addWeaponToSlot(0, new WeaponBeamSmall());
                    ship.addWeaponToSlot(1, new WeaponBeamSmall());
                } else {
                    ship.addWeaponToSlot(0, new WeaponPlasmSmall());
                    ship.addWeaponToSlot(1, new WeaponPlasmSmall());
                }
                ship.setName("[BOT] " + ship.getFaction().toString());
                ship.sendSpawnPacket();
            }

            count = maxCount;
            if (sameFaction && lastFaction == Faction.SAIMON) count = count - botCount;
            for (int i = 0; i < count; i++) {
                pos.y = rand.nextInt(1) - 50;
                pos.x = rand.nextInt(100) - 50;
                ship = new ShipSaimonSmall0();
                ship.setPosition(pos.x, pos.y);
                ship.setRotation(rand.nextFloat() * MathUtils.TWO_PI);
                ship.init(this);
                ship.setFaction(Faction.SAIMON);
                if (rand.nextInt(2) == 0) {
                    ship.addWeaponToSlot(0, new WeaponBeamSmall());
                    ship.addWeaponToSlot(1, new WeaponBeamSmall());
                } else {
                    ship.addWeaponToSlot(0, new WeaponLaserSmall());
                    ship.addWeaponToSlot(1, new WeaponLaserSmall());
                }
                ship.setName("[BOT] " + ship.getFaction().toString());
                ship.sendSpawnPacket();
            }

            count = maxCount;
            if (sameFaction && lastFaction == Faction.ENGI) count = count - botCount;
            for (int i = 0; i < count; i++) {
                pos.x = rand.nextInt(1) + 50;
                pos.y = rand.nextInt(100) - 50;
                ship = new ShipEngiSmall0();
                ship.setPosition(pos.x, pos.y);
                ship.setRotation(rand.nextFloat() * MathUtils.TWO_PI);
                ship.init(this);
                ship.setFaction(Faction.ENGI);
                if (rand.nextInt(2) == 0) {
                    ship.addWeaponToSlot(0, new WeaponBeamSmall());
                    ship.addWeaponToSlot(1, new WeaponBeamSmall());
                } else {
                    ship.addWeaponToSlot(0, new WeaponGausSmall());
                    ship.addWeaponToSlot(1, new WeaponGausSmall());
                }
                ship.setName("[BOT] " + ship.getFaction().toString());
                ship.sendSpawnPacket();
            }
        }
    }

    @Override
    public void update() {
        spawnShips();

        while (damagesToAdd.size() > 0) {
            ShipWreckDamagable shipWreckDamagable = damagesToAdd.poll();
            shipWrecks.add(shipWreckDamagable);
            addPhysicObject(shipWreckDamagable);
        }

        for (int i = 0; i < shipWrecks.size(); i++) {
            ShipWreckDamagable shipWreckDamagable = shipWrecks.get(i);
            shipWreckDamagable.update();
            if (shipWreckDamagable.isDead()) {
                shipWrecks.remove(i--);
                removePhysicObject(shipWreckDamagable);
            } else if (shipWreckDamagable.getFixturesToAdd().size() > 0) {
                shipWreckDamagable.getBody().removeAllFixtures();
                List<BodyFixture> fixturesToAdd = shipWreckDamagable.getFixturesToAdd();
                while (fixturesToAdd.size() > 0) {
                    shipWreckDamagable.getBody().addFixture(fixturesToAdd.remove(0));
                }

                shipWreckDamagable.getBody().updateMass();
            }
        }

        super.update();
    }

    @Override
    protected void postPhysicsUpdate() {
        super.postPhysicsUpdate();
        for (int i = 0; i < shipWrecks.size(); i++) {
            shipWrecks.get(i).postPhysicsUpdate();
        }
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
            Player player = ship.getOwner();
            player.removeShip(ship);
            if (player.getShips().size() == 0) {
                CollisionObject lastAttacker = ship.getLastAttacker();
                String attacker = "";
                if (lastAttacker != null) {
                    if (lastAttacker instanceof Ship attackerShip) {
                        attacker = attackerShip.getName();
                    }
                }
                player.getNetworkHandler().sendUDPPacket(new PacketOpenGui(GuiType.DESTROYED, attacker));
            }
        }
    }

    public void addDamage(ShipWreckDamagable shipWreckDamagable) {
        damagesToAdd.add(shipWreckDamagable);
    }

    public void addWreck(Wreck wreck) {
        particles.add(wreck);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
        this.playersByName.remove(player.getUsername());
    }

    public void addNewPlayer(Player player) {
        this.players.add(player);
        this.playersByName.put(player.getUsername(), player);
    }

    public Player getPlayer(String name) {
        return playersByName.get(name);
    }

    public boolean canJoin(Player player) {
        return !playersByName.containsKey(player.getUsername());
    }

    @Override
    public void clear() {
        super.clear();
        players.clear();
        playersByName.clear();
        particles.clear();
    }
}
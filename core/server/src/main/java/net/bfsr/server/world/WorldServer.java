package net.bfsr.server.world;

import lombok.Getter;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.profiler.Profiler;
import net.bfsr.server.core.Server;
import net.bfsr.server.network.packet.server.entity.bullet.PacketSpawnBullet;
import net.bfsr.util.RandomHelper;
import net.bfsr.util.Side;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.util.LinkedList;
import java.util.Queue;

public class WorldServer extends World {
    public static final float PACKET_SPAWN_DISTANCE = 600;
    public static final float PACKET_UPDATE_DISTANCE = 400;

    @Getter
    private final long seed;
    private final Queue<ShipWreck> damagesToAdd = new LinkedList<>();

    private float timer;

    public WorldServer(Profiler profiler) {
        super(profiler, Side.SERVER);
        this.seed = rand.nextLong();
    }

    private void spawnShips() {
        if (timer-- > 0) return;

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

        if (botCount < 50 || sameFaction) {
            timer = 10;
            int maxCount = 1;
            int count = maxCount;

            float rotation = MathUtils.TWO_PI / 3;
            Vector2f pos = RotationHelper.angleToVelocity(0, 200);
            float spawnRandomOffset = 75;
            if (sameFaction && lastFaction == Faction.HUMAN) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                addShip(ShipFactory.get().createBotHumanSmall(this, pos.x + addX, pos.y + addY, rand.nextFloat() * MathUtils.TWO_PI));
            }

            RotationHelper.rotate(rotation, pos.x, pos.y, pos);
            count = maxCount;
            if (sameFaction && lastFaction == Faction.SAIMON) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                addShip(ShipFactory.get().createBotSaimonSmall(this, pos.x + addX, pos.y + addY, rand.nextFloat() * MathUtils.TWO_PI));
            }

            RotationHelper.rotate(rotation, pos.x, pos.y, pos);
            count = maxCount;
            if (sameFaction && lastFaction == Faction.ENGI) count = count - botCount;
            for (int i = 0; i < count; i++) {
                float addX = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                float addY = RandomHelper.randomFloat(rand, -spawnRandomOffset, spawnRandomOffset);
                addShip(ShipFactory.get().createBotEngiSmall(this, pos.x + addX, pos.y + addY, rand.nextFloat() * MathUtils.TWO_PI));
            }
        }
    }

    @Override
    public void update() {
        spawnShips();

        while (damagesToAdd.size() > 0) {
            super.addWreck(damagesToAdd.poll());
        }

        super.update();
    }

    @Override
    protected void updateParticles() {}

    public void addBullet(Bullet bullet) {
        super.addBullet(bullet);
        Server.getNetwork().sendUDPPacketToAllNearby(new PacketSpawnBullet(bullet), bullet.getPosition(), PACKET_SPAWN_DISTANCE);
    }

    @Override
    public void addWreck(ShipWreck wreck) {
        damagesToAdd.add(wreck);
    }
}
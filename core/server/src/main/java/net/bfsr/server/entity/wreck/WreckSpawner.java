package net.bfsr.server.entity.wreck;

import net.bfsr.entity.wreck.WreckType;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.server.core.Server;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.packet.server.entity.wreck.PacketSpawnWreck;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.ObjectPool;
import org.joml.Vector2f;

import java.util.Random;

public final class WreckSpawner {
    private static final Random RAND = new Random();
    public static final Vector2f CACHED_VECTOR = new Vector2f();

    public static final ObjectPool<Wreck> PARTICLE_WREAK_POOL = new ObjectPool<>();
    public static final ObjectPool<ShipWreck> PARTICLE_SHIP_WREAK_POOL = new ObjectPool<>();

    public static void spawnDestroyShipSmall(Ship ship) {
        Vector2f pos = ship.getPosition();
        Vector2f velocity = ship.getVelocity();
        WorldServer w = ship.getWorld();
        Random rand = w.getRand();
        spawnDamageDebris(w, rand.nextInt(3), pos.x, pos.y, velocity.x * 0.025f, velocity.y * 0.025f, 1.0f);
        spawnDamageWrecks(w, rand.nextInt(2), pos.x, pos.y, velocity.x * 0.25f, velocity.y * 0.25f);
//        Vector2 bodyVelocity = ship.getBody().getLinearVelocity();
//        float rot = ship.getRotation();
//        if (rand.nextInt(2) == 0) {
//            spawnShipWreck(ship, 0, pos.x, pos.y, rot, -rot * 3.0f + (float) bodyVelocity.x * 0.4f, -rot * 3.0f + (float) bodyVelocity.y * 0.4f, 750.0f);
//        }
//
//        if (rand.nextInt(2) == 0) {
//            spawnShipWreck(ship, 1, pos.x, pos.y, rot, rot * 3.0f - (float) bodyVelocity.x * 0.4f, rot * 3.0f - (float) bodyVelocity.y * 0.4f, 750.0f);
//        }
    }

    public static void spawnShipWreck(Ship s, int wreckIndex, float x, float y, float angle, float velocityX, float velocityY, float lifeTime) {
        float angleVel = (-0.005f + RAND.nextFloat() / 200.0f) * 60.0f;
        ShipWreck wreck = PARTICLE_SHIP_WREAK_POOL.getOrCreate(ShipWreck::new).init(s.getWorld().getNextId(), wreckIndex, s, x, y, velocityX, velocityY, angle, angleVel,
                s.getScale().x, s.getScale().y, lifeTime);
        Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketSpawnWreck(wreck), x, y, WorldServer.PACKET_SPAWN_DISTANCE);
    }

    public static void spawnDamageDebris(WorldServer world, int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(RAND.nextFloat() * MathUtils.TWO_PI, 4.0f + RAND.nextFloat() * 2.0f, CACHED_VECTOR);
            CACHED_VECTOR.add(velocityX, velocityY).mul(0.7f);
            float angle = RAND.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + RAND.nextFloat() / 200.0f) * 60.0f;
            float size2 = (1.0F - RAND.nextFloat() / 3.0F) * 2.0f * size;
            float alphaVel = 0.1f;
            boolean isFire = RAND.nextInt(3) == 0;
            boolean isFireExplosion = isFire && RAND.nextInt(5) == 0;
            Wreck wreck = PARTICLE_WREAK_POOL.getOrCreate(Wreck::new).init(world, world.getNextId(), RAND.nextInt(6), false, isFire, isFireExplosion, x, y,
                    CACHED_VECTOR.x, CACHED_VECTOR.y, angle, angleVel, size2, size2, 1.0f, alphaVel, WreckType.SMALL);
            Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketSpawnWreck(wreck), x, y, WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    public static void spawnDamageWrecks(WorldServer world, int count, float x, float y, float velocityX, float velocityY) {
        for (int i = 0; i < count; i++) {
            RotationHelper.angleToVelocity(RAND.nextFloat() * MathUtils.TWO_PI, 4.0f + RAND.nextFloat() * 2.0f, CACHED_VECTOR);
            float angle = RAND.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + RAND.nextFloat() / 200.0f) * 60.0f;
            float size = (1.0F - RAND.nextFloat() / 3.0F) * 4.0f;
            float alphaVel = 0.04f;
            boolean isFireExplosion = RAND.nextInt(4) == 0;
            Wreck wreck = PARTICLE_WREAK_POOL.getOrCreate(Wreck::new).init(world, world.getNextId(), RAND.nextInt(3), true, true, isFireExplosion,
                    x, y, CACHED_VECTOR.x + velocityX * 0.7f, CACHED_VECTOR.y + velocityY * 0.7f, angle, angleVel, size, size, 1.0f, alphaVel, WreckType.DEFAULT);
            Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketSpawnWreck(wreck), x, y, WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }
}
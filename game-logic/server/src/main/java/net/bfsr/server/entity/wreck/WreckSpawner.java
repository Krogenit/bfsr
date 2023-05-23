package net.bfsr.server.entity.wreck;

import net.bfsr.common.math.LUT;
import net.bfsr.common.math.MathUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.Wreck;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.math.RotationHelper;
import net.bfsr.server.core.Server;
import net.bfsr.server.network.packet.server.entity.wreck.PacketSpawnWreck;
import net.bfsr.server.world.WorldServer;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.util.Random;

public final class WreckSpawner {
    private static final Random RAND = Server.getInstance().getWorld().getRand();

    public static void spawnDestroyShipSmall(Ship ship) {
        Vector2f pos = ship.getPosition();
        Vector2f velocity = ship.getVelocity();
        WorldServer w = ((WorldServer) ship.getWorld());
        Random rand = w.getRand();
        spawnDamageDebris(w, rand.nextInt(3), pos.x, pos.y, velocity.x * 0.025f, velocity.y * 0.025f, 1.0f);
        spawnDamageWrecks(w, rand.nextInt(2), pos.x, pos.y, velocity.x * 0.25f, velocity.y * 0.25f);
    }

    public static void spawnDamageDebris(WorldServer world, int count, float x, float y, float velocityX, float velocityY, float size) {
        for (int i = 0; i < count; i++) {
            Vector2f velocity = RotationHelper.angleToVelocity(RAND.nextFloat() * MathUtils.TWO_PI, 4.0f + RAND.nextFloat() * 2.0f);
            velocity.add(velocityX, velocityY).mul(0.7f);
            float angle = RAND.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + RAND.nextFloat() / 200.0f) * 60.0f;
            float size2 = (1.0F - RAND.nextFloat() / 3.0F) * 2.0f * size;
            float alphaVel = 0.1f;
            boolean isFire = RAND.nextInt(3) == 0;
            boolean isFireExplosion = isFire && RAND.nextInt(5) == 0;
            Wreck wreck = World.WREAK_POOL.getOrCreate(Wreck::new).init(world, world.getNextId(), RAND.nextInt(6), false, isFire, isFireExplosion, x, y,
                    velocity.x, velocity.y, LUT.sin(angle), LUT.cos(angle), angleVel, size2, size2, alphaVel, WreckType.SMALL);
            world.addWreck(wreck);
            Server.getNetwork().sendUDPPacketToAllNearby(new PacketSpawnWreck(wreck), x, y, WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    private static void spawnDamageWrecks(WorldServer world, int count, float x, float y, float velocityX, float velocityY) {
        for (int i = 0; i < count; i++) {
            Vector2f velocity = RotationHelper.angleToVelocity(RAND.nextFloat() * MathUtils.TWO_PI, 4.0f + RAND.nextFloat() * 2.0f);
            float angle = RAND.nextFloat() * MathUtils.TWO_PI;
            float angleVel = (-0.005f + RAND.nextFloat() / 200.0f) * 60.0f;
            float size = (1.0F - RAND.nextFloat() / 3.0F) * 4.0f;
            float alphaVel = 0.04f;
            boolean isFireExplosion = RAND.nextInt(4) == 0;
            Wreck wreck = World.WREAK_POOL.getOrCreate(Wreck::new).init(world, world.getNextId(), RAND.nextInt(3), true, true, isFireExplosion, x, y,
                    velocity.x + velocityX * 0.7f, velocity.y + velocityY * 0.7f, LUT.sin(angle), LUT.cos(angle), angleVel, size, size, alphaVel, WreckType.DEFAULT);
            world.addWreck(wreck);
            Server.getNetwork().sendUDPPacketToAllNearby(new PacketSpawnWreck(wreck), x, y, WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }
}
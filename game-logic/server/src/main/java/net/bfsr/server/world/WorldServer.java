package net.bfsr.server.world;

import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.util.Side;
import net.bfsr.world.World;

import java.util.Random;

public class WorldServer extends World {
    public static final float PACKET_SPAWN_DISTANCE = 600;
    public static final float PACKET_UPDATE_DISTANCE = 400;

    public WorldServer(Profiler profiler) {
        super(profiler, Side.SERVER, new Random().nextLong());
    }
}
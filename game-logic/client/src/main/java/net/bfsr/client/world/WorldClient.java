package net.bfsr.client.world;

import net.bfsr.client.Core;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.util.Side;
import net.bfsr.world.World;

public class WorldClient extends World {
    public WorldClient(Profiler profiler, long seed) {
        super(profiler, Side.CLIENT, seed);
        Core.get().getWorldRenderer().createBackgroundTexture(seed);
    }
}
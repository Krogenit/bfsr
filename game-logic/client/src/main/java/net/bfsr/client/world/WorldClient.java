package net.bfsr.client.world;

import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.client.renderer.WorldRenderer;
import net.bfsr.util.Side;
import net.bfsr.world.World;

public class WorldClient extends World {
    private final WorldRenderer renderer = Core.get().getWorldRenderer();
    @Getter
    private final ParticleManager particleManager = new ParticleManager();

    public WorldClient() {
        super(Core.get().getProfiler(), Side.CLIENT);
    }

    public void setSeed(long seed) {
        renderer.createBackgroundTexture(seed);
    }

    @Override
    protected void updateParticles() {
        particleManager.update();
    }

    @Override
    public void clear() {
        super.clear();
        particleManager.clear();
    }

    public int getParticlesCount() {
        return particleManager.getParticlesCount();
    }
}
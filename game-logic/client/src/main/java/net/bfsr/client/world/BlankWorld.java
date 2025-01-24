package net.bfsr.client.world;

import net.bfsr.physics.CommonCollisionHandler;
import net.bfsr.world.World;

public final class BlankWorld extends World {
    private static final BlankWorld BLANK_WORLD = new BlankWorld();

    private BlankWorld() {
        super(null, 0, null, null, null, null, new CommonCollisionHandler(null));
    }

    @Override
    public void init() {}

    @Override
    public void update(double timestamp) {}

    @Override
    public void clear() {}

    public static BlankWorld get() {
        return BLANK_WORLD;
    }
}
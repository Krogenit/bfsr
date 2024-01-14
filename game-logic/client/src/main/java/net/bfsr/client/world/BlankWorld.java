package net.bfsr.client.world;

import net.bfsr.engine.GameLogic;
import net.bfsr.world.World;

public final class BlankWorld extends World {
    private static final BlankWorld BLANK_WORLD = new BlankWorld();

    private BlankWorld() {
        super(null, null, 0, null, null, new GameLogic());
    }

    @Override
    public void update(double timestamp) {}

    @Override
    public void clear() {}

    public static BlankWorld get() {
        return BLANK_WORLD;
    }
}
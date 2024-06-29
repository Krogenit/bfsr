package net.bfsr.engine.logic;

import net.bfsr.engine.profiler.Profiler;

public abstract class ClientGameLogic extends GameLogic {
    protected ClientGameLogic(Profiler profiler) {
        super(profiler);
    }

    public abstract void resize(int width, int height);
    public abstract void render(float interpolation);
    public abstract boolean isVSync();
    public abstract int getTargetFPS();
    public abstract boolean needSync();
}
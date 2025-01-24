package net.bfsr.engine.logic;

import lombok.Getter;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.util.Side;

@Getter
public abstract class ClientGameLogic extends GameLogic {
    protected ClientGameLogic(Profiler profiler, EventBus eventBus) {
        super(Side.CLIENT, profiler, eventBus);
    }

    public abstract void init();
    public abstract void resize(int width, int height);
    public abstract void render(float interpolation);
    public abstract boolean isVSync();
    public abstract int getTargetFPS();
    public abstract boolean needSync();
}
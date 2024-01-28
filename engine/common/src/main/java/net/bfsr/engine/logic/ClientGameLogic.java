package net.bfsr.engine.logic;

public abstract class ClientGameLogic extends GameLogic {
    public abstract void resize(int width, int height);
    public abstract void render(float interpolation);
    public abstract boolean isVSync();
    public abstract int getTargetFPS();
    public abstract boolean needSync();
}
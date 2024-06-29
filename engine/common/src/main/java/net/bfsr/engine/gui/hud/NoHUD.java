package net.bfsr.engine.gui.hud;

public final class NoHUD extends HUDAdapter {
    private static final NoHUD INSTANCE = new NoHUD();

    private NoHUD() {}

    public static NoHUD get() {
        return INSTANCE;
    }
}
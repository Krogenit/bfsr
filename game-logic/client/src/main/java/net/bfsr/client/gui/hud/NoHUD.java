package net.bfsr.client.gui.hud;

public final class NoHUD extends HUDAdapter {
    private static final NoHUD NO_HUD = new NoHUD();

    private NoHUD() {}

    public static NoHUD get() {
        return NO_HUD;
    }
}
package net.bfsr.engine.gui;

public final class NoGui extends Gui {
    private static final NoGui INSTANCE = new NoGui();

    private NoGui() {}

    public static NoGui get() {
        return INSTANCE;
    }
}

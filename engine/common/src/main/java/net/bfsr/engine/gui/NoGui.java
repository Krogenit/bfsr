package net.bfsr.engine.gui;

public final class NoGui extends Gui {
    private static final NoGui INSTANCE = new NoGui();

    private NoGui() {}

    public static NoGui get() {
        return INSTANCE;
    }

    @Override
    public void updateLastValues() {
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).updateLastValues();
        }
    }
}

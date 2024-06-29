package net.bfsr.engine.gui.hud;

import net.bfsr.engine.gui.Gui;

public class HUDAdapter extends Gui {
    protected HUDAdapter() {
        setCanBeHovered(false);
    }

    public boolean isActive() {
        return false;
    }
}
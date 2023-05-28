package net.bfsr.client.gui.hud;

import net.bfsr.engine.gui.Gui;
import net.bfsr.entity.ship.Ship;

public class HUDAdapter extends Gui {
    @Override
    protected void initElements() {}

    public boolean isActive() {
        return false;
    }

    public Ship getSelectedShip() {
        return null;
    }

    public void selectShip(Ship ship) {}

    public void selectShipSecondary(Ship ship) {}

    public void onShipControlStarted() {}

    public void addChatMessage(String message) {}

    public void setPing(float ping) {}
}
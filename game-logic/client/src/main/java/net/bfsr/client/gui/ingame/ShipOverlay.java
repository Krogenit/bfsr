package net.bfsr.client.gui.ingame;

public class ShipOverlay extends CommonShipOverlay {
    @Override
    protected void rebuildScene() {
        addHullCells();
        addArmorPlates();
        addShip();
        addWeaponSlots();
        addShieldValue();
        addEnergy();
    }
}
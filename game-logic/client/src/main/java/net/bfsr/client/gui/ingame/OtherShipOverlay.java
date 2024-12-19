package net.bfsr.client.gui.ingame;

public class OtherShipOverlay extends CommonShipOverlay {
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

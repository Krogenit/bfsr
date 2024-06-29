package net.bfsr.client.gui.ingame;

import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.entity.ship.module.shield.Shield;

public class OtherShipOverlay extends ShipOverlayRenderer {
    @Override
    public void render(AbstractGUIRenderer guiRenderer, int lastX, int lastY, int x, int y) {
        super.render(guiRenderer, lastX, lastY, x, y);
    }

    @Override
    protected void renderShipInfo() {
        int x = this.x + width / 2;
        int y = this.y + height / 2;

        float shipSize = 10.0f;

        Shield shield = ship.getModules().getShield();
        if (shield != null && shield.isAlive()) {
            renderShield(shield, x, y);
        }

        renderArmorPlates(ship, x, y, shipSize * 2.0f);
        renderWeaponSlots(ship, x, y, shipSize);
    }
}

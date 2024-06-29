package net.bfsr.client.gui.ingame;

import net.bfsr.client.Core;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.client.input.PlayerInputController;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.packet.client.PacketShipControl;

public class ShipOverlay extends ShipOverlayRenderer {
    private final Label shipCargo = new Label(FontType.CONSOLA);
    private final Label shipCrew = new Label(FontType.CONSOLA);
    private final Label controlText = new Label(FontType.XOLONIUM, Lang.getString("gui.control"), 16);
    private final Core core = Core.get();
    private final PlayerInputController playerInputController = core.getInputHandler().getPlayerInputController();

    public ShipOverlay(HUD hud) {
        TexturedRectangle shipAddInfoPanel = new TexturedRectangle(TextureRegister.guiHudShipAdd, 140, 72);
        add(shipAddInfoPanel.atBottomRight(-width - shipAddInfoPanel.getWidth() + 20, -shipAddInfoPanel.getHeight()));
        shipAddInfoPanel.add(shipCargo.atTopLeft(16, 26));
        shipAddInfoPanel.add(shipCrew.atTopLeft(16, 40));

        if (playerInputController.isControllingShip()) {
            controlText.setStringAndCompileAtOrigin(Lang.getString("gui.cancelControl"));
        } else {
            controlText.setStringAndCompileAtOrigin(Lang.getString("gui.control"));
        }
        add(controlText.atBottomRight(-width / 2 - controlText.getWidth() / 2, -height - 1));
        add(new Button(TextureRegister.guiButtonControl, 256, 40, () -> {
            Ship playerControlledShip = playerInputController.getShip();
            if (playerControlledShip != null) {
                core.sendTCPPacket(new PacketShipControl(playerControlledShip.getId(), false));
                playerInputController.resetControlledShip();
                selectShip(playerControlledShip);
                onShipControlCanceled();
            } else if (ship != null) {
                playerInputController.setShip(ship);
                hud.onShipControlStarted();
                core.sendTCPPacket(new PacketShipControl(ship.getId(), true));
            }
        }).atBottomRight(-128 - width / 2, -height - 26));
    }

    private void onCurrentShipSelected() {
        Cargo cargo = ship.getModules().getCargo();
        shipCargo.setStringAndCompileAtOrigin(Lang.getString(Lang.getString("gui.shipCargo") + ": " + cargo.getCapacity() + "/" +
                cargo.getMaxCapacity()));

        Crew crew = ship.getModules().getCrew();
        shipCrew.setStringAndCompileAtOrigin(Lang.getString(Lang.getString("gui.shipCrew") + ": " + crew.getCrewSize() + "/" +
                crew.getMaxCrewSize()));
    }

    private void onCurrentShipDeselected() {}

    @Override
    protected void renderShipInfo() {
        int x = this.x + width / 2;
        int y = this.y + height / 2;
        float shipSize = 10.0f;

        renderHullValue(ship, x, y);

        Shield shield = ship.getModules().getShield();
        if (shield != null && shield.isAlive()) {
            renderShield(shield, x, y);
            renderShieldValue(shield, x, y);
        }

        renderArmorPlates(ship, x, y, shipSize * 2.0f);

        int energyWidth = 16;
        int energyHeight = 8;
        Reactor reactor = ship.getModules().getReactor();
        float energy = reactor.getEnergy() / reactor.getMaxEnergy() * 20.0f;
        for (int i = 0; i < 20; i++) {
            float rot = (float) (i * 0.08f - Math.PI / 4.0f);
            RotationHelper.rotate(rot, -100, 0, rotationVector);
            rotationVector.x += x;
            rotationVector.y += y;
            renderQuad((int) rotationVector.x, (int) rotationVector.y, -MathUtils.PI + rot, energyWidth, energyHeight, 0.0f, 0.0f,
                    0.0f, 1.0f, this.energy);
            if (energy >= i) {
                renderQuad((int) rotationVector.x, (int) rotationVector.y, (float) (-Math.PI + rot), energyWidth, energyHeight,
                        0.25f, 0.5f, 1.0f, 1.0f, this.energy);
            }
        }

        renderWeaponSlots(ship, x, y, shipSize);
    }

    public void selectShip(Ship ship) {
        if (this.ship != null) {
            onCurrentShipDeselected();
        }

        this.ship = ship;

        if (this.ship != null) {
            onCurrentShipSelected();
        }
    }

    private boolean canControlShip(Ship ship) {
        return core.getPlayerName().equals(ship.getName());
    }

    public void onShipControlStarted() {
        controlText.setStringAndCompileAtOrigin(Lang.getString("gui.cancelControl"));
    }

    private void onShipControlCanceled() {
        controlText.setStringAndCompileAtOrigin(Lang.getString("gui.control"));
    }
}
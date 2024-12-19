package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.hud.HUD;
import net.bfsr.client.input.PlayerInputController;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.network.packet.client.PacketShipControl;

public class ShipOverlay extends CommonShipOverlay {
    private final PlayerInputController playerInputController = client.getInputHandler().getPlayerInputController();

    private final Label shipCargo = new Label(Font.CONSOLA_FT);
    private final Label shipCrew = new Label(Font.CONSOLA_FT);
    private final Button controlButton;

    public ShipOverlay(HUD hud) {
        TexturedRectangle shipAddInfoPanel = new TexturedRectangle(TextureRegister.guiHudShipAdd, 140, 72);
        add(shipAddInfoPanel.atBottomRight(-width + 20, 0));
        shipAddInfoPanel.add(shipCargo.atBottomLeft(16, 30));
        shipAddInfoPanel.add(shipCrew.atBottomLeft(16, 44));

        controlButton = new Button(TextureRegister.guiButtonControl, 256, 40,
                playerInputController.isControllingShip() ? Lang.getString("gui.cancelControl") : Lang.getString("gui.control"),
                Font.XOLONIUM_FT, 16,
                () -> {
                    Ship playerControlledShip = playerInputController.getShip();
                    if (playerControlledShip != null) {
                        client.sendTCPPacket(new PacketShipControl(playerControlledShip.getId(), false));
                        playerInputController.resetControlledShip();
                        selectShip(playerControlledShip);
                        onShipControlCanceled();
                    } else if (ship != null) {
                        playerInputController.setShip(ship);
                        hud.onShipControlStarted();
                        client.sendTCPPacket(new PacketShipControl(ship.getId(), true));
                    }
                });
        add(controlButton.atBottomRight(-15, height - 8));
    }

    @Override
    protected void rebuildScene() {
        addHullCells();
        addArmorPlates();
        addShip();
        addWeaponSlots();
        addShieldValue();
        addEnergy();
    }

    @Override
    protected void onCurrentShipSelected() {
        super.onCurrentShipSelected();
        Cargo cargo = ship.getModules().getCargo();
        shipCargo.setString(Lang.getString(Lang.getString("gui.shipCargo") + ": " + cargo.getCapacity() + "/" +
                cargo.getMaxCapacity()));

        Crew crew = ship.getModules().getCrew();
        shipCrew.setString(Lang.getString(Lang.getString("gui.shipCrew") + ": " + crew.getCrewSize() + "/" +
                crew.getMaxCrewSize()));
    }

    private boolean canControlShip(Ship ship) {
        return client.getPlayerName().equals(ship.getName());
    }

    public void onShipControlStarted() {
        controlButton.setString(Lang.getString("gui.cancelControl"));
    }

    private void onShipControlCanceled() {
        controlButton.setString(Lang.getString("gui.control"));
    }
}
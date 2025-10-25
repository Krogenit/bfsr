package net.bfsr.client.gui.ingame;

import net.bfsr.client.font.FontType;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.client.input.PlayerInputController;
import net.bfsr.client.language.LanguageManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.network.packet.client.PacketShipControl;

public class ShipOverlay extends CommonShipOverlay {
    private final PlayerInputController playerInputController = client.getPlayerInputController();
    private final LanguageManager languageManager = client.getLanguageManager();

    private final Label shipCargo = new Label(Engine.getFontManager().getFont(FontType.CONSOLA.getFontName()));
    private final Label shipCrew = new Label(Engine.getFontManager().getFont(FontType.CONSOLA.getFontName()));
    private final Button controlButton;

    public ShipOverlay(HUD hud) {
        TexturedRectangle shipAddInfoPanel = new TexturedRectangle(TextureRegister.guiHudShipAdd, 140, 72);
        add(shipAddInfoPanel.atBottomRight(-width + 20, 0));
        shipAddInfoPanel.add(shipCargo.atBottomLeft(16, 30));
        shipAddInfoPanel.add(shipCrew.atBottomLeft(16, 44));

        controlButton = new Button(TextureRegister.guiButtonControl, 256, 40,
                playerInputController.isControllingShip() ? languageManager.getString("gui.cancelControl") :
                        languageManager.getString("gui.control"),
                Engine.getFontManager().getFont(FontType.XOLONIUM.getFontName()), 16,
                (mouseX, mouseY) -> {
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
        shipCargo.setString(languageManager.getString(languageManager.getString("gui.shipCargo") + ": " + cargo.getCapacity() + "/" +
                cargo.getMaxCapacity()));

        Crew crew = ship.getModules().getCrew();
        shipCrew.setString(languageManager.getString(languageManager.getString("gui.shipCrew") + ": " + crew.getCrewSize() + "/" +
                crew.getMaxCrewSize()));
    }

    private boolean canControlShip(Ship ship) {
        return client.getPlayerName().equals(ship.getName());
    }

    public void onShipControlStarted() {
        controlButton.setString(languageManager.getString("gui.cancelControl"));
    }

    private void onShipControlCanceled() {
        controlButton.setString(languageManager.getString("gui.control"));
    }
}
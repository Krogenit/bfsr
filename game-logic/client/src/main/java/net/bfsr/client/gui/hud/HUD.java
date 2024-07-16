package net.bfsr.client.gui.hud;

import net.bfsr.client.Core;
import net.bfsr.client.gui.ingame.Chat;
import net.bfsr.client.gui.ingame.DebugInfoElement;
import net.bfsr.client.gui.ingame.GuiInGameMenu;
import net.bfsr.client.gui.ingame.MiniMap;
import net.bfsr.client.gui.ingame.OtherShipOverlay;
import net.bfsr.client.gui.ingame.ShipOverlay;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.gui.hud.HUDAdapter;
import net.bfsr.entity.ship.Ship;

import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

public class HUD extends HUDAdapter {
    private final ShipOverlay shipOverlay = new ShipOverlay(this);
    private final OtherShipOverlay otherShipOverlay = new OtherShipOverlay();
    private final DebugInfoElement debugInfoElement = new DebugInfoElement(this);
    private final MiniMap miniMap = new MiniMap();
    private final Chat chat = new Chat();
    private final GuiManager guiManager = Core.get().getGuiManager();

    public HUD() {
        shipOverlay.atBottomRight(-shipOverlay.getWidth(), -shipOverlay.getHeight());
        otherShipOverlay.atTopRight(-otherShipOverlay.getWidth(), 0);
        add(miniMap.atTopLeft(0, 0));
        add(chat.atBottomLeft(0, -chat.getHeight()));
        add(debugInfoElement.atTopLeft(6, miniMap.getHeight() + 6));
    }

    @Override
    public boolean isActive() {
        return chat.isActive();
    }

    @Override
    public boolean input(int key) {
        if (super.input(key)) {
            return true;
        }

        if (key == KEY_ESCAPE) {
            guiManager.openGui(new GuiInGameMenu());
            return true;
        }

        return false;
    }

    public void addChatMessage(String message) {
        chat.addChatMessage(message);
    }

    public void setPing(float ping) {
        debugInfoElement.setPing(ping);
    }

    public void selectShip(Ship ship) {
        shipOverlay.selectShip(ship);

        if (ship != null) {
            addIfAbsent(shipOverlay);
        } else {
            remove(shipOverlay);
        }
    }

    public void selectShipSecondary(Ship ship) {
        otherShipOverlay.setShip(ship);

        if (ship != null) {
            addIfAbsent(otherShipOverlay);
        } else {
            remove(otherShipOverlay);
        }
    }

    public Ship getSelectedShip() {
        return otherShipOverlay.getShip();
    }

    public void onShipControlStarted() {
        shipOverlay.onShipControlStarted();
    }
}
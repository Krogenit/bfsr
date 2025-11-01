package net.bfsr.client.gui.hud;

import net.bfsr.client.Client;
import net.bfsr.client.gui.ingame.Chat;
import net.bfsr.client.gui.ingame.DebugInfoElement;
import net.bfsr.client.gui.ingame.GuiInGameMenu;
import net.bfsr.client.gui.ingame.MiniMap;
import net.bfsr.client.gui.ingame.OtherShipOverlay;
import net.bfsr.client.gui.ingame.ShipOverlay;
import net.bfsr.client.listener.gui.HUDEventListener;
import net.bfsr.engine.gui.hud.HUDAdapter;
import net.bfsr.entity.ship.Ship;

import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

public class HUD extends HUDAdapter {
    private final ShipOverlay shipOverlay = new ShipOverlay();
    protected final OtherShipOverlay otherShipOverlay = new OtherShipOverlay();
    private final Chat chat = new Chat();
    private final HUDEventListener eventListener = new HUDEventListener(this);

    public HUD() {
        shipOverlay.atBottomRight(0, 0);
        otherShipOverlay.atTopRight(0, 0);
        add(new MiniMap().atTopLeft(0, 0));
        add(chat.atBottomLeft(0, 0));
        add(new DebugInfoElement(this).atTopRight(-6, -6));
        Client.get().getEventBus().register(eventListener);
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

    public void setPlayerShip(Ship ship) {
        shipOverlay.setShip(ship);

        if (ship != null) {
            addIfAbsent(shipOverlay);
        } else {
            remove(shipOverlay);
        }
    }

    public void selectShip(Ship ship) {
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

    @Override
    public void remove() {
        super.remove();
        Client.get().getEventBus().unregister(eventListener);
    }
}
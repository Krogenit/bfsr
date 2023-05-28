package net.bfsr.client.gui.hud;

import net.bfsr.client.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.gui.ingame.*;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.entity.ship.Ship;

import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

public class HUD extends HUDAdapter {
    private final DebugInfoElement debugInfoElement = new DebugInfoElement();
    private final MiniMap miniMap = new MiniMap();
    private final Chat chat = new Chat();
    private final ShipHUD shipHUD = new ShipHUD();
    private final GuiManager guiManager = Core.get().getGuiManager();

    @Override
    protected void initElements() {
        shipHUD.init(this);
        miniMap.init(this);
        chat.init(this);
        debugInfoElement.init(6, miniMap.getHeight() + 6);
    }

    @Override
    public void addChatMessage(String message) {
        chat.addChatMessage(message);
    }

    @Override
    public boolean onMouseLeftClick() {
        if (super.onMouseLeftClick()) {
            return true;
        }
        return chat.onMouseLeftClick();
    }

    @Override
    public boolean isActive() {
        return chat.isActive();
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (key == KEY_ESCAPE && !isActive() && guiManager.noGui()) {
            guiManager.openGui(new GuiInGameMenu());
        }

        return input;
    }

    @Override
    public boolean onMouseLeftRelease() {
        boolean leftRelease = super.onMouseLeftRelease();

        if (chat.onMouseLeftRelease()) {
            leftRelease = true;
        }

        return leftRelease;
    }

    @Override
    public void update() {
        super.update();
        shipHUD.update();
        if (ClientSettings.IS_DEBUG.getBoolean()) debugInfoElement.update();
    }

    @Override
    public void render() {
        super.render();
        guiRenderer.render();
        miniMap.render(Core.get().getWorld());
        shipHUD.render();

        if (ClientSettings.IS_DEBUG.getBoolean()) debugInfoElement.render();
    }

    @Override
    public void onScreenResize(int width, int height) {
        super.onScreenResize(width, height);
        shipHUD.resize();
    }

    @Override
    public void setPing(float ping) {
        debugInfoElement.setPing(ping);
    }

    @Override
    public void selectShip(Ship ship) {
        shipHUD.selectShip(ship);
    }

    @Override
    public void selectShipSecondary(Ship ship) {
        shipHUD.selectShipSecondary(ship);
    }

    @Override
    public Ship getSelectedShip() {
        return shipHUD.getOtherShip();
    }

    @Override
    public void onShipControlStarted() {
        shipHUD.onShipControlStarted();
    }
}
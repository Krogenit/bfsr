package net.bfsr.client.gui.ingame;

import net.bfsr.client.core.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.renderer.gui.GUIRenderer;
import net.bfsr.client.settings.Option;
import net.bfsr.entity.ship.Ship;
import org.lwjgl.glfw.GLFW;

public class GuiInGame extends Gui {
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

    public boolean isActive() {
        return chat.isActive();
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == GLFW.GLFW_KEY_ESCAPE && !isActive() && guiManager.getCurrentGui() == null) {
            guiManager.setCurrentGui(new GuiInGameMenu());
        }
    }

    @Override
    public void onMouseLeftRelease() {
        super.onMouseLeftRelease();
        chat.onMouseLeftRelease();
    }

    @Override
    public void onMouseScroll(float y) {
        super.onMouseScroll(y);
        chat.scroll(y);
    }

    @Override
    public void update() {
        super.update();
        shipHUD.update();
        if (Option.IS_DEBUG.getBoolean()) debugInfoElement.update();
    }

    @Override
    public void render() {
        super.render();
        GUIRenderer.get().render();
        miniMap.render(Core.get().getWorld());
        shipHUD.render();

        if (Option.IS_DEBUG.getBoolean()) debugInfoElement.render();
    }

    @Override
    public void onScreenResize(int width, int height) {
        super.onScreenResize(width, height);
        shipHUD.resize();
    }

    public void setPing(float ping) {
        debugInfoElement.setPing(ping);
    }

    public void selectShip(Ship ship) {
        shipHUD.selectShip(ship);
    }

    public void selectShipSecondary(Ship ship) {
        shipHUD.selectShipSecondary(ship);
    }

    public Ship getSelectedShip() {
        return shipHUD.getOtherShip();
    }

    public void onShipControlStarted() {
        shipHUD.onShipControlStarted();
    }
}
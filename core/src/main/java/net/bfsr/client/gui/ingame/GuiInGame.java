package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.Gui;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.settings.EnumOption;
import org.lwjgl.glfw.GLFW;

public class GuiInGame extends Gui {
    private final DebugInfoElement debugInfoElement = new DebugInfoElement();
    private final MiniMap miniMap = new MiniMap();
    private final Chat chat = new Chat();
    private final ShipHUD shipHUD = new ShipHUD();

    @Override
    protected void initElements() {
        shipHUD.init(this);
        miniMap.init(this);
        chat.init(this);
    }

    public void addChatMessage(String message) {
        chat.addChatMessage(message);
    }

    @Override
    public void onMouseLeftClicked() {
        super.onMouseLeftClicked();
        chat.onMouseLeftClick();
    }

    public boolean isActive() {
        return chat.isActive();
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == GLFW.GLFW_KEY_ESCAPE && Core.get().canControlShip()) {
            Core.get().setCurrentGui(new GuiInGameMenu());
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
        if (EnumOption.IS_DEBUG.getBoolean()) debugInfoElement.update(6, miniMap.getHeight() + 6);
    }

    @Override
    public void render(float interpolation) {
        super.render(interpolation);
        SpriteRenderer.INSTANCE.render(BufferType.GUI);
        miniMap.render(Core.get().getWorld());
        shipHUD.render();

        if (EnumOption.IS_DEBUG.getBoolean()) debugInfoElement.render();
    }

    public void setPing(long ping) {
        debugInfoElement.setPing(ping);
    }

    public void selectShip(Ship ship) {
        shipHUD.selectShip(ship);
    }

    public void selectShipSecondary(Ship ship) {
        shipHUD.selectShipSecondary(ship);
    }

    public void onShipControlStarted() {
        shipHUD.onShipControlStarted();
    }
}

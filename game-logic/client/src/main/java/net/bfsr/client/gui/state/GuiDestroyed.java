package net.bfsr.client.gui.state;

import net.bfsr.client.Client;
import net.bfsr.client.language.Lang;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.network.packet.client.PacketRespawn;
import org.joml.Vector2f;

import static net.bfsr.engine.input.Keys.KEY_C;
import static net.bfsr.engine.input.Keys.KEY_LEFT_CONTROL;

public class GuiDestroyed extends Gui {
    private final AbstractKeyboard keyboard = Engine.keyboard;

    public GuiDestroyed(String destroyedBy) {
        TexturedRectangle rectangle = new TexturedRectangle(TextureRegister.guiAdd);
        add(rectangle.atCenter(0, 0).setSize(600, 278));

        int buttonWidth = 220;
        int buttonHeight = 40;
        int buttonsOffset = 160;
        rectangle.add(new Button(TextureRegister.guiButtonBase, buttonWidth, buttonHeight, Lang.getString("gui.destroyed.respawn"), 16,
                () -> {
                    Vector2f position = renderer.camera.getPosition();
                    Client.get().sendTCPPacket(new PacketRespawn(position.x, position.y));
                    Client.get().closeGui();
                }).atBottom(buttonsOffset, 24));

        rectangle.add(new Button(TextureRegister.guiButtonBase, buttonWidth, buttonHeight, Lang.getString("gui.ingamemenu.tomainmenu"), 16,
                () -> Client.get().quitToMainMenu()).atBottom(-buttonsOffset, 24));
        rectangle.add(new Label(Font.XOLONIUM_FT, Lang.getString("gui.destroyed.shipWasDestroyed"), 20).atTopLeft(14, -20));
        rectangle.add(new Label(Font.CONSOLA_FT, Lang.getString("gui.destroyed.destroyedBy") + ": " + destroyedBy, 16)
                .atTopLeft(14, -60));
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (ClientSettings.IS_DEBUG.getBoolean() && keyboard.isKeyDown(KEY_LEFT_CONTROL) && key == KEY_C) {
            Client.get().closeGui();
        }

        return input;
    }
}
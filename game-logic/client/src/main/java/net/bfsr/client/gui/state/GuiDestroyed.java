package net.bfsr.client.gui.state;

import net.bfsr.client.Core;
import net.bfsr.client.language.Lang;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.gui.object.TexturedGuiObject;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.network.packet.client.PacketRespawn;
import org.joml.Vector2f;

import static net.bfsr.engine.input.Keys.KEY_C;
import static net.bfsr.engine.input.Keys.KEY_LEFT_CONTROL;

public class GuiDestroyed extends Gui {
    private final String destroyedBy;
    private final AbstractKeyboard keyboard = Engine.keyboard;

    public GuiDestroyed(String destroyedBy) {
        this.destroyedBy = destroyedBy;
    }

    @Override
    protected void initElements() {
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiAdd).atCenter(-300, -139).setSize(600, 278));

        int buttonWidth = 220;
        int buttonHeight = 40;
        int buttonsOffset = 160;
        registerGuiObject(
                new Button(TextureRegister.guiButtonBase, buttonWidth, buttonHeight, Lang.getString("gui.destroyed.respawn"), 16,
                        () -> {
                            Vector2f position = renderer.camera.getPosition();
                            Core.get().sendTCPPacket(new PacketRespawn(position.x, position.y));
                            Core.get().closeGui();
                        }).atCenter(buttonsOffset - buttonWidth / 2, 72));

        registerGuiObject(
                new Button(TextureRegister.guiButtonBase, buttonWidth, buttonHeight, Lang.getString("gui.ingamemenu.tomainmenu"),
                        16, () -> Core.get().quitToMainMenu()).atCenter(-buttonsOffset - buttonWidth / 2, 72));
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.destroyed.shipWasDestroyed"), 20).compile()
                .atCenter(-286, -104));
        registerGuiObject(
                new StringObject(FontType.CONSOLA, Lang.getString("gui.destroyed.destroyedBy") + ": " + destroyedBy, 16).compile()
                        .atCenter(-286, -64));
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (ClientSettings.IS_DEBUG.getBoolean() && keyboard.isKeyDown(KEY_LEFT_CONTROL) && key == KEY_C) {
            Core.get().closeGui();
        }

        return input;
    }
}
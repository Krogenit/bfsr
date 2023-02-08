package net.bfsr.client.gui;

import net.bfsr.client.gui.button.Button;
import net.bfsr.client.language.Lang;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.string.StringObject;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.core.Core;
import net.bfsr.network.packet.client.PacketRespawn;
import org.joml.Vector2f;

public class GuiDestroyed extends Gui {
    private final String destroyedBy;

    public GuiDestroyed(String destroyedBy) {
        this.destroyedBy = destroyedBy;
    }

    @Override
    protected void initElements() {
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiAdd).atCenter(-300, -139).setSize(600, 278));

        int buttonWidth = 220;
        int buttonHeight = 40;
        int buttonsOffset = 160;
        registerGuiObject(new Button(TextureRegister.guiButtonBase, buttonWidth, buttonHeight, Lang.getString("gui.destroyed.respawn"), 16, () -> {
            Vector2f position = Core.getCore().getRenderer().getCamera().getPosition();
            Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketRespawn(position.x, position.y));
            Core.getCore().setCurrentGui(null);
        }).atCenter(buttonsOffset - buttonWidth / 2, 72));

        registerGuiObject(new Button(TextureRegister.guiButtonBase, buttonWidth, buttonHeight, Lang.getString("gui.ingamemenu.tomainmenu"),
                16, () -> Core.getCore().quitToMainMenu()).atCenter(-buttonsOffset - buttonWidth / 2, 72));
        registerGuiObject(new StringObject(FontType.XOLONIUM, Lang.getString("gui.destroyed.shipWasDestroyed"), 20).compile().atCenter(-286, -104));
        registerGuiObject(new StringObject(FontType.CONSOLA, Lang.getString("gui.destroyed.destroyedBy") + ": " + destroyedBy, 16).compile().atCenter(-286, -64));
    }
}

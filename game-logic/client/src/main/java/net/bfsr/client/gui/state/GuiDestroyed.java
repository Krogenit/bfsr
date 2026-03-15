package net.bfsr.client.gui.state;

import net.bfsr.client.Client;
import net.bfsr.client.gui.GuiStyle;
import net.bfsr.client.gui.objects.SimpleButton;
import net.bfsr.client.language.LanguageManager;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.network.packet.client.PacketRespawn;
import org.joml.Vector2f;

import static net.bfsr.engine.input.Keys.KEY_C;
import static net.bfsr.engine.input.Keys.KEY_LEFT_CONTROL;

public class GuiDestroyed extends Gui {
    private final Client client = Client.get();

    public GuiDestroyed(String destroyedBy) {
        LanguageManager languageManager = client.getLanguageManager();

        Rectangle rectangle = new Rectangle(600, 278);
        add(GuiStyle.setupTransparentRectangle(rectangle).atCenter(0, 0));

        int buttonWidth = 220;
        int buttonHeight = 40;
        int buttonsOffset = 160;
        rectangle.add(new SimpleButton(buttonWidth, buttonHeight, languageManager.getString("gui.destroyed.respawn"), 16,
                (mouseX, mouseY) -> {
                    Vector2f position = renderer.getCamera().getPosition();
                    client.sendTCPPacket(new PacketRespawn(position.x, position.y));
                    client.closeGui();
                }).atBottom(buttonsOffset, 24));

        rectangle.add(new SimpleButton(buttonWidth, buttonHeight, languageManager.getString("gui.ingamemenu.tomainmenu"), 16,
                (mouseX, mouseY) -> client.quitToMainMenu()).atBottom(-buttonsOffset, 24));
        Font font = Engine.getFontManager().getDefaultFont();
        Rectangle header = new Rectangle(600, 50);
        header.add(new Label(font, languageManager.getString("gui.destroyed.shipWasDestroyed"), 20).atTopLeft(14, -16));
        rectangle.add(GuiStyle.setupTransparentRectangle(header).atTop(0, 0));
        rectangle.add(new Label(font, languageManager.getString("gui.destroyed.destroyedBy") + ": " + destroyedBy, 16).atTopLeft(14, -60));
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (ClientSettings.IS_DEBUG.getBoolean() && Engine.getKeyboard().isKeyDown(KEY_LEFT_CONTROL) && key == KEY_C) {
            client.closeGui();
        }

        return input;
    }
}
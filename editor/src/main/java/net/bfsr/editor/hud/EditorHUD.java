package net.bfsr.editor.hud;

import net.bfsr.client.Client;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.particle.GuiParticleEditor;
import net.bfsr.editor.gui.ship.GuiShipEditor;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.renderer.font.string.StringOffsetType;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.client.PacketCommand;

import static net.bfsr.editor.gui.EditorTheme.CONTEXT_MENU_BUTTON_HEIGHT;
import static net.bfsr.editor.gui.EditorTheme.CONTEXT_MENU_STRING_OFFSET_X;
import static net.bfsr.editor.gui.EditorTheme.FONT;
import static net.bfsr.editor.gui.EditorTheme.FONT_SIZE;
import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButton;

public class EditorHUD extends HUD {
    private final Rectangle commandsRectangle = new Rectangle(100, 100);

    public EditorHUD() {
        int buttonWidth = 240;
        int buttonHeight = 36;
        int y = 0;
        add(EditorTheme.setupButton(new Button(buttonWidth, buttonHeight, "Particle Editor", 22,
                (mouseX, mouseY) -> Client.get().openGui(new GuiParticleEditor()))).atLeft(0, y));
        y += buttonHeight;
        add(EditorTheme.setupButton(new Button(buttonWidth, buttonHeight, "Ship Editor", 22,
                (mouseX, mouseY) -> Client.get().openGui(new GuiShipEditor()))).atLeft(0, y));

        commandsRectangle.atTopRight(-otherShipOverlay.getWidth(), 0);
        commandsRectangle.setAllColors(0.2f, 0.2f, 0.2f, 0.75f);
        String title = "Destroy ship";
        Button destroyShipButton = new Button(commandsRectangle.getWidth(), CONTEXT_MENU_BUTTON_HEIGHT, title, FONT, FONT_SIZE,
                CONTEXT_MENU_STRING_OFFSET_X / 2, 0, StringOffsetType.DEFAULT,
                (mouseX1, mouseY1) -> Client.get().sendTCPPacket(PacketCommand.destroyShip(otherShipOverlay.getShip().getId())));
        commandsRectangle.add(setupContextMenuButton(destroyShipButton).atTopLeft(0, 0));
    }

    @Override
    public void selectShipSecondary(Ship ship) {
        super.selectShipSecondary(ship);

        if (ship != null) {
            addIfAbsent(commandsRectangle);
        } else {
            remove(commandsRectangle);
        }
    }
}
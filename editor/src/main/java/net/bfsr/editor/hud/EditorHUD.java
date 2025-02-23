package net.bfsr.editor.hud;

import net.bfsr.client.Client;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.command.Command;
import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.editor.gui.particle.GuiParticleEditor;
import net.bfsr.editor.gui.ship.GuiShipEditor;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.renderer.font.string.StringOffsetType;
import net.bfsr.entity.ship.Ship;

import java.util.Locale;

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

        commandsRectangle.atTopRight(-otherShipOverlay.getWidth(), -20);
        commandsRectangle.setAllColors(0.2f, 0.2f, 0.2f, 0.75f);
        addShipCommandButtons(commandsRectangle);
    }

    private void addShipCommandButtons(GuiObject guiObject) {
        Command[] commands = Command.values();
        int y = 0;
        for (int i = 0; i < commands.length; i++) {
            Command command = commands[i];
            if (command.isShipCommand()) {
                String title = command.name().toLowerCase(Locale.getDefault()).replace("_", " ");
                Button destroyShipButton = new Button(guiObject.getWidth(), CONTEXT_MENU_BUTTON_HEIGHT, title, FONT, FONT_SIZE,
                        CONTEXT_MENU_STRING_OFFSET_X / 2, 0, StringOffsetType.DEFAULT,
                        (mouseX1, mouseY1) -> {
                            Ship ship = otherShipOverlay.getShip();
                            if (ship != null) {
                                Client.get().sendTCPPacket(command.createShipPacketCommand(command, ship));
                            }
                        });
                guiObject.add(setupContextMenuButton(destroyShipButton).atTopLeft(0, y));
                y -= CONTEXT_MENU_BUTTON_HEIGHT;
            }
        }
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
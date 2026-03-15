package net.bfsr.client.gui.connect;

import net.bfsr.client.Client;
import net.bfsr.client.gui.GuiStyle;
import net.bfsr.client.gui.objects.SimpleButton;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.renderer.font.glyph.Font;

public class GuiDisconnected extends Gui {
    public GuiDisconnected(Gui parentGui, String errorMessage, String description) {
        super(parentGui);

        Rectangle rectangle = new Rectangle(360, 168);
        add(GuiStyle.setupTransparentRectangle(rectangle).atCenter(0, 0));
        rectangle.add(new SimpleButton(180, 30, "gui.ok", 14,
                (mouseX, mouseY) -> Client.get().openGui(parentGui)).atBottom(0, 8));
        Font font = Engine.getFontManager().getDefaultFont();
        Rectangle header = new Rectangle(360, 34);
        header.add(new Label(font, errorMessage, 16).atTopLeft(6, -10));
        rectangle.add(GuiStyle.setupTransparentRectangle(header).atTop(0, 0));
        rectangle.add(new Label(font, description, 16).setMaxWidth(342).atTopLeft(6, -44));
    }
}
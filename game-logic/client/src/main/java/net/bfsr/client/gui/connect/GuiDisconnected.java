package net.bfsr.client.gui.connect;

import net.bfsr.client.Client;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class GuiDisconnected extends Gui {
    public GuiDisconnected(Gui parentGui, String errorMessage, String description) {
        super(parentGui);

        TexturedRectangle rectangle = new TexturedRectangle(TextureRegister.guiAdd);
        add(rectangle.atCenter(0, 0).setSize(360, 168));
        rectangle.add(new Button(TextureRegister.guiButtonBase, 180, 30, "gui.ok", 14, () -> Client.get().openGui(parentGui))
                .atBottom(0, 8));
        rectangle.add(new Label(Font.XOLONIUM_FT, errorMessage, 16).atTopLeft(6, -10));
        rectangle.add(new Label(Font.XOLONIUM_FT, description, 16).setMaxWidth(342).atTopLeft(6, -36));
    }
}
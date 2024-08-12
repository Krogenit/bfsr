package net.bfsr.client.gui.connect;

import net.bfsr.client.Core;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class GuiDisconnected extends Gui {
    public GuiDisconnected(Gui parentGui, String errorMessage, String description) {
        super(parentGui);

        add(new TexturedRectangle(TextureRegister.guiAdd).atCenter(-180, -168 / 2).setSize(360, 168));
        add(new Button(TextureRegister.guiButtonBase, 180, 30, "gui.ok", 14, () -> Core.get().openGui(parentGui))
                .atCenter(-90, 46));
        add(new Label(Font.XOLONIUM_FT, errorMessage, 16).atCenter(-172, -72));
        add(new Label(Font.XOLONIUM_FT, description, 16).setMaxWidth(342).atCenter(-172, -44));
    }
}
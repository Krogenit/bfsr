package net.bfsr.client.gui.connect;

import net.bfsr.client.Core;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.gui.object.TexturedGuiObject;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class GuiDisconnected extends Gui {
    private final String errorMessage;
    private final String description;

    public GuiDisconnected(Gui parentGui, String errorMessage, String description) {
        super(parentGui);
        this.errorMessage = errorMessage;
        this.description = description;
    }

    @Override
    protected void initElements() {
        registerGuiObject(new TexturedGuiObject(TextureRegister.guiAdd).atCenter(-180, -168 / 2).setSize(360, 168));
        registerGuiObject(
                new Button(TextureRegister.guiButtonBase, 180, 30, "gui.ok", 14, () -> Core.get().openGui(parentGui)).atCenter(
                        -90, 46));
        registerGuiObject(new StringObject(FontType.XOLONIUM, errorMessage, 16).compile().atCenter(-172, -62));
        registerGuiObject(new StringObject(FontType.XOLONIUM, description, 16).compile().atCenter(-172, -34));
    }
}
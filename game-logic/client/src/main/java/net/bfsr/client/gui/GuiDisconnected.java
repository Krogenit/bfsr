package net.bfsr.client.gui;

import net.bfsr.client.Core;
import net.bfsr.client.font.StringObject;
import net.bfsr.client.gui.button.Button;
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
        registerGuiObject(new Button(TextureRegister.guiButtonBase, 180, 30, "gui.ok", 14, () -> Core.get().setCurrentGui(parentGui)).atCenter(-90, 46));
        registerGuiObject(new StringObject(FontType.XOLONIUM, errorMessage, 16).compile().atCenter(-172, -62));
        registerGuiObject(new StringObject(FontType.XOLONIUM, description, 16).compile().atCenter(-172, -34));
    }
}
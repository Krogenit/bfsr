package net.bfsr.client.gui.button;

import net.bfsr.client.texture.TextureRegister;
import net.bfsr.math.Transformation;
import org.joml.Vector2f;

public class ButtonBase extends Button {

    public ButtonBase(int id, Vector2f pos, Vector2f scale, String text, Vector2f fontSize) {
        super(id, TextureRegister.guiButtonBase, Transformation.getOffsetByScale(pos), scale, text, fontSize);
    }

    public ButtonBase(int id, Vector2f pos, String text) {
        super(id, TextureRegister.guiButtonBase, Transformation.getOffsetByScale(pos), new Vector2f(300, 50), text);
    }

}

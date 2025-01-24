package net.bfsr.engine.gui.renderer.inputbox;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class TexturedInputBoxRenderer extends InputBoxRenderer {
    private final AbstractTexture texture;

    public TexturedInputBoxRenderer(InputBox inputBox, TextureRegister textureRegister) {
        super(inputBox);
        this.texture = Engine.getAssetsManager().getTexture(textureRegister);
    }

    @Override
    protected void createBody() {
        idList.add(id = guiRenderer.add(guiObject.getSceneX(), guiObject.getSceneY(), guiObject.getWidth(), guiObject.getHeight(),
                guiObject.getColor(), texture));
    }
}

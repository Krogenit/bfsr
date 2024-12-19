package net.bfsr.engine.gui.renderer;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;

@Getter
public class RectangleTexturedRenderer extends SimpleGuiObjectRenderer {
    protected final AbstractTexture texture;

    public RectangleTexturedRenderer(GuiObject guiObject, TextureRegister textureRegister) {
        this(guiObject, Engine.assetsManager.getTexture(textureRegister));
    }

    public RectangleTexturedRenderer(GuiObject guiObject, AbstractTexture texture) {
        super(guiObject);
        this.texture = texture;
    }

    @Override
    protected void create() {
        idList.add(id = guiRenderer.add(guiObject.getSceneX(), guiObject.getSceneY(), guiObject.getWidth(), guiObject.getHeight(),
                guiObject.getColor(), texture));
    }
}

package net.bfsr.engine.gui.renderer;

import lombok.Getter;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.texture.AbstractTexture;

@Getter
public class RectangleTexturedRenderer extends SimpleGuiObjectRenderer {
    protected final AbstractTexture texture;

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

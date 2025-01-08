package net.bfsr.editor.gui.component;

import lombok.Getter;
import net.bfsr.engine.gui.component.MinimizableGuiObject;
import net.bfsr.engine.renderer.font.Font;

@Getter
public class MinimizableHolder<P> extends MinimizableGuiObject {
    protected final P object;

    public MinimizableHolder(int width, int height, String name, Font font, int fontSize, int stringOffsetY, P object) {
        super(width, height, name, font, fontSize, stringOffsetY);
        this.object = object;
    }
}
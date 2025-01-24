package net.bfsr.editor.gui.component;

import lombok.Getter;
import net.bfsr.engine.gui.component.MinimizableGuiObject;

@Getter
public class MinimizableHolder<P> extends MinimizableGuiObject {
    protected final P object;

    public MinimizableHolder(int width, int height, String name, String fontName, int fontSize, int stringOffsetY, P object) {
        super(width, height, name, fontName, fontSize, stringOffsetY);
        this.object = object;
    }
}
package net.bfsr.editor.gui.component;

import lombok.Getter;
import net.bfsr.engine.renderer.font.FontType;

@Getter
public class MinimizableHolder<P> extends MinimizableGuiObject {
    protected final P object;

    public MinimizableHolder(int width, int height, String name, FontType fontType, int fontSize, int stringYOffset, P object) {
        super(width, height, name, fontType, fontSize, stringYOffset);
        this.object = object;
    }
}
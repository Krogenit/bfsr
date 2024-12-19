package net.bfsr.engine.gui.renderer.inputbox;

import net.bfsr.engine.gui.component.InputBox;

public class EmptyInputRenderer extends InputBoxRenderer {
    public EmptyInputRenderer(InputBox inputBox) {
        super(inputBox);
    }

    @Override
    protected void renderBody() {}
}

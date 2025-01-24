package net.bfsr.engine.gui.component;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.gui.renderer.CheckBoxRenderer;

@Getter
@Setter
public class CheckBox extends GuiObject {
    private boolean checked;

    public CheckBox(int width, int height, boolean checked) {
        super(width, height);
        this.checked = checked;
        setRenderer(new CheckBoxRenderer(this));
        setLeftClickConsumer((mouseX, mouseY) -> this.checked = !this.checked);
    }
}
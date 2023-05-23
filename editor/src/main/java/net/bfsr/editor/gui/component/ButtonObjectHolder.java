package net.bfsr.editor.gui.component;

import lombok.Getter;
import net.bfsr.client.gui.button.Button;
import net.bfsr.engine.renderer.font.FontType;

@Getter
public class ButtonObjectHolder<V> extends Button {
    private final V value;

    public ButtonObjectHolder(V value, int width, int height, String string, FontType fontType, int fontSize, int stringYOffset, Runnable onMouseClickedRunnable) {
        super(null, 0, 0, width, height, string, fontType, fontSize, stringYOffset, onMouseClickedRunnable);
        this.value = value;
    }
}
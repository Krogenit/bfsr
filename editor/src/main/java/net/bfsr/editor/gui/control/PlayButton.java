package net.bfsr.editor.gui.control;

import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;

import static net.bfsr.editor.gui.EditorTheme.setupButton;

public class PlayButton extends Button {
    public PlayButton(Playble playble, int width, int height) {
        super(width, height, () -> playble.setPlaying(!playble.isPlaying()));
        setupButton(this).setRenderer(new GuiObjectRenderer(this) {
            @Override
            public void render(int lastX, int lastY, int x, int y, int width, int height) {
                int centerX = x + width / 2;
                int centerY = y + height / 2;
                int xOffset = 8;
                int yOffset = 8;
                float r, g, b;
                if (isMouseHover()) {
                    guiRenderer.add(x, y, width, height, outlineColor.x, outlineHoverColor.y, outlineHoverColor.z, outlineHoverColor.w);
                    if (playble.isPlaying()) {
                        float scale = 1.2f;
                        guiRenderer.add(x + 1, y + 1, width - 2, height - 2, 35 / 255.0f * scale, 74 / 255.0f * scale,
                                108 / 255.0f * scale, color.w);
                    } else {
                        guiRenderer.add(x + 1, y + 1, width - 2, height - 2, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
                    }

                    r = g = b = 210 / 255.0f;
                } else {
                    guiRenderer.add(x, y, width, height, outlineColor.x, outlineColor.y, outlineColor.z, outlineColor.w);
                    if (playble.isPlaying()) {
                        guiRenderer.add(x + 1, y + 1, width - 2, height - 2, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
                    } else {
                        guiRenderer.add(x + 1, y + 1, width - 2, height - 2, color.x, color.y, color.z, color.w);
                    }

                    r = g = b = 192 / 255.0f;
                }

                guiRenderer.addPrimitive(centerX - xOffset, centerY - yOffset, centerX - xOffset, centerY + yOffset,
                        centerX + xOffset, centerY, centerX - xOffset, centerY - yOffset, r, g, b, 1.0f, 0);
            }
        });
    }
}
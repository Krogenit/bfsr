package net.bfsr.editor.gui.control;

import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;

import static net.bfsr.editor.gui.EditorTheme.setupButton;

public class PauseButton extends Button {
    public PauseButton(Pausable pausable, int width, int height) {
        super(width, height, () -> pausable.setPause(!pausable.isPaused()));
        setupButton(this).setRenderer(new GuiObjectRenderer(this) {
            @Override
            public void render(int lastX, int lastY, int x, int y, int width, int height) {
                int centerX = x + width / 2;
                int centerY = y + height / 2;
                int xOffset = 3;
                int yOffset = 10;
                int lineWidth = 4;
                float r, g, b;
                if (isMouseHover()) {
                    guiRenderer.add(x, y, width, height, outlineColor.x, outlineHoverColor.y, outlineHoverColor.z, outlineHoverColor.w);
                    if (pausable.isPaused()) {
                        float scale = 1.2f;
                        guiRenderer.add(x + 1, y + 1, width - 2, height - 2, 35 / 255.0f * scale, 74 / 255.0f * scale,
                                108 / 255.0f * scale, color.w);
                    } else {
                        guiRenderer.add(x + 1, y + 1, width - 2, height - 2, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
                    }

                    r = g = b = 210 / 255.0f;
                } else {
                    guiRenderer.add(x, y, width, height, outlineColor.x, outlineColor.y, outlineColor.z, outlineColor.w);
                    if (pausable.isPaused()) {
                        guiRenderer.add(x + 1, y + 1, width - 2, height - 2, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
                    } else {
                        guiRenderer.add(x + 1, y + 1, width - 2, height - 2, color.x, color.y, color.z, color.w);
                    }

                    r = g = b = 192 / 255.0f;
                }

                guiRenderer.add(centerX - xOffset - lineWidth, centerY - yOffset, lineWidth, yOffset << 1, r, g, b, 1.0f);
                guiRenderer.add(centerX + xOffset, centerY - yOffset, lineWidth, yOffset << 1, r, g, b, 1.0f);
            }
        });
    }
}
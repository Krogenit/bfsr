package net.bfsr.editor.gui.control;

import net.bfsr.client.gui.button.Button;
import net.bfsr.engine.Engine;

public class PauseButton extends Button {
    private final Pausable pausable;

    public PauseButton(Pausable pausable, int width, int height) {
        super(width, height, () -> pausable.setPause(!pausable.isPaused()));
        this.pausable = pausable;
    }

    @Override
    public void render() {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int xOffset = 3;
        int yOffset = 10;
        int lineWidth = 4;
        float r, g, b, a;
        if (isMouseHover()) {
            Engine.renderer.guiRenderer.add(x, y, width, height, outlineColor.x, outlineHoverColor.y, outlineHoverColor.z, outlineHoverColor.w);
            if (pausable.isPaused()) {
                float scale = 1.2f;
                Engine.renderer.guiRenderer.add(x + 1, y + 1, width - 2, height - 2, 35 / 255.0f * scale, 74 / 255.0f * scale, 108 / 255.0f * scale, color.w);
                r = g = b = 210 / 255.0f;
                a = 1.0f;
            } else {
                Engine.renderer.guiRenderer.add(x + 1, y + 1, width - 2, height - 2, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
                r = g = b = 210 / 255.0f;
                a = 1.0f;
            }
        } else {
            Engine.renderer.guiRenderer.add(x, y, width, height, outlineColor.x, outlineColor.y, outlineColor.z, outlineColor.w);
            if (pausable.isPaused()) {
                Engine.renderer.guiRenderer.add(x + 1, y + 1, width - 2, height - 2, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
                r = g = b = 192 / 255.0f;
                a = 1.0f;
            } else {
                Engine.renderer.guiRenderer.add(x + 1, y + 1, width - 2, height - 2, color.x, color.y, color.z, color.w);
                r = g = b = 192 / 255.0f;
                a = 1.0f;
            }
        }

        Engine.renderer.guiRenderer.add(centerX - xOffset - lineWidth, centerY - yOffset, lineWidth, yOffset << 1, r, g, b, a);
        Engine.renderer.guiRenderer.add(centerX + xOffset, centerY - yOffset, lineWidth, yOffset << 1, r, g, b, a);
    }
}
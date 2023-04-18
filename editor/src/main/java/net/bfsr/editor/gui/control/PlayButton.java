package net.bfsr.editor.gui.control;

import net.bfsr.client.gui.button.Button;
import net.bfsr.client.renderer.gui.GUIRenderer;

public class PlayButton extends Button {
    private final Playble playble;

    public PlayButton(Playble playble, int width, int height) {
        super(width, height, () -> playble.setPlaying(!playble.isPlaying()));
        this.playble = playble;
    }

    @Override
    public void render() {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int xOffset = 8;
        int yOffset = 8;
        float r, g, b, a;
        if (isMouseHover()) {
            GUIRenderer.get().add(x, y, width, height, outlineColor.x, outlineHoverColor.y, outlineHoverColor.z, outlineHoverColor.w);
            if (playble.isPlaying()) {
                float scale = 1.2f;
                GUIRenderer.get().add(x + 1, y + 1, width - 2, height - 2, 35 / 255.0f * scale, 74 / 255.0f * scale, 108 / 255.0f * scale, color.w);
                r = g = b = 210 / 255.0f;
                a = 1.0f;
            } else {
                GUIRenderer.get().add(x + 1, y + 1, width - 2, height - 2, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
                r = g = b = 210 / 255.0f;
                a = 1.0f;
            }

        } else {
            GUIRenderer.get().add(x, y, width, height, outlineColor.x, outlineColor.y, outlineColor.z, outlineColor.w);
            if (playble.isPlaying()) {
                GUIRenderer.get().add(x + 1, y + 1, width - 2, height - 2, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
                r = g = b = 192 / 255.0f;
                a = 1.0f;
            } else {
                GUIRenderer.get().add(x + 1, y + 1, width - 2, height - 2, color.x, color.y, color.z, color.w);
                r = g = b = 192 / 255.0f;
                a = 1.0f;
            }
        }

        GUIRenderer.get().addPrimitive(centerX - xOffset, centerY - yOffset, centerX - xOffset, centerY + yOffset, centerX + xOffset, centerY, centerX - xOffset, centerY - yOffset,
                r, g, b, a, 0);
    }
}
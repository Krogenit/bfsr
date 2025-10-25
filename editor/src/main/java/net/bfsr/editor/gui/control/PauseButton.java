package net.bfsr.editor.gui.control;

import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;

import static net.bfsr.editor.gui.EditorTheme.setupButton;

public class PauseButton extends Button {
    private final Pausable pausable;

    public PauseButton(Pausable pausable, int width, int height) {
        super(width, height);
        this.pausable = pausable;
        setLeftClickConsumer((mouseX, mouseY) -> {
            pausable.setPause(!pausable.isPaused());
            ((PauseButtonRenderer) getRenderer()).setPause(pausable.isPaused());
        });
        setupButton(this).setRenderer(new PauseButtonRenderer(this));
    }

    private static class PauseButtonRenderer extends GuiObjectRenderer {
        private final PauseButton pauseButton;
        private int bodyId, outlineId, pauseId1, pauseId2;

        PauseButtonRenderer(PauseButton pauseButton) {
            super(pauseButton);
            this.pauseButton = pauseButton;
        }

        @Override
        public void create() {
            int x = guiObject.getSceneX();
            int y = guiObject.getSceneY();
            int width = guiObject.getWidth();
            int height = guiObject.getHeight();
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            int xOffset = 3;
            int yOffset = 10;
            int lineWidth = 4;

            idList.add(outlineId = guiRenderer.add(x, y, width, height, outlineColor));
            idList.add(bodyId = guiRenderer.add(x + 1, y + 1, width - 2, height - 2, color));

            float color = 192 / 255.0f;
            idList.add(pauseId1 = guiRenderer.add(centerX - xOffset - lineWidth, centerY - yOffset, lineWidth, yOffset << 1, color,
                    color, color, 1.0f));
            idList.add(pauseId2 = guiRenderer.add(centerX + xOffset, centerY - yOffset, lineWidth, yOffset << 1, color, color, color,
                    1.0f));
        }

        void setPause(boolean paused) {
            if (paused) {
                if (guiObject.isMouseHover()) {
                    float scale = 1.2f;
                    guiRenderer.setColor(bodyId, 35 / 255.0f * scale, 74 / 255.0f * scale, 108 / 255.0f * scale, color.w);
                } else {
                    guiRenderer.setColor(bodyId, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
                }
            } else {
                if (guiObject.isMouseHover()) {
                    guiRenderer.setColor(bodyId, hoverColor);
                } else {
                    guiRenderer.setColor(bodyId, color);
                }
            }
        }

        @Override
        public void onMouseHover() {
            guiRenderer.setColor(outlineId, outlineHoverColor);
            if (pauseButton.pausable.isPaused()) {
                float scale = 1.2f;
                guiRenderer.setColor(bodyId, 35 / 255.0f * scale, 74 / 255.0f * scale, 108 / 255.0f * scale, color.w);
            } else {
                guiRenderer.setColor(bodyId, hoverColor);
            }

            float color = 210 / 255.0f;
            guiRenderer.setColor(pauseId1, color, color, color, 1.0f);
            guiRenderer.setColor(pauseId2, color, color, color, 1.0f);
        }

        @Override
        public void onMouseStopHover() {
            guiRenderer.setColor(outlineId, outlineColor);
            if (pauseButton.pausable.isPaused()) {
                guiRenderer.setColor(bodyId, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
            } else {
                guiRenderer.setColor(bodyId, color);
            }

            float color = 192 / 255.0f;
            guiRenderer.setColor(pauseId1, color, color, color, 1.0f);
            guiRenderer.setColor(pauseId2, color, color, color, 1.0f);
        }

        @Override
        public void updateLastValues() {
            super.updateLastValues();
            int x = guiObject.getSceneX();
            int y = guiObject.getSceneY();
            int width = guiObject.getWidth();
            int height = guiObject.getHeight();
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            int xOffset = 3;
            int yOffset = 10;
            int lineWidth = 4;

            guiRenderer.setLastPosition(outlineId, x, y);
            guiRenderer.setLastPosition(bodyId, x + 1, y + 1);
            guiRenderer.setLastPosition(pauseId1, centerX - xOffset - lineWidth, centerY - yOffset);
            guiRenderer.setLastPosition(pauseId2, centerX + xOffset, centerY - yOffset);
        }

        @Override
        public void updatePosition() {
            int x = guiObject.getSceneX();
            int y = guiObject.getSceneY();
            int width = guiObject.getWidth();
            int height = guiObject.getHeight();
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            int xOffset = 3;
            int yOffset = 10;
            int lineWidth = 4;

            guiRenderer.setPosition(outlineId, x, y);
            guiRenderer.setPosition(bodyId, x + 1, y + 1);
            guiRenderer.setPosition(pauseId1, centerX - xOffset - lineWidth, centerY - yOffset);
            guiRenderer.setPosition(pauseId2, centerX + xOffset, centerY - yOffset);
        }

        @Override
        public void render(int mouseX, int mouseY) {
            guiRenderer.addDrawCommand(outlineId);
            guiRenderer.addDrawCommand(bodyId);
            guiRenderer.addDrawCommand(pauseId1);
            guiRenderer.addDrawCommand(pauseId2);
        }
    }
}
package net.bfsr.editor.gui.control;

import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;
import net.bfsr.engine.renderer.primitive.Primitive;

import static net.bfsr.editor.gui.EditorTheme.setupButton;

public class PlayButton extends Button {
    private final Playble playble;

    public PlayButton(Playble playble, int width, int height) {
        super(width, height);
        this.playble = playble;
        setLeftClickConsumer((mouseX, mouseY) -> {
            playble.setPlaying(!playble.isPlaying());
            ((PlayButtonRenderer) getRenderer()).setPlaying(playble.isPlaying());
        });
        setupButton(this).setRenderer(new PlayButtonRenderer(this));
    }

    private static class PlayButtonRenderer extends GuiObjectRenderer {
        private static final Primitive PLAY_SIGN_PRIMITIVE_PARAMS = new Primitive(-0.5f, 0.5f, 0.0f, 1.0f, -0.5f, -0.5f, 1.0f, 1.0f, 0.5f,
                0.0f, 1.0f, 0.0f, -0.5f, 0.5f, 0.0f, 0.0f);

        private final PlayButton playButton;
        private int outlineId, bodyId, playId;

        PlayButtonRenderer(PlayButton playButton) {
            super(playButton);
            this.playButton = playButton;
        }

        @Override
        public void create() {
            int x = guiObject.getSceneX();
            int y = guiObject.getSceneY();
            int width = guiObject.getWidth();
            int height = guiObject.getHeight();
            int centerX = x + width / 2;
            int centerY = y + height / 2;

            idList.add(outlineId = guiRenderer.add(x, y, width, height, outlineColor));
            idList.add(bodyId = guiRenderer.add(x + 1, y + 1, width - 2, height - 2, color.x, color.y, color.z, color.w));

            renderer.getSpriteRenderer().addPrimitive(PLAY_SIGN_PRIMITIVE_PARAMS);

            float color = 192 / 255.0f;
            idList.add(playId = guiRenderer.add(centerX, centerY, width - 10, height - 10, color, color, color, 1.0f));
        }

        void setPlaying(boolean playing) {
            if (playing) {
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
            if (playButton.playble.isPlaying()) {
                float scale = 1.2f;
                guiRenderer.setColor(bodyId, 35 / 255.0f * scale, 74 / 255.0f * scale, 108 / 255.0f * scale, color.w);
            } else {
                guiRenderer.setColor(bodyId, hoverColor);
            }

            float color = 210 / 255.0f;
            guiRenderer.setColor(playId, color, color, color, 1.0f);
        }

        @Override
        public void onMouseStopHover() {
            guiRenderer.setColor(outlineId, outlineColor);
            if (playButton.playble.isPlaying()) {
                guiRenderer.setColor(bodyId, 35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
            } else {
                guiRenderer.setColor(bodyId, color);
            }

            float color = 192 / 255.0f;
            guiRenderer.setColor(playId, color, color, color, 1.0f);
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
            guiRenderer.setLastPosition(outlineId, x, y);
            guiRenderer.setLastPosition(bodyId, x + 1, y + 1);
            guiRenderer.setLastPosition(playId, centerX, centerY);
        }

        @Override
        public void updatePosition() {
            int x = guiObject.getSceneX();
            int y = guiObject.getSceneY();
            int width = guiObject.getWidth();
            int height = guiObject.getHeight();
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            guiRenderer.setPosition(outlineId, x, y);
            guiRenderer.setPosition(bodyId, x + 1, y + 1);
            guiRenderer.setPosition(playId, centerX, centerY);
        }

        @Override
        public void render(int mouseX, int mouseY) {
            guiRenderer.addDrawCommand(outlineId);
            guiRenderer.addDrawCommand(bodyId);
            guiRenderer.addDrawCommand(playId, PLAY_SIGN_PRIMITIVE_PARAMS.getBaseVertex());
        }
    }
}
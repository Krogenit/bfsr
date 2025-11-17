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
                    activeColor.set(35 / 255.0f * scale, 74 / 255.0f * scale, 108 / 255.0f * scale, color.w);
                } else {
                    activeColor.set(35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
                }
            } else {
                if (guiObject.isMouseHover()) {
                    activeColor.set(hoverColor);
                } else {
                    activeColor.set(color);
                }
            }

            guiRenderer.setColor(bodyId, activeColor);
        }

        @Override
        public void onMouseHover() {
            activeOutlineColor = outlineHoverColor;
            guiRenderer.setColor(outlineId, activeOutlineColor);
            if (playButton.playble.isPlaying()) {
                float scale = 1.2f;
                activeColor.set(35 / 255.0f * scale, 74 / 255.0f * scale, 108 / 255.0f * scale, color.w);
            } else {
                activeColor.set(hoverColor);
            }

            guiRenderer.setColor(bodyId, activeColor);

            float color = 210 / 255.0f;
            guiRenderer.setColor(playId, color, color, color, 1.0f);
            guiRenderer.setLastColor(playId, color, color, color, 1.0f);
        }

        @Override
        public void onMouseStopHover() {
            activeOutlineColor = outlineColor;
            guiRenderer.setColor(outlineId, activeOutlineColor);
            if (playButton.playble.isPlaying()) {
                activeColor.set(35 / 255.0f, 74 / 255.0f, 108 / 255.0f, hoverColor.w);
            } else {
                activeColor.set(color);
            }

            guiRenderer.setColor(bodyId, activeColor);

            float color = 192 / 255.0f;
            guiRenderer.setColor(playId, color, color, color, 1.0f);
            guiRenderer.setLastColor(playId, color, color, color, 1.0f);
        }

        @Override
        protected void setLastUpdateValues() {
            super.setLastUpdateValues();
            int x = guiObject.getSceneX();
            int y = guiObject.getSceneY();
            int width = guiObject.getWidth();
            int height = guiObject.getHeight();
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            guiRenderer.setLastPosition(outlineId, x, y);
            guiRenderer.setLastPosition(bodyId, x + 1, y + 1);
            guiRenderer.setLastPosition(playId, centerX, centerY);
            guiRenderer.setLastColor(outlineId, activeOutlineColor);
            guiRenderer.setLastColor(bodyId, activeColor);
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
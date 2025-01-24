package net.bfsr.engine;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.dialog.AbstractSystemDialogs;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.font.AbstractFontManager;
import net.bfsr.engine.sound.AbstractSoundManager;

public final class Engine {
    @Getter
    @Setter
    private static AbstractRenderer renderer;
    @Getter
    @Setter
    private static AbstractFontManager fontManager;
    @Getter
    @Setter
    private static AbstractSoundManager soundManager;
    @Getter
    @Setter
    private static AssetsManager assetsManager;
    @Getter
    @Setter
    private static AbstractKeyboard keyboard;
    @Getter
    @Setter
    private static AbstractMouse mouse;
    @Getter
    @Setter
    private static AbstractSystemDialogs systemDialogs;
    @Getter
    @Setter
    private static GuiManager guiManager;

    private static final int UPDATES_PER_SECOND = 60;
    private static final float UPDATE_DELTA_TIME = 1.0f / 60.0f;
    private static final double TIME_BETWEEN_UPDATES_IN_NANOS = 1_000_000_000.0 / UPDATES_PER_SECOND;
    private static final int CLIENT_RENDER_DELAY_IN_MILLS = 100;

    public static int convertToTicks(int value) {
        return value * UPDATES_PER_SECOND;
    }

    public static int convertToTicks(float value) {
        return (int) (value * UPDATES_PER_SECOND);
    }

    public static float convertToDeltaTime(float value) {
        return value * UPDATE_DELTA_TIME;
    }

    public static float getUpdateDeltaTime() {
        return UPDATE_DELTA_TIME;
    }

    public static double getTimeBetweenUpdatesInNanos() {
        return TIME_BETWEEN_UPDATES_IN_NANOS;
    }

    public static int getClientRenderDelayInMills() {
        return CLIENT_RENDER_DELAY_IN_MILLS;
    }

    public static void clear() {
        renderer.clear();
        fontManager.clear();
        soundManager.clear();
        assetsManager.clear();
        keyboard.clear();
        mouse.clear();
    }
}
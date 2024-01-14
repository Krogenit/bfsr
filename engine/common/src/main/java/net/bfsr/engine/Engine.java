package net.bfsr.engine;

import net.bfsr.engine.dialog.AbstractSystemDialogs;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.sound.AbstractSoundManager;

public final class Engine {
    public static AbstractRenderer renderer;
    public static AbstractSoundManager soundManager;
    public static AssetsManager assetsManager;
    public static AbstractKeyboard keyboard;
    public static AbstractMouse mouse;
    public static AbstractSystemDialogs systemDialogs;

    private static final int updatesPerSecond = 60;
    private static final float updateDeltaTime = 1.0f / 60.0f;
    private static final double timeBetweenUpdates = 1_000_000_000.0 / updatesPerSecond;

    public static void clear() {
        renderer.clear();
        soundManager.cleanup();
    }

    public static int convertToTicks(int value) {
        return value * updatesPerSecond;
    }

    public static int convertToTicks(float value) {
        return (int) (value * updatesPerSecond);
    }

    public static float convertToDeltaTime(float value) {
        return value * updateDeltaTime;
    }

    public static int getUpdatesPerSecond() {
        return updatesPerSecond;
    }

    public static float getUpdateDeltaTime() {
        return updateDeltaTime;
    }

    public static double getTimeBetweenUpdates() {
        return timeBetweenUpdates;
    }
}
package net.bfsr.engine;

import net.bfsr.engine.dialog.AbstractSystemDialogs;
import net.bfsr.engine.input.AbstractInputHandler;
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

    private static boolean paused;

    public static void init(long window, int width, int height) {
        renderer.init(window, width, height);
        soundManager.init();
        assetsManager.init();
        mouse.init(window);
        keyboard.init(window);
    }

    public static void setInputHandler(AbstractInputHandler inputHandler) {
        mouse.setInputHandler(inputHandler);
        keyboard.setInputHandler(inputHandler);
    }

    public static void setPaused(boolean value) {
        paused = value;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static void stop() {
        renderer.closeWindow();
    }

    public static void clear() {
        renderer.clear();
        soundManager.cleanup();
    }
}
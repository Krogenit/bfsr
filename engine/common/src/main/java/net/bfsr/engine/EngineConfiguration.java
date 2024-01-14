package net.bfsr.engine;

import net.bfsr.engine.dialog.AbstractSystemDialogs;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.sound.AbstractSoundManager;

public interface EngineConfiguration {
    default void create() {
        Engine.assetsManager = createAssetManager();
        Engine.renderer = createRenderer();
        Engine.soundManager = createSoundManager();
        Engine.mouse = createMouse();
        Engine.keyboard = createKeyboard();
        Engine.systemDialogs = createSystemDialogs();
    }

    AbstractSystemDialogs createSystemDialogs();
    AbstractKeyboard createKeyboard();
    AbstractMouse createMouse();
    AbstractSoundManager createSoundManager();
    AbstractRenderer createRenderer();
    AssetsManager createAssetManager();
}
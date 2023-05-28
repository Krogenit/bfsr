package net.bfsr.engine;

import net.bfsr.engine.dialog.AbstractSystemDialogs;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.sound.AbstractSoundManager;
import net.bfsr.engine.util.Side;

public final class Engine {
    private static final GameLogic[] SIDED_GAME_LOGIC = new GameLogic[2];
    private static ClientGameLogic clientGameLogic;
    private static GameLogic serverGameLogic;
    public static AbstractRenderer renderer;
    public static AbstractSoundManager soundManager;
    public static AssetsManager assetsManager;
    public static AbstractKeyboard keyboard;
    public static AbstractMouse mouse;
    public static AbstractSystemDialogs systemDialogs;

    public static void setGameLogic(Side side, GameLogic gameLogic) {
        SIDED_GAME_LOGIC[side.ordinal()] = gameLogic;

        if (side.isClient()) {
            clientGameLogic = (ClientGameLogic) gameLogic;
        } else {
            serverGameLogic = gameLogic;
        }
    }

    public static GameLogic getGameLogic(Side side) {
        return SIDED_GAME_LOGIC[side.ordinal()];
    }

    public static GameLogic getClient() {
        return clientGameLogic;
    }

    public static GameLogic getServer() {
        return serverGameLogic;
    }

    public static void clear() {
        renderer.clear();
        soundManager.cleanup();
    }
}
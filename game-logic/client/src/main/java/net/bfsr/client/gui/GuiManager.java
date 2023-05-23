package net.bfsr.client.gui;

import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.gui.ingame.GuiInGame;

public class GuiManager {
    @Getter
    private Gui currentGui;
    @Getter
    private GuiInGame guiInGame;

    public void init(Gui startGui, GuiInGame guiInGame) {
        this.guiInGame = guiInGame;
        this.guiInGame.init();
        this.currentGui = startGui;
        this.currentGui.init();
    }

    public void update() {
        if (Core.get().getWorld() != null) {
            guiInGame.update();
        }

        if (currentGui != null) currentGui.update();
    }

    public void resize(int width, int height) {
        if (guiInGame != null) guiInGame.onScreenResize(width, height);
        if (currentGui != null) currentGui.onScreenResize(width, height);
    }

    public void setCurrentGui(Gui gui) {
        if (currentGui != null) currentGui.clear();
        currentGui = gui;
        if (currentGui != null) currentGui.init();
    }

    public boolean isActive() {
        return currentGui != null || guiInGame.isActive();
    }
}
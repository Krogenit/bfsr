package net.bfsr.client.gui;

import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.event.PlayerJoinGameEvent;
import net.bfsr.client.event.gui.*;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.client.gui.hud.HUDAdapter;
import net.bfsr.client.gui.hud.NoHUD;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.gui.Gui;
import org.jetbrains.annotations.NotNull;

public class GuiManager {
    private EventBus eventBus;
    @Getter
    private Gui gui;
    @Getter
    private HUDAdapter hud = NoHUD.get();

    public void init() {
        eventBus = Core.get().getEventBus();
        eventBus.register(this);
    }

    @EventHandler
    public EventListener<PlayerJoinGameEvent> playerJoinGameEvent() {
        return event -> showHUD(new HUD());
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> showHUD(NoHUD.get());
    }

    public void update() {
        hud.update();
        if (gui != null) gui.update();
    }

    public void resize(int width, int height) {
        hud.onScreenResize(width, height);
        if (gui != null) gui.onScreenResize(width, height);
    }

    public void render() {
        hud.render();

        if (gui != null) {
            gui.render();
        }
    }

    public void showHUD(HUDAdapter hud) {
        if (this.hud != NoHUD.get()) {
            eventBus.publish(new CloseHUDEvent(this.hud));
            this.hud.clear();
        }

        this.hud = hud;

        if (hud != NoHUD.get()) {
            hud.init();
            eventBus.publish(new ShowHUDEvent(hud));
        }
    }

    public void openGui(@NotNull Gui gui) {
        if (this.gui != null) {
            eventBus.publish(new CloseGuiEvent(this.gui));
            this.gui.clear();
        }

        this.gui = gui;
        this.gui.init();
        eventBus.publish(new OpenGuiEvent(gui));
    }

    public void closeGui() {
        if (gui != null) {
            eventBus.publish(new CloseGuiEvent(gui));
            gui.clear();
        }

        gui = null;
    }

    public boolean isActive() {
        return gui != null || hud.isActive();
    }

    public boolean noGui() {
        return gui == null;
    }

    public boolean hasGui() {
        return gui != null;
    }
}
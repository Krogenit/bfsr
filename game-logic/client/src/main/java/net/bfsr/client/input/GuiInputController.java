package net.bfsr.client.input;

import net.bfsr.client.Core;
import net.bfsr.client.event.gui.CloseGuiEvent;
import net.bfsr.client.event.gui.CloseHUDEvent;
import net.bfsr.client.event.gui.OpenGuiEvent;
import net.bfsr.client.event.gui.ShowHUDEvent;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.gui.Gui;

import java.util.ArrayDeque;
import java.util.Deque;

public class GuiInputController extends InputController {
    private final Deque<Gui> guiStack = new ArrayDeque<>();

    @Override
    public void init() {
        Core.get().getEventBus().register(this);
    }

    @EventHandler
    public EventListener<OpenGuiEvent> openGuiEvent() {
        return event -> guiStack.add(event.gui());
    }

    @EventHandler
    public EventListener<CloseGuiEvent> closeGuiEvent() {
        return event -> guiStack.remove(event.gui());
    }

    @EventHandler
    public EventListener<ShowHUDEvent> showHUDEvent() {
        return event -> guiStack.add(event.hud());
    }

    @EventHandler
    public EventListener<CloseHUDEvent> closeHUDEvent() {
        return event -> guiStack.remove(event.hud());
    }

    @Override
    public boolean input(int key) {
        if (guiStack.size() > 0) {
            return guiStack.getLast().input(key);
        }

        return false;
    }

    @Override
    public void textInput(int key) {
        if (guiStack.size() > 0) {
            guiStack.getLast().textInput(key);
        }
    }

    @Override
    public boolean scroll(float y) {
        if (guiStack.size() > 0) {
            return guiStack.getLast().onMouseScroll(y);
        }

        return false;
    }

    @Override
    public boolean onMouseLeftClick() {
        if (guiStack.size() > 0) {
            return guiStack.getLast().onMouseLeftClick();
        }

        return false;
    }

    @Override
    public boolean onMouseLeftRelease() {
        if (guiStack.size() > 0) {
            return guiStack.getLast().onMouseLeftRelease();
        }

        return false;
    }

    @Override
    public boolean onMouseRightClick() {
        if (guiStack.size() > 0) {
            return guiStack.getLast().onMouseRightClick();
        }

        return false;
    }

    @Override
    public boolean onMouseRightRelease() {
        if (guiStack.size() > 0) {
            return guiStack.getLast().onMouseRightRelease();
        }

        return false;
    }
}
package net.bfsr.engine.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.gui.CloseGuiEvent;
import net.bfsr.engine.event.gui.CloseHUDEvent;
import net.bfsr.engine.event.gui.OpenGuiEvent;
import net.bfsr.engine.event.gui.ShowHUDEvent;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.hud.HUDAdapter;
import net.bfsr.engine.gui.hud.NoHUD;
import net.bfsr.engine.input.AbstractMouse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class GuiManager {
    @Getter
    private final List<GuiObject> guiStack = new ArrayList<>(3);
    private final ContextMenu contextMenu = new ContextMenu();
    @Getter
    private GuiObject hoveredGuiObject;
    @Getter
    protected Gui gui = NoGui.get();
    @Getter
    protected HUDAdapter hud = NoHUD.get();
    private final EventBus eventBus;
    private final AbstractMouse mouse = Engine.getMouse();
    @Setter
    private GuiObject activeGuiObject;

    public void showHUD(HUDAdapter hud) {
        if (this.hud != NoHUD.get()) {
            closeHUD();
        }

        this.hud = hud;
        guiStack.add(0, hud);
        eventBus.publish(new ShowHUDEvent(hud));
        hud.addToScene();
    }

    public void closeHUD() {
        if (hud != NoHUD.get()) {
            eventBus.publish(new CloseHUDEvent(hud));
            guiStack.remove(0);
            hud.remove();
            hud = NoHUD.get();
        }
    }

    public void openGui(@NotNull Gui gui) {
        if (this.gui != NoGui.get()) {
            closeGui();
        }

        this.gui = gui;
        guiStack.add(gui);
        eventBus.publish(new OpenGuiEvent(gui));
        gui.addToScene();
    }

    public void closeGui() {
        if (gui != NoGui.get()) {
            eventBus.publish(new CloseGuiEvent(gui));
            guiStack.remove(gui);
            gui.remove();
            gui = NoGui.get();
        }
    }

    public void update() {
        Vector2i mousePosition = mouse.getGuiPosition();
        GuiObject hoveredObject = findHoveredGuiObject(mousePosition.x, mousePosition.y);

        if (hoveredObject != hoveredGuiObject) {
            if (hoveredGuiObject != null && hoveredGuiObject.isMouseHover()) {
                hoveredGuiObject.setMouseHover(false);
                hoveredGuiObject.onMouseStopHover();
            }

            hoveredGuiObject = hoveredObject;

            if (hoveredGuiObject != null) {
                hoveredGuiObject.setMouseHover(true);
                hoveredGuiObject.onMouseHover();
            }
        }

        for (int i = 0; i < guiStack.size(); i++) {
            guiStack.get(i).update(mousePosition.x, mousePosition.y);
        }
    }

    public void resize(int width, int height) {
        for (int i = 0; i < guiStack.size(); i++) {
            guiStack.get(i).onScreenResize(width, height);
        }
    }

    public void openContextMenu(GuiObject... objects) {
        contextMenu.add(objects);
        getLast().addIfAbsent(contextMenu);
    }

    @Nullable
    public GuiObject findHoveredGuiObject(int mouseX, int mouseY) {
        if (activeGuiObject != null) {
            return activeGuiObject;
        }

        GuiObject hoveredObject = null;
        for (int i = 0; i < guiStack.size(); i++) {
            hoveredObject = guiStack.get(i).getHovered(hoveredObject, mouseX, mouseY);
        }

        return hoveredObject;
    }

    public void forEach(Consumer<GuiObject> consumer) {
        for (int i = 0; i < guiStack.size(); i++) {
            consumer.accept(guiStack.get(i));
        }
    }

    public boolean noGui() {
        return gui == NoGui.get();
    }

    public boolean hasGui() {
        return gui != NoGui.get();
    }

    public boolean isActive() {
        return gui != NoGui.get() || hud.isActive();
    }

    public GuiObject getLast() {
        return guiStack.size() > 0 ? guiStack.get(guiStack.size() - 1) : NoGui.get();
    }

    public Vector2i getMousePosition() {
        return mouse.getGuiPosition();
    }

    public boolean isContextMenuOpen() {
        return contextMenu.isOpen();
    }
}

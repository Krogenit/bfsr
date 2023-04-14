package net.bfsr.client.gui;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GuiObjectWithSubObjects extends TexturedGuiObject {
    protected GuiObjectsHandler gui;
    @Getter
    protected final List<AbstractGuiObject> subObjects = new ArrayList<>();
    protected RepositionFunction subObjectsRepositionConsumer;

    @FunctionalInterface
    protected interface RepositionFunction {
        void setup(AbstractGuiObject guiObject, int offsetX, int offsetY);
    }

    public GuiObjectWithSubObjects(int width, int height) {
        super(null);
        setSize(width, height);
    }

    public void addSubObject(AbstractGuiObject object) {
        subObjects.add(object);
        if (gui != null) {
            gui.registerGuiObject(object);
        }
    }

    public void addSubObject(int index, AbstractGuiObject object) {
        subObjects.add(index, object);
        if (gui != null) {
            gui.registerGuiObject(object);
        }
    }

    public void removeSubObject(AbstractGuiObject object) {
        subObjects.remove(object);
        if (gui != null) {
            gui.unregisterGuiObject(object);
        }
    }

    @Override
    public void onRegistered(GuiObjectsHandler gui) {
        super.onRegistered(gui);
        this.gui = gui;
        registerSubElements(this.gui);
    }

    @Override
    public void onUnregistered(GuiObjectsHandler gui) {
        super.onUnregistered(gui);
        unregisterSubElements(gui);
    }

    protected void registerSubElements(GuiObjectsHandler gui) {
        for (int i = 0; i < subObjects.size(); i++) {
            gui.registerGuiObject(subObjects.get(i));
        }
    }

    protected void unregisterSubElements(GuiObjectsHandler gui) {
        for (int i = 0; i < subObjects.size(); i++) {
            gui.unregisterGuiObject(subObjects.get(i));
        }
    }

    @Override
    public void renderNoInterpolation() {}

    @Override
    public void render() {}

    @Override
    public AbstractGuiObject atTopLeftCorner(int x, int y) {
        subObjectsRepositionConsumer = (guiObject, offsetX, offsetY) -> guiObject.atTopLeftCorner(x + offsetX, y + offsetY);
        setRepositionConsumerForSubObjects();
        return super.atTopLeftCorner(x, y);
    }

    @Override
    public AbstractGuiObject atTopRightCorner(int x, int y) {
        subObjectsRepositionConsumer = (guiObject, offsetX, offsetY) -> guiObject.atTopRightCorner(x + offsetX, y + offsetY);
        setRepositionConsumerForSubObjects();
        return super.atTopRightCorner(x, y);
    }

    protected void setRepositionConsumerForSubObjects() {
        for (int i = 0; i < subObjects.size(); i++) {
            subObjectsRepositionConsumer.setup(subObjects.get(i), 0, 0);
        }
    }

    public void forEachSubObject(Consumer<AbstractGuiObject> consumer) {
        for (int i = 0; i < subObjects.size(); i++) {
            consumer.accept(subObjects.get(i));
        }
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        for (int i = 0; i < subObjects.size(); i++) {
            subObjects.get(i).updatePositionAndSize(width, height);
        }
    }

    public void removeAllSubObjects() {
        unregisterSubElements(gui);
        subObjects.clear();
    }
}
package net.bfsr.client.gui;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.Engine;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class AbstractGuiObject implements GuiObject {
    protected BiConsumer<Integer, Integer> repositionConsumer = (width, height) -> {};
    protected BiFunction<Integer, Integer, Integer> widthResizeFunction = (width, height) -> getWidth();
    protected BiFunction<Integer, Integer, Integer> heightResizeFunction = (width, height) -> getHeight();
    @Getter
    @Setter
    protected boolean mouseHover;
    @Setter
    protected Supplier<Boolean> onLeftClickSupplier = () -> false;
    @Setter
    protected Supplier<Boolean> onRightClickSupplier = () -> false;
    @Setter
    @Getter
    protected boolean visible = true;

    public AbstractGuiObject atTopLeftCorner(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(x, y);
        return this;
    }

    public AbstractGuiObject atBottomLeftCorner(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(x, height + y);
        return this;
    }

    public AbstractGuiObject atBottomRightCorner(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width + x, height + y);
        return this;
    }

    public AbstractGuiObject atTop(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width / 2 + x, y);
        return this;
    }

    public AbstractGuiObject atTopRightCorner(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width + x, y);
        return this;
    }

    public AbstractGuiObject atBottom(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width / 2 + x, height + y);
        return this;
    }

    public AbstractGuiObject atRight(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width + x, height / 2 + y);
        return this;
    }

    public AbstractGuiObject atCenter(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width / 2 + x, height / 2 + y);
        return this;
    }

    public AbstractGuiObject setFullScreenWidth() {
        widthResizeFunction = (width, height) -> width;
        return this;
    }

    public AbstractGuiObject setHeightResizeFunction(BiFunction<Integer, Integer, Integer> resizeHeightFunction) {
        this.heightResizeFunction = resizeHeightFunction;
        return this;
    }

    public AbstractGuiObject setRepositionConsumer(BiConsumer<Integer, Integer> repositionConsumer) {
        this.repositionConsumer = repositionConsumer;
        return this;
    }

    @Override
    public void onScreenResize(int width, int height) {
        updatePositionAndSize(width, height);
    }

    public void updatePositionAndSize() {
        updatePositionAndSize(Engine.renderer.getScreenWidth(), Engine.renderer.getScreenHeight());
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        repositionConsumer.accept(width, height);
        setWidth(widthResizeFunction.apply(width, height));
        setHeight(heightResizeFunction.apply(width, height));
        update();
    }

    @Override
    public void onRegistered(GuiObjectsHandler gui) {
        updatePositionAndSize();
    }

    @Override
    public void onUnregistered(GuiObjectsHandler gui) {}

    @Override
    public void update() {}

    @Override
    public void updateMouseHover() {}

    @Override
    public void render() {}

    @Override
    public boolean onMouseLeftClick() {
        return onLeftClickSupplier.get();
    }

    @Override
    public void onMouseLeftRelease() {}

    @Override
    public boolean onMouseRightClick() {
        return onRightClickSupplier.get();
    }

    @Override
    public void onMouseScroll(float y) {}

    @Override
    public void onOtherGuiObjectMouseLeftClick(GuiObject guiObject) {}

    @Override
    public void onOtherGuiObjectMouseRightClick(GuiObject guiObject) {}

    @Override
    public void input(int key) {}

    @Override
    public void textInput(int key) {}

    @Override
    public void onMouseHover() {}

    @Override
    public void onMouseStopHover() {}

    @Override
    public void onContextMenuClosed() {}

    public abstract AbstractGuiObject setPosition(int x, int y);

    @Override
    public void setX(int x) {}

    @Override
    public void setY(int y) {}

    public AbstractGuiObject setSize(int width, int height) {
        return this;
    }

    @Override
    public AbstractGuiObject setWidth(int width) {
        return this;
    }

    public AbstractGuiObject setHeight(int height) {
        return this;
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getYForScroll() {
        return getY();
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void clear() {}
}
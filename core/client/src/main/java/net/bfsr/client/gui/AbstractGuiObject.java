package net.bfsr.client.gui;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public abstract class AbstractGuiObject implements IGuiObject {
    protected BiConsumer<Integer, Integer> repositionConsumer = (width, height) -> {};
    protected BiConsumer<Integer, Integer> widthResizeConsumer = (width, height) -> {};
    protected BiFunction<Integer, Integer, Integer> heightResizeConsumer = (width, height) -> getHeight();

    public AbstractGuiObject atUpperLeftCorner(int x, int y) {
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
        widthResizeConsumer = (width, height) -> setWidth(width);
        return this;
    }

    public AbstractGuiObject setHeightResizeConsumer(BiFunction<Integer, Integer, Integer> resizeHeightConsumer) {
        this.heightResizeConsumer = resizeHeightConsumer;
        return this;
    }

    public AbstractGuiObject setRepositionConsumer(BiConsumer<Integer, Integer> repositionConsumer) {
        this.repositionConsumer = repositionConsumer;
        return this;
    }

    @Override
    public void resize(int width, int height) {
        repositionConsumer.accept(width, height);
        widthResizeConsumer.accept(width, height);
        setHeight(heightResizeConsumer.apply(width, height));
    }

    @Override
    public void update() {}

    @Override
    public void render() {}

    @Override
    public void onMouseLeftClick() {}

    @Override
    public void onMouseLeftRelease() {}

    @Override
    public void onMouseRightClick() {}

    @Override
    public void scroll(float y) {}

    @Override
    public void input(int key) {}

    @Override
    public void textInput(int key) {}

    public abstract AbstractGuiObject setPosition(int x, int y);

    @Override
    public void setX(int x) {}

    @Override
    public void setY(int y) {}

    public AbstractGuiObject setSize(int width, int height) {
        return this;
    }

    public AbstractGuiObject setWidth(int width) {
        return this;
    }

    public AbstractGuiObject setHeight(int height) {
        return this;
    }

    @Override
    public int getY() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }

    @Override
    public void clear() {}
}

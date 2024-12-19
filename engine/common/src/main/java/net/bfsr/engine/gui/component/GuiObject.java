package net.bfsr.engine.gui.component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;
import net.bfsr.engine.util.RunnableUtils;
import org.joml.Vector4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@NoArgsConstructor
public class GuiObject {
    @Getter
    protected int x;
    @Getter
    protected int y;
    @Getter
    protected int width, height;
    @Getter
    protected float rotation;
    @Getter
    protected final Vector4f color = new Vector4f(1.0f);
    @Getter
    protected final Vector4f outlineColor = new Vector4f(1.0f);
    @Getter
    protected final Vector4f hoverColor = new Vector4f(1.0f);
    @Getter
    protected final Vector4f outlineHoverColor = new Vector4f(1.0f);
    @Setter
    private Supplier<Boolean> intersectsCheckMethod = () -> isIntersects(Engine.mouse.getPosition().x, Engine.mouse.getGuiPosition().y);
    private BiFunction<Integer, Integer, Integer> widthFunction = (width, height) -> this.width;
    private BiFunction<Integer, Integer, Integer> heightFunction = (width, height) -> this.height;
    private BiFunction<Integer, Integer, Integer> xFunction = (width, height) -> 0;
    private BiFunction<Integer, Integer, Integer> yFunction = (width, height) -> 0;
    @Getter
    @Setter
    protected boolean mouseHover;
    @Getter
    protected Runnable leftClickRunnable = RunnableUtils.EMPTY_RUNNABLE;
    @Getter
    protected Runnable leftReleaseRunnable = RunnableUtils.EMPTY_RUNNABLE;
    @Getter
    protected Runnable rightClickRunnable = RunnableUtils.EMPTY_RUNNABLE;
    @Getter
    protected Runnable rightReleaseRunnable = RunnableUtils.EMPTY_RUNNABLE;
    @Getter
    protected final List<GuiObject> guiObjects = new ArrayList<>();
    @Setter
    @Getter
    protected GuiObject parent = BlankGuiObject.INSTANCE;
    private boolean canBeHovered = true;
    @Getter
    protected GuiObjectRenderer renderer = new GuiObjectRenderer(this);
    @Getter
    protected boolean isOnScene;

    public GuiObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    protected GuiObject(int width, int height) {
        this(0, 0, width, height);
    }

    public GuiObject atTopLeft(int x, int y) {
        xFunction = (width, height) -> x;
        yFunction = (width, height) -> height - this.height + y;
        return this;
    }

    public GuiObject atTopLeft(int x, int y, Supplier<Integer> xSupplier, Supplier<Integer> ySupplier) {
        xFunction = (width, height) -> x + xSupplier.get();
        yFunction = (width, height) -> y + height - this.height + ySupplier.get();
        return this;
    }

    public GuiObject atBottomLeft(int x, int y) {
        xFunction = (width, height) -> x;
        yFunction = (width, height) -> y;
        return this;
    }

    public GuiObject atBottomLeft(Supplier<Integer> xSupplier, Supplier<Integer> ySupplier) {
        xFunction = (width, height) -> xSupplier.get();
        yFunction = (width, height) -> ySupplier.get();
        return this;
    }

    public GuiObject atBottomRight(int x, int y) {
        xFunction = (width, height) -> width + x - this.width;
        yFunction = (width, height) -> y;
        return this;
    }

    public GuiObject atTop(int x, int y) {
        xFunction = (width, height) -> width / 2 + x - this.width / 2;
        yFunction = (width, height) -> height + y - this.height;
        return this;
    }

    public GuiObject atTopRight(int x, int y) {
        xFunction = (width, height) -> width + x - this.width;
        yFunction = (width, height) -> height + y - this.height;
        return this;
    }

    public GuiObject atBottomRight(Supplier<Integer> xSupplier, Supplier<Integer> ySupplier) {
        xFunction = (width, height) -> width + xSupplier.get() - this.width;
        yFunction = (width, height) -> ySupplier.get();
        return this;
    }

    public GuiObject atBottom(int x, int y) {
        xFunction = (width, height) -> width / 2 + x - this.width / 2;
        yFunction = (width, height) -> y;
        return this;
    }

    public GuiObject atLeft(int x, int y) {
        xFunction = (width, height) -> x;
        yFunction = (width, height) -> height / 2 + y - this.height / 2;
        return this;
    }

    public GuiObject atRight(int x, int y) {
        xFunction = (width, height) -> width + x - this.width;
        yFunction = (width, height) -> height / 2 + y - this.height / 2;
        return this;
    }

    public GuiObject atCenter(int x, int y) {
        xFunction = (width, height) -> width / 2 + x - this.width / 2;
        yFunction = (width, height) -> height / 2 + y - this.height / 2;
        return this;
    }

    public GuiObject setWidthFunction(BiFunction<Integer, Integer, Integer> resizeFunction) {
        widthFunction = resizeFunction;
        return this;
    }

    public GuiObject setHeightFunction(BiFunction<Integer, Integer, Integer> resizeFunction) {
        heightFunction = resizeFunction;
        return this;
    }

    public GuiObject setFillParent() {
        return setWidthFunction((width, height) -> width).setHeightFunction((width, height) -> height);
    }

    public void onScreenResize(int width, int height) {
        updatePositionAndSize(width, height);
    }

    public void updatePositionAndSize() {
        updatePositionAndSize(parent.width, parent.height);
    }

    public void updatePositionAndSize(int width, int height) {
        setWidth(widthFunction.apply(width, height));
        setHeight(heightFunction.apply(width, height));
        setPosition(xFunction.apply(width, height), yFunction.apply(width, height));
        updateLastValues();

        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).updatePositionAndSize(this.width, this.height);
        }
    }

    public void add() {
        updatePositionAndSize();
        if (parent.isOnScene) {
            addToScene();
        }
    }

    public void remove() {
        if (isOnScene) {
            isOnScene = false;
            mouseHover = false;
            renderer.remove();

            for (int i = 0, size = guiObjects.size(); i < size; i++) {
                guiObjects.get(i).remove();
            }
        }
    }

    public void update() {
        renderer.update();
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).update();
        }
    }

    public void updateLastValues() {
        renderer.updateLastValues();
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).updateLastValues();
        }
    }

    @Nullable
    public GuiObject mouseLeftClick() {
        GuiObject child = null;
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject child1 = guiObjects.get(i).mouseLeftClick();
            if (child1 != null) {
                child = child1;
            }
        }

        return child != null ? child : mouseHover ? this : null;
    }

    @Nullable
    public GuiObject mouseLeftRelease() {
        GuiObject child = null;
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject child1 = guiObjects.get(i).mouseLeftRelease();
            if (child1 != null) {
                child = child1;
            }
        }

        return child != null ? child : mouseHover ? this : null;
    }

    @Nullable
    public GuiObject mouseRightClick() {
        GuiObject child = null;
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject child1 = guiObjects.get(i).mouseRightClick();
            if (child1 != null) {
                child = child1;
            }
        }

        return child != null ? child : mouseHover ? this : null;
    }

    @Nullable
    public GuiObject mouseRightRelease() {
        GuiObject child = null;
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject child1 = guiObjects.get(i).mouseRightRelease();
            if (child1 != null) {
                child = child1;
            }
        }

        return child != null ? child : mouseHover ? this : null;
    }

    public boolean mouseScroll(float y) {
        for (int i = 0; i < guiObjects.size(); i++) {
            if (guiObjects.get(i).mouseScroll(y)) {
                return true;
            }
        }

        return false;
    }

    public boolean mouseMove(float x, float y) {
        boolean moveMove = false;
        for (int i = 0; i < guiObjects.size(); i++) {
            if (guiObjects.get(i).mouseMove(x, y)) {
                moveMove = true;
            }
        }

        return moveMove || mouseHover;
    }

    public boolean input(int key) {
        for (int i = 0; i < guiObjects.size(); i++) {
            if (guiObjects.get(i).input(key)) {
                return true;
            }
        }

        return false;
    }

    public boolean textInput(int key) {
        for (int i = 0; i < guiObjects.size(); i++) {
            if (guiObjects.get(i).textInput(key)) {
                return true;
            }
        }

        return false;
    }

    public void onMouseHover() {
        if (isOnScene) {
            renderer.onMouseHover();
        }
    }

    public void onMouseStopHover() {
        if (isOnScene) {
            renderer.onMouseStopHover();
        }
    }

    protected void onParentSizeChanged(int width, int height) {
        updatePositionAndSize(width, height);
    }

    protected void onChildSizeChanged(GuiObject guiObject, int width, int height) {
        updatePositionAndSize(parent.width, parent.height);
        parent.onChildSizeChanged(guiObject, width, height);
    }

    protected void onParentPositionChanged() {
        if (isOnScene) {
            renderer.updatePosition();

            for (int i = 0; i < guiObjects.size(); i++) {
                guiObjects.get(i).onParentPositionChanged();
            }
        }
    }

    protected void onChildPositionChanged(GuiObject guiObject, int x, int y) {
        parent.onChildPositionChanged(guiObject, x, y);
    }

    public void addIfAbsent(GuiObject guiObject) {
        if (hasGuiObject(guiObject)) return;
        add(guiObject);
    }

    public void add(GuiObject guiObject) {
        guiObjects.add(guiObject);
        guiObject.setParent(this);
        guiObject.add();
    }

    public void addAt(int index, GuiObject guiObject) {
        guiObjects.add(index, guiObject);
        guiObject.setParent(this);
        guiObject.add();
    }

    public int addBefore(GuiObject guiObject, GuiObject beforeObject) {
        int index = guiObjects.indexOf(beforeObject);
        if (index >= 0) {
            addAt(index, guiObject);
            return index;
        } else {
            throw new RuntimeException("Failed to add gui object " + guiObject + " before " + beforeObject);
        }
    }

    public void remove(GuiObject guiObject) {
        guiObjects.remove(guiObject);
        guiObject.remove();
        guiObject.setParent(BlankGuiObject.INSTANCE);
    }

    public void removeAll() {
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).setParent(BlankGuiObject.INSTANCE);
        }

        guiObjects.clear();
    }

    protected void forEach(Consumer<GuiObject> consumer) {
        for (int i = 0; i < guiObjects.size(); i++) {
            consumer.accept(guiObjects.get(i));
        }
    }

    public void applyPositionFunctions(BiConsumer<Integer, Integer> consumer) {
        consumer.accept(xFunction.apply(parent.width, parent.height), yFunction.apply(parent.width, parent.height));
    }

    public GuiObject setLeftClickRunnable(Runnable runnable) {
        this.leftClickRunnable = runnable;
        return this;
    }

    public GuiObject setLeftReleaseRunnable(Runnable runnable) {
        this.leftReleaseRunnable = runnable;
        return this;
    }

    public GuiObject setRightClickRunnable(Runnable runnable) {
        this.rightClickRunnable = runnable;
        return this;
    }

    public GuiObject setRightReleaseRunnable(Runnable runnable) {
        this.rightReleaseRunnable = runnable;
        return this;
    }

    public GuiObject setPosition(int x, int y) {
        boolean changed = x != this.x || y != this.y;

        if (changed) {
            this.x = x;
            this.y = y;
            if (isOnScene) {
                renderer.updatePosition();
            }
            forEach(GuiObject::onParentPositionChanged);
            parent.onChildPositionChanged(this, x, y);
        }

        return this;
    }

    public GuiObject setX(int x) {
        boolean changed = x != this.x;

        if (changed) {
            this.x = x;
            if (isOnScene) {
                renderer.updatePosition();
            }
            forEach(GuiObject::onParentPositionChanged);
        }

        return this;
    }

    public GuiObject setY(int y) {
        boolean changed = y != this.y;

        if (changed) {
            this.y = y;
            if (isOnScene) {
                renderer.updatePosition();
            }
            forEach(GuiObject::onParentPositionChanged);
        }

        return this;
    }

    public GuiObject setRotation(float rotation) {
        boolean changed = rotation != this.rotation;

        if (changed) {
            this.rotation = rotation;
            if (isOnScene) {
                renderer.updateRotation();
            }
        }

        return this;
    }

    public GuiObject setSize(int width, int height) {
        boolean changed = width != this.width || height != this.height;

        if (changed) {
            this.width = width;
            this.height = height;
            if (isOnScene) {
                renderer.updateSize();
            }
            forEach(guiObject -> guiObject.onParentSizeChanged(width, height));
            parent.onChildSizeChanged(this, width, height);
        }

        return this;
    }

    public GuiObject setWidth(int width) {
        boolean changed = width != this.width;

        if (changed) {
            this.width = width;
            if (isOnScene) {
                renderer.updateSize();
            }
            forEach(guiObject -> guiObject.onParentSizeChanged(width, height));
            parent.onChildSizeChanged(this, width, height);
        }

        return this;
    }

    public GuiObject setHeight(int height) {
        boolean changed = height != this.height;

        if (changed) {
            this.height = height;
            if (isOnScene) {
                renderer.updateSize();
            }
            forEach(guiObject -> guiObject.onParentSizeChanged(width, height));
            parent.onChildSizeChanged(this, width, height);
        }

        return this;
    }

    public GuiObject setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
        if (isOnScene) {
            renderer.updateColor();
        }
        return this;
    }

    public GuiObject setColor(Vector4f color) {
        this.color.set(color);
        if (isOnScene) {
            renderer.updateColor();
        }
        return this;
    }

    public GuiObject setOutlineColor(float r, float g, float b, float a) {
        outlineColor.set(r, g, b, a);
        return this;
    }

    public GuiObject setOutlineColor(Vector4f color) {
        outlineColor.set(color);
        return this;
    }

    public GuiObject setOutlineHoverColor(float r, float g, float b, float a) {
        outlineHoverColor.set(r, g, b, a);
        return this;
    }

    public GuiObject setOutlineHoverColor(Vector4f color) {
        outlineHoverColor.set(color);
        return this;
    }

    public GuiObject setHoverColor(float r, float g, float b, float a) {
        hoverColor.set(r, g, b, a);
        return this;
    }

    public GuiObject setHoverColor(Vector4f color) {
        hoverColor.set(color);
        return this;
    }

    public GuiObject setAllColors(float r, float g, float b, float a) {
        setColor(r, g, b, a);
        setHoverColor(r, g, b, a);
        setOutlineColor(r, g, b, a);
        setOutlineHoverColor(r, g, b, a);
        return this;
    }

    public GuiObject setTextColor(float r, float g, float b, float a) {
        return this;
    }

    public GuiObject setCanBeHovered(boolean canBeHovered) {
        this.canBeHovered = canBeHovered;
        return this;
    }

    public GuiObject setRenderer(GuiObjectRenderer renderer) {
        this.renderer = renderer;
        return this;
    }

    protected boolean hasGuiObject(GuiObject object) {
        return guiObjects.contains(object);
    }

    public boolean isIntersectsWithMouse() {
        return intersectsCheckMethod.get();
    }

    public boolean isIntersects(float x, float y) {
        int sceneX = getSceneX();
        int sceneY = getSceneY();
        return x >= sceneX && y >= sceneY && x < sceneX + width && y < sceneY + height;
    }

    public boolean isIntersects(GuiObject guiObject) {
        int sceneX = getSceneX();
        int sceneY = getSceneY();
        int sceneX1 = guiObject.getSceneX();
        int sceneY1 = guiObject.getSceneY();

        return sceneX + width >= sceneX1 && sceneX <= sceneX1 + guiObject.width && sceneY + height >= sceneY1 &&
                sceneY <= sceneY1 + guiObject.height;
    }

    public GuiObject getHovered(GuiObject hoveredObject) {
        if (canBeHovered && isIntersectsWithMouse()) {
            hoveredObject = this;
        }

        for (int i = 0, size = guiObjects.size(); i < size; i++) {
            hoveredObject = guiObjects.get(i).getHovered(hoveredObject);
        }

        return hoveredObject;
    }

    public int getSceneX() {
        return x + parent.getSceneX();
    }

    public int getSceneY() {
        return y + parent.getSceneY();
    }

    public void addToScene() {
        isOnScene = true;
        renderer.addToScene();

        for (int i = 0, size = guiObjects.size(); i < size; i++) {
            guiObjects.get(i).addToScene();
        }
    }

    public void clear() {
        remove();

        for (int i = 0, size = guiObjects.size(); i < size; i++) {
            guiObjects.get(i).clear();
        }

        while (guiObjects.size() > 0) {
            remove(guiObjects.get(0));
        }
    }
}
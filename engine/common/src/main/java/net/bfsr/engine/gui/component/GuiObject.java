package net.bfsr.engine.gui.component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.util.RunnableUtils;
import org.joml.Vector2f;
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
    @Setter
    protected int x;
    @Getter
    @Setter
    protected int y;
    protected int lastX, lastY;
    @Getter
    protected int width, height;
    @Getter
    @Setter
    protected float rotation;
    @Getter
    protected float lastRotation;
    @Getter
    protected final Vector4f color = new Vector4f(1.0f);
    @Getter
    protected final Vector4f outlineColor = new Vector4f(1.0f);
    @Getter
    protected final Vector4f hoverColor = new Vector4f(1.0f);
    @Getter
    protected final Vector4f outlineHoverColor = new Vector4f(1.0f);
    @Setter
    private Supplier<Boolean> intersectsCheckMethod = () -> isIntersects(Engine.mouse.getPosition().x, Engine.mouse.getPosition().y);
    private BiConsumer<Integer, Integer> repositionConsumer = (width, height) -> {};
    private BiFunction<Integer, Integer, Integer> widthFunction = (width, height) -> getWidth();
    private BiFunction<Integer, Integer, Integer> heightFunction = (width, height) -> getHeight();
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
        repositionConsumer = (width, height) -> setPosition(x, y);
        return this;
    }

    public GuiObject atBottomLeft(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(x, height + y);
        return this;
    }

    public GuiObject atBottomRight(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width + x, height + y);
        return this;
    }

    public GuiObject atTop(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width / 2 + x, y);
        return this;
    }

    public GuiObject atTopRight(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width + x, y);
        return this;
    }

    public GuiObject atTopRight(Supplier<Integer> xSupplier, Supplier<Integer> ySupplier) {
        repositionConsumer = (width, height) -> setPosition(width + xSupplier.get(), ySupplier.get());
        return this;
    }

    public GuiObject atBottom(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width / 2 + x, height + y);
        return this;
    }

    public GuiObject atRight(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width + x, height / 2 + y);
        return this;
    }

    public GuiObject atCenter(int x, int y) {
        repositionConsumer = (width, height) -> setPosition(width / 2 + x, height / 2 + y);
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

    public GuiObject setRepositionConsumer(BiConsumer<Integer, Integer> repositionConsumer) {
        this.repositionConsumer = repositionConsumer;
        return this;
    }

    public GuiObject setFillParent() {
        return setWidthFunction((width, height) -> width).setHeightFunction((width, height) -> height);
    }

    public void onScreenResize(int width, int height) {
        updatePositionAndSize(width, height);
    }

    public void updatePositionAndSize() {
        updatePositionAndSize(parent.getWidth(), parent.getHeight());
    }

    public void updatePositionAndSize(int width, int height) {
        setWidth(widthFunction.apply(width, height));
        setHeight(heightFunction.apply(width, height));
        repositionConsumer.accept(width, height);
        update();

        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).updatePositionAndSize(this.width, this.height);
        }
    }

    public void onAdded() {
        updatePositionAndSize();
    }

    public void onRemoved() {}

    public void update() {
        updateLastValues();
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).update();
        }
    }

    public void updateLastValues() {
        lastX = x;
        lastY = y;
        lastRotation = rotation;
    }

    public void render() {
        renderer.render(lastX, lastY, x, y, width, height);
    }

    public void render(AbstractGUIRenderer guiRenderer, int lastX, int lastY, int x, int y) {
        renderer.render(lastX + this.lastX, lastY + this.lastY, x + this.x, y + this.y, width, height);
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

    public void onMouseHover() {}

    public void onMouseStopHover() {}

    protected void onParentSizeChanged(int width, int height) {
        updatePositionAndSize(width, height);
    }

    protected void onChildSizeChanged(GuiObject guiObject, int width, int height) {
        parent.onChildSizeChanged(guiObject, width, height);
    }

    protected void onParentPositionChanged(int x, int y) {}

    protected void onChildPositionChanged(GuiObject guiObject, int x, int y) {
        parent.onChildPositionChanged(this, x, y);
    }

    public void addIfAbsent(GuiObject guiObject) {
        if (hasGuiObject(guiObject)) return;
        add(guiObject);
    }

    public void add(GuiObject guiObject) {
        guiObjects.add(guiObject);
        guiObject.setParent(this);
        guiObject.onAdded();
    }

    public void addAt(int index, GuiObject guiObject) {
        guiObjects.add(index, guiObject);
        guiObject.setParent(this);
        guiObject.onAdded();
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
        guiObject.onRemoved();
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
            forEach(guiObject -> guiObject.onParentPositionChanged(x, y));
            parent.onChildPositionChanged(this, x, y);
        }

        return this;
    }

    public GuiObject setSize(int width, int height) {
        boolean changed = width != this.width || height != this.height;

        if (changed) {
            this.width = width;
            this.height = height;
            forEach(guiObject -> guiObject.onParentSizeChanged(width, height));
            parent.onChildSizeChanged(this, width, height);
        }

        return this;
    }

    public GuiObject setWidth(int width) {
        boolean changed = width != this.width;

        if (changed) {
            this.width = width;
            forEach(guiObject -> guiObject.onParentSizeChanged(width, height));
            parent.onChildSizeChanged(this, width, height);
        }

        return this;
    }

    public GuiObject setHeight(int height) {
        boolean changed = height != this.height;

        if (changed) {
            this.height = height;
            forEach(guiObject -> guiObject.onParentSizeChanged(width, height));
            parent.onChildSizeChanged(this, width, height);
        }

        return this;
    }

    public GuiObject setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
        return this;
    }

    public GuiObject setColor(Vector4f color) {
        this.color.set(color);
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

    public boolean isIntersects(Vector2f vector) {
        return isIntersects(vector.x, vector.y);
    }

    private boolean isIntersects(float x, float y) {
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

    public int getYForScroll() {
        return y;
    }

    public void clear() {
        for (int i = 0, size = guiObjects.size(); i < size; i++) {
            guiObjects.get(i).clear();
        }

        while (guiObjects.size() > 0) {
            remove(guiObjects.get(0));
        }
    }
}
package net.bfsr.client.gui.component;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.AbstractGuiObject;
import net.bfsr.client.gui.GuiObjectsHandler;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringCache;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.gui.GUIRenderer;
import net.bfsr.texture.TextureRegister;
import org.joml.Vector4f;

public class CompoBoxElement<V> extends TexturedGuiObject {
    @Getter
    private final V value;
    private final StringObject stringObject;
    private final int stringYOffset;
    @Setter
    private boolean selected;
    private final int triangleXOffset = 4;
    private final ComboBox<V> comboBox;

    public CompoBoxElement(TextureRegister texture, int width, int height, V value, String name, FontType fontType, int fontSize, int stringYOffset, ComboBox<V> comboBox) {
        super(texture, width, height);
        this.stringObject = new StringObject(fontType, name, fontSize, StringOffsetType.CENTERED).compile();
        this.value = value;
        this.stringYOffset = stringYOffset;
        this.comboBox = comboBox;
    }

    public CompoBoxElement(int width, int height, V value, String name, FontType fontType, int fontSize, int stringYOffset, ComboBox<V> comboBox) {
        this(null, width, height, value, name, fontType, fontSize, stringYOffset, comboBox);
    }

    @Override
    public boolean onMouseLeftClick() {
        if (isMouseHover() && !comboBox.isOpened()) {
            comboBox.open();
            return true;
        }

        return false;
    }

    @Override
    public void onRegistered(GuiObjectsHandler gui) {
        super.onRegistered(gui);
        gui.registerGuiObject(stringObject);
    }

    @Override
    public void onUnregistered(GuiObjectsHandler gui) {
        super.onUnregistered(gui);
        gui.unregisterGuiObject(stringObject);
    }

    @Override
    public AbstractGuiObject atTopLeftCorner(int x, int y) {
        StringCache stringCache = stringObject.getStringCache();
        int fontSize = stringObject.getFontSize();
        stringObject.atTopLeftCorner(x + width / 2, y + stringCache.getCenteredYOffset(stringObject.getString(), height, fontSize) + stringYOffset);
        return super.atTopLeftCorner(x, y);
    }

    @Override
    public AbstractGuiObject atTopRightCorner(int x, int y) {
        StringCache stringCache = stringObject.getStringCache();
        int fontSize = stringObject.getFontSize();
        stringObject.atTopRightCorner(x + width / 2, y + stringCache.getCenteredYOffset(stringObject.getString(), height, fontSize) + stringYOffset);
        return super.atTopRightCorner(x, y);
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        stringObject.updatePositionAndSize(width, height);
    }

    @Override
    public void render() {
        super.render();
        if (selected) {
            int triangleWidth = 14;
            int triangleHeight = 8;
            float interpolation = Core.get().getRenderer().getInterpolation();
            float triangleX = lastX + (x - lastX) * interpolation + width - triangleWidth / 2 - triangleXOffset;
            float triangleY = lastY + (y - lastY) * interpolation + height / 2;

            Vector4f color = stringObject.getColor();
            GUIRenderer.get().addPrimitive(triangleX - triangleWidth / 2, triangleY - triangleHeight / 2, triangleX, triangleY + triangleHeight / 2, triangleX + triangleWidth / 2,
                    triangleY - triangleHeight / 2, triangleX - triangleWidth / 2, triangleY - triangleHeight / 2, color.x, color.y, color.z, color.w, 0);
        }
    }

    @Override
    public void renderNoInterpolation() {
        super.renderNoInterpolation();
        if (selected) {
            int triangleWidth = 14;
            int triangleHeight = 8;
            int triangleX = x + width - triangleWidth / 2 - triangleXOffset;
            int triangleY = y + height / 2;

            Vector4f color = stringObject.getColor();
            GUIRenderer.get().addPrimitive(triangleX - triangleWidth / 2, triangleY - triangleHeight / 2, triangleX, triangleY + triangleHeight / 2, triangleX + triangleWidth / 2,
                    triangleY - triangleHeight / 2, triangleX - triangleWidth / 2, triangleY - triangleHeight / 2, color.x, color.y, color.z, color.w, 0);
        }
    }

    @Override
    public CompoBoxElement<V> setTextColor(float r, float g, float b, float a) {
        stringObject.setColor(r, g, b, a).compile();
        return this;
    }
}
package net.bfsr.engine.gui.component;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.renderer.combobox.ComboBoxRenderer;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.font.StringOffsetType;

import java.util.ArrayList;
import java.util.List;

public class ComboBox<V> extends GuiObject {
    @Getter
    private int selectedIndex;
    @Getter
    private boolean opened;
    @Getter
    private final Label label;
    private final Font font;
    private final int fontSize;
    private final int stringOffsetY;
    private final List<ComboBoxData<V>> data = new ArrayList<>();

    public ComboBox(int width, int height, Font font, int fontSize, int stringOffsetY) {
        super(width, height);
        this.font = font;
        this.fontSize = fontSize;
        this.stringOffsetY = stringOffsetY;

        add(this.label = new Label(font, "", fontSize, StringOffsetType.CENTERED));
        label.atBottomLeft(width / 2, label.getCenteredOffsetY(height));

        setRenderer(new ComboBoxRenderer(this));
        setLeftReleaseRunnable(() -> {
            if (opened) {
                opened = false;
                removeNotSelected();
            } else {
                open();
            }
        });
    }

    public void addData(V value) {
        ComboBoxData<V> data = new ComboBoxData<>(width, height, value, value.toString(), font, fontSize, stringOffsetY);
        this.data.add(data);
        data.setWidthFunction((width, height) -> this.width);
        data.setLeftReleaseRunnable(() -> {
            opened = false;
            removeNotSelected();
            setSelectedIndex(this.data.indexOf(data));
        });
    }

    public void removeData(ComboBoxData<V> data) {
        this.data.remove(data);
    }

    private void open() {
        opened = true;
        Gui gui = Engine.guiManager.getGui();
        int y = -height;
        for (int i = 0; i < data.size(); i++) {
            if (i != selectedIndex) {
                gui.add(data.get(i).atBottomLeft(getSceneX(), getSceneY() + y));
                y -= height;
            }
        }
    }

    private void removeNotSelected() {
        Gui gui = Engine.guiManager.getGui();
        for (int i = 0; i < data.size(); i++) {
            if (i != selectedIndex) {
                gui.remove(data.get(i));
            }
        }
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        label.setString(getSelected().getLabel().getString());
    }

    @Override
    public GuiObject setColor(float r, float g, float b, float a) {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).setColor(r, g, b, a);
        }
        return super.setColor(r, g, b, a);
    }

    @Override
    public GuiObject setHoverColor(float r, float g, float b, float a) {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).setHoverColor(r, g, b, a);
        }
        return super.setHoverColor(r, g, b, a);
    }

    @Override
    public GuiObject setOutlineColor(float r, float g, float b, float a) {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).setOutlineColor(r, g, b, a);
        }
        return super.setOutlineColor(r, g, b, a);
    }

    @Override
    public GuiObject setOutlineHoverColor(float r, float g, float b, float a) {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).setOutlineHoverColor(r, g, b, a);
        }
        return super.setOutlineHoverColor(r, g, b, a);
    }

    @Override
    public GuiObject setTextColor(float r, float g, float b, float a) {
        label.setColor(r, g, b, a);
        for (int i = 0; i < data.size(); i++) {
            data.get(i).setTextColor(r, g, b, a);
        }
        return this;
    }

    @Override
    public GuiObject setWidth(int width) {
        super.setWidth(width);
        for (int i = 0; i < data.size(); i++) {
            data.get(i).updatePositionAndSize(width, height);
        }
        return this;
    }

    private ComboBoxData<V> getSelected() {
        return data.get(selectedIndex);
    }

    public V getSelectedValue() {
        return getSelected().getValue();
    }
}
package net.bfsr.client.gui.component;

import lombok.Getter;
import net.bfsr.client.gui.GuiObjectWithSubObjects;
import net.bfsr.client.gui.GuiObjectsHandler;
import net.bfsr.client.gui.SimpleGuiObject;

public class ComboBox<V> extends GuiObjectWithSubObjects {
    @Getter
    private int selectedIndex;
    @Getter
    private boolean opened;

    public ComboBox(int width, int height) {
        super(width, height);
    }

    @Override
    protected void registerSubElements(GuiObjectsHandler gui) {
        gui.registerGuiObject(subObjects.get(selectedIndex));
    }

    @Override
    protected void unregisterSubElements(GuiObjectsHandler gui) {
        gui.unregisterGuiObject(subObjects.get(selectedIndex));
    }

    @Override
    protected void setRepositionConsumerForSubObjects() {
        subObjectsRepositionConsumer.setup(subObjects.get(selectedIndex), 0, 0);
        int offsetIndex = 1;
        for (int i = 0; i < subObjects.size(); i++) {
            if (i != selectedIndex) {
                subObjectsRepositionConsumer.setup(subObjects.get(i), 0, offsetIndex * height);
                offsetIndex++;
            }
        }
    }

    @Override
    public boolean onMouseLeftClick() {
        if (opened) {
            opened = false;
            unregisterNotSelected();

            for (int i = 0; i < subObjects.size(); i++) {
                CompoBoxElement<V> element = (CompoBoxElement<V>) subObjects.get(i);
                if (element.isMouseHover()) {
                    if (i != selectedIndex) {
                        gui.unregisterGuiObject(subObjects.get(selectedIndex));
                        selectedIndex = i;
                        setRepositionConsumerForSubObjects();
                        CompoBoxElement<V> compoBoxElement = (CompoBoxElement<V>) subObjects.get(selectedIndex);
                        compoBoxElement.setSelected(true);
                        gui.registerGuiObject(compoBoxElement);
                    }

                    return true;
                }
            }
        } else if (isMouseHover()) {
            open();
            return true;
        }

        return false;
    }

    public void open() {
        opened = true;

        for (int i = 0; i < subObjects.size(); i++) {
            if (i != selectedIndex) {
                CompoBoxElement<V> element = (CompoBoxElement<V>) subObjects.get(i);
                element.setSelected(false);
                gui.registerGuiObject(element);
            }
        }
    }

    private void unregisterNotSelected() {
        for (int i = 0; i < subObjects.size(); i++) {
            if (i != selectedIndex) {
                CompoBoxElement<V> element = (CompoBoxElement<V>) subObjects.get(i);
                gui.unregisterGuiObject(element);
            }
        }
    }

    @Override
    public SimpleGuiObject setColor(float r, float g, float b, float a) {
        for (int i = 0; i < subObjects.size(); i++) {
            subObjects.get(i).setColor(r, g, b, a);
        }
        return super.setColor(r, g, b, a);
    }

    @Override
    public SimpleGuiObject setHoverColor(float r, float g, float b, float a) {
        for (int i = 0; i < subObjects.size(); i++) {
            subObjects.get(i).setHoverColor(r, g, b, a);
        }
        return super.setHoverColor(r, g, b, a);
    }

    @Override
    public SimpleGuiObject setOutlineColor(float r, float g, float b, float a) {
        for (int i = 0; i < subObjects.size(); i++) {
            subObjects.get(i).setOutlineColor(r, g, b, a);
        }
        return super.setOutlineColor(r, g, b, a);
    }

    @Override
    public SimpleGuiObject setOutlineHoverColor(float r, float g, float b, float a) {
        for (int i = 0; i < subObjects.size(); i++) {
            subObjects.get(i).setOutlineHoverColor(r, g, b, a);
        }
        return super.setOutlineHoverColor(r, g, b, a);
    }

    public SimpleGuiObject setTextColor(float r, float g, float b, float a) {
        for (int i = 0; i < subObjects.size(); i++) {
            subObjects.get(i).setTextColor(r, g, b, a);
        }
        return this;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        ((CompoBoxElement<V>) subObjects.get(selectedIndex)).setSelected(true);
    }

    @Override
    public SimpleGuiObject setWidth(int width) {
        for (int i = 0; i < subObjects.size(); i++) {
            subObjects.get(i).setWidth(width);
        }
        return super.setWidth(width);
    }

    public V getSelectedValue() {
        return ((CompoBoxElement<V>) subObjects.get(selectedIndex)).getValue();
    }
}
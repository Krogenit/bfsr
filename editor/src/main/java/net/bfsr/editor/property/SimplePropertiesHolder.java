package net.bfsr.editor.property;

import lombok.Getter;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.event.ChangeNameEventListener;

import java.util.ArrayList;
import java.util.List;

public abstract class SimplePropertiesHolder implements PropertiesHolder {
    @Property(elementType = PropertyGuiElementType.MAIN_NAME_INPUT_BOX)
    @Getter
    private String name;

    private final List<ChangeNameEventListener> listeners = new ArrayList<>();

    @Override
    public void registerChangeNameEventListener(ChangeNameEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void setName(String name) {
        this.name = name;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onChangeName(name);
        }
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }
}
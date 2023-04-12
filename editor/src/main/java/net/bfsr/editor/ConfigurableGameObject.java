package net.bfsr.editor;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.property.PropertiesHolder;
import net.bfsr.property.Property;
import net.bfsr.property.event.ChangeNameEventListener;

@Getter
@Setter
public class ConfigurableGameObject implements PropertiesHolder {
    @Property(name = "position", fieldsAmount = 2)
    private float posX, posY;
    @Property(name = "size", fieldsAmount = 2)
    private float sizeX, sizeY;
    @Property(name = "velocity", fieldsAmount = 2)
    private float velocityX, velocityY;

    @Override
    public void setDefaultValues() {
        sizeX = sizeY = 10.0f;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return "Game Object";
    }

    @Override
    public void registerChangeNameEventListener(ChangeNameEventListener listener) {

    }

    @Override
    public void clearListeners() {

    }
}
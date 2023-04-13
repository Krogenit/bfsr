package net.bfsr.client.particle;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.Configurable;
import net.bfsr.property.PropertiesHolder;
import net.bfsr.property.Property;
import net.bfsr.property.PropertyGuiElementType;
import net.bfsr.property.event.ChangeNameEventListener;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("TransientFieldInNonSerializableClass")
public class ChildParticleEffect implements PropertiesHolder {
    @Configurable
    @Property(elementType = PropertyGuiElementType.MAIN_NAME_INPUT_BOX)
    @Getter
    private String name;
    @Configurable
    @Property(fieldsAmount = 2)
    @Setter
    @Getter
    private int minSpawnCount, maxSpawnCount;
    @Configurable
    @Property
    @Setter
    @Getter
    private float scale;

    private final transient List<ChangeNameEventListener> listeners = new ArrayList<>();

    @Override
    public void setDefaultValues() {
        setName("Child Effect");
        minSpawnCount = maxSpawnCount = 1;
        scale = 1.0f;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onChangeName(name);
        }
    }

    @Override
    public void registerChangeNameEventListener(ChangeNameEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }
}
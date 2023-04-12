package net.bfsr.client.particle;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.Configurable;
import net.bfsr.property.Property;
import net.bfsr.property.SimplePropertiesHolder;

public class ChildParticleEffect extends SimplePropertiesHolder {
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

    @Override
    public void setDefaultValues() {
        setName("Child Effect");
        minSpawnCount = maxSpawnCount = 1;
        scale = 1.0f;
    }
}
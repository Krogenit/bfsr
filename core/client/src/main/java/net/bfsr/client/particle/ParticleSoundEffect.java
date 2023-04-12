package net.bfsr.client.particle;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.sound.SoundBuffer;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.config.Configurable;
import net.bfsr.property.Property;
import net.bfsr.property.PropertyGuiElementType;
import net.bfsr.property.SimplePropertiesHolder;

@SuppressWarnings("TransientFieldInNonSerializableClass")
@Getter
@Setter
public class ParticleSoundEffect extends SimplePropertiesHolder {
    @Configurable
    @Property(elementType = PropertyGuiElementType.FILE_SELECTOR)
    private String path;
    @Configurable
    @Property
    private float volume;
    private transient SoundBuffer soundBuffer;

    @Override
    public void setDefaultValues() {
        setName("Sound Effect");
        path = SoundRegistry.shieldUp0.getPath();
    }
}
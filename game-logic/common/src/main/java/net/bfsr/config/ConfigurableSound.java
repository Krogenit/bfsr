package net.bfsr.config;

import lombok.Getter;
import net.bfsr.property.Property;
import net.bfsr.property.PropertyGuiElementType;
import net.bfsr.property.SimplePropertiesHolder;

@Configurable
@Getter
public class ConfigurableSound extends SimplePropertiesHolder {
    @Property(elementType = PropertyGuiElementType.FILE_SELECTOR)
    private String path;
    @Property
    private float volume;

    @Override
    public void setDefaultValues() {
        setName("Sound Effect");
        path = "sound/gui/buttonClick.ogg";
    }
}
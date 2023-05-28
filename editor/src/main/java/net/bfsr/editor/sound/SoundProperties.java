package net.bfsr.editor.sound;

import lombok.Getter;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.SimplePropertiesHolder;

@Getter
public class SoundProperties extends SimplePropertiesHolder {
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
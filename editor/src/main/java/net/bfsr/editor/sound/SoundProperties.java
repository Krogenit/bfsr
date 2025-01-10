package net.bfsr.editor.sound;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.holder.PropertiesHolderAdapter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SoundProperties extends PropertiesHolderAdapter {
    @Property(elementType = PropertyGuiElementType.FILE_SELECTOR)
    private String path;
    @Property
    private float volume;

    @Override
    public void setDefaultValues() {
        path = "sound/gui/buttonClick.ogg";
        volume = 1.0f;
    }
}
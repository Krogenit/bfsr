package net.bfsr.editor.sound;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.holder.PropertiesHolderAdapter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SoundEffectProperties extends PropertiesHolderAdapter {
    @Property(elementType = PropertyGuiElementType.MINIMIZABLE_LIST, arrayElementName = "sound")
    private List<SoundProperties> sounds;
    @Property
    private boolean randomFromList;

    @Override
    public void setDefaultValues() {
        sounds = new ArrayList<>();
        randomFromList = false;
    }
}

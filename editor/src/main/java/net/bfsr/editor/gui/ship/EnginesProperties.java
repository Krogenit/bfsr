package net.bfsr.editor.gui.ship;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.holder.PropertiesHolderAdapter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class EnginesProperties extends PropertiesHolderAdapter {
    @Property(elementType = PropertyGuiElementType.MINIMIZABLE_LIST, arrayElementName = "engine")
    private List<EngineProperties> engines;

    @Override
    public void setDefaultValues() {
        engines = new ArrayList<>();
    }
}
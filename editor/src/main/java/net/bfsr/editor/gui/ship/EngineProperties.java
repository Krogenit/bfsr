package net.bfsr.editor.gui.ship;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.holder.PolygonPropertiesHolder;
import net.bfsr.editor.property.holder.PropertiesHolderAdapter;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class EngineProperties extends PropertiesHolderAdapter {
    @Property(elementType = PropertyGuiElementType.MINIMIZABLE_LIST, arrayElementName = "polygon")
    private List<PolygonPropertiesHolder> polygons;
    @Property(elementType = PropertyGuiElementType.INPUT_BOX)
    private Vector2fPropertiesHolder effectPosition;

    @Override
    public void setDefaultValues() {
        polygons = new ArrayList<>();
        effectPosition = new Vector2fPropertiesHolder();
    }
}
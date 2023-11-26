package net.bfsr.editor.property.holder;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.Property;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PolygonPropertiesHolder extends PropertiesHolderAdapter {
    @Property(elementType = PropertyGuiElementType.POLYGON, arrayElementType = PropertyGuiElementType.INPUT_BOX,
            arrayElementName = "vertex")
    private List<Vector2fPropertiesHolder> vertices;

    @Override
    public void setDefaultValues() {
        vertices = new ArrayList<>();
        vertices.add(new Vector2fPropertiesHolder(-2.0f, 0.5f));
        vertices.add(new Vector2fPropertiesHolder(-2.0f, -0.5f));
        vertices.add(new Vector2fPropertiesHolder(-1.0f, -3.0f));
        vertices.add(new Vector2fPropertiesHolder(-1.0f, 3.0f));
    }
}
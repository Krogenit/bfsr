package net.bfsr.editor.property.holder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.Property;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Vector2fPropertiesHolder extends PropertiesHolderAdapter {
    @Property(elementType = PropertyGuiElementType.INPUT_BOX, fieldsAmount = 2, name = "vector")
    private float x, y;
}
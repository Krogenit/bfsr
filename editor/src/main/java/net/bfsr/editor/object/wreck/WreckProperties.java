package net.bfsr.editor.object.wreck;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.object.ObjectProperties;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.server.dto.Default;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(onConstructor_ = @Default)
@NoArgsConstructor
public class WreckProperties extends ObjectProperties {
    @Property
    private Vector2fPropertiesHolder size;
    @Property(elementType = PropertyGuiElementType.FILE_SELECTOR)
    private String texture;
    @Property(elementType = PropertyGuiElementType.POLYGON, arrayElementType = PropertyGuiElementType.INPUT_BOX,
            arrayElementName = "vertex")
    private List<Vector2fPropertiesHolder> vertices;

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        size = new Vector2fPropertiesHolder(0.5f, 0.5f);
        texture = "texture/entity/wreck/small0.png";
        vertices = new ArrayList<>();
        vertices.add(new Vector2fPropertiesHolder(-0.5f, 0.5f));
        vertices.add(new Vector2fPropertiesHolder(-0.5f, -0.5f));
        vertices.add(new Vector2fPropertiesHolder(0.5f, -0.5f));
        vertices.add(new Vector2fPropertiesHolder(0.5f, 0.5f));
    }
}
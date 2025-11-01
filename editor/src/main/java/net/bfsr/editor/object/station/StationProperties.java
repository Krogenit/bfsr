package net.bfsr.editor.object.station;

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
public class StationProperties extends ObjectProperties {
    @Property
    private Vector2fPropertiesHolder size;
    @Property
    private float destroyTimeInSeconds;
    @Property(elementType = PropertyGuiElementType.FILE_SELECTOR)
    private String texture;
    @Property(elementType = PropertyGuiElementType.POLYGON, arrayElementType = PropertyGuiElementType.INPUT_BOX,
            arrayElementName = "vertex")
    private List<Vector2fPropertiesHolder> vertices;
    @Property
    private float shieldOutlineOffset;
    @Property
    private float shieldBlurSize;

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        size = new Vector2fPropertiesHolder(6.9423f, 6.9423f);
        destroyTimeInSeconds = 1.0f;
        texture = "texture/entity/station/human.png";
        vertices = new ArrayList<>();
        vertices.add(new Vector2fPropertiesHolder(-2.9f, 0.2f));
        vertices.add(new Vector2fPropertiesHolder(-2.9f, -0.2f));
        vertices.add(new Vector2fPropertiesHolder(-1.0f, -3.1f));
        vertices.add(new Vector2fPropertiesHolder(0.6f, -3.1f));
        vertices.add(new Vector2fPropertiesHolder(2.7f, 0.1f));
        vertices.add(new Vector2fPropertiesHolder(0.6f, 3.1f));
        vertices.add(new Vector2fPropertiesHolder(-1.0f, 3.1f));
        shieldOutlineOffset = 1.0f / 64.0f;
        shieldBlurSize = 4.0f;
    }
}
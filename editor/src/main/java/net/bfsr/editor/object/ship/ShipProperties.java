package net.bfsr.editor.object.ship;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.gui.ship.ModulesPolygonsPropertiesHolder;
import net.bfsr.editor.object.ObjectProperties;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.holder.ColorPropertiesHolder;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.editor.property.holder.Vector2iPropertiesHolder;
import net.bfsr.server.dto.Default;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(onConstructor_ = @Default)
@NoArgsConstructor
public class ShipProperties extends ObjectProperties {
    @Property(elementType = PropertyGuiElementType.INPUT_BOX)
    private Vector2fPropertiesHolder size;
    @Property(elementType = PropertyGuiElementType.INPUT_BOX)
    private float destroyTimeInSeconds;
    @Property(elementType = PropertyGuiElementType.FILE_SELECTOR)
    private String texture;
    @Property
    private ColorPropertiesHolder effectsColor;
    @Property(elementType = PropertyGuiElementType.SIMPLE_LIST, arrayElementType = PropertyGuiElementType.INPUT_BOX,
            arrayElementName = "position")
    private List<Vector2fPropertiesHolder> weaponSlotPositions;
    @Property(elementType = PropertyGuiElementType.POLYGON, arrayElementType = PropertyGuiElementType.INPUT_BOX,
            arrayElementName = "vertex")
    private List<Vector2fPropertiesHolder> vertices;
    @Property(elementType = PropertyGuiElementType.INPUT_BOX, name = "verticesMinDistSq")
    private float minDistanceBetweenVerticesSq;
    @Property(elementType = PropertyGuiElementType.INPUT_BOX)
    private Vector2iPropertiesHolder damageMaskSize;
    @Property(elementType = PropertyGuiElementType.OBJECT)
    private ModulesPolygonsPropertiesHolder modules;

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        size = new Vector2fPropertiesHolder(6.9423f, 6.9423f);
        destroyTimeInSeconds = 1.0f;
        texture = "texture/entity/ship/human_small0.png";
        effectsColor = new ColorPropertiesHolder(0.5f, 0.6f, 1.0f, 1.0f);
        weaponSlotPositions = new ArrayList<>();
        weaponSlotPositions.add(new Vector2fPropertiesHolder(0.7f, 2.4f));
        weaponSlotPositions.add(new Vector2fPropertiesHolder(0.7f, -2.4f));
        vertices = new ArrayList<>();
        vertices.add(new Vector2fPropertiesHolder(-2.9f, 0.2f));
        vertices.add(new Vector2fPropertiesHolder(-2.9f, -0.2f));
        vertices.add(new Vector2fPropertiesHolder(-1.0f, -3.1f));
        vertices.add(new Vector2fPropertiesHolder(0.6f, -3.1f));
        vertices.add(new Vector2fPropertiesHolder(2.7f, 0.1f));
        vertices.add(new Vector2fPropertiesHolder(0.6f, 3.1f));
        vertices.add(new Vector2fPropertiesHolder(-1.0f, 3.1f));
        minDistanceBetweenVerticesSq = GameObjectConfigData.MIN_DISTANCE_BETWEEN_VERTICES_SQ;
        damageMaskSize = new Vector2iPropertiesHolder(32, 32);
        modules = new ModulesPolygonsPropertiesHolder();
        modules.setDefaultValues();
    }
}
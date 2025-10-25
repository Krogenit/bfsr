package net.bfsr.editor.gui.ship;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.holder.PolygonPropertiesHolder;
import net.bfsr.editor.property.holder.PropertiesHolderAdapter;
import net.bfsr.engine.math.Direction;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class ModulesPolygonsPropertiesHolder extends PropertiesHolderAdapter {
    @Property(elementType = PropertyGuiElementType.OBJECT)
    private PolygonPropertiesHolder reactor;
    @Property(elementType = PropertyGuiElementType.OBJECT)
    private PolygonPropertiesHolder shield;
    @Property(elementType = PropertyGuiElementType.MAP)
    private Map<Direction, EnginesProperties> engines;

    @Override
    public void setDefaultValues() {
        engines = new HashMap<>();
        reactor = new PolygonPropertiesHolder();
        reactor.setDefaultValues();
        shield = new PolygonPropertiesHolder();
        shield.setDefaultValues();
    }
}
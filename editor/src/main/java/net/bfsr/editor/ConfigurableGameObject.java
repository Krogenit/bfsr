package net.bfsr.editor;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Client;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.event.ChangeNameEventListener;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureData;
import net.bfsr.engine.util.PathHelper;

@Getter
@Setter
public class ConfigurableGameObject implements PropertiesHolder {
    @Property(name = "position", fieldsAmount = 2)
    private float posX, posY;
    @Property(name = "size", fieldsAmount = 2)
    private float sizeX, sizeY;
    @Property(name = "velocity", fieldsAmount = 2)
    private float velocityX, velocityY;
    @Property(elementType = PropertyGuiElementType.FILE_SELECTOR)
    private String texturePath;

    private AbstractTexture texture;

    @Override
    public void setDefaultValues() {
        sizeX = sizeY = 1.0f;
        ShipRegistry shipRegistry = Client.get().getConfigConverterManager().getConverter(ShipRegistry.class);
        ShipData shipData = shipRegistry.get(0);
        texturePath = PathHelper.convertToLocalPath(shipData.getTextureData().getPath());
    }

    public void init() {
        texture = Engine.getAssetsManager().getTexture(new TextureData(PathHelper.convertPath(texturePath)));
    }

    @Override
    public void addChangeNameEventListener(ChangeNameEventListener listener) {}

    @Override
    public void clearListeners() {}
}
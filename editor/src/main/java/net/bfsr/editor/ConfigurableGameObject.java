package net.bfsr.editor;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.property.PropertiesHolder;
import net.bfsr.property.Property;
import net.bfsr.property.PropertyGuiElementType;
import net.bfsr.property.event.ChangeNameEventListener;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.PathHelper;

@SuppressWarnings("TransientFieldInNonSerializableClass")
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

    private transient Texture texture;

    @Override
    public void setDefaultValues() {
        sizeX = sizeY = 7.0f;
        texturePath = PathHelper.convertToLocalPath(TextureRegister.shipHumanSmall0.getPath());
    }

    public void init() {
        texture = TextureLoader.getTexture(PathHelper.convertPath(texturePath));
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return "Game Object";
    }

    @Override
    public void registerChangeNameEventListener(ChangeNameEventListener listener) {

    }

    @Override
    public void clearListeners() {

    }
}
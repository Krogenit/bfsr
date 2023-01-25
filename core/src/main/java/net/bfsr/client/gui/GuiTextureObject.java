package net.bfsr.client.gui;

import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.ModelMatrixType;
import net.bfsr.math.Transformation;
import org.joml.Vector2f;

public class GuiTextureObject extends TextureObject {
    public GuiTextureObject(TextureRegister tex) {
        super(TextureLoader.getTexture(tex), new Vector2f());
        setModelMatrixType(ModelMatrixType.GUI);
    }

    public GuiTextureObject(Texture tex) {
        super(tex, new Vector2f(), new Vector2f(tex.getWidth(), tex.getHeight()));
        setModelMatrixType(ModelMatrixType.GUI);
    }

    @Override
    public void setPosition(float x, float y) {
        this.setPosition(new Vector2f(x, y));
    }

    @Override
    public void setPosition(Vector2f position) {
        this.position = Transformation.getOffsetByScale(position);
    }

    @Override
    public void setScale(float x, float y) {
        this.scale.x = Transformation.guiScale.x * x;
        this.scale.y = Transformation.guiScale.y * y;
    }

    @Override
    public void setScale(Vector2f scale) {
        this.scale = new Vector2f(Transformation.guiScale.x * scale.x, Transformation.guiScale.y * scale.y);
    }
}

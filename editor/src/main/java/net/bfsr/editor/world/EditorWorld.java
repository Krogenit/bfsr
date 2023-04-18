package net.bfsr.editor.world;

import lombok.Setter;
import net.bfsr.client.entity.TextureObject;
import net.bfsr.client.renderer.SpriteRenderer;
import net.bfsr.client.renderer.buffer.BufferType;
import net.bfsr.client.world.WorldClient;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class EditorWorld extends WorldClient {
    @Setter
    private TextureObject testObject;

    @Override
    public void renderEntitiesAlpha() {
        super.renderEntitiesAlpha();

        if (testObject != null) {
            Vector2f position = testObject.getPosition();
            Vector2f scale = testObject.getScale();
            Vector4f color = testObject.getColor();
            SpriteRenderer.get().add(position.x, position.y, scale.x, scale.y,
                    color.x, color.y, color.z, color.w, testObject.getTexture(), BufferType.ENTITIES_ALPHA);
        }
    }
}
package net.bfsr.client.renderer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureLoader;
import net.bfsr.engine.util.AABB;
import net.bfsr.entity.GameObject;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

@Getter
@NoArgsConstructor
public class Render<T extends GameObject> {
    protected T object;
    @Setter
    protected AbstractTexture texture;
    protected final Vector2f lastSize = new Vector2f();
    protected final Vector2f lastPosition = new Vector2f();
    protected final Vector4f color = new Vector4f();
    protected final Vector4f lastColor = new Vector4f();
    @Getter
    protected float lastSin, lastCos;
    protected final AABB aabb = new AABB();
    protected final AbstractRenderer renderer = Engine.renderer;
    protected final AbstractSpriteRenderer spriteRenderer = renderer.spriteRenderer;
    protected final AbstractDebugRenderer debugRenderer = renderer.debugRenderer;

    public Render(AbstractTexture texture, T object, float r, float g, float b, float a) {
        this.object = object;
        this.texture = texture;
        this.lastPosition.set(object.getPosition());
        this.color.set(r, g, b, a);
        this.lastColor.set(r, g, b, a);
        this.lastSize.set(object.getSize());
    }

    public Render(AbstractTexture texture, T gameObject) {
        this(texture, gameObject, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public Render(T gameObject) {
        this(AbstractTextureLoader.dummyTexture, gameObject);
    }

    public void update() {}

    public void postWorldUpdate() {
        updateAABB();
    }

    public void renderAlpha() {}

    public void renderAdditive() {}

    public void renderDebug() {}

    protected void updateAABB() {
        Vector2f position = object.getPosition();
        Vector2f size = object.getSize();
        float halfSizeX = size.x / 2;
        float halfSizeY = size.y / 2;
        aabb.set(position.x - halfSizeX, position.y - halfSizeY, position.x + halfSizeX, position.y + halfSizeY);
    }

    public void updateDamageMask(int x, int y, int width, int height, ByteBuffer byteBuffer) {}

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public boolean isDead() {
        return object.isDead();
    }

    public DamageMaskTexture getMaskTexture() {
        return null;
    }

    public void clear() {}
}
package net.bfsr.client.renderer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureLoader;
import net.bfsr.entity.GameObject;
import org.jbox2d.collision.AABB;
import org.joml.Vector2f;
import org.joml.Vector4f;

@Getter
@NoArgsConstructor
public class Render {
    protected GameObject object;
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
    protected int id = -1;

    public Render(AbstractTexture texture, GameObject object, float r, float g, float b, float a) {
        this.object = object;
        this.texture = texture;
        this.lastPosition.set(object.getX(), object.getY());
        this.color.set(r, g, b, a);
        this.lastColor.set(r, g, b, a);
        this.lastSize.set(object.getSizeX(), object.getSizeY());
    }

    public Render(AbstractTexture texture, GameObject object) {
        this(texture, object, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public Render(GameObject object) {
        this(AbstractTextureLoader.dummyTexture, object);
    }

    public void init() {
        id = spriteRenderer.add(object.getX(), object.getY(), object.getSizeX(), object.getSizeY(), color.x, color.y, color.z, color.w,
                texture.getTextureHandle(), BufferType.ENTITIES_ALPHA);
    }

    public void update() {
        updateLastRenderValues();
    }

    public void postWorldUpdate() {
        updateAABB();
        updateRenderValues();
    }

    protected void updateLastRenderValues() {
        spriteRenderer.setLastPosition(id, BufferType.ENTITIES_ALPHA, object.getX(), object.getY());
    }

    protected void updateRenderValues() {
        spriteRenderer.setPosition(id, BufferType.ENTITIES_ALPHA, object.getX(), object.getY());
    }

    public void renderAlpha() {}

    public void renderAdditive() {}

    public void renderDebug() {}

    protected void updateAABB() {
        float x = object.getX();
        float y = object.getY();
        float halfSizeX = object.getSizeX() / 2;
        float halfSizeY = object.getSizeY() / 2;
        aabb.set(x - halfSizeX, y - halfSizeY, x + halfSizeX, y + halfSizeY);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public void setTexture(AbstractTexture texture) {
        this.texture = texture;
        spriteRenderer.setTexture(id, BufferType.ENTITIES_ALPHA, texture.getTextureHandle());
    }

    public boolean isDead() {
        return object.isDead();
    }

    public void clear() {
        if (id != -1) {
            spriteRenderer.removeObject(id, BufferType.ENTITIES_ALPHA);
        }
    }
}
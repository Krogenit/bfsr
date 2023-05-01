package net.bfsr.client.renderer.render;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.client.core.Core;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.RigidBody;
import net.bfsr.math.RotationHelper;
import org.dyn4j.geometry.AABB;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

@Getter
@NoArgsConstructor
public class Render<T extends GameObject> {
    protected T object;
    @Setter
    protected Texture texture;
    protected final Vector2f lastSize = new Vector2f();
    protected final Vector2f lastPosition = new Vector2f();
    protected final Vector4f color = new Vector4f();
    protected final Vector4f lastColor = new Vector4f();
    @Getter
    protected float lastSin, lastCos;
    protected final AABB aabb = new AABB(0, 0, 0, 0);

    public Render(Texture texture, T object, float r, float g, float b, float a) {
        this.object = object;
        this.texture = texture;
        this.lastPosition.set(object.getPosition());
        this.color.set(r, g, b, a);
        this.lastColor.set(r, g, b, a);
        this.lastSize.set(object.getSize());
    }

    public Render(Texture texture, T gameObject) {
        this(texture, gameObject, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public Render(T gameObject) {
        this(TextureLoader.dummyTexture, gameObject);
    }

    public void update() {}

    public void postWorldUpdate() {
        updateAABB();
    }

    public void renderAlpha() {}

    public void renderAdditive() {}

    public void renderDebug() {
        if (object instanceof RigidBody rigidBody) {
            Core.get().getRenderer().getDebugRenderer().render(rigidBody);
        }
    }

    protected void updateAABB() {
        Vector2f position = object.getPosition();
        Vector2f size = object.getSize();
        float halfSizeX = size.x / 2;
        float halfSizeY = size.y / 2;
        aabb.set(position.x - halfSizeX, position.y - halfSizeY, position.x + halfSizeX, position.y + halfSizeY);
    }

    protected void updateAABB(float sin, float cos) {
        Vector2f position = object.getPosition();
        Vector2f size = object.getSize();
        float halfSizeX = size.x / 2;
        float halfSizeY = size.y / 2;
        RotationHelper.rotateAABB(sin, cos, -halfSizeX, -halfSizeY, halfSizeX, halfSizeY, position.x, position.y, aabb);
    }

    public void updateDamageMask(int x, int y, int width, int height, ByteBuffer byteBuffer) {}

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public Vector2f getScale() {
        return object.getSize();
    }

    public boolean isDead() {
        return object.isDead();
    }

    public DamageMaskTexture getMaskTexture() {
        return null;
    }

    public void clear() {}
}
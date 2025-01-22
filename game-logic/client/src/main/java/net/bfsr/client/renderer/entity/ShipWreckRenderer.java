package net.bfsr.client.renderer.entity;

import net.bfsr.client.renderer.Render;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ShipWreckRenderer extends DamageableRigidBodyRenderer {
    private final ShipWreck wreck;
    private final List<Render> connectedObjectRenders = new ArrayList<>();
    private final Vector2f localOffsetRotated = new Vector2f();

    public ShipWreckRenderer(ShipWreck wreck, Path texturePath) {
        super(Engine.assetsManager.getTexture(texturePath), wreck, 0.25f, 0.25f, 0.25f, 1.0f);
        this.wreck = wreck;

        List<ConnectedObject<?>> connectedObjects = wreck.getConnectedObjects();
        for (int i = 0; i < connectedObjects.size(); i++) {
            connectedObjectRenders.add(new ConnectedObjectRenderer(connectedObjects.get(i)));
        }
    }

    @Override
    public void init() {
        float sin = rigidBody.getSin();
        float cos = rigidBody.getCos();
        float localOffsetX = wreck.getLocalOffsetX();
        float localOffsetY = wreck.getLocalOffsetY();
        RotationHelper.rotate(sin, cos, localOffsetX, localOffsetY, localOffsetRotated);
        id = spriteRenderer.add(rigidBody.getX() - localOffsetRotated.x, rigidBody.getY() - localOffsetRotated.y, sin, cos,
                object.getSizeX(), object.getSizeY(), color.x, color.y, color.z, color.w, texture.getTextureHandle(),
                maskTexture.getTextureHandle(), BufferType.ENTITIES_ALPHA);

        for (int i = 0; i < connectedObjectRenders.size(); i++) {
            connectedObjectRenders.get(i).init();
        }
    }

    @Override
    public void update() {
        super.update();

        for (int i = 0; i < connectedObjectRenders.size(); i++) {
            connectedObjectRenders.get(i).update();
        }
    }

    @Override
    public void postWorldUpdate() {
        super.postWorldUpdate();

        for (int i = 0; i < connectedObjectRenders.size(); i++) {
            connectedObjectRenders.get(i).postWorldUpdate();
        }
    }

    @Override
    protected void updateLastRenderValues() {
        super.updateLastRenderValues();
        float sin = rigidBody.getSin();
        float cos = rigidBody.getCos();
        float localOffsetX = wreck.getLocalOffsetX();
        float localOffsetY = wreck.getLocalOffsetY();
        RotationHelper.rotate(sin, cos, localOffsetX, localOffsetY, localOffsetRotated);
        spriteRenderer.setLastPosition(id, BufferType.ENTITIES_ALPHA, rigidBody.getX() - localOffsetRotated.x,
                rigidBody.getY() - localOffsetRotated.y);
        spriteRenderer.setLastRotation(id, BufferType.ENTITIES_ALPHA, sin, cos);
    }

    @Override
    protected void updateRenderValues() {
        super.updateRenderValues();
        float sin = rigidBody.getSin();
        float cos = rigidBody.getCos();
        float localOffsetX = wreck.getLocalOffsetX();
        float localOffsetY = wreck.getLocalOffsetY();
        RotationHelper.rotate(sin, cos, localOffsetX, localOffsetY, localOffsetRotated);
        spriteRenderer.setPosition(id, BufferType.ENTITIES_ALPHA, rigidBody.getX() - localOffsetRotated.x,
                rigidBody.getY() - localOffsetRotated.y);
        spriteRenderer.setRotation(id, BufferType.ENTITIES_ALPHA, sin, cos);
    }

    @Override
    protected void updateAABB() {
        super.updateAABB();
        float offset = 0.5f;
        aabb.lowerBound.addLocal(-offset, -offset);
        aabb.upperBound.addLocal(offset, offset);
    }

    @Override
    public void renderAlpha() {
        super.renderAlpha();

        for (int i = 0; i < connectedObjectRenders.size(); i++) {
            connectedObjectRenders.get(i).renderAlpha();
        }
    }

    @Override
    public void clear() {
        super.clear();

        for (int i = 0; i < connectedObjectRenders.size(); i++) {
            connectedObjectRenders.get(i).clear();
        }
    }
}
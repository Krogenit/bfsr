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
    public void renderAlpha() {
        float sin = rigidBody.getSin();
        float cos = rigidBody.getCos();
        float localOffsetX = wreck.getLocalOffsetX();
        float localOffsetY = wreck.getLocalOffsetY();
        RotationHelper.rotate(sin, cos, localOffsetX, localOffsetY, localOffsetRotated);
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x - localOffsetRotated.x, lastPosition.y - localOffsetRotated.y,
                rigidBody.getX() - localOffsetRotated.x, rigidBody.getY() - localOffsetRotated.y, lastSin, lastCos, sin, cos,
                object.getSizeX(), object.getSizeY(), color.x, color.y, color.z, color.w, texture, maskTexture, BufferType.ENTITIES_ALPHA);

        for (int i = 0; i < connectedObjectRenders.size(); i++) {
            connectedObjectRenders.get(i).renderAlpha();
        }
    }
}
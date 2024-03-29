package net.bfsr.client.renderer.entity;

import net.bfsr.client.renderer.Render;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class ShipWreckRenderer extends DamageableRigidBodyRenderer<ShipWreck> {
    private final List<Render<?>> connectedObjectRenders = new ArrayList<>();
    private final Vector2f localOffsetRotated = new Vector2f();

    public ShipWreckRenderer(ShipWreck wreck) {
        super(Engine.assetsManager.getTexture(ShipRegistry.INSTANCE.get(wreck.getDataId()).getTexture()), wreck, 0.25f,
                0.25f, 0.25f, 1.0f);

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
        Vector2f position = object.getPosition();
        float sin = object.getSin();
        float cos = object.getCos();
        Vector2f scale = object.getSize();
        float localOffsetX = object.getLocalOffsetX();
        float localOffsetY = object.getLocalOffsetY();
        RotationHelper.rotate(sin, cos, localOffsetX, localOffsetY, localOffsetRotated);
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x - localOffsetRotated.x, lastPosition.y - localOffsetRotated.y,
                position.x - localOffsetRotated.x, position.y - localOffsetRotated.y, lastSin, lastCos, sin, cos, scale.x,
                scale.y, color.x, color.y, color.z, color.w, texture, maskTexture, BufferType.ENTITIES_ALPHA);

        for (int i = 0; i < connectedObjectRenders.size(); i++) {
            connectedObjectRenders.get(i).renderAlpha();
        }
    }
}
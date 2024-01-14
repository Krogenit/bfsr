package net.bfsr.client.renderer.entity;

import net.bfsr.client.renderer.Render;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.engine.Engine;
import net.bfsr.entity.wreck.ShipWreck;

import java.util.ArrayList;
import java.util.List;

public class ShipWreckRenderer extends DamageableRigidBodyRenderer<ShipWreck> {
    private final List<Render<?>> connectedObjectRenders = new ArrayList<>();

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
        super.renderAlpha();

        for (int i = 0; i < connectedObjectRenders.size(); i++) {
            connectedObjectRenders.get(i).renderAlpha();
        }
    }
}
package net.bfsr.client.renderer.entity;

import lombok.Getter;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.SimpleConnectedObject;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.entity.wreck.ShipWreck;
import org.dyn4j.dynamics.Body;
import org.joml.Vector2f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ShipWreckRenderer extends RigidBodyRender<ShipWreck> {
    @Getter
    private final DamageMaskTexture maskTexture;
    private final List<Render<?>> connectedObjectRenders = new ArrayList<>();

    public ShipWreckRenderer(ShipWreck wreck) {
        super(Engine.assetsManager.getTexture(ShipRegistry.INSTANCE.get(wreck.getDataId()).getTexture()), wreck, 0.25f,
                0.25f, 0.25f, 1.0f);

        DamageMask mask = wreck.getMask();
        maskTexture = new DamageMaskTexture(mask.getWidth(), mask.getHeight(),
                renderer.createByteBuffer(mask.getWidth() * mask.getHeight()));
        maskTexture.createEmpty();

        List<ConnectedObject> connectedObjects = wreck.getConnectedObjects();
        for (int i = 0; i < connectedObjects.size(); i++) {
            SimpleConnectedObject connectedObject = (SimpleConnectedObject) connectedObjects.get(i);
            connectedObjectRenders.add(
                    new Render<>(Engine.assetsManager.getTexture(connectedObject.getConfigData().getTexture()), connectedObject) {
                        private final Vector2f position = new Vector2f();

                        @Override
                        public void update() {
                            lastPosition.x = position.x;
                            lastPosition.y = position.y;
                        }

                        @Override
                        public void postWorldUpdate() {
                            ShipWreck shipWreck = ShipWreckRenderer.this.object;
                            float sin = shipWreck.getSin();
                            float cos = shipWreck.getCos();
                            position.x = cos * object.getConnectPointX() - sin * object.getConnectPointY();
                            position.y = sin * object.getConnectPointX() + cos * object.getConnectPointY();
                        }

                        @Override
                        public void renderAlpha() {
                            ShipWreck shipWreck = ShipWreckRenderer.this.object;
                            Body body = shipWreck.getBody();
                            float x = (float) body.getTransform().getTranslationX();
                            float y = (float) body.getTransform().getTranslationY();
                            Vector2f size = object.getSize();

                            spriteRenderer.addToRenderPipeLineSinCos(ShipWreckRenderer.this.lastPosition.x + lastPosition.x,
                                    ShipWreckRenderer.this.lastPosition.y + lastPosition.y, x + position.x, y + position.y,
                                    ShipWreckRenderer.this.lastSin, ShipWreckRenderer.this.lastCos, shipWreck.getSin(),
                                    shipWreck.getCos(), size.x, size.y, 0.25f, 0.25f, 0.25f, 1.0f, texture,
                                    BufferType.ENTITIES_ALPHA);
                        }
                    });
        }
    }

    @Override
    public void update() {
        Body body = object.getBody();
        lastPosition.x = (float) body.getTransform().getTranslationX();
        lastPosition.y = (float) body.getTransform().getTranslationY();
        lastSin = (float) body.getTransform().getSint();
        lastCos = (float) body.getTransform().getCost();
        maskTexture.updateEffects();

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
    protected void updateAABB() {

    }

    @Override
    public void renderAlpha() {
        Body body = object.getBody();
        float x = (float) body.getTransform().getTranslationX();
        float y = (float) body.getTransform().getTranslationY();
        Vector2f size = object.getSize();
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, x, y, lastSin, lastCos, object.getSin(),
                object.getCos(), size.x, size.y, 0.25f, 0.25f, 0.25f, 1.0f, texture, maskTexture, BufferType.ENTITIES_ALPHA);

        for (int i = 0; i < connectedObjectRenders.size(); i++) {
            connectedObjectRenders.get(i).renderAlpha();
        }
    }

    @Override
    public void updateDamageMask(int x, int y, int width, int height, ByteBuffer byteBuffer) {
        maskTexture.upload(x, y, width, height, byteBuffer);
    }

    @Override
    public void clear() {
        maskTexture.delete();
    }
}
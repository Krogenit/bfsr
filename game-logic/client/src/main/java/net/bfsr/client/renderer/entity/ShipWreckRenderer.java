package net.bfsr.client.renderer.entity;

import lombok.Getter;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.entity.wreck.ShipWreck;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.AABB;
import org.joml.Vector2f;

import java.nio.ByteBuffer;

public class ShipWreckRenderer extends Render<ShipWreck> {
    private final AABB geometryAABB = new AABB(0, 0, 0, 0);
    @Getter
    private final DamageMaskTexture maskTexture;

    public ShipWreckRenderer(ShipWreck object) {
        super(Engine.assetsManager.textureLoader.getTexture(ShipRegistry.INSTANCE.get(object.getDataIndex()).getTexture()), object, 0.25f, 0.25f, 0.25f, 1.0f);

        maskTexture = new DamageMaskTexture(texture.getWidth(), texture.getHeight(), Engine.renderer.createByteBuffer(texture.getWidth() * texture.getHeight()));
        maskTexture.createEmpty();
    }

    @Override
    public void update() {
        Body body = object.getBody();
        lastPosition.x = (float) body.getTransform().getTranslationX();
        lastPosition.y = (float) body.getTransform().getTranslationY();
        lastSin = (float) body.getTransform().getSint();
        lastCos = (float) body.getTransform().getCost();
        maskTexture.updateEffects();
    }

    @Override
    public void postWorldUpdate() {
        Vector2f position = object.getPosition();
        aabb.set(geometryAABB.getMinX() + position.x, geometryAABB.getMinY() + position.y,
                geometryAABB.getMaxX() + position.x, geometryAABB.getMaxY() + position.y);
    }

    public void computeAABB() {
        MathUtils.computeAABB(object.getBody(), geometryAABB);
    }

    @Override
    public void renderAlpha() {
        Body body = object.getBody();
        float x = (float) body.getTransform().getTranslationX();
        float y = (float) body.getTransform().getTranslationY();
        Vector2f size = object.getSize();
        Engine.renderer.spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, x, y, lastSin, lastCos, object.getSin(), object.getCos(),
                size.x, size.y, 0.25f, 0.25f, 0.25f, 1.0f, texture, maskTexture, BufferType.ENTITIES_ALPHA);
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
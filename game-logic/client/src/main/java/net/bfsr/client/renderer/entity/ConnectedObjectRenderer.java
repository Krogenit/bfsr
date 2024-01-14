package net.bfsr.client.renderer.entity;

import net.bfsr.client.renderer.Render;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.entity.GameObject;
import org.joml.Vector2f;

public class ConnectedObjectRenderer extends Render<GameObject> {
    private final ConnectedObject<?> connectedObject;

    ConnectedObjectRenderer(ConnectedObject<?> connectedObject) {
        super(Engine.assetsManager.getTexture(connectedObject.getConfigData().getTexture()), new GameObject());
        this.connectedObject = connectedObject;
    }

    @Override
    public void update() {
        Vector2f position = connectedObject.getPosition();
        lastPosition.x = position.x;
        lastPosition.y = position.y;
        lastSin = connectedObject.getSin();
        lastCos = connectedObject.getCos();
    }

    @Override
    public void postWorldUpdate() {}

    @Override
    public void renderAlpha() {
        Vector2f position = connectedObject.getPosition();
        Vector2f size = connectedObject.getSize();
        float sin = connectedObject.getSin();
        float cos = connectedObject.getCos();

        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin,
                cos, size.x, size.y, 0.25f, 0.25f, 0.25f, 1.0f, texture, BufferType.ENTITIES_ALPHA);
    }
}
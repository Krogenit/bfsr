package net.bfsr.client.renderer.entity;

import net.bfsr.client.renderer.Render;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.entity.GameObject;

public class ConnectedObjectRenderer extends Render {
    private final ConnectedObject<?> connectedObject;

    ConnectedObjectRenderer(ConnectedObject<?> connectedObject) {
        super(Engine.assetsManager.getTexture(connectedObject.getConfigData().getTexture()), new GameObject());
        this.connectedObject = connectedObject;
    }

    @Override
    public void update() {
        lastPosition.set(connectedObject.getX(), connectedObject.getY());
        lastSin = connectedObject.getSin();
        lastCos = connectedObject.getCos();
    }

    @Override
    public void postWorldUpdate() {}

    @Override
    public void renderAlpha() {
        float sin = connectedObject.getSin();
        float cos = connectedObject.getCos();

        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, connectedObject.getX(), connectedObject.getY(), lastSin,
                lastCos, sin, cos, connectedObject.getSizeX(), connectedObject.getSizeY(), 0.25f, 0.25f, 0.25f, 1.0f, texture,
                BufferType.ENTITIES_ALPHA);
    }
}
package net.bfsr.client.renderer.entity;

import net.bfsr.client.renderer.Render;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.engine.Engine;
import net.bfsr.engine.entity.GameObject;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;

public class ConnectedObjectRenderer extends Render {
    private final ConnectedObject<?> connectedObject;

    ConnectedObjectRenderer(ConnectedObject<?> connectedObject) {
        super(Engine.getAssetsManager().getTexture(connectedObject.getConfigData().getTexture()), new GameObject());
        this.connectedObject = connectedObject;
    }

    @Override
    public void init() {
        id = spriteRenderer.add(connectedObject.getX(), connectedObject.getY(), connectedObject.getSin(),
                connectedObject.getCos(), connectedObject.getSizeX(), connectedObject.getSizeY(), 0.25f, 0.25f, 0.25f, 1.0f,
                texture.getTextureHandle(), BufferType.ENTITIES_ALPHA);
    }

    @Override
    public void postWorldUpdate() {
        updateRenderValues();
    }

    @Override
    protected void updateLastRenderValues() {
        spriteRenderer.setLastPosition(id, BufferType.ENTITIES_ALPHA, connectedObject.getX(), connectedObject.getY());
        spriteRenderer.setLastRotation(id, BufferType.ENTITIES_ALPHA, connectedObject.getSin(), connectedObject.getCos());
    }

    @Override
    protected void updateRenderValues() {
        spriteRenderer.setPosition(id, BufferType.ENTITIES_ALPHA, connectedObject.getX(), connectedObject.getY());
        spriteRenderer.setRotation(id, BufferType.ENTITIES_ALPHA, connectedObject.getSin(), connectedObject.getCos());
    }

    @Override
    public void renderAlpha() {
        spriteRenderer.addDrawCommand(id, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.ENTITIES_ALPHA);
    }
}
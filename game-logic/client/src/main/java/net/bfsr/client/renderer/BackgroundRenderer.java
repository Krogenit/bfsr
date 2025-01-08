package net.bfsr.client.renderer;

import net.bfsr.client.Client;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureLoader;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.event.world.WorldInitEvent;

import java.util.Random;

public class BackgroundRenderer {
    private final AbstractSpriteRenderer spriteRenderer = Engine.renderer.spriteRenderer;
    private AbstractTexture texture = AbstractTextureLoader.dummyTexture;
    private Runnable renderRunnable = RunnableUtils.EMPTY_RUNNABLE;

    private int id;

    public void init() {
        Client.get().getEventBus().register(this);
    }

    @EventHandler
    public EventListener<WorldInitEvent> event() {
        return (event) -> createBackgroundTexture(event.getWorld().getSeed());
    }

    void createBackgroundTexture(long seed) {
        texture = Engine.renderer.textureGenerator.generateNebulaTexture(4096, 4096, new Random(seed));
        renderRunnable = this::renderBackground;

        float zoomFactor = 0.005f;
        id = spriteRenderer.add(0, 0, texture.getWidth() * 0.5f, texture.getHeight() * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                texture.getTextureHandle(),
                zoomFactor, BufferType.BACKGROUND);
    }

    public void update() {
    }

    public void render() {
        renderRunnable.run();
    }

    private void renderBackground() {
        spriteRenderer.addDrawCommand(id, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.BACKGROUND);
    }

    public void clear() {
        texture.delete();
        texture = AbstractTextureLoader.dummyTexture;
        renderRunnable = RunnableUtils.EMPTY_RUNNABLE;
        spriteRenderer.removeObject(id, BufferType.BACKGROUND);
    }
}

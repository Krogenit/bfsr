package net.bfsr.client.renderer;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.event.world.WorldInitEvent;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.util.RunnableUtils;

public class BackgroundRenderer {
    private final AbstractRenderer renderer;
    private final AbstractSpriteRenderer spriteRenderer;
    private AbstractTexture texture;
    private Runnable renderRunnable = RunnableUtils.EMPTY_RUNNABLE;

    private int renderId = -1;

    BackgroundRenderer(AbstractRenderer renderer) {
        this.renderer = renderer;
        this.spriteRenderer = renderer.getSpriteRenderer();
        this.texture = renderer.getDummyTexture();
    }

    @EventHandler
    public EventListener<WorldInitEvent> event() {
        return event -> createBackgroundTexture(event.getWorld().getSeed());
    }

    void createBackgroundTexture(long seed) {
        texture = renderer.getTextureGenerator().generateNebulaTexture(4096, 4096, new XoRoShiRo128PlusRandom(seed), renderer);
        renderRunnable = this::renderBackground;

        float zoomFactor = 0.005f;
        renderId = spriteRenderer.add(0, 0, texture.getWidth() * 0.5f, texture.getHeight() * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                texture.getTextureHandle(), zoomFactor, BufferType.BACKGROUND);
    }

    public void render() {
        renderRunnable.run();
    }

    private void renderBackground() {
        spriteRenderer.addDrawCommand(renderId, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.BACKGROUND);
    }

    public void clear() {
        texture.delete();
        texture = renderer.getDummyTexture();
        renderRunnable = RunnableUtils.EMPTY_RUNNABLE;

        if (renderId != -1) {
            spriteRenderer.removeObject(renderId, BufferType.BACKGROUND);
            renderId = -1;
        }
    }
}

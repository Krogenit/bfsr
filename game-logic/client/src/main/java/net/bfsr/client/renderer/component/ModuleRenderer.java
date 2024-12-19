package net.bfsr.client.renderer.component;

import net.bfsr.client.renderer.Render;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.engine.Engine;
import net.bfsr.math.Direction;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.common.Rotation;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vector2;

public class ModuleRenderer extends Render {
    private float x, y;
    private float sin, cos;
    private final Runnable updateRunnable;
    private final float sizeX, sizeY;

    public ModuleRenderer(Ship ship, DamageableModule module, AbstractTexture texture) {
        super(texture, module);

        Polygon polygon = (Polygon) module.getFixture().getShape();
        Vector2 center = polygon.centroid;
        AABB aabb1 = new AABB();
        polygon.computeAABB(aabb1, new Transform(new Vector2(), new Rotation(0)), 0);
        float dx = aabb1.getWidth();
        float dy = aabb1.getHeight();

        if (dy > dx) {
            sizeX = dx;
            sizeY = dy;

            updateRunnable = () -> {
                sin = ship.getSin();
                cos = ship.getCos();
                x = cos * center.x - sin * center.y + ship.getX();
                y = sin * center.x + cos * center.y + ship.getY();
            };
        } else {
            sizeX = dy;
            sizeY = dx;

            float sin1 = LUT.sin(MathUtils.HALF_PI);
            float cos1 = LUT.cos(MathUtils.HALF_PI);

            updateRunnable = () -> {
                float sin = ship.getSin();
                float cos = ship.getCos();
                this.cos = cos1 * cos - sin1 * sin;
                this.sin = sin1 * cos + cos1 * sin;
                x = cos * center.x - sin * center.y + ship.getX();
                y = sin * center.x + cos * center.y + ship.getY();
            };
        }
    }

    public ModuleRenderer(Ship ship, Engine engine, AbstractTexture engineTexture, Direction direction) {
        super(engineTexture, engine);

        Polygon shape = (Polygon) engine.getFixture().getShape();
        Vector2 center = shape.centroid;
        AABB aabb1 = new AABB();
        shape.computeAABB(aabb1, new Transform(new Vector2(), new Rotation(0)), 0);
        float dx = aabb1.getWidth();
        float dy = aabb1.getHeight();

        if (dy > dx) {
            sizeX = dy;
            sizeY = dx;
        } else {
            sizeX = dx;
            sizeY = dy;
        }

        if (direction == Direction.RIGHT) {
            updateRunnable = () -> {
                sin = ship.getSin();
                cos = ship.getCos();
                x = cos * center.x - sin * center.y + ship.getX();
                y = sin * center.x + cos * center.y + ship.getY();
            };
        } else {
            float sin1;
            float cos1;
            if (direction == Direction.FORWARD) {
                sin1 = LUT.sin(-MathUtils.HALF_PI);
                cos1 = LUT.cos(-MathUtils.HALF_PI);
            } else if (direction == Direction.BACKWARD) {
                sin1 = LUT.sin(MathUtils.HALF_PI);
                cos1 = LUT.cos(MathUtils.HALF_PI);
            } else if (direction == Direction.LEFT) {
                sin1 = LUT.sin(MathUtils.PI);
                cos1 = LUT.cos(MathUtils.PI);
            } else {
                sin1 = 0;
                cos1 = 1;
            }

            updateRunnable = () -> {
                float sin = ship.getSin();
                float cos = ship.getCos();
                this.cos = cos1 * cos - sin1 * sin;
                this.sin = sin1 * cos + cos1 * sin;
                x = cos * center.x - sin * center.y + ship.getX();
                y = sin * center.x + cos * center.y + ship.getY();
            };
        }
    }

    @Override
    public void init() {
        updateRunnable.run();
        id = spriteRenderer.add(x, y, sin, cos, sizeX, sizeY, color.x, color.y, color.z, color.w,
                texture.getTextureHandle(), BufferType.ENTITIES_ALPHA);
    }

    @Override
    protected void updateLastRenderValues() {
        spriteRenderer.setLastPosition(id, BufferType.ENTITIES_ALPHA, x, y);
        spriteRenderer.setLastRotation(id, BufferType.ENTITIES_ALPHA, sin, cos);
    }

    @Override
    protected void updateRenderValues() {
        spriteRenderer.setPosition(id, BufferType.ENTITIES_ALPHA, x, y);
        spriteRenderer.setRotation(id, BufferType.ENTITIES_ALPHA, sin, cos);
    }

    @Override
    public void postWorldUpdate() {
        updateRunnable.run();
        updateRenderValues();
    }

    @Override
    public void renderAlpha() {
        spriteRenderer.addDrawCommand(id, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.ENTITIES_ALPHA);
    }
}
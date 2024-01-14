package net.bfsr.client.renderer.component;

import net.bfsr.client.renderer.Render;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.Module;
import net.bfsr.entity.ship.module.engine.Engine;
import net.bfsr.math.Direction;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class ModuleRenderer extends Render<Module> {
    private float x, y;
    private float sin, cos;
    private final Vector2f shipPosition;
    private final Runnable updateRunnable;
    private final float sizeX, sizeY;

    public ModuleRenderer(Ship ship, DamageableModule module, AbstractTexture texture) {
        super(texture, module);
        this.shipPosition = ship.getPosition();

        Convex convex = module.getFixture().getShape();
        Vector2 center = convex.getCenter();
        float centerX = (float) center.x;
        float centerY = (float) center.y;
        org.dyn4j.geometry.AABB aabb1 = new AABB(0, 0, 0, 0);
        convex.computeAABB(aabb1);
        float dx = (float) (aabb1.getMaxX() - aabb1.getMinX());
        float dy = (float) (aabb1.getMaxY() - aabb1.getMinY());

        if (dy > dx) {
            sizeX = dx;
            sizeY = dy;

            updateRunnable = () -> {
                sin = ship.getSin();
                cos = ship.getCos();
                x = cos * centerX - sin * centerY + shipPosition.x;
                y = sin * centerX + cos * centerY + shipPosition.y;
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
                x = cos * centerX - sin * centerY + shipPosition.x;
                y = sin * centerX + cos * centerY + shipPosition.y;
            };
        }
    }

    public ModuleRenderer(Ship ship, Engine engine, AbstractTexture engineTexture, Direction direction) {
        super(engineTexture, engine);
        this.shipPosition = ship.getPosition();

        Convex convex = engine.getFixture().getShape();
        Vector2 center = convex.getCenter();
        float centerX = (float) center.x;
        float centerY = (float) center.y;
        org.dyn4j.geometry.AABB aabb1 = new AABB(0, 0, 0, 0);
        convex.computeAABB(aabb1);
        float dx = (float) (aabb1.getMaxX() - aabb1.getMinX());
        float dy = (float) (aabb1.getMaxY() - aabb1.getMinY());

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
                x = cos * centerX - sin * centerY + shipPosition.x;
                y = sin * centerX + cos * centerY + shipPosition.y;
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
                x = cos * centerX - sin * centerY + shipPosition.x;
                y = sin * centerX + cos * centerY + shipPosition.y;
            };
        }
    }

    @Override
    public void update() {
        lastPosition.x = x;
        lastPosition.y = y;
        lastSin = sin;
        lastCos = cos;
    }

    @Override
    public void postWorldUpdate() {
        updateRunnable.run();
    }

    @Override
    public void renderAlpha() {
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, x, y, lastSin, lastCos, sin, cos, sizeX, sizeY,
                1.0f, 1.0f, 1.0f, 1.0f, texture, BufferType.ENTITIES_ALPHA);
    }
}
package net.bfsr.client.entity.wreck;

import clipper2.core.PathsD;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.client.collision.filter.ShipWreckFilter;
import net.bfsr.client.core.Core;
import net.bfsr.client.damage.Damagable;
import net.bfsr.client.entity.CollisionObject;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.physics.PhysicsUtils;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ShipWreckDamagable extends CollisionObject implements Damagable {
    @Getter
    private final DamageMaskTexture maskTexture;
    @Getter
    @Setter
    private PathsD contours;
    @Getter
    private final List<BodyFixture> fixturesToAdd = new ArrayList<>();
    private final int maxLifeTime = 1200;

    public ShipWreckDamagable(int id, float x, float y, float sin, float cos, float scaleX, float scaleY, Texture texture, DamageMaskTexture maskTexture, PathsD contours) {
        super(Core.get().getWorld(), id, x, y, sin, cos, scaleX, scaleY, 0.25f, 0.25f, 0.25f, 1.0f, texture);
        this.maskTexture = maskTexture;
        this.contours = contours;
    }

    @Override
    public void setupFixture(BodyFixture bodyFixture) {
        bodyFixture.setFilter(new ShipWreckFilter(this));
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
    }

    @Override
    public void update() {
        lastPosition.x = (float) body.getTransform().getTranslationX();
        lastPosition.y = (float) body.getTransform().getTranslationY();
        lastSin = (float) body.getTransform().getSint();
        lastCos = (float) body.getTransform().getCost();
        maskTexture.updateEffects();

        if (lifeTime++ >= maxLifeTime) {
            setDead();
        }
    }

    public void render() {
        float x = (float) body.getTransform().getTranslationX();
        float y = (float) body.getTransform().getTranslationY();
        SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, x, y, lastSin, lastCos, sin, cos, scale.x, scale.y, scale.x, scale.y,
                0.25f, 0.25f, 0.25f, 1.0f, texture, maskTexture, BufferType.ENTITIES_ALPHA);
    }

    @Override
    public void setFixtures(List<BodyFixture> fixtures) {
        Damagable.super.setFixtures(fixtures);
        AABB aabb = computeAABB();
        this.aabb.set((float) aabb.getMinX(), (float) aabb.getMinY(), (float) aabb.getMaxX(), (float) aabb.getMaxY());
    }

    @Override
    public void destroy() {
        isDead = true;
        maskTexture.delete();
    }
}
package net.bfsr.client.particle;

import lombok.Getter;
import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.client.renderer.particle.ParticleBeamRender;
import net.bfsr.engine.renderer.particle.RenderLayer;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;
import java.util.function.Supplier;

public class ParticleBeamEffect extends Particle {
    private static final ObjectPool<ParticleBeamRender> RENDER_POOL = new ObjectPool<>();
    private static final Supplier<ParticleBeamRender> RENDER_SUPPLIER = ParticleBeamRender::new;

    private WeaponSlotBeam slot;
    private Ship ship;
    private final Vector2f addPos = new Vector2f();
    private Random rand;
    private ParticleBeamRender render;
    @Getter
    private Vector4f beamColor;

    public ParticleBeamEffect init(WeaponSlotBeam slot, TextureRegister texture, Vector4f color) {
        Ship ship = slot.getShip();
        Random random = ship.getWorld().getRand();
        this.beamColor = color;
        this.slot = slot;
        this.ship = ship;
        this.rand = random;
        this.addPos.set(rand.nextFloat(), (rand.nextFloat() * 2.0f - 1.0f) * slot.getSize().y / 2.0f);
        calculateTransform();
        init(texture, position.x, position.y, 0.0f, 0.0f, sin, cos, 0.0f, 5.0f + 2.8f * random.nextFloat(),
                slot.getSize().y / 2.0f + 0.4f * random.nextFloat(), 0.0f, color.x, color.y, color.z, color.w, 0.0f,
                false, RenderLayer.DEFAULT_ADDITIVE);
        return this;
    }

    @Override
    protected void addParticle(long textureHandle, float r, float g, float b, float a, boolean isAlphaFromZero,
                               RenderLayer renderLayer) {
        PARTICLE_MANAGER.addParticle(this);
        render = RENDER_POOL.getOrCreate(RENDER_SUPPLIER).init(this, textureHandle, r, g, b, a);
        PARTICLE_RENDERER.addParticleToRenderLayer(render, renderLayer);
    }

    @Override
    public void update() {
        calculateTransform();

        if (ship.isDead()) {
            setDead();
        }
    }

    public void calculateTransform() {
        float beamRange = slot.getCurrentBeamRange();
        Vector2f slotPos = slot.getPosition();

        cos = ship.getCos();
        sin = ship.getSin();

        float l = beamRange * addPos.x + (rand.nextFloat() * 2.0f - 1.0f);
        float k = addPos.y;

        position.x = cos * l - sin * k + slotPos.x;
        position.y = sin * l + cos * k + slotPos.y;
    }

    @Override
    public void onRemoved() {
        BeamEffects.PARTICLE_BEAM_EFFECT_POOL.returnBack(this);
        RENDER_POOL.returnBack(render);
    }
}
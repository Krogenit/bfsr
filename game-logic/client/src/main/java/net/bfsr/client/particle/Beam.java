package net.bfsr.client.particle;

import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class Beam extends GameObject {
    private final WeaponSlotBeam slot;
    private final Ship ship;
    private final List<ParticleBeamEffect> particlesEffects = new ArrayList<>();

    private final Vector2f lastPosition = new Vector2f();
    private final Vector2f lastSize = new Vector2f();

    private final Vector4f color;
    private final AbstractTexture texture;

    public Beam(WeaponSlotBeam slot, Vector4f color) {
        this.texture = Engine.assetsManager.getTexture(TextureRegister.particleBeam);
        this.slot = slot;
        this.ship = slot.getShip();
        this.color = color;
    }

    public void init() {
        calculateTransform();
        setLastValues();
    }

    private void updatePosition() {
        calculateTransform();
        for (int i = 0; i < particlesEffects.size(); i++) {
            particlesEffects.get(i).calculateTransform();
        }
    }

    private void calculateTransform() {
        Vector2f slotScale = slot.getSize();
        Vector2f slotPos = slot.getPosition();

        float cos = ship.getCos();
        float sin = ship.getSin();
        float startRange = -slotScale.x;

        float startX = cos * startRange;
        float startY = sin * startRange;

        float posX = startX + cos * slot.getCurrentBeamRange();
        float posY = startY + sin * slot.getCurrentBeamRange();

        position.x = slotPos.x + posX / 2.0f;
        position.y = slotPos.y + posY / 2.0f;

        size.x = (float) Math.sqrt((posX - startX) * (posX - startX) + (posY - startY) * (posY - startY));
        size.y = slotScale.y;
    }

    @Override
    public void update() {
        setLastValues();
        updateEffects();
    }

    @Override
    public void postPhysicsUpdate() {
        if (color.w > 0) {
            updatePosition();

            while (particlesEffects.size() < slot.getCurrentBeamRange() / 90.0f) {
                particlesEffects.add(BeamEffects.beamEffect(slot, color));
            }
        }
    }

    private void updateEffects() {
        for (int i = 0; i < particlesEffects.size(); i++) {
            Particle p = particlesEffects.get(i);
            if (p.isDead()) {
                particlesEffects.remove(i--);
            }
        }
    }

    private void setLastValues() {
        lastSize.set(size);
        lastPosition.set(position);
    }

    public void render(AbstractSpriteRenderer spriteRenderer, float lastSin, float lastCos, float sin, float cos) {
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin,
                cos, lastSize.x, lastSize.y, size.x, size.y, color.x, color.y, color.z, color.w * 0.3333f, texture,
                BufferType.ENTITIES_ADDITIVE);
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin,
                cos, lastSize.x, lastSize.y * 0.3333f, size.x, size.y * 0.3333f, color.x, color.y, color.z, color.w, texture,
                BufferType.ENTITIES_ADDITIVE);
    }
}
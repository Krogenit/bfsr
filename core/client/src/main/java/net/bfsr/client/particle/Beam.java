package net.bfsr.client.particle;

import net.bfsr.client.component.weapon.WeaponSlotBeam;
import net.bfsr.client.entity.TextureObject;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.particle.effect.BeamEffects;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.texture.TextureRegister;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class Beam extends TextureObject {
    private final WeaponSlotBeam slot;
    private final Ship ship;
    private final List<ParticleBeamEffect> particlesEffects = new ArrayList<>();

    public Beam(WeaponSlotBeam slot, Ship ship) {
        super(TextureLoader.getTexture(TextureRegister.particleBeam));
        this.slot = slot;
        this.ship = ship;
    }

    public void init() {
        calculateTransform();
        setLastValues();
    }

    public void updatePosition() {
        calculateTransform();
        for (int i = 0; i < particlesEffects.size(); i++) {
            particlesEffects.get(i).calculateTransform();
        }
    }

    private void calculateTransform() {
        Vector2f slotScale = slot.getScale();
        Vector2f slotPos = slot.getPosition();
        this.color.set(slot.getBeamColor());

        float cos = ship.getCos();
        float sin = ship.getSin();
        float startRange = -slotScale.x;

        float startX = cos * startRange;
        float startY = sin * startRange;

        float posX;
        float posY;

        Vector2f collisionPoint = slot.getCollisionPoint();
        if (collisionPoint.x != 0 || collisionPoint.y != 0) {
            posX = (startX + cos * slot.getCurrentBeamRange());
            posY = (startY + sin * slot.getCurrentBeamRange());
            position.x = slotPos.x + posX / 2.0f;
            position.y = slotPos.y + posY / 2.0f;
        } else {
            posX = startX + cos * slot.getBeamMaxRange();
            posY = startY + sin * slot.getBeamMaxRange();
            position.x = slotPos.x + posX / 2.0f;
            position.y = slotPos.y + posY / 2.0f;
        }

        scale.x = (float) Math.sqrt((posX - startX) * (posX - startX) + (posY - startY) * (posY - startY));
        scale.y = slotScale.y;
    }

    public void update() {
        calculateTransform();

        while (particlesEffects.size() < slot.getCurrentBeamRange() / 90.0f) {
            particlesEffects.add(BeamEffects.beamEffect(slot));
        }
    }

    public void updateEffects() {
        for (int i = 0; i < particlesEffects.size(); i++) {
            Particle p = particlesEffects.get(i);
            if (p.isDead()) {
                particlesEffects.remove(i--);
            }
        }
    }

    public void render() {
        SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                lastScale.x, lastScale.y, scale.x, scale.y, color.x, color.y, color.z, color.w / 3.0f, texture, BufferType.ENTITIES_ADDITIVE);
        SpriteRenderer.get().addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                lastScale.x, lastScale.y / 3.0f, scale.x, scale.y / 3.0f, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ADDITIVE);
    }

    public void setLastValues() {
        lastScale.set(scale);
        lastPosition.set(position);
    }
}
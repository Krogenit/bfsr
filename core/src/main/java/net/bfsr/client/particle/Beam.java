package net.bfsr.client.particle;

import net.bfsr.client.render.InstancedRenderer;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.entity.TextureObject;
import net.bfsr.entity.ship.Ship;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class Beam extends TextureObject {
    private final WeaponSlotBeam slot;
    private final Ship ship;
    private final List<ParticleBeamEffect> particlesEffects = new ArrayList<>();

    public Beam(WeaponSlotBeam slot, Ship ship) {
        super(TextureRegister.particleBeam);
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
        this.rotation = slot.getRotation();

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
            particlesEffects.add(ParticleSpawner.spawnBeamEffect(slot));
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

    @Override
    public void render(float interpolation) {
        InstancedRenderer.INSTANCE.addToRenderPipeLine(lastPosition.x, lastPosition.y, position.x, position.y, lastRotation, rotation, lastScale.x,
                lastScale.y, scale.x, scale.y, color.x, color.y, color.z, color.w / 3.0f, texture, interpolation);
        InstancedRenderer.INSTANCE.addToRenderPipeLine(lastPosition.x, lastPosition.y, position.x, position.y, lastRotation, rotation, lastScale.x,
                lastScale.y / 3.0f, scale.x, scale.y / 3.0f, color.x, color.y, color.z, color.w, texture, interpolation);
    }

    public void setLastValues() {
        lastScale.set(scale);
        lastPosition.set(position);
        lastRotation = rotation;
    }
}

package net.bfsr.client.renderer.component;

import net.bfsr.client.Client;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.math.RotationHelper;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.entity.Render;
import net.bfsr.engine.sound.AbstractSoundManager;
import net.bfsr.engine.sound.SoundEffect;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class WeaponSlotRender extends Render {
    private final Vector2f rotationHelper = new Vector2f();
    private final WeaponSlot weaponSlot;
    private final WeaponEffects weaponEffects = Client.get().getParticleEffects().getWeaponEffects();
    private final AbstractSoundManager soundManager = Engine.getSoundManager();

    WeaponSlotRender(WeaponSlot object, float z) {
        super(object, z, Engine.getAssetsManager().getTexture(object.getGunData().getTextureData()));
        this.weaponSlot = object;
    }

    @Override
    public void init() {
        id = spriteRenderer.add(weaponSlot.getX(), weaponSlot.getY(), z, weaponSlot.getSin(), weaponSlot.getCos(),
                object.getSizeX(), object.getSizeY(), color.x, color.y, color.z, color.w, texture.getTextureHandle(),
                BufferType.ENTITIES_ALPHA);
    }

    @Override
    public void postWorldUpdate() {
        updateRenderValues();
    }

    @Override
    protected void updateLastRenderValues() {
        spriteRenderer.setLastPosition(id, BufferType.ENTITIES_ALPHA, weaponSlot.getX(), weaponSlot.getY());
        spriteRenderer.setLastRotation(id, BufferType.ENTITIES_ALPHA, weaponSlot.getSin(), weaponSlot.getCos());
    }

    @Override
    protected void updateRenderValues() {
        spriteRenderer.setPosition(id, BufferType.ENTITIES_ALPHA, weaponSlot.getX(), weaponSlot.getY());
        spriteRenderer.setRotation(id, BufferType.ENTITIES_ALPHA, weaponSlot.getSin(), weaponSlot.getCos());
    }

    @Override
    public void render() {
        spriteRenderer.addDrawCommand(id, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.ENTITIES_ALPHA);
    }

    public void onShot() {
        float x = object.getX();
        float y = object.getY();
        Vector4f color = weaponSlot.getGunData().getColor();
        Ship ship = weaponSlot.getShip();
        float sin = ship.getSin();
        float cos = ship.getCos();
        RotationHelper.rotate(sin, cos, 0.1f, 0, rotationHelper);
        weaponEffects.spawnWeaponShoot(x, y, rotationHelper.x, rotationHelper.y, z, sin, cos, weaponSlot.getSizeY() * 4.0f,
                color.x, color.y, color.z, color.w, particle -> {
                    particle.setRotation(ship.getSin(), ship.getCos());
                    particle.setPosition(object.getX() + rotationHelper.x, object.getY() + rotationHelper.y);
                });
        playSounds(weaponSlot.getGunData(), x, y);
    }

    void playSounds(GunData gunData, float x, float y) {
        SoundEffect soundEffect = gunData.getSoundEffect();
        if (soundEffect.getSounds().size() > 0) {
            soundManager.play(soundEffect, x, y);
        }
    }
}
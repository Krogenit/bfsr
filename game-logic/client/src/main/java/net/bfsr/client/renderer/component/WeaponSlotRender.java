package net.bfsr.client.renderer.component;

import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.client.renderer.Render;
import net.bfsr.config.SoundData;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

public class WeaponSlotRender extends Render {
    private final Vector2f rotationHelper = new Vector2f();
    private final WeaponSlot weaponSlot;

    WeaponSlotRender(WeaponSlot object) {
        super(Engine.assetsManager.getTexture(object.getGunData().getTexture()), object);
        this.weaponSlot = object;
    }

    @Override
    public void update() {
        lastPosition.set(object.getX(), object.getY());
    }

    public void renderAlpha(float lastSin, float lastCos, float sin, float cos) {
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, object.getX(), object.getY(), lastSin, lastCos, sin,
                cos, object.getSizeX(), object.getSizeY(), color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
    }

    public void renderAdditive(float lastSin, float lastCos, float sin, float cos) {}

    @Override
    public void renderAlpha() {
        throw new UnsupportedOperationException("Use renderAlpha with params instead");
    }

    @Override
    public void renderAdditive() {
        throw new UnsupportedOperationException("Use renderAdditive with params instead");
    }

    public void onShot() {
        float x = object.getX();
        float y = object.getY();
        Vector4f color = weaponSlot.getGunData().getColor();
        Ship ship = weaponSlot.getShip();
        float sin = ship.getSin();
        float cos = ship.getCos();
        RotationHelper.rotate(sin, cos, 1.0f, 0, rotationHelper);
        WeaponEffects.spawnWeaponShoot(x, y, rotationHelper.x, rotationHelper.y, sin, cos, 8.0f, color.x,
                color.y, color.z, color.w, particle -> {
                    particle.setSin(ship.getSin());
                    particle.setCos(ship.getCos());
                    particle.setPosition(object.getX() + rotationHelper.x, object.getY() + rotationHelper.y);
                });
        playSounds(weaponSlot.getGunData(), ship.getWorld().getRand(), x, y);
    }

    void playSounds(GunData gunData, Random random, float x, float y) {
        SoundData[] sounds = gunData.getSounds();
        if (sounds.length > 0) {
            SoundData sound = sounds[random.nextInt(sounds.length)];
            Engine.soundManager.play(Engine.assetsManager.getSound(sound.path()), sound.volume(), x, y);
        }
    }
}
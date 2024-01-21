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

public class WeaponSlotRender<T extends WeaponSlot> extends Render<T> {
    private final Vector2f rotationHelper = new Vector2f();

    WeaponSlotRender(T object) {
        super(Engine.assetsManager.getTexture(object.getGunData().getTexture()), object);
    }

    @Override
    public void update() {
        lastPosition.set(object.getPosition());
    }

    public void renderAlpha(float lastSin, float lastCos, float sin, float cos) {
        Vector2f position = object.getPosition();
        Vector2f scale = object.getSize();
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos, sin,
                cos, scale.x, scale.y, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
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
        Vector2f position = object.getPosition();
        Vector4f color = object.getGunData().getColor();
        Ship ship = object.getShip();
        float sin = ship.getSin();
        float cos = ship.getCos();
        RotationHelper.rotate(sin, cos, 1.0f, 0, rotationHelper);
        rotationHelper.add(position);
        Vector2f velocity = ship.getVelocity();
        WeaponEffects.spawnWeaponShoot(rotationHelper, -sin, -cos, 8.0f, velocity, color.x, color.y, color.z, color.w);
        playSounds(object.getGunData(), ship.getWorld().getRand(), position.x, position.y);
    }

    void playSounds(GunData gunData, Random random, float x, float y) {
        SoundData[] sounds = gunData.getSounds();
        if (sounds.length > 0) {
            SoundData sound = sounds[random.nextInt(sounds.length)];
            Engine.soundManager.play(Engine.assetsManager.getSound(sound.path()), sound.volume(), x, y);
        }
    }
}
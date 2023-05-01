package net.bfsr.client.listener.module.weapon;

import net.bfsr.client.core.Core;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.client.renderer.Renderer;
import net.bfsr.client.renderer.render.entity.ShipRender;
import net.bfsr.client.sound.SoundLoader;
import net.bfsr.client.sound.SoundManager;
import net.bfsr.client.sound.SoundSource;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.config.SoundData;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.module.weapon.BeamShotEvent;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.math.RotationHelper;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

@Listener(references = References.Strong)
public class WeaponEventListener {
    private final SoundManager soundManager = Core.get().getSoundManager();
    private final Renderer renderer = Core.get().getRenderer();

    @Handler
    public void event(WeaponShotEvent event) {
        WeaponSlot weaponSlot = event.weaponSlot();
        Vector2f position = weaponSlot.getPosition();
        Vector4f color = weaponSlot.getGunData().getColor();
        Ship ship = weaponSlot.getShip();
        float sin = ship.getSin();
        float cos = ship.getCos();
        Vector2f pos = RotationHelper.rotate(sin, cos, 1.0f, 0).add(position);
        Vector2f velocity = ship.getVelocity();
        WeaponEffects.spawnWeaponShoot(pos, -sin, -cos, 8.0f, velocity, color.x, color.y, color.z, color.w);
        playSounds(weaponSlot.getGunData(), ship.getWorld().getRand(), position.x, position.y);
    }

    @Handler
    public void event(BeamShotEvent event) {
        WeaponSlot weaponSlot = event.weaponSlot();
        Vector2f position = weaponSlot.getPosition();
        playSounds(weaponSlot.getGunData(), weaponSlot.getShip().getWorld().getRand(), position.x, position.y);
        ShipRender shipRender = (ShipRender) renderer.getRender(weaponSlot.getShip().getId());
        shipRender.onWeaponShot(weaponSlot);
    }

    private void playSounds(GunData gunData, Random random, float x, float y) {
        SoundData[] sounds = gunData.getSounds();
        if (sounds.length > 0) {
            SoundData sound = sounds[random.nextInt(sounds.length)];
            soundManager.play(new SoundSource(SoundLoader.getBuffer(sound.path()), sound.volume(), x, y));
        }
    }
}
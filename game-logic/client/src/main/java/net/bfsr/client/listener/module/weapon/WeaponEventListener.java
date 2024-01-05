package net.bfsr.client.listener.module.weapon;

import net.bfsr.client.Core;
import net.bfsr.client.particle.effect.WeaponEffects;
import net.bfsr.client.renderer.RenderManager;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.config.SoundData;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.engine.Engine;
import net.bfsr.engine.sound.AbstractSoundManager;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.event.module.weapon.BeamShotEvent;
import net.bfsr.event.module.weapon.WeaponShotEvent;
import net.bfsr.event.module.weapon.WeaponSlotRemovedEvent;
import net.bfsr.math.RotationHelper;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

@Listener(references = References.Strong)
public class WeaponEventListener {
    private final AbstractSoundManager soundManager = Engine.soundManager;
    private final RenderManager renderManager = Core.get().getRenderManager();

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
        ShipRender shipRender = renderManager.getRender(weaponSlot.getShip().getId());
        shipRender.onWeaponShot(weaponSlot);
    }

    private void playSounds(GunData gunData, Random random, float x, float y) {
        SoundData[] sounds = gunData.getSounds();
        if (sounds.length > 0) {
            SoundData sound = sounds[random.nextInt(sounds.length)];
            soundManager.play(Engine.assetsManager.getSound(sound.path()), sound.volume(), x, y);
        }
    }

    @Handler
    public void event(WeaponSlotRemovedEvent event) {
        WeaponSlot weaponSlot = event.getWeaponSlot();
        ShipRender render = renderManager.getRender(weaponSlot.getShip().getId());
        if (render != null) {
            render.removeWeaponRender(weaponSlot.getId());
        }
    }
}
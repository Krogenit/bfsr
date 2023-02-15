package net.bfsr.client.component;

import lombok.Getter;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.common.PacketWeaponShoot;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.component.weapon.WeaponSlotCommon;
import net.bfsr.entity.ship.ShipCommon;

import java.util.Random;

public abstract class WeaponSlot extends WeaponSlotCommon {
    private final SoundRegistry[] shootSounds;
    @Getter
    private final Texture texture;

    protected WeaponSlot(ShipCommon ship, SoundRegistry[] shootSounds, float shootTimerMax, float energyCost, float bulletSpeed, float alphaReducer, float scaleX, float scaleY,
                         TextureRegister texture) {
        super(ship, shootTimerMax, energyCost, bulletSpeed, alphaReducer, scaleX, scaleY);
        this.shootSounds = shootSounds;
        this.color.set(1.0f, 1.0f, 1.0f, 1.0f);
        this.scale.set(scaleX, scaleY);
        this.texture = TextureLoader.getTexture(texture);
    }

    @Override
    protected void shoot() {
        Core.get().sendPacket(new PacketWeaponShoot(ship.getId(), id));
    }

    public void clientShoot() {
        float energy = ship.getReactor().getEnergy();
        spawnShootParticles();
        playSound();
        shootTimer = shootTimerMax;
        ship.getReactor().setEnergy(energy - energyCost);
    }

    protected void playSound() {
        if (shootSounds != null) {
            int size = shootSounds.length;
            Random rand = world.getRand();
            SoundRegistry sound = shootSounds[rand.nextInt(size)];
            SoundSourceEffect source = new SoundSourceEffect(sound, position.x, position.y);
            Core.get().getSoundManager().play(source);
        }
    }

    @Override
    public void render() {
        SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(),
                scale.x, scale.y, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ALPHA);
    }

    public void renderAdditive() {

    }
}

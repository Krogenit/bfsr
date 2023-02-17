package net.bfsr.client.component;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.config.component.ShieldConfig;
import net.bfsr.util.TimeUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Shield extends ShieldCommon {
    @Setter
    private Texture texture;
    @Getter
    private final Vector4f color;
    private final Ship ship;

    public Shield(Ship ship, ShieldConfig shieldConfig, float r, float g, float b, float a) {
        super(ship.getBody(), shieldConfig.getMaxShield(), shieldConfig.getShieldRegen(), shieldConfig.getRebuildTime());
        this.texture = TextureLoader.getTexture(shieldConfig.getTexture());
        this.color = new Vector4f(r, g, b, a);
        this.ship = ship;
    }

    @Override
    protected void onShieldAlive() {
        if (size < 1.0f) {
            size += 3.6f * TimeUtils.UPDATE_DELTA_TIME;
            if (size > 1.0f) size = 1.0f;
        }
    }

    @Override
    public void rebuildShield() {
        super.rebuildShield();
        Vector2f position = ship.getPosition();
        Vector3f shipEffectColor = ship.getEffectsColor();
        ParticleSpawner.spawnLight(position.x, position.y, ship.getScale().x * 2.0f, shipEffectColor.x, shipEffectColor.y, shipEffectColor.z, 1.0f, 0.04f * 60.0f, false,
                RenderLayer.DEFAULT_ADDITIVE);
        if (ship.getWorld().getRand().nextInt(2) == 0) {
            Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldUp0, position.x, position.y));
        } else {
            Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldUp1, position.x, position.y));
        }
    }

    @Override
    public void removeShield() {
        super.removeShield();
        Vector2f shipPosition = ship.getPosition();
        Vector3f shipEffectColor = ship.getEffectsColor();
        Vector2f position = ship.getPosition();
        ParticleSpawner.spawnLight(position.x, position.y, ship.getScale().x * 2.0f, 5.0f * 6.0f, shipEffectColor.x, shipEffectColor.y, shipEffectColor.z, 1.0f, 0.04f * 60.0f, false,
                RenderLayer.DEFAULT_ADDITIVE);
        ParticleSpawner.spawnDisableShield(position.x, position.y, ship.getScale().x * 4.0f, -24.0f, color.x, color.y, color.z, color.w);
        Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldDown, shipPosition.x, shipPosition.y));
    }

    public void render() {
        if (isShieldAlive()) {
            SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(ship.getLastPosition().x, ship.getLastPosition().y, ship.getPosition().x, ship.getPosition().y, ship.getLastSin(), ship.getLastCos(),
                    ship.getSin(), ship.getCos(), diameter.x * size, diameter.y * size, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ADDITIVE);
        }
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }
}

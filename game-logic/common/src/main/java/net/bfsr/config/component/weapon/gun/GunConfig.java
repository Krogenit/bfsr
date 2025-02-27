package net.bfsr.config.component.weapon.gun;

import lombok.Getter;
import net.bfsr.config.entity.bullet.DamageConfigurable;
import net.bfsr.engine.config.ColorConfigurable;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.ConfigurableSound;
import net.bfsr.engine.config.Vector2fConfigurable;
import net.bfsr.engine.config.entity.GameObjectConfig;

@Configurable
@Getter
public class GunConfig extends GameObjectConfig {
    private float reloadTimeInSeconds;
    private float energyCost;
    private DamageConfigurable damage;
    private ColorConfigurable color;
    private ConfigurableSound[] sounds;
    private float bulletSpeed;
    private float bulletLifeTimeInSeconds;
    private Vector2fConfigurable bulletSize;
    private String bulletTexture;
    private Vector2fConfigurable[] bulletVertices;
    private float hp;
}
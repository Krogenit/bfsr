package net.bfsr.config.component.weapon.gun;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.bfsr.config.entity.bullet.DamageConfigurable;
import net.bfsr.engine.config.ColorConfigurable;
import net.bfsr.engine.config.Configurable;
import net.bfsr.engine.config.ConfigurableSoundEffect;
import net.bfsr.engine.config.Vector2fConfigurable;
import net.bfsr.engine.config.entity.GameObjectConfig;

@Configurable
@Accessors(fluent = true)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GunConfig extends GameObjectConfig {
    private float reloadTimeInSeconds;
    private float energyCost;
    private DamageConfigurable damage;
    private ColorConfigurable color;
    private ConfigurableSoundEffect soundEffect;
    private float bulletSpeed;
    private float bulletLifeTimeInSeconds;
    private Vector2fConfigurable bulletSize;
    private String bulletTexture;
    private Vector2fConfigurable[] bulletVertices;
    private float hp;
}
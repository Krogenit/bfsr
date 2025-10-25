package net.bfsr.entity.bullet;

import lombok.Getter;
import net.bfsr.config.entity.bullet.DamageConfigurable;

public class BulletDamage {
    @Getter
    private float armor, hull, shield;
    private final float average;

    private BulletDamage(float armor, float hull, float shield) {
        this.armor = armor;
        this.hull = hull;
        this.shield = shield;
        this.average = (armor + hull + shield) / 3.0f;
    }

    public BulletDamage(DamageConfigurable bulletDamage) {
        this(bulletDamage.armor(), bulletDamage.hull(), bulletDamage.shield());
    }

    void reduceArmor(float amount) {
        this.armor -= amount;
    }

    void reduceHull(float amount) {
        this.hull -= amount;
    }

    void reduceShield(float amount) {
        this.shield -= amount;
    }

    float getAverage() {
        return average;
    }

    public BulletDamage copy() {
        return new BulletDamage(armor, hull, shield);
    }

    public BulletDamage multiply(float multiplier) {
        armor *= multiplier;
        hull *= multiplier;
        shield *= multiplier;
        return this;
    }
}
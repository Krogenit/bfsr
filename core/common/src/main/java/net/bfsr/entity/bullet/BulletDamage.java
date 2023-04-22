package net.bfsr.entity.bullet;

import net.bfsr.config.bullet.DamageConfigurable;

public class BulletDamage {
    private float armor, hull, shield;
    private final float average;

    public BulletDamage(float armor, float hull, float shield) {
        this.armor = armor;
        this.hull = hull;
        this.shield = shield;
        this.average = (armor + hull + shield) / 3.0f;
    }

    public BulletDamage(DamageConfigurable bulletDamage) {
        this(bulletDamage.armor(), bulletDamage.hull(), bulletDamage.shield());
    }

    public float getArmor() {
        return armor;
    }

    public void reduceBulletDamageArmor(float amount) {
        this.armor -= amount;
    }

    public float getHull() {
        return hull;
    }

    public void reduceBulletDamageHull(float amount) {
        this.hull -= amount;
    }

    public float getShield() {
        return shield;
    }

    public void reduceBulletDamageShield(float amount) {
        this.shield -= amount;
    }

    public float getAverage() {
        return average;
    }
}
package net.bfsr.entity.bullet;

public class BulletDamage {
    float bulletDamageArmor, bulletDamageHull, bulletDamageShield;
    float averageDamage;

    public BulletDamage(float bulletDamageArmor, float bulletDamageHull, float bulletDamageShield) {
        this.bulletDamageArmor = bulletDamageArmor;
        this.bulletDamageHull = bulletDamageHull;
        this.bulletDamageShield = bulletDamageShield;
        this.averageDamage = (bulletDamageArmor + bulletDamageHull + bulletDamageShield) / 3.0f;
    }

    public float getBulletDamageArmor() {
        return bulletDamageArmor;
    }

    public void reduceBulletDamageArmor(float amount) {
        this.bulletDamageArmor -= amount;
    }

    public float getBulletDamageHull() {
        return bulletDamageHull;
    }

    public void reduceBulletDamageHull(float amount) {
        this.bulletDamageHull -= amount;
    }

    public float getBulletDamageShield() {
        return bulletDamageShield;
    }

    public void reduceBulletDamageShield(float amount) {
        this.bulletDamageShield -= amount;
    }

    public float getAverageDamage() {
        return averageDamage;
    }
}

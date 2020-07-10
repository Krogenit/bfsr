package ru.krogenit.bfsr.entity.bullet;

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

	public void setBulletDamageArmor(float bulletDamageArmor) {
		this.bulletDamageArmor = bulletDamageArmor;
	}

	public float getBulletDamageHull() {
		return bulletDamageHull;
	}

	public void setBulletDamageHull(float bulletDamageHull) {
		this.bulletDamageHull = bulletDamageHull;
	}

	public float getBulletDamageShield() {
		return bulletDamageShield;
	}

	public void setBulletDamageShield(float bulletDamageShield) {
		this.bulletDamageShield = bulletDamageShield;
	}
	
	public float getAverageDamage() {
		return averageDamage;
	}
}

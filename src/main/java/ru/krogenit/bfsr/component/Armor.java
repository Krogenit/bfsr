package ru.krogenit.bfsr.component;

import ru.krogenit.bfsr.math.Direction;

public class Armor {

	private final ArmorPlate[] armorPlates;
	
	public Armor() {
		this.armorPlates = new ArmorPlate[4];
	}
	
	public void update(double delta) {
		for (ArmorPlate armorPlate : armorPlates) {
			if (armorPlate != null) armorPlate.update(delta);
		}
	}
	
	public float reduceDamageByArmor(float damageToArmor, float damageToHull, Direction dir) {
		ArmorPlate plate = getArmorPlatreByDir(dir);
		if(plate != null) {
			return plate.reduceDamage(damageToArmor, damageToHull);
		} else return damageToHull;
	}
	
	public void setArmorPlateByDir(Direction dir, ArmorPlate armor) {
		switch(dir) {
		case FORWARD:
			setArmorPlateToSlot(0, armor);
			break;
		case LEFT:
			setArmorPlateToSlot(1, armor);
			break;
		case BACKWARD:
			setArmorPlateToSlot(2, armor);
			break;
		case RIGHT:
			setArmorPlateToSlot(3, armor);
			break;
		}
	}
	
	public ArmorPlate getArmorPlatreByDir(Direction dir) {
		switch(dir) {
		case FORWARD:
			return armorPlates[0];
		case LEFT:
			return armorPlates[1];
		case BACKWARD:
			return armorPlates[2];
		case RIGHT:
			return armorPlates[3];
			default:
				return null;
		}
	}
	
	public void setArmorPlateToSlot(int i, ArmorPlate armor) {
		armor.setId(i);
		this.armorPlates[i] = armor;
	}
	
	public ArmorPlate getArmorPlate(int i) {
		return armorPlates[i];
	}
	
	public ArmorPlate[] getArmorPlates() {
		return armorPlates;
	}
}

package ru.krogenit.bfsr.component.reactor;

public class Reactor {
	
	private float energy, maxEnergy, regenEnergy;
	
	public Reactor(float maxEnergy, float regenEnergy) {
		this.energy = maxEnergy;
		this.maxEnergy = maxEnergy;
		this.regenEnergy = regenEnergy;
	}

	public void update(double delta) {
		regenEnergy(delta);
	}

	private void regenEnergy(double delta) {
		if (energy < maxEnergy) {
			energy += regenEnergy * delta;
			
			if (energy > maxEnergy) {
				energy = maxEnergy;
			}
		}
	}
	
	public void setEnergy(float energy) {
		this.energy = energy;
		
		if (this.energy < 0) {
			this.energy = 0;
		}
	}
	
	public float getEnergy() {
		return energy;
	}
	
	public void setMaxEnergy(float maxEnergy) {
		this.maxEnergy = maxEnergy;
	}
	
	public float getMaxEnergy() {
		return maxEnergy;
	}
	
	public void setRegenEnergy(float regenEnergy) {
		this.regenEnergy = regenEnergy;
	}
	
	public float getRegenEnergy() {
		return regenEnergy;
	}
}

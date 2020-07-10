package ru.krogenit.bfsr.component;

public class Engine {
	private float forwardSpeed, backwardSpeed, sideSpeed;
	private float maxForwardSpeed, maxBackwardSpeed, maxSideSpeed;
	private float maneuverability;
	private float rotationSpeed;
	private boolean maxPower;
	
	public Engine(float forwardSpeed, float backwardSpeed, float sideSpeed, 
			float maxForwardSpeed, float maxBackwardSpeed, float maxSideSpeed,
			float maneuverability, float rotationSpeed) {
		this.forwardSpeed = forwardSpeed;
		this.backwardSpeed = backwardSpeed;
		this.sideSpeed = sideSpeed;
		this.maxForwardSpeed = maxForwardSpeed;
		this.maxBackwardSpeed = maxBackwardSpeed;
		this.maxSideSpeed = maxSideSpeed;
		this.maneuverability = maneuverability;
		this.rotationSpeed = rotationSpeed;
	}

	public void setForwardSpeed(float forwardSpeed) {
		this.forwardSpeed = forwardSpeed;
	}

	public void setBackwardSpeed(float backwardSpeed) {
		this.backwardSpeed = backwardSpeed;
	}

	public void setSideSpeed(float sideSpeed) {
		this.sideSpeed = sideSpeed;
	}

	public void setMaxForwardSpeed(float maxForwardSpeed) {
		this.maxForwardSpeed = maxForwardSpeed;
	}

	public void setMaxBackwardSpeed(float maxBackwardSpeed) {
		this.maxBackwardSpeed = maxBackwardSpeed;
	}

	public void setMaxSideSpeed(float maxSideSpeed) {
		this.maxSideSpeed = maxSideSpeed;
	}

	public void setManeuverability(float maneuverability) {
		this.maneuverability = maneuverability;
	}

	public float getForwardSpeed() {
		return forwardSpeed;
	}

	public float getBackwardSpeed() {
		return backwardSpeed;
	}

	public float getSideSpeed() {
		return sideSpeed;
	}

	public float getMaxForwardSpeed() {
		return maxForwardSpeed;
	}

	public float getMaxBackwardSpeed() {
		return maxBackwardSpeed;
	}

	public float getMaxSideSpeed() {
		return maxSideSpeed;
	}
	
	public float getManeuverability() {
		return maneuverability;
	}
	
	public void setMaxPower(boolean maxPower) {
		this.maxPower = maxPower;
	}
	
	public boolean isMaxPower() {
		return maxPower;
	}
	
	public void setRotationSpeed(float rotationSpeed) {
		this.rotationSpeed = rotationSpeed;
	}
	
	public float getRotationSpeed() {
		return rotationSpeed;
	}
}

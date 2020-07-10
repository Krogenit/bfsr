package ru.krogenit.bfsr.ai.task;

import ru.krogenit.bfsr.entity.ship.Ship;

public abstract class AiTask {
	
	protected Ship ship;
	
	public AiTask(Ship ship) {
		this.ship = ship;
	}
	
	public abstract void execute(double delta);
	public abstract boolean shouldExecute();
}

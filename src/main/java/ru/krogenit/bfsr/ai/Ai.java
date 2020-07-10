package ru.krogenit.bfsr.ai;

import java.util.ArrayList;
import java.util.List;

import ru.krogenit.bfsr.ai.task.AiTask;
import ru.krogenit.bfsr.entity.ship.Ship;

public class Ai {
	private final Ship ship;
	private final List<AiTask> tasks;
	
	private AiAggressiveType aggressiveType;
	
	public Ai(Ship ship) {
		this.ship = ship;
		this.tasks = new ArrayList<>();
	}
	
	public void addTask(AiTask task) {
		this.tasks.add(task);
	}
	
	public void update(double delta) {
		for (AiTask task : tasks) {
			if (task.shouldExecute())
				task.execute(delta);
		}
	}
	
	public void setAggressiveType(AiAggressiveType aggressiveType) {
		this.aggressiveType = aggressiveType;
	}
	
	public AiAggressiveType getAggressiveType() {
		return aggressiveType;
	}
	
	public Ship getOwnerShip() {
		return ship;
	}
	
	public List<AiTask> getTasks() {
		return tasks;
	}
	
	public AiTask getTask(int i) {
		return this.tasks.get(i);
	}
}

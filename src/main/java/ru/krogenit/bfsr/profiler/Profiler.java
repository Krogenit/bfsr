package ru.krogenit.bfsr.profiler;

import java.util.HashMap;

public class Profiler {
	
	private final HashMap<String, Long> prevResults = new HashMap<>();
	private final HashMap<String, Float> results = new HashMap<>();
	private String currentSectionName;
	private boolean enable;
	
	public Profiler(boolean isEnable) {
		this.enable = isEnable;
	}
	
	public void startSection(String name) {
		if(enable) {
			currentSectionName = name;
			prevResults.put(name, System.nanoTime());
		}
	}
	
	public void endStartSection(String name) {
		if(enable) {
			Long prevTime = prevResults.get(currentSectionName);
			long time = System.nanoTime();
			prevResults.put(name, time);
			
			if(prevTime != null) {
				long dif = time - prevTime;
				results.put(currentSectionName, dif / (float) 1000000);
			}

			currentSectionName = name;
		}
	}
	
	public void endSection() {
		if(enable) {
			Long prevTime = prevResults.get(currentSectionName);
			long time = System.nanoTime();
			long dif = time - prevTime;
			results.put(currentSectionName,  dif / (float) 1000000);
		}
	}
	
	public float getResult(String name) {
		if(results.containsKey(name)) return results.get(name);
		else return 0;
	}
	
	public void setEnable(boolean value) {
		this.enable = value;
	}
}

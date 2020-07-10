package ru.krogenit.bfsr.client.input;

import ru.krogenit.bfsr.client.gui.Gui;
import ru.krogenit.bfsr.core.Core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {
	static long window;
	
	private static final Map<Integer, Boolean> KEY_STATES = new HashMap<>();
	private static final Map<Integer, Integer> KEY_REPEATING = new HashMap<>();

	public static void init(long win) {
		window = win;
		
		glfwSetCharCallback(window, (window, key) -> {
			Gui gui = Core.getCore().getCurrentGui();
			if(gui != null) {
				gui.textInput(key);
			}
			
			gui = Core.getCore().getGuiInGame();
			if(gui != null) {
				gui.textInput(key);
			}
		});
	}

	public static void setKeyState(int keyId, int isPress) {

	}

	public static boolean isKeyDown(int keyCode) {
		return glfwGetKey(window, keyCode) == GLFW_PRESS;
	}
	
	public static void update() {
		for (Entry<Integer, Integer> next : KEY_REPEATING.entrySet()) {
			Integer i = next.getValue();
			if (i > 0 && !isKeyDown(next.getKey())) {
				KEY_REPEATING.put(next.getKey(), 0);
			}
		}
	} 
	
	public static boolean isKeyRepeating(int keyCode) {
		Integer timer = KEY_REPEATING.get(keyCode);
		if(timer == null) {
			timer = 0;
		}
		
		boolean repeating = false;
		
		if(timer < 30) timer++;
		else repeating = true;
		
		KEY_REPEATING.put(keyCode, timer);
		return repeating;
	}
	
	public static boolean isKeyPressed(int keyCode) {
		boolean prevKeyState = false;
		if(KEY_STATES.containsKey(keyCode)) prevKeyState = KEY_STATES.get(keyCode);
		
		boolean keyDown = isKeyDown(keyCode);
		boolean pressed = !prevKeyState && keyDown;
		
		KEY_STATES.put(keyCode, keyDown);
		
		return pressed;
	}
	
	public static boolean isKeyRelease(int keyCode) {
		return glfwGetKey(window, keyCode) == GLFW_RELEASE;
	}
}

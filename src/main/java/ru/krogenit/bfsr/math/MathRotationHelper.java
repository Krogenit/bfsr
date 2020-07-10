package ru.krogenit.bfsr.math;

import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

public class MathRotationHelper {

	public static void rotateAboutZ(Vector2f vec, double a) {
		float newX = (float) (Math.cos(a) * vec.x - Math.sin(a) * vec.y);
		vec.y = (float) (Math.sin(a) * vec.x + Math.cos(a) * vec.y);
		vec.x = newX;
	}
	
	public static void rotateAboutZ(Vector2 vec, double a) {
		float newX = (float) (Math.cos(a) * vec.x - Math.sin(a) * vec.y);
		vec.y = (float) (Math.sin(a) * vec.x + Math.cos(a) * vec.y);
		vec.x = newX;
	}
}

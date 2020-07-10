package ru.krogenit.bfsr.math;

import org.joml.Vector2f;

public class RotationHelper {

	public static float TWOPI = (float) (Math.PI  * 2.0);
	
	public static Vector2f rotate(float cos, float sin, float x, float y) {
		return new Vector2f(cos * x - sin * y, sin * x + cos * y);
	}
	
	public static Vector2f rotate(float rotation, float x, float y) {
		float sin = (float) Math.sin(rotation);
		float cos = (float) Math.cos(rotation);
		return new Vector2f(cos * x - sin * y, sin * x + cos * y);
	}

	public static void rotate(float rotation, float x, float y, Vector2f dest) {
		float sin = (float) Math.sin(rotation);
		float cos = (float) Math.cos(rotation);
		dest.x = cos * x - sin * y;
		dest.y = sin * x + cos * y;
	}
	
	public static Vector2f angleToVelocity(double angle, float length) {
		return new Vector2f((float) Math.cos(angle) * length, (float) Math.sin(angle) * length);
	}

	public static void angleToVelocity(double angle, float length, Vector2f dest) {
		dest.x = (float) Math.cos(angle) * length;
		dest.y = (float) Math.sin(angle) * length;
	}
}

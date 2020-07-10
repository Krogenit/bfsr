package ru.krogenit.bfsr.client.model;

public class Quad extends Mesh
{
	 protected static float[] positions = new float[]{
		        -0.5f,  0.5f,
		        -0.5f, -0.5f,
		        0.5f, -0.5f,
		        0.5f,  0.5f,
		    };
	 protected static int[] indices = new int[]{
		        0, 1, 3, 3, 1, 2,
		    };
	 
	public Quad() {
		super(positions, indices);
	}
	 
	public Quad(float[] text) {
		super(positions, text, indices);
	}
}

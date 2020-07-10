package ru.krogenit.bfsr.client.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

public class OpenGLHelper {
	private static boolean isTexture0Active;
	public static void activateTexture0() {
		if(!isTexture0Active) {
			glActiveTexture(GL_TEXTURE0);
			isTexture0Active = true;
			isTexture1Active = false;
		}
	}
	
	private static boolean isTexture1Active;
	public static void activateTexture1() {
		if(!isTexture1Active) {
			glActiveTexture(GL_TEXTURE1);
			isTexture1Active = true;
			isTexture0Active = false;
		}
	}
	
	private static int lastTextureId;
	public static void bindTexture(int id) {
		if(lastTextureId != id) {
			glBindTexture(GL_TEXTURE_2D, id);
			lastTextureId = id;
		}
	}
	
	private static boolean isBlend;
	public static void enableBlend() {
		if(!isBlend) {
			glEnable(GL_BLEND);
			isBlend = true;
		}
	}
	
	public static void disableBlend() {
		if(isBlend) {
			glDisable(GL_BLEND);
			isBlend = false;
		}
	}
	
	private static int lastBuffer;
	public static void bindFrameBuffer(int buffer) {
		if(buffer != lastBuffer) {
			glBindFramebuffer(GL_FRAMEBUFFER, buffer);
			lastBuffer = buffer;
		}
	}
	
	private static int oldSfactor, oldDfactor;
	
	public static void blendFunc(int sfactor, int dfactor) {
		if(sfactor != oldSfactor || dfactor != oldDfactor) {
			glBlendFunc(sfactor, dfactor);
			oldSfactor = sfactor;
			oldDfactor = dfactor;
		}
	}
	
	private static float oldGreater;
	
	public static void alphaGreater(float greater) {
		if(oldGreater != greater) {
			glAlphaFunc(GL_GREATER, greater);
			oldGreater = greater;
		}
	}
}

package net.bfsr.client.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30C;

public class OpenGLHelper {
    private static boolean isTexture0Active;

    public static void activateTexture0() {
        if (!isTexture0Active) {
            GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
            isTexture0Active = true;
            isTexture1Active = false;
        }
    }

    private static boolean isTexture1Active;

    public static void activateTexture1() {
        if (!isTexture1Active) {
            GL13C.glActiveTexture(GL13C.GL_TEXTURE1);
            isTexture1Active = true;
            isTexture0Active = false;
        }
    }

    private static int lastTextureId;

    public static void bindTexture(int id) {
        if (lastTextureId != id) {
            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
            lastTextureId = id;
        }
    }

    private static boolean isBlend;

    public static void enableBlend() {
        if (!isBlend) {
            GL11C.glEnable(GL11C.GL_BLEND);
            isBlend = true;
        }
    }

    public static void disableBlend() {
        if (isBlend) {
            GL11C.glDisable(GL11C.GL_BLEND);
            isBlend = false;
        }
    }

    private static int lastBuffer;

    public static void bindFrameBuffer(int buffer) {
        if (buffer != lastBuffer) {
            GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, buffer);
            lastBuffer = buffer;
        }
    }

    private static int oldSfactor, oldDfactor;

    public static void blendFunc(int sfactor, int dfactor) {
        if (sfactor != oldSfactor || dfactor != oldDfactor) {
            GL11C.glBlendFunc(sfactor, dfactor);
            oldSfactor = sfactor;
            oldDfactor = dfactor;
        }
    }

    private static float oldGreater;

    public static void alphaGreater(float greater) {
        if (oldGreater != greater) {
            GL11.glAlphaFunc(GL11C.GL_GREATER, greater);
            oldGreater = greater;
        }
    }
}

package net.bfsr.client.gui;

import net.bfsr.client.input.Mouse;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.sound.GuiSoundSource;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.EnumZoomFactor;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

public class Scroll extends TextureObject {
    private float value, maxValue;
    private float visible;
    private AxisAlignedBoundingBox aabb;
    private boolean collided, isMoving;
    private final Vector2f basePos;

    public Scroll(Vector2f pos, Vector2f scale) {
        super(pos);
        this.scale = scale;
        this.origin = new Vector2f(-scale.x / 2.0f, -scale.y / 2.0f);
        this.aabb = new AxisAlignedBoundingBox(new Vector2f(position.x + origin.x, position.y + origin.y), new Vector2f(position.x - origin.x, position.y - origin.y));
        this.basePos = new Vector2f(pos);
        setZoomFactor(EnumZoomFactor.Gui);
    }

    public void scroll(float y) {
        value += y;
        if (value > maxValue) {
            value = maxValue;
        } else if (value < 0) {
            value = 0;
        }

        setPosition(basePos.x, basePos.y);
    }

    public void update() {
        super.update();

        if (isMoving) {
            this.position.y = Mouse.getPosition().y;

            float factor = visible / (maxValue + visible);
            float littleScale = scale.y * factor / 2f;
            float maxYpos = basePos.y + scale.y / 2f - littleScale;//button.getPosition().x + button.getScale().x / 2.0f - this.getScale().x / 2.0f - sideOut;
            float minYpos = basePos.y + (-scale.y / 2f + littleScale);
//		System.out.println("pos: " + position.y + " maxYPos: " + maxYpos + " baseY: " + basePos.y);
            if (this.position.y > maxYpos)
                this.position.y = maxYpos;
            else if (this.position.y < minYpos)
                this.position.y = minYpos;

            this.value = maxValue - ((this.position.y - minYpos) / (maxYpos - minYpos)) * maxValue;
        }

        if (aabb.isIntersects(Mouse.getPosition())) {
            if (!collided) {
                collided = true;
                Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonCollide));
            }
        } else {
            collided = false;
        }
    }

    public boolean isIntersects() {
        return aabb.isIntersects(Mouse.getPosition());
    }

    public void setMoving(boolean moving) {
        this.isMoving = moving;

        if (moving) {
            Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonClick));
        }
    }

    @Override
    public void render(BaseShader shader) {
        shader.disable();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        Core core = Core.getCore();
        int width = core.getWidth();
        int height = core.getHeight();
        GL11.glOrtho(0, width, height, 0, 0f, 100f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glPushMatrix();
        GL11.glTranslatef(position.x, position.y, 0);
        GL11.glScalef(scale.x, scale.y * visible / (maxValue + visible), 1);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(-0.5f, -0.5f);
        GL11.glVertex2f(-0.5f, 0.5f);
        GL11.glVertex2f(0.5f, 0.5f);
        GL11.glVertex2f(0.5f, -0.5f);
        GL11.glEnd();
        GL11.glPopMatrix();
//		GL11.glBegin(GL11.GL_LINE_LOOP);
//		GL11.glVertex2f(aabb.getMin().x, aabb.getMin().y);
//		GL11.glVertex2f(aabb.getMin().x, aabb.getMax().y);
//		GL11.glVertex2f(aabb.getMax().x, aabb.getMax().y);
//		GL11.glVertex2f(aabb.getMax().x, aabb.getMin().y);
//		GL11.glEnd();
        shader.enable();
    }

    public boolean isMoving() {
        return isMoving;
    }

    @Override
    public void setPosition(float x, float y) {
        this.basePos.x = x;
        this.basePos.y = y;
        float factor = visible / (maxValue + visible);
        float littleScale = scale.y * factor / 2f;
        this.position.x = x;
        this.position.y = y;
        this.position.y += scale.y / 2f - littleScale;
        float value = maxValue == 0 ? 1f : this.value / (maxValue);
        this.position.y += value * (-scale.y + littleScale * 2f);
    }

    @Override
    public void setPosition(Vector2f position) {
        this.basePos.x = position.x;
        this.basePos.y = position.y;
        float factor = visible / (maxValue + visible);
        float littleScale = scale.y * factor / 2f;
        this.position = position;
        this.position.y += scale.y / 2f - littleScale;
        float value = maxValue == 0 ? 1f : this.value / (maxValue);
        this.position.y += value * (-scale.y + littleScale * 2f);
    }

    @Override
    public void setScale(float x, float y) {
        this.scale.x = x;
        this.scale.y = y;
        this.origin.x = -scale.x / 2.0f;
        this.origin.y = -scale.y / 2.0f;
        this.aabb = new AxisAlignedBoundingBox(new Vector2f(basePos.x + origin.x, basePos.y + origin.y), new Vector2f(basePos.x - origin.x, basePos.y - origin.y));
    }

    @Override
    public void setScale(Vector2f scale) {
        this.scale.x = scale.x;
        this.scale.y = scale.y;
        this.origin.x = -scale.x / 2.0f;
        this.origin.y = -scale.y / 2.0f;
        this.aabb = new AxisAlignedBoundingBox(new Vector2f(basePos.x + origin.x, basePos.y + origin.y), new Vector2f(basePos.x - origin.x, basePos.y - origin.y));
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue - visible;
        if (this.maxValue < 0) this.maxValue = 0;
    }

    public void setVisible(float visible) {
        this.visible = visible;
    }

    public float getValue() {
        return value;
    }

    public float getVisible() {
        return visible;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void onMouseLeftClicked() {
        if (isIntersects()) {
            setMoving(true);
        }
    }

    public void onMouseLeftRelease() {
        setMoving(false);
    }
}

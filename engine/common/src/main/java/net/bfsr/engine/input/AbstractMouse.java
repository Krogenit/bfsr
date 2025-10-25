package net.bfsr.engine.input;

import net.bfsr.engine.renderer.camera.AbstractCamera;
import org.joml.Vector2f;
import org.joml.Vector2i;

public abstract class AbstractMouse {
    public abstract void setInputHandler(AbstractInputHandler inputHandler);

    public abstract void changeCursor(long cursor);
    public abstract long getInputCursor();
    public abstract long getDefaultCursor();

    public abstract boolean isLeftDown();
    public abstract boolean isRightDown();

    public abstract Vector2f getScreenPosition();
    public abstract Vector2i getGuiPosition();
    public abstract Vector2f getWorldPosition(AbstractCamera camera);

    public abstract void clear();
}
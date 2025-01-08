package net.bfsr.engine.input;

import net.bfsr.engine.renderer.camera.AbstractCamera;
import org.joml.Vector2f;

public abstract class AbstractMouse {
    public abstract void init(long window);
    public abstract void setInputHandler(AbstractInputHandler inputHandler);

    public abstract void changeCursor(long cursor);
    public abstract long getInputCursor();
    public abstract long getDefaultCursor();

    public abstract boolean isLeftDown();
    public abstract boolean isRightDown();

    public abstract Vector2f getPosition();
    public abstract Vector2f getGuiPosition();
    public abstract Vector2f getWorldPosition(AbstractCamera camera);
}
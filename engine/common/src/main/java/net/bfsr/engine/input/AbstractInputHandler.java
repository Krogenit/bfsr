package net.bfsr.engine.input;

import net.bfsr.engine.Engine;

public abstract class AbstractInputHandler {
    private final MouseConsumer[][] mouseConsumers = new MouseConsumer[2][2];

    protected AbstractInputHandler() {
        mouseConsumers[0][1] = action -> mouseLeftClick();
        mouseConsumers[0][0] = action -> mouseLeftRelease();
        mouseConsumers[1][1] = action -> mouseRightClick();
        mouseConsumers[1][0] = action -> mouseRightRelease();
    }

    public void init() {
        Engine.mouse.setInputHandler(this);
        Engine.keyboard.setInputHandler(this);
    }

    public void mouseInput(int button, int action) {
        mouseConsumers[button][action].input(action);
    }

    protected abstract void mouseLeftClick();
    protected abstract void mouseLeftRelease();
    protected abstract void mouseRightClick();
    protected abstract void mouseRightRelease();
    public abstract void mouseMove(float dx, float dy);
    public abstract void scroll(float y);

    public abstract void input(int key);
    public abstract void release(int key);
    public abstract void textInput(int key);
}
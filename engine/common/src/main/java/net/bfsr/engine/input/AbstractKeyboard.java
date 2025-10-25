package net.bfsr.engine.input;

public abstract class AbstractKeyboard {
    public abstract void setInputHandler(AbstractInputHandler inputHandler);
    public abstract boolean isKeyDown(int key);
    public abstract void clear();
}
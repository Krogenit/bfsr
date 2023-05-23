package net.bfsr.engine.input;

public abstract class AbstractInputHandler {
    public abstract void mouseMove(float dx, float dy);
    public abstract void mouseInput(int button, int action);
    public abstract void scroll(float y);

    public abstract void input(int key);
    public abstract void release(int key);
    public abstract void textInput(int key);
}
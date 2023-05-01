package net.bfsr.client.input;

public class InputController {
    public void init() {}

    public void update() {}

    public boolean onMouseLeftClick() {
        return false;
    }

    public boolean onMouseLeftRelease() {
        return false;
    }

    public boolean onMouseRightClick() {
        return false;
    }

    public boolean onMouseRightRelease() {
        return false;
    }

    public boolean scroll(float y) {
        return false;
    }

    public void mouseMove(float x, float y) {}

    public boolean input(int key) {
        return false;
    }

    public void textInput(int key) {}

    public void release(int key) {}
}
package net.bfsr.client.input;

public class InputController {
    public void update(int frame) {}

    public boolean mouseLeftClick() {
        return false;
    }

    public boolean mouseLeftRelease() {
        return false;
    }

    public boolean mouseRightClick() {
        return false;
    }

    public boolean mouseRightRelease() {
        return false;
    }

    public boolean scroll(float scrollY) {
        return false;
    }

    public boolean mouseMove(float x, float y) {
        return false;
    }

    public boolean input(int key) {
        return false;
    }

    public boolean textInput(int key) {
        return false;
    }

    public void release(int key) {}
}
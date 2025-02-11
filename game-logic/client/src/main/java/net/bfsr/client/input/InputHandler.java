package net.bfsr.client.input;

import net.bfsr.engine.input.AbstractInputHandler;

import java.util.ArrayList;
import java.util.List;

public class InputHandler extends AbstractInputHandler {
    private final List<InputController> inputControllers = new ArrayList<>(4);

    public InputHandler(InputController... controllers) {
        for (int i = 0; i < controllers.length; i++) {
            inputControllers.add(controllers[i]);
        }
    }

    public void addInputController(InputController controller) {
        inputControllers.add(controller);
    }

    public void update() {
        for (int i = 0; i < inputControllers.size(); i++) {
            inputControllers.get(i).update();
        }
    }

    @Override
    public void input(int key) {
        for (int i = 0; i < inputControllers.size(); i++) {
            if (inputControllers.get(i).input(key)) {
                break;
            }
        }
    }

    @Override
    public void textInput(int key) {
        for (int i = 0; i < inputControllers.size(); i++) {
            if (inputControllers.get(i).textInput(key)) {
                break;
            }
        }
    }

    @Override
    public void release(int key) {
        for (int i = 0; i < inputControllers.size(); i++) {
            inputControllers.get(i).release(key);
        }
    }

    @Override
    protected void mouseLeftClick() {
        for (int i = 0; i < inputControllers.size(); i++) {
            if (inputControllers.get(i).mouseLeftClick()) {
                break;
            }
        }
    }

    @Override
    protected void mouseLeftRelease() {
        for (int i = 0; i < inputControllers.size(); i++) {
            if (inputControllers.get(i).mouseLeftRelease()) {
                break;
            }
        }
    }

    @Override
    protected void mouseRightClick() {
        for (int i = 0; i < inputControllers.size(); i++) {
            if (inputControllers.get(i).mouseRightClick()) {
                break;
            }
        }
    }

    @Override
    protected void mouseRightRelease() {
        for (int i = 0; i < inputControllers.size(); i++) {
            if (inputControllers.get(i).mouseRightRelease()) {
                break;
            }
        }
    }

    @Override
    public void mouseMove(float dx, float dy) {
        for (int i = 0; i < inputControllers.size(); i++) {
            if (inputControllers.get(i).mouseMove(dx, dy)) {
                break;
            }
        }
    }

    @Override
    public void scroll(float y) {
        for (int i = 0; i < inputControllers.size(); i++) {
            if (inputControllers.get(i).scroll(y)) {
                break;
            }
        }
    }
}
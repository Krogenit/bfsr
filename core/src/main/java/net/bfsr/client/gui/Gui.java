package net.bfsr.client.gui;

import net.bfsr.client.font.FontRenderer;
import net.bfsr.client.font_new.FontType;
import net.bfsr.client.font_new.GLString;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.input.InputBox;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.core.Core;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class Gui {
    protected int width, height;
    protected List<Button> buttons = new ArrayList<>();
    protected List<Slider> sliders = new ArrayList<>();
    protected List<InputBox> inputBoxes = new ArrayList<>();
    protected List<Scroll> scrolls = new ArrayList<>();
    protected final List<GLString> staticStrings = new ArrayList<>(0);
    protected Vector2f center;

    public Gui() {
        center = new Vector2f();
    }

    public void init() {
        width = Core.getCore().getWidth();
        height = Core.getCore().getHeight();
        center.x = width / 2.0f;
        center.y = height / 2.0f;
    }

    protected void createString(FontType font, String text, int x, int y, int fontSize, float r, float g, float b, float a) {
        GLString glString = FontRenderer.getInstance().createString(font, text, x, y, fontSize, r, g, b, a);
        staticStrings.add(glString);
    }

    public void textInput(int key) {
        int size = inputBoxes.size();
        for (int i = 0; i < size; i++) {
            InputBox inputBox = inputBoxes.get(i);
            inputBox.textInput(key);
        }
    }

    public void update() {
        int size = buttons.size();
        for (int i = 0; i < size; i++) {
            Button button = buttons.get(i);
            button.update();
        }

        size = sliders.size();
        for (int i = 0; i < size; i++) {
            Slider slider = sliders.get(i);
            slider.update();
        }

        size = inputBoxes.size();
        for (int i = 0; i < size; i++) {
            InputBox boxes = inputBoxes.get(i);
            boxes.update();
        }

        size = scrolls.size();
        for (int i = 0; i < size; i++) {
            Scroll scroll = scrolls.get(i);
            scroll.update();
        }
    }

    protected void drawStaticStrings() {
        FontRenderer.getInstance().render(staticStrings);
    }

    public void render(BaseShader shader) {
        int size = buttons.size();
        for (int i = 0; i < size; i++) {
            Button button = buttons.get(i);
            button.render(shader);
        }

        size = sliders.size();
        for (int i = 0; i < size; i++) {
            Slider slider = sliders.get(i);
            slider.render(shader);
        }

        size = inputBoxes.size();
        for (int i = 0; i < size; i++) {
            InputBox inputBox = inputBoxes.get(i);
            inputBox.render(shader);
        }

        size = scrolls.size();
        for (int i = 0; i < size; i++) {
            Scroll scroll = scrolls.get(i);
            scroll.render(shader);
        }
    }

    protected void onButtonLeftClick(Button b) {

    }

    protected void onButtonRightClick(Button b) {

    }

    public void onMouseLeftClicked() {
        for (int i = 0; i < buttons.size(); i++) {
            Button b = buttons.get(i);
            if (b.isIntersects()) {
                b.leftClick();
                onButtonLeftClick(b);
            }
        }

        int size = sliders.size();
        for (int i = 0; i < size; i++) {
            Slider slider = sliders.get(i);
            slider.onMouseLeftClicked();
        }

        size = scrolls.size();
        for (int i = 0; i < size; i++) {
            Scroll scroll = scrolls.get(i);
            scroll.onMouseLeftClicked();
        }

        size = inputBoxes.size();
        for (int i = 0; i < size; i++) {
            InputBox inputBox = inputBoxes.get(i);
            inputBox.onMouseLeftClicked();
        }
    }

    public void onMouseLeftRelease() {
        int size = scrolls.size();
        for (int i = 0; i < size; i++) {
            Scroll scroll = scrolls.get(i);
            scroll.onMouseLeftRelease();
        }

        size = sliders.size();
        for (int i = 0; i < size; i++) {
            Slider slider = sliders.get(i);
            slider.onMouseLeftRelease();
        }
    }

    public void onMouseRightClicked() {
        int size = buttons.size();
        for (int i = 0; i < size; i++) {
            Button b = buttons.get(i);
            if (b.isIntersects()) {
                b.rightClick();
                onButtonRightClick(b);
            }
        }
    }

    public void onMouseRightRelease() {

    }

    public void onMouseScroll(float y) {
        int size = scrolls.size();
        for (int i = 0; i < size; i++) {
            Scroll scroll = scrolls.get(i);
            scroll.scroll(y);
        }
    }

    public void resize(int width, int height) {
        init();
    }

    public void input(int key) {
        int size = inputBoxes.size();
        for (int i = 0; i < size; i++) {
            InputBox in = inputBoxes.get(i);
            in.input(key);
        }
    }

    public void clear() {
        while (staticStrings.size() > 0) {
            staticStrings.remove(0).clear();
        }
        int size = buttons.size();
        for (int i = 0; i < size; i++) {
            Button b = buttons.get(i);
            b.clear();
        }
        size = sliders.size();
        for (int i = 0; i < size; i++) {
            Slider b = sliders.get(i);
            b.clear();
        }
        size = inputBoxes.size();
        for (int i = 0; i < size; i++) {
            InputBox b = inputBoxes.get(i);
            b.clear();
        }
        buttons.clear();
        sliders.clear();
        inputBoxes.clear();
        scrolls.clear();
    }
}

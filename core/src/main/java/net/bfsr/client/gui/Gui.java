package net.bfsr.client.gui;

import net.bfsr.client.font.FontRenderer;
import net.bfsr.client.font_new.FontType;
import net.bfsr.client.font_new.GLString;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.input.InputBox;
import net.bfsr.client.input.Mouse;
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
        for (InputBox inputBox : inputBoxes) {
            if (inputBox.isTyping()) {
                inputBox.textInput(key);
            }
        }
    }

    public void update(double delta) {
        for (Button button : buttons) {
            button.update(delta);
        }

        for (Slider slider : sliders) {
            slider.update(delta);
        }

        for (InputBox boxes : inputBoxes) {
            boxes.update(delta);
        }

        for (Scroll scroll : scrolls) {
            scroll.update(delta);
        }
    }

    protected void drawStaticStrings() {
        FontRenderer.getInstance().render(staticStrings);
    }

    public void render(BaseShader shader) {
        for (Button button : buttons) {
            button.render(shader);
        }

        for (Slider slider : sliders) {
            slider.render(shader);
        }

        for (InputBox inputBox : inputBoxes) {
            inputBox.render(shader);
        }

        for (Scroll scroll : scrolls) {
            scroll.render(shader);
        }
    }

    protected void onButtonLeftClick(Button b) {

    }

    protected void onButtonRightClick(Button b) {

    }

    protected void onLeftClicked() {
        for (int i = 0; i < buttons.size(); i++) {
            Button b = buttons.get(i);
            if (b.isIntersects()) {
                b.leftClick();
                onButtonLeftClick(b);
            }
        }

        for (Slider slider : sliders) {
            slider.setMoving(false);
        }

        for (Scroll scroll : scrolls) {
            scroll.setMoving(false);
        }

        for (InputBox inputBox : inputBoxes) {
            if (inputBox.isIntersects()) {
                if (!inputBox.isTyping()) inputBox.setTyping(true);
            } else {
                inputBox.setTyping(false);
            }
        }
    }

    protected void onRightClicked() {
        for (Button b : buttons) {
            if (b.isIntersects()) {
                b.rightClick();
                onButtonRightClick(b);
            }
        }
    }

    public void resize(int width, int height) {
        init();
    }

    public void input() {
        if (Mouse.isLeftStartDown()) onLeftClicked();
        if (Mouse.isRightStartDown()) onRightClicked();

        Vector2f mousePos = Mouse.getPosition();

        if (Mouse.isLeftDown()) {
            for (Slider slider : sliders) {
                if (slider.isMoving()) {
                    slider.input(mousePos.x, mousePos.y);
                }
            }

            for (Scroll scroll : scrolls) {
                if (scroll.isMoving()) {
                    scroll.input(mousePos.x, mousePos.y);
                }
            }
        }

        if (Mouse.isLeftStartDown()) {
            for (Slider slider : sliders) {
                if (slider.isIntersects()) {
                    slider.setMoving(true);
                }
            }

            for (Scroll scroll : scrolls) {
                if (scroll.isIntersects()) {
                    scroll.setMoving(true);
                }
            }
        }

        for (InputBox in : inputBoxes) {
            if (in.isTyping()) {
                in.input();
            }
        }

        Vector2f mouseScroll = Mouse.getScroll();

        for (Scroll scroll : scrolls) {
            scroll.scroll(mouseScroll);
        }

        if (Mouse.isLeftRelease()) {
            for (Scroll scroll : scrolls) {
                scroll.setMoving(false);
            }
        }
    }

    public void clear() {
        while (staticStrings.size() > 0) {
            staticStrings.remove(0).clear();
        }
        for (Button b : buttons)
            b.clear();
        for (Slider b : sliders)
            b.clear();
        for (InputBox b : inputBoxes)
            b.clear();
        buttons.clear();
        sliders.clear();
        inputBoxes.clear();
        scrolls.clear();
    }
}

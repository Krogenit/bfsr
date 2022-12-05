package net.bfsr.client.gui;

import net.bfsr.client.font.GUIText;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.button.ButtonBase;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.language.Lang;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.core.Core;
import net.bfsr.math.Transformation;
import net.bfsr.settings.ClientSettings;
import net.bfsr.settings.EnumOption;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GuiSettings extends Gui {

    private final Gui prevGui;
    private boolean isInGame;
    private final List<GUIText> sections;
    private final Scroll scroll;
    private final GUIText mainText;
    private boolean firstInit = false;

    public GuiSettings(Gui prevGui) {
        this.prevGui = prevGui;
        this.scroll = new Scroll(new Vector2f(), new Vector2f());
        this.mainText = new GUIText(Lang.getString("gui.settings.mainText"), new Vector2f(), new Vector4f(1, 1, 1, 1), EnumParticlePositionType.Gui);

        this.sections = new ArrayList<>();
    }

    @Override
    public void init() {
        super.init();
        isInGame = Core.getCore().getWorld() != null;

        mainText.setPosition(center.x, 50 * Transformation.guiScale.y);
        mainText.setFontSize(Transformation.getScale(1.25f, 1.25f));
        mainText.updateText(Lang.getString("gui.settings.mainText"));

        float buttonXOffset = 250;
        float baseYOffset = 60;
        float baseY = 100 - (scroll.getMaxValue() - scroll.getValue()) * baseYOffset;
        float x;
        float y = baseY * Transformation.guiScale.y;
        float visible = 9f;
        float minY = 100;
        float maxY = minY + baseYOffset * Transformation.guiScale.y * visible;
        Vector2f fontSectionSize = Transformation.getScale(1f, 1f);

        int countItems = 0;
        for (String section : ClientSettings.optionsByCategory.keySet()) {
            List<EnumOption> options = ClientSettings.optionsByCategory.get(section);

            x = center.x;

            if (y >= minY && y < maxY) {
                GUIText sectionText = new GUIText(Lang.getString("settings.section." + section), fontSectionSize, new Vector2f(x, y), new Vector4f(1, 1, 1, 1), true, EnumParticlePositionType.Gui);
                this.sections.add(sectionText);
            }

//			y += baseYOffset * Transformation.guiScale.y;
            countItems++;

            for (int i = 0; i < options.size(); i++) {
                EnumOption option = options.get(i);

                if (i % 2 == 0) {
                    countItems++;
                    x = center.x - buttonXOffset * Transformation.guiScale.x;
                    y += baseYOffset * Transformation.guiScale.y;
                } else x = center.x + buttonXOffset * Transformation.guiScale.x;

                if (y >= minY && y < maxY) {
                    if (option.getType() == float.class || option.getType() == int.class) {
                        Slider slider = new Slider(new Vector2f(x, y), new Vector2f(450, 50), new Vector4f(1, 1, 1, 1), option);
                        slider.setPosition(x, y);
                        sliders.add(slider);
                    } else {
                        Button button = new Button(1, TextureRegister.guiButtonBase, new Vector2f(x, y), new Vector2f(450, 50), option);
                        buttons.add(button);
                    }
                }
            }

            y += baseYOffset * Transformation.guiScale.y;
        }

        if (!firstInit) scroll.setValue(countItems - visible);
        scroll.setVisible(visible);
        scroll.setMaxValue(countItems);
        scroll.setPosition(width - 25, center.y);
        scroll.setScale(25f, 750f);
        scrolls.add(scroll);

        x = center.x;
        y = center.y + 310;
        Button button = new ButtonBase(0, new Vector2f(x, y), "gui.settings.save");
        buttons.add(button);
        firstInit = true;
    }

    @Override
    public void input() {
        super.input();
        if (Keyboard.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            Core.getCore().setCurrentGui(prevGui);
        }

        Vector2f scroll = Mouse.getScroll();
        if (scroll.y != 0 || this.scroll.isMoving()) {
            clear();
            init();
        }
    }

    @Override
    protected void onButtonLeftClick(Button b) {
        if (b.getId() == 0) {
            Core.getCore().setCurrentGui(prevGui);
        }
    }

    public Vector2f getScissorSize() {
        return new Vector2f();
    }

    public Vector2f getScissorPos() {
        return new Vector2f();
    }

    @Override
    public void render(BaseShader shader) {
        if (isInGame) {
            shader.setColor(new Vector4f(0, 0, 0, 0.5f));
            shader.setModelViewMatrix(Transformation.getModelViewMatrixGui(width / 2.0F, height / 2.0f, 0, width, height));
            shader.disableTexture();
            Renderer.quad.render();
        }

        super.render(shader);
    }

    @Override
    public void clear() {
        super.clear();
        for (GUIText text : sections) {
            text.clear();
        }
        sections.clear();
        mainText.clear();
    }

}

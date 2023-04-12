package net.bfsr.client.gui;

import net.bfsr.client.core.Core;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.scroll.Scroll;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.language.Lang;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import net.bfsr.client.settings.Option;
import net.bfsr.settings.SettingsCategory;
import net.bfsr.settings.option.SettingsOption;
import net.bfsr.texture.TextureRegister;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GuiSettings extends Gui {
    private final boolean isInGame;
    private final List<StringObject> sections = new ArrayList<>();
    private final Scroll scroll = new Scroll();
    private final StringObject mainText = new StringObject(FontType.XOLONIUM, Lang.getString("gui.settings.mainText"));
    private final List<SimpleGuiObject> scissorAffected = new ArrayList<>();
    private Button saveButton;
    private final Option[] options = Option.values();
    private final SettingsOption<?>[] lastOptions = new SettingsOption[options.length];

    public GuiSettings(Gui parentGui) {
        super(parentGui);
        isInGame = Core.get().getWorld() != null;
        mainText.setStringOffsetType(StringOffsetType.CENTERED);
        for (int i = 0; i < lastOptions.length; i++) {
            lastOptions[i] = new SettingsOption<>(options[i].getValue());
        }
    }

    @Override
    protected void initElements() {
        int backgroundHeight = 60;
        registerGuiObject(mainText.setFontSize(24).atTop(0, 40));
        mainText.compile();

        int buttonXOffset = 250;
        int baseYOffset = 60;
        int baseY = 90;
        int x;
        int y = baseY;
        int halfBackgroundHeight = backgroundHeight << 1;
        float minY = 100;
        float maxY = height - backgroundHeight;
        int fontSectionSize = 20;
        int buttonWidth = 450;
        int buttonHeight = 50;

        EnumMap<SettingsCategory, List<Option>> optionsByCategory = new EnumMap<>(SettingsCategory.class);
        Option[] enumOptions = Option.values();
        for (int i = 0; i < enumOptions.length; i++) {
            Option option = enumOptions[i];
            List<Option> options = optionsByCategory.computeIfAbsent(option.getCategory(), settingsCategory -> new ArrayList<>(1));
            options.add(option);
            optionsByCategory.put(option.getCategory(), options);
        }

        for (Map.Entry<SettingsCategory, List<Option>> entry : optionsByCategory.entrySet()) {
            List<Option> options = entry.getValue();

            StringObject sectionText = new StringObject(FontType.XOLONIUM, Lang.getString("settings.section." + entry.getKey().getCategoryName()), fontSectionSize, StringOffsetType.CENTERED);
            sections.add(sectionText);
            scroll.registerGuiObject(sectionText);
            registerGuiObject(sectionText);
            sectionText.atTop(0, y);
            sectionText.compile();

            for (int i = 0; i < options.size(); i++) {
                Option option = options.get(i);

                if (i % 2 == 0) {
                    x = -buttonXOffset - buttonWidth / 2;
                    y += baseYOffset;
                } else {
                    x = buttonXOffset - buttonWidth / 2;
                }

                if (option.useSlider()) {
                    Slider slider = new Slider(x, y - 35, buttonWidth, buttonHeight, option);
                    slider.atTop(x, y - 35);
                    registerGuiObject(slider);
                    scroll.registerGuiObject(slider);
                    scissorAffected.add(slider);
                } else {
                    Button button = new Button(TextureRegister.guiButtonBase, x, y - 35, buttonWidth, buttonHeight, Lang.getString("settings." + option.getOptionName()) + ": " + option.getValue(), 20);
                    button.atTop(x, y - 35);
                    button.setOnMouseClickRunnable(() -> {
                        option.changeValue();
                        button.setString(Lang.getString("settings." + option.getOptionName()) + ": " + option.getValue());
                    });
                    button.setIntersectsCheckMethod(() -> Mouse.getPosition().y > minY && Mouse.getPosition().y < maxY && button.isIntersects(Mouse.getPosition().x, Mouse.getPosition().y));
                    registerGuiObject(button);
                    scroll.registerGuiObject(button);
                    scissorAffected.add(button);
                }
            }

            y += baseYOffset;
        }

        scroll.setTotalHeight(y - baseY).setViewHeightResizeFunction((width, height) -> height - halfBackgroundHeight)
                .setRepositionConsumer((width, height) -> scroll.setPosition(width - 25, height / 2 - (height - halfBackgroundHeight) / 2))
                .setWidth(25).setHeightResizeFunction((width, height) -> height - halfBackgroundHeight);
        registerGuiObject(scroll);
        saveButton = new Button(Lang.getString("gui.settings.save"), 20, () -> {
            Core.get().getSettings().saveSettings();
            Core.get().setCurrentGui(parentGui);
        });
        registerGuiObject(saveButton.atBottom(-150, -55));
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            restoreSettings();
            Core.get().setCurrentGui(parentGui);
        }
    }

    private void restoreSettings() {
        for (int i = 0; i < options.length; i++) {
            options[i].setValue(lastOptions[i].getValue());
        }
    }

    @Override
    public void render() {
        if (isInGame) {
            GUIRenderer.get().add(0, 0, width, height, 0.0f, 0.0f, 0.0f, 0.5f);
        }

        int backgroundHeight = 60;
        GUIRenderer.get().add(0, 0, width, backgroundHeight, 0.1f, 0.2f, 0.4f, 1.0f);
        GUIRenderer.get().add(0, height - backgroundHeight, width, backgroundHeight, 0.1f, 0.2f, 0.4f, 1.0f);
        mainText.render();

        GUIRenderer.get().render();

        GL11C.glEnable(GL11C.GL_SCISSOR_TEST);
        GL11C.glScissor(0, 60, width, Math.max(height - 120, 0));

        for (int i = 0; i < sections.size(); i++) {
            sections.get(i).render();
        }

        int size = scissorAffected.size();
        for (int i = 0; i < size; i++) {
            scissorAffected.get(i).render();
        }

        GUIRenderer.get().render();
        GL11C.glDisable(GL11C.GL_SCISSOR_TEST);

        saveButton.render();
        scroll.render();
    }
}
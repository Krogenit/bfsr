package net.bfsr.client.gui.settings;

import net.bfsr.client.Core;
import net.bfsr.client.language.Lang;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.engine.gui.object.GuiObjectsContainer;
import net.bfsr.engine.gui.object.TexturedGuiObject;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.settings.SettingsCategory;
import net.bfsr.settings.option.SettingsOption;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

public class GuiSettings extends Gui {
    private final boolean isInGame;
    private final GuiObjectsContainer container = new GuiObjectsContainer(25);
    private final StringObject mainText = new StringObject(FontType.XOLONIUM, Lang.getString("gui.settings.mainText"));
    private Button saveButton;
    private final ClientSettings[] options = ClientSettings.values();
    private final SettingsOption<?>[] lastOptions = new SettingsOption[options.length];

    public GuiSettings(Gui parentGui) {
        super(parentGui);
        isInGame = Core.get().isInWorld();
        mainText.setStringOffsetType(StringOffsetType.CENTERED);
        for (int i = 0; i < lastOptions.length; i++) {
            lastOptions[i] = new SettingsOption<>(options[i].getValue());
        }
    }

    @Override
    protected void initElements() {
        if (isInGame) {
            registerGuiObject(new TexturedGuiObject(null, 0, 0, width, height).setAllColors(0.0f, 0.0f, 0.0f, 0.5f));
        }

        int backgroundHeight = 60;
        registerGuiObject(new TexturedGuiObject(null, 0, 0, width, backgroundHeight).setAllColors(0.1f, 0.2f, 0.4f, 1.0f));
        registerGuiObject(new TexturedGuiObject(null, 0, height - backgroundHeight, width, backgroundHeight)
                .setAllColors(0.1f, 0.2f, 0.4f, 1.0f));
        registerGuiObject(mainText.setFontSize(24).compileAtOrigin().atTop(0, 40));

        int buttonXOffset = 250;
        int baseYOffset = 60;
        int baseY = 90;
        int x;
        int y = baseY;
        int fontSectionSize = 20;
        int buttonWidth = 450;
        int buttonHeight = 50;

        EnumMap<SettingsCategory, List<ClientSettings>> optionsByCategory = new EnumMap<>(SettingsCategory.class);
        ClientSettings[] enumOptions = ClientSettings.values();
        for (int i = 0; i < enumOptions.length; i++) {
            ClientSettings option = enumOptions[i];
            List<ClientSettings> options =
                    optionsByCategory.computeIfAbsent(option.getCategory(), settingsCategory -> new ArrayList<>(1));
            options.add(option);
            optionsByCategory.put(option.getCategory(), options);
        }

        container.setScrollColor(0.5f, 0.5f, 0.5f, 1.0f);
        container.setScrollHoverColor(0.7f, 0.7f, 0.7f, 1.0f);
        registerGuiObject(container.atTopLeftCorner(0, 60).setWidthResizeFunction((width, height) -> width)
                .setHeightResizeFunction((width, height) -> height - 120));

        for (Map.Entry<SettingsCategory, List<ClientSettings>> entry : optionsByCategory.entrySet()) {
            List<ClientSettings> options = entry.getValue();

            StringObject sectionText = new StringObject(FontType.XOLONIUM, Lang.getString("settings.section." +
                    entry.getKey().getCategoryName()), fontSectionSize, StringOffsetType.CENTERED);
            container.registerGuiObject(sectionText.compileAtOrigin().atTop(0, y));

            for (int i = 0; i < options.size(); i++) {
                ClientSettings option = options.get(i);

                if (i % 2 == 0) {
                    x = -buttonXOffset - buttonWidth / 2;
                    y += baseYOffset;
                } else {
                    x = buttonXOffset - buttonWidth / 2;
                }

                if (option.useSlider()) {
                    container.registerGuiObject(new OptionSlider(x, y - 35, buttonWidth, buttonHeight, option).atTop(x, y - 35));
                } else {
                    Button button = new Button(TextureRegister.guiButtonBase, x, y - 35, buttonWidth, buttonHeight,
                            Lang.getString("settings." + option.getOptionName()) + ": " + option.getValue(), 20);
                    button.setOnMouseClickRunnable(() -> {
                        option.changeValue();
                        button.setString(Lang.getString("settings." + option.getOptionName()) + ": " + option.getValue());
                    });
                    container.registerGuiObject(button.atTop(x, y - 35));
                }
            }

            y += baseYOffset;
        }

        saveButton = new Button(Lang.getString("gui.settings.save"), 20, () -> {
            Core.get().getSettings().save();
            Core.get().openGui(parentGui);
        });
        registerGuiObject(saveButton.atBottom(-150, -55));
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (key == KEY_ESCAPE) {
            restoreSettings();
            Core.get().openGui(parentGui);
        }

        return input;
    }

    private void restoreSettings() {
        for (int i = 0; i < options.length; i++) {
            options[i].setValue(lastOptions[i].getValue());
        }
    }
}
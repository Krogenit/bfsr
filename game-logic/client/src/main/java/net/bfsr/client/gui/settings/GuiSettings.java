package net.bfsr.client.gui.settings;

import net.bfsr.client.Core;
import net.bfsr.client.language.Lang;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.ScrollPane;
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
    private final ClientSettings[] options = ClientSettings.values();
    private final SettingsOption<?>[] lastOptions = new SettingsOption[options.length];

    public GuiSettings(Gui parentGui) {
        super(parentGui);

        for (int i = 0; i < lastOptions.length; i++) {
            lastOptions[i] = new SettingsOption<>(options[i].getValue());
        }

        if (Core.get().isInWorld()) {
            add(new Rectangle(0, 0, width, height).setAllColors(0.0f, 0.0f, 0.0f, 0.5f));
        }

        int buttonXOffset = 250;
        int baseYOffset = 60;
        int baseY = 30;
        int x;
        int y = baseY;
        int fontSectionSize = 20;
        int buttonWidth = 450;
        int buttonHeight = 50;

        EnumMap<SettingsCategory, List<ClientSettings>> optionsByCategory = new EnumMap<>(SettingsCategory.class);
        ClientSettings[] enumOptions = ClientSettings.values();
        for (int i = 0; i < enumOptions.length; i++) {
            ClientSettings option = enumOptions[i];
            List<ClientSettings> options = optionsByCategory.computeIfAbsent(option.getCategory(), settingsCategory -> new ArrayList<>(1));
            options.add(option);
            optionsByCategory.put(option.getCategory(), options);
        }

        ScrollPane scrollPane = new ScrollPane(width, height - 120, 25);
        scrollPane.setScrollColor(0.5f, 0.5f, 0.5f, 1.0f).setScrollHoverColor(0.7f, 0.7f, 0.7f, 1.0f);
        add(scrollPane.atTopLeft(0, 60).setHeightFunction((width, height) -> height - 120));

        for (Map.Entry<SettingsCategory, List<ClientSettings>> entry : optionsByCategory.entrySet()) {
            List<ClientSettings> options = entry.getValue();

            Label sectionText = new Label(FontType.XOLONIUM, Lang.getString("settings.section." +
                    entry.getKey().getCategoryName()), fontSectionSize, StringOffsetType.CENTERED);
            sectionText.atTop(0, y);
            scrollPane.add(sectionText.compile());

            for (int i = 0; i < options.size(); i++) {
                ClientSettings option = options.get(i);

                if (i % 2 == 0) {
                    x = -buttonXOffset - buttonWidth / 2;
                    y += baseYOffset;
                } else {
                    x = buttonXOffset - buttonWidth / 2;
                }

                if (option.useSlider()) {
                    scrollPane.add(new OptionSlider(x, y - 35, buttonWidth, buttonHeight, option).atTop(x, y - 35));
                } else {
                    Button button = new Button(TextureRegister.guiButtonBase, x, y - 35, buttonWidth, buttonHeight,
                            Lang.getString("settings." + option.getOptionName()) + ": " + option.getValue(), 20);
                    button.setLeftReleaseRunnable(() -> {
                        option.changeValue();
                        button.setString(Lang.getString("settings." + option.getOptionName()) + ": " + option.getValue());
                    });
                    scrollPane.add(button.atTop(x, y - 35));
                }
            }

            y += baseYOffset;
        }

        int backgroundHeight = 60;
        add(new Rectangle(0, 0, width, backgroundHeight).setAllColors(0.1f, 0.2f, 0.4f, 1.0f));
        add(new Rectangle(0, height - backgroundHeight, width, backgroundHeight).setAllColors(0.1f, 0.2f, 0.4f, 1.0f));

        add(new Label(FontType.XOLONIUM, Lang.getString("gui.settings.mainText"), 24, StringOffsetType.CENTERED).compileAtOrigin()
                .atTop(0, 40));

        add(new Button(Lang.getString("gui.settings.save"), 20, () -> {
            Core.get().getSettings().save();
            Core.get().openGui(parentGui);
        }).atBottom(-150, -55));
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (!input && key == KEY_ESCAPE) {
            restoreSettings();
            Core.get().openGui(parentGui);
            return true;
        }

        return input;
    }

    private void restoreSettings() {
        for (int i = 0; i < options.length; i++) {
            options[i].setValue(lastOptions[i].getValue());
        }
    }
}
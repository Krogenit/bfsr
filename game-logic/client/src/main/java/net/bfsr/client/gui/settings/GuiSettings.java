package net.bfsr.client.gui.settings;

import net.bfsr.client.Client;
import net.bfsr.client.font.FontType;
import net.bfsr.client.language.LanguageManager;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.settings.SettingsOption;
import net.bfsr.settings.SettingsCategory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

public class GuiSettings extends Gui {
    private final Client client = Client.get();
    private final ClientSettings[] options = ClientSettings.values();
    private final SettingsOption<?>[] lastOptions = new SettingsOption[options.length];

    public GuiSettings(Gui parentGui) {
        super(parentGui);

        for (int i = 0; i < lastOptions.length; i++) {
            lastOptions[i] = new SettingsOption<>(options[i].getValue());
        }

        if (client.isInWorld()) {
            add(new Rectangle(0, 0, width, height).setAllColors(0.0f, 0.0f, 0.0f, 0.5f));
        }

        int buttonXOffset = 250;
        int baseYOffset = 60;
        int x;
        int y = 10;
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
        add(scrollPane.atBottomLeft(0, 60).setHeightFunction((width, height) -> height - 120).setWidthFunction((width, height) -> width));
        LanguageManager languageManager = client.getLanguageManager();

        for (Map.Entry<SettingsCategory, List<ClientSettings>> entry : optionsByCategory.entrySet()) {
            List<ClientSettings> options = entry.getValue();

            Label sectionText = new Label(Engine.getFontManager().getFont(FontType.XOLONIUM.getFontName()),
                    languageManager.getString("settings.section." + entry.getKey().getCategoryName()), fontSectionSize);
            scrollPane.add(sectionText.atTop(0, y - 20));

            for (int i = 0; i < options.size(); i++) {
                ClientSettings option = options.get(i);

                if (i % 2 == 0) {
                    x = -buttonXOffset;
                    y -= baseYOffset;
                } else {
                    x = buttonXOffset;
                }

                if (option.useSlider()) {
                    scrollPane.add(new OptionSlider(buttonWidth, buttonHeight, option).atTop(x, y - 5));
                } else {
                    Button button = new Button(TextureRegister.guiButtonBase, buttonWidth, buttonHeight,
                            languageManager.getString("settings." + option.getOptionName()) + ": " + option.getValue());
                    button.setLeftReleaseConsumer((mouseX, mouseY) -> {
                        option.changeValue(client);
                        button.setString(languageManager.getString("settings." + option.getOptionName()) + ": " + option.getValue());
                    });
                    scrollPane.add(button.atTop(x, y - 5));
                }
            }

            y -= baseYOffset;
        }

        int backgroundHeight = 60;
        add(new Rectangle(width, backgroundHeight).setWidthFunction((width, height) -> width).atBottomLeft(0, 0)
                .setAllColors(0.1f, 0.2f, 0.4f, 1.0f));
        add(new Rectangle(width, backgroundHeight).setWidthFunction((width, height) -> width).atTopLeft(0, 0)
                .setAllColors(0.1f, 0.2f, 0.4f, 1.0f));

        Label label = new Label(Engine.getFontManager().getFont(FontType.XOLONIUM.getFontName()),
                languageManager.getString("gui.settings.mainText"), 24);
        add(label.atTop(0, label.getCenteredOffsetY(backgroundHeight) - 36));

        add(new Button(languageManager.getString("gui.settings.save"), 20, (mouseX, mouseY) -> {
            client.getSettings().save();
            client.openGui(parentGui);
        }).atBottom(0, 6));
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (!input && key == KEY_ESCAPE) {
            restoreSettings();
            client.openGui(parentGui);
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
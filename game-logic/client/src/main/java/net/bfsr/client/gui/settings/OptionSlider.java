package net.bfsr.client.gui.settings;

import net.bfsr.client.Client;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.gui.component.Slider;
import net.bfsr.engine.util.DecimalUtils;

public class OptionSlider extends Slider {
    private final ClientSettings option;

    OptionSlider(int width, int height, ClientSettings option) {
        super(width, height, 20, (option.getFloat() - option.getMinValue()) / (option.getMaxValue() - option.getMinValue()),
                Client.get().getLanguageManager().getString("settings." + option.getOptionName()) + ": " +
                        DecimalUtils.strictFormatWithToDigits(option.getFloat()));
        this.option = option;
    }

    @Override
    protected void onValueChanged() {
        option.changeValue(value);
        label.setString(Client.get().getLanguageManager().getString("settings." + option.getOptionName()) + ": " +
                DecimalUtils.strictFormatWithToDigits(option.getFloat()));
    }
}
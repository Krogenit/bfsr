package net.bfsr.client.gui.settings;

import net.bfsr.client.language.Lang;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.gui.component.Slider;
import net.bfsr.util.DecimalUtils;

public class OptionSlider extends Slider {
    private final ClientSettings option;

    OptionSlider(int x, int y, int width, int height, ClientSettings option) {
        super(x, y, width, height, 20, (option.getFloat() - option.getMinValue()) / (option.getMaxValue() - option.getMinValue()),
                Lang.getString("settings." + option.getOptionName()) + ": " +
                        DecimalUtils.strictFormatWithToDigits(option.getFloat()));
        this.option = option;
    }

    @Override
    protected void onValueChanged() {
        option.changeValue(value);
        stringObject.setStringAndCompile(Lang.getString("settings." + option.getOptionName()) + ": " +
                DecimalUtils.strictFormatWithToDigits(option.getFloat()));
    }
}
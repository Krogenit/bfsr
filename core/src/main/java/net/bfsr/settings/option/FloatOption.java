package net.bfsr.settings.option;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FloatOption extends SettingsOption<Float> {
    public FloatOption(Float value) {
        super(value);
    }

    @Override
    public void setNumber(Number number) {
        value = number.floatValue();
    }

    @Override
    public float getFloat() {
        return value;
    }
}

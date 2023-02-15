package net.bfsr.settings.option;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IntegerOption extends SettingsOption<Integer> {
    public IntegerOption(Integer value) {
        super(value);
    }

    @Override
    public void setNumber(Number number) {
        value = number.intValue();
    }

    @Override
    public int getInteger() {
        return value;
    }

    @Override
    public float getFloat() {
        return value;
    }
}

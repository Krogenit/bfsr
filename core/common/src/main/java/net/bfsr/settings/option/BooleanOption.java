package net.bfsr.settings.option;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BooleanOption extends SettingsOption<Boolean> {
    public BooleanOption(Boolean value) {
        super(value);
    }

    @Override
    public boolean getBoolean() {
        return value;
    }
}

package net.bfsr.engine.settings;

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

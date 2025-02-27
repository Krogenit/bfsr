package net.bfsr.engine.settings;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StringOption extends SettingsOption<String> {
    public StringOption(String value) {
        super(value);
    }

    @Override
    public void setString(String string) {
        this.value = string;
    }

    @Override
    public String getString() {
        return value;
    }
}

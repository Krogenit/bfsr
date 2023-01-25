package net.bfsr.settings.option;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SettingsOption<T> {
    protected T value;

    public void setValue(Object value) {
        if (value instanceof Number number) {
            setNumber(number);
        } else {
            this.value = (T) value;
        }
    }

    public void setNumber(Number number) {
        throw new RuntimeException("Not supported");
    }

    public void setString(String string) {
        throw new RuntimeException("Not supported");
    }

    public String getString() {
        throw new RuntimeException("Not supported");
    }

    public float getFloat() {
        throw new RuntimeException("Not supported");
    }

    public boolean getBoolean() {
        throw new RuntimeException("Not supported");
    }

    public int getInteger() {
        throw new RuntimeException("Not supported");
    }
}

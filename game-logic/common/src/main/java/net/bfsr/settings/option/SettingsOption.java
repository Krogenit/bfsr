package net.bfsr.settings.option;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SettingsOption<T> {
    protected T value;
    private final List<Consumer<T>> changeValueListeners = new ArrayList<>();

    public void setValue(Object value) {
        if (value instanceof Number number) {
            setNumber(number);
        } else {
            this.value = (T) value;
        }

        for (int i = 0; i < changeValueListeners.size(); i++) {
            changeValueListeners.get(i).accept(this.value);
        }
    }

    public SettingsOption<T> addListener(Consumer<T> consumer) {
        changeValueListeners.add(consumer);
        return this;
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

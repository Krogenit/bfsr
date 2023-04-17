package net.bfsr.editor.property;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FloatConverter implements PropertyConverter<Float> {
    @Override
    public String toString(Float value) {
        return value.toString();
    }

    @Override
    public Float fromString(String value) {
        if (value.isEmpty()) return 0.0f;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            log.error("Couldn't convert value {} to float", value, e);
            return 0.0f;
        }
    }
}
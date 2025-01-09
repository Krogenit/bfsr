package net.bfsr.client.settings;

import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.Engine;
import net.bfsr.settings.SettingsCategory;
import net.bfsr.settings.option.BooleanOption;
import net.bfsr.settings.option.FloatOption;
import net.bfsr.settings.option.IntegerOption;
import net.bfsr.settings.option.SettingsOption;
import net.bfsr.settings.option.StringOption;

import java.util.function.BiConsumer;

public enum ClientSettings {
    SOUND_VOLUME(SettingsCategory.SOUND, 0.0f, 1.0f, new FloatOption(0.1f), ConsumerUtils.FLOAT_DEFAULT_CONSUMER),

    CAMERA_MOVE_BY_SCREEN_BORDERS(SettingsCategory.CAMERA, new BooleanOption(true), ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),
    CAMERA_MOVE_BY_SCREEN_BORDERS_SPEED(SettingsCategory.CAMERA, 1.0f, 50.0f, new FloatOption(6.0f),
            ConsumerUtils.FLOAT_DEFAULT_CONSUMER),
    CAMERA_MOVE_BY_SCREEN_BORDERS_OFFSET(SettingsCategory.CAMERA, 2.0f, 25.0f, new FloatOption(15.0f),
            ConsumerUtils.FLOAT_DEFAULT_CONSUMER),
    CAMERA_MOVE_BY_KEY_SPEED(SettingsCategory.CAMERA, 0.25f, 40.0f, new FloatOption(10.0f), ConsumerUtils.FLOAT_DEFAULT_CONSUMER),
    CAMERA_ZOOM_SPEED(SettingsCategory.CAMERA, 0.01f, 0.5f, new FloatOption(0.2f), ConsumerUtils.FLOAT_DEFAULT_CONSUMER),
    CAMERA_FOLLOW_PLAYER(SettingsCategory.CAMERA, new BooleanOption(true), ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),

    LANGUAGE(SettingsCategory.LANGUAGE, new StringOption("eng"), (option, value) -> {
        String nextLang = Lang.getNextLang(option.option.getString());
        option.option.setValue(nextLang);
    }),

    V_SYNC(SettingsCategory.GRAPHICS, new BooleanOption(true).addListener(value -> Engine.renderer.setVSync(value)),
            (option, value) -> option.option.setValue(!(Boolean) option.option.getValue())),
    MAX_FPS(SettingsCategory.GRAPHICS, 10, 240, new IntegerOption(60), ConsumerUtils.INTEGER_DEFAULT_CONSUMER),

    IS_DEBUG(SettingsCategory.DEBUG, new BooleanOption(false), ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),
    IS_PROFILING(SettingsCategory.DEBUG, new BooleanOption(false).addListener(value -> Client.get().getProfiler().setEnable(value)),
            (option, value) -> option.option.setValue(!(Boolean) option.option.getValue())),
    SHOW_DEBUG_BOXES(SettingsCategory.DEBUG,
            new BooleanOption(false).addListener(value -> Client.get().getGlobalRenderer().setDebugBoxesEnabled(value)),
            (option, value) -> option.invertBooleanValue());

    @Getter
    private final SettingsCategory category;
    @Getter
    private final float minValue;
    @Getter
    private final float maxValue;
    private final SettingsOption<?> option;
    private final BiConsumer<ClientSettings, Object> runnable;

    ClientSettings(SettingsCategory category, SettingsOption<?> option, BiConsumer<ClientSettings, Object> runnable) {
        this(category, 0, 0, option, runnable);
    }

    ClientSettings(SettingsCategory category, float minValue, float maxValue, SettingsOption<?> option,
                   BiConsumer<ClientSettings, Object> runnable) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.category = category;
        this.option = option;
        this.runnable = runnable;
    }

    public void changeValue(Object newValue) {
        runnable.accept(this, newValue);
    }

    public void changeValue() {
        runnable.accept(this, null);
    }

    public void setFloat(float value) {
        option.setValue(minValue + value * (maxValue - minValue));
    }

    void invertBooleanValue() {
        option.setValue(!((Boolean) option.getValue()).booleanValue());
    }

    public void setInteger(float value) {
        option.setValue((int) (minValue + value * (maxValue - minValue)));
    }

    public void setValue(Object value) {
        option.setValue(value);
    }

    public float getFloat() {
        return option.getFloat();
    }

    public boolean getBoolean() {
        return option.getBoolean();
    }

    public boolean useSlider() {
        return option instanceof FloatOption || option instanceof IntegerOption;
    }

    public int getInteger() {
        return option.getInteger();
    }

    public String getString() {
        return option.getString();
    }

    public Object getValue() {
        return option.getValue();
    }

    public String getOptionName() {
        String string = this.toString();
        StringBuilder stringBuilder = new StringBuilder(string.length());
        char[] chars = string.toCharArray();
        stringBuilder.append(Character.toLowerCase(chars[0]));
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == '_') {
                i++;
                if (i == chars.length) {
                    break;
                }

                stringBuilder.append(chars[i]);
            } else {
                stringBuilder.append(Character.toLowerCase(chars[i]));
            }
        }

        return stringBuilder.toString();
    }
}
package net.bfsr.client.settings;

import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.engine.Engine;
import net.bfsr.engine.settings.BooleanOption;
import net.bfsr.engine.settings.FloatOption;
import net.bfsr.engine.settings.IntegerOption;
import net.bfsr.engine.settings.SettingsOption;
import net.bfsr.engine.settings.StringOption;
import net.bfsr.settings.SettingsCategory;

public enum ClientSettings {
    SOUND_VOLUME(SettingsCategory.SOUND, 0.0f, 1.0f, new FloatOption(0.05f), ConsumerUtils.FLOAT_DEFAULT_CONSUMER),

    CAMERA_MOVE_BY_SCREEN_BORDERS(SettingsCategory.CAMERA, new BooleanOption(true), ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),
    CAMERA_MOVE_BY_SCREEN_BORDERS_SPEED(SettingsCategory.CAMERA, 0.1f, 5.0f, new FloatOption(0.f),
            ConsumerUtils.FLOAT_DEFAULT_CONSUMER),
    CAMERA_MOVE_BY_SCREEN_BORDERS_OFFSET(SettingsCategory.CAMERA, 2.0f, 25.0f, new FloatOption(15.0f),
            ConsumerUtils.FLOAT_DEFAULT_CONSUMER),
    CAMERA_MOVE_BY_KEY_SPEED(SettingsCategory.CAMERA, 0.025f, 4.0f, new FloatOption(1.0f), ConsumerUtils.FLOAT_DEFAULT_CONSUMER),
    CAMERA_ZOOM_SPEED(SettingsCategory.CAMERA, 0.01f, 0.5f, new FloatOption(0.2f), ConsumerUtils.FLOAT_DEFAULT_CONSUMER),
    CAMERA_FOLLOW_PLAYER(SettingsCategory.CAMERA, new BooleanOption(true), ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),

    LANGUAGE(SettingsCategory.LANGUAGE, new StringOption("eng"), (option, value) -> {
        String nextLang = Client.get().getLanguageManager().getNextLang(option.option.getString());
        option.option.setValue(nextLang);
    }),

    V_SYNC(SettingsCategory.GRAPHICS, new BooleanOption(true).addListener(value -> Engine.getRenderer().setVSync(value)),
            ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),
    MAX_FPS(SettingsCategory.GRAPHICS, 10, 240, new IntegerOption(60), ConsumerUtils.INTEGER_DEFAULT_CONSUMER),
    PERSISTENT_MAPPED_BUFFERS(SettingsCategory.GRAPHICS, new BooleanOption(true)
            .addListener(value -> Engine.getRenderer().setPersistentMappedBuffers(value)),
            ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),
    ENTITIES_GPU_FRUSTUM_CULLING(SettingsCategory.GRAPHICS, new BooleanOption(false)
            .addListener(value -> Engine.getRenderer().setEntitiesGPUFrustumCulling(value)),
            ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),
    PARTICLES_GPU_FRUSTUM_CULLING(SettingsCategory.GRAPHICS, new BooleanOption(true)
            .addListener(value -> Engine.getRenderer().setParticlesGPUFrustumCulling(value)),
            ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),

    IS_DEBUG(SettingsCategory.DEBUG, new BooleanOption(false), ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),
    IS_PROFILING(SettingsCategory.DEBUG, new BooleanOption(false)
            .addListener(value -> Client.get().getProfiler().setEnable(value)), ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER),
    SHOW_DEBUG_BOXES(SettingsCategory.DEBUG, new BooleanOption(false)
            .addListener(value -> Client.get().getGlobalRenderer().setDebugBoxesEnabled(value)),
            ConsumerUtils.BOOLEAN_DEFAULT_CONSUMER);

    @Getter
    private final SettingsCategory category;
    @Getter
    private final float minValue;
    @Getter
    private final float maxValue;
    private final SettingsOption<?> option;
    private final ChangeValueConsumer changeValueConsumer;

    ClientSettings(SettingsCategory category, SettingsOption<?> option, ChangeValueConsumer changeValueConsumer) {
        this(category, 0, 0, option, changeValueConsumer);
    }

    ClientSettings(SettingsCategory category, float minValue, float maxValue, SettingsOption<?> option,
                   ChangeValueConsumer changeValueConsumer) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.category = category;
        this.option = option;
        this.changeValueConsumer = changeValueConsumer;
    }

    public void changeValue(Object newValue) {
        changeValueConsumer.change(this, newValue);
    }

    public void changeValue() {
        changeValueConsumer.change(this, null);
    }

    public void setFloat(float value) {
        option.setValue(minValue + value * (maxValue - minValue));
    }

    void invertBooleanValue() {
        option.setValue(!(Boolean) option.getValue());
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

    @FunctionalInterface
    public interface ChangeValueConsumer {
        void change(ClientSettings settings, Object value);
    }
}
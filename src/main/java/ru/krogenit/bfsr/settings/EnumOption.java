package ru.krogenit.bfsr.settings;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum EnumOption {
    soundVolume(float.class, "sound", 0.0f, 1.0f),

    cameraMoveByScreenBorders(boolean.class, "camera", 0, 0),
    cameraMoveByScreenBordersSpeed(float.class, "camera", 1f, 50f),
    cameraMoveByScreenBordersOffset(float.class, "camera", 2f, 25f),

    cameraMoveByKeySpeed(float.class, "camera", 0.25f, 40f),

    cameraZoomSpeed(float.class, "camera", 0.01f, 0.5f),

    cameraFollowPlayer(boolean.class, "camera", 0, 0),

    language(String.class, "lang", 0, 0),

    vSync(boolean.class, "graphics", 0, 0),
    maxFps(int.class, "graphics", 30, 240),

    isDebug(boolean.class, "debug", 0, 0),
    isProfiling(boolean.class, "debug", 0, 0);

    private final String category;
    private final Class<?> type;
    private final float minValue;
    private final float maxValue;

    EnumOption(Class<?> type, String category, float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.category = category;
        this.type = type;

        List<EnumOption> optins = ClientSettings.optionsByCategory.get(category);
        if(optins == null) optins = new ArrayList<>();

        optins.add(this);
        ClientSettings.optionsByCategory.put(category, optins);
    }
}

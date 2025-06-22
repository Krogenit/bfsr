package net.bfsr.client.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SettingsCategory {
    SOUND("sound"), CAMERA("camera"), LANGUAGE("lang"), GRAPHICS("graphics"), DEBUG("debug");

    private final String categoryName;
}

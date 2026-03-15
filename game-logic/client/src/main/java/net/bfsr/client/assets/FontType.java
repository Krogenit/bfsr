package net.bfsr.client.assets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FontType {
    ROBOTO_CONDENSED_REGULAR("roboto-condensed-regular", "roboto-condensed-regular.ttf"),
    JETBRAINSMONO_LIGHT("jetbrainsmono-light", "jetbrainsmono-light.ttf");// for debug info and editor

    private final String fontName;
    private final String fontFile;
}

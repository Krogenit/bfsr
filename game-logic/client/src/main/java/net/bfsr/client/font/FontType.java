package net.bfsr.client.font;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FontType {
    NOTOSANS_REGULAR("NotoSans-Regular", "NotoSans-Regular.ttf"),
    XOLONIUM("Xolonium-Regular", "Xolonium-Regular.ttf"),
    CONSOLA("consola", "consola.ttf"),
    SEGOE_UI("Segoe UI", "Segoe UI.ttf");

    private final String fontName;
    private final String fontFile;
}

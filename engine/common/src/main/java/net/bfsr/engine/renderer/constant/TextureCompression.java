package net.bfsr.engine.renderer.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum TextureCompression {
    COMPRESSED_RGB_S3TC_DXT1(0x83F0),
    COMPRESSED_RGBA_S3TC_DXT1(0x83F1),
    COMPRESSED_RGBA_S3TC_DXT3(0x83F2),
    COMPRESSED_RGBA_S3TC_DXT5(0x83F3);

    private final int gl;
}

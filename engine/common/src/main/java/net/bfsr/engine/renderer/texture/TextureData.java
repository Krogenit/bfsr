package net.bfsr.engine.renderer.texture;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.renderer.constant.TextureFilter;
import net.bfsr.engine.renderer.constant.TextureWrap;

import java.nio.file.Path;

@Getter
@RequiredArgsConstructor
public class TextureData {
    private static final TextureWrap DEFAULT_WRAP = TextureWrap.CLAMP_TO_EDGE;
    private static final TextureFilter DEFAULT_FILTER = TextureFilter.NEAREST;

    private final Path path;
    private final TextureWrap wrap;
    private final TextureFilter filter;

    public TextureData(Path path) {
        this.path = path;
        this.wrap = DEFAULT_WRAP;
        this.filter = DEFAULT_FILTER;
    }
}

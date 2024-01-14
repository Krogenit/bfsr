package net.bfsr.engine.renderer.shader.loader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
class IncludeEntry {
    private final String filename;
    private String content;
}
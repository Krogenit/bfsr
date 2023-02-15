package net.bfsr.client.shader.loader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class IncludeEntry {
    private final String filename;
    private String content;
}

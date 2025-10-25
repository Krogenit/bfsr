package net.bfsr.engine.renderer.font;

import it.unimi.dsi.fastutil.chars.CharList;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FontPackResult {
    private boolean allCharsPacked;
    private final CharList packedCharsList;
    private final CharList unpackedCharsList;
}

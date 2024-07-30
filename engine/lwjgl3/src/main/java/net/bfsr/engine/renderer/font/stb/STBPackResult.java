package net.bfsr.engine.renderer.font.stb;

import it.unimi.dsi.fastutil.chars.CharList;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class STBPackResult {
    private boolean allCharsPacked;
    private final CharList packedCharsList;
    private final CharList unpackedCharsList;
}

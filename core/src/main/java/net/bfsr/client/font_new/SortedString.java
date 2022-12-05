package net.bfsr.client.font_new;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;

import java.util.List;

@Getter
public class SortedString {
    private final TIntObjectMap<List<GlyphAndColor>> glyphAndColorList = new TIntObjectHashMap<>(16);

    public void clear() {
        glyphAndColorList.clear();
    }
}

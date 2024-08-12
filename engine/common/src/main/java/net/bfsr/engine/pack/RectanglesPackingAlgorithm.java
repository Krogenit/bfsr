package net.bfsr.engine.pack;

import net.bfsr.engine.pack.maxrects.Rectangle;

public interface RectanglesPackingAlgorithm {
    Rectangle insert(int width, int height);
}

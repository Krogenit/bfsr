package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.renderer.MiniMapRenderer;
import net.bfsr.engine.gui.component.Rectangle;

public class MiniMap extends Rectangle {
    public MiniMap() {
        super(280, 220);
        setRenderer(new MiniMapRenderer(this));
    }
}
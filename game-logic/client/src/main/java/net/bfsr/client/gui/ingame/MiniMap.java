package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.renderer.MiniMapRenderer;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class MiniMap extends TexturedRectangle {
    public MiniMap() {
        super(TextureRegister.guiHudShip, 280, 220);
        setRenderer(new MiniMapRenderer(this));
    }
}
package net.bfsr.editor;

import net.bfsr.client.Client;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.editor.hud.EditorHUD;
import net.bfsr.engine.profiler.Profiler;
import net.engio.mbassy.listener.Listener;

@Listener
public class EditorGameLogic extends Client {
    public EditorGameLogic(Profiler profiler) {
        super(profiler);
    }

    @Override
    public void init() {
        super.init();
        startSinglePlayer();
    }

    @Override
    public HUD createHUD() {
        return new EditorHUD();
    }
}
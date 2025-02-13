package net.bfsr.editor;

import net.bfsr.client.Client;
import net.bfsr.client.gui.hud.HUD;
import net.bfsr.editor.gui.input.EditorInputController;
import net.bfsr.editor.hud.EditorHUD;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.profiler.Profiler;
import net.engio.mbassy.listener.Listener;

@Listener
public class EditorGameLogic extends Client {
    public EditorGameLogic(Profiler profiler, EventBus eventBus) {
        super(profiler, eventBus);
    }

    @Override
    public void init() {
        super.init();
        startSinglePlayer();
        getInputHandler().addInputController(new EditorInputController());
    }

    @Override
    public HUD createHUD() {
        return hud = new EditorHUD();
    }
}
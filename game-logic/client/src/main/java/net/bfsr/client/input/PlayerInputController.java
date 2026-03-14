package net.bfsr.client.input;

import net.bfsr.client.Client;
import net.bfsr.client.world.entity.PlayerShipManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import org.jbox2d.dynamics.Fixture;
import org.jetbrains.annotations.Nullable;

public abstract class PlayerInputController extends InputController {
    protected final Client client;
    protected final GuiManager guiManager = Engine.getGuiManager();
    protected final AbstractCamera camera = Engine.getRenderer().getCamera();
    protected final AbstractMouse mouse = Engine.getMouse();
    protected final AbstractKeyboard keyboard = Engine.getKeyboard();
    protected final EventBus eventBus;
    protected final PlayerShipManager playerShipManager;

    protected @Nullable Fixture selectedFixture;

    public PlayerInputController(Client client) {
        this.client = client;
        this.eventBus = client.getEventBus();
        this.playerShipManager = client.getPlayerShipManager();
    }
}
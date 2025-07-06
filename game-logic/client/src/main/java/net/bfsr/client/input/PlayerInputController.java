package net.bfsr.client.input;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Client;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.event.gui.SelectSecondaryShipEvent;
import net.bfsr.client.event.gui.SelectShipEvent;
import net.bfsr.client.event.player.ShipControlStartedEvent;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.math.RigidBodyUtils;
import net.bfsr.engine.physics.correction.CorrectionHandler;
import net.bfsr.engine.physics.correction.DynamicCorrectionHandler;
import net.bfsr.engine.physics.correction.HistoryCorrectionHandler;
import net.bfsr.engine.physics.correction.LocalPlayerInputCorrectionHandler;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.network.packet.client.input.PacketPlayerInput;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import static net.bfsr.engine.input.Keys.KEY_A;
import static net.bfsr.engine.input.Keys.KEY_D;
import static net.bfsr.engine.input.Keys.KEY_S;
import static net.bfsr.engine.input.Keys.KEY_W;
import static net.bfsr.engine.input.Keys.KEY_X;

public class PlayerInputController extends InputController {
    private static final int NOT_CONTROLLED_SHIP_ID = -1;

    private final Client client;
    private final GuiManager guiManager = Engine.getGuiManager();
    private final AbstractCamera camera = Engine.getRenderer().getCamera();
    private final AbstractMouse mouse = Engine.getMouse();
    private final AbstractKeyboard keyboard = Engine.getKeyboard();
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final EventBus eventBus;
    private final LocalPlayerInputCorrectionHandler localPlayerInputCorrectionHandler;

    @Setter
    private int controlledShipId = NOT_CONTROLLED_SHIP_ID;
    @Getter
    private Ship ship;

    private @Nullable Fixture selectedFixture;

    public PlayerInputController(Client client) {
        this.client = client;
        this.localPlayerInputCorrectionHandler = new LocalPlayerInputCorrectionHandler();
        this.eventBus = client.getEventBus();
        this.eventBus.register(this);
    }

    @Override
    public void update(int frame) {
        if (guiManager.isActive()) {
            return;
        }

        if (ship == null) {
            if (controlledShipId == NOT_CONTROLLED_SHIP_ID) {
                return;
            }

            RigidBody entity = client.getWorld().getEntityById(controlledShipId);
            if (!(entity instanceof Ship ship)) {
                return;
            }

            setShip(ship);
            eventBus.publish(new SelectShipEvent(ship));
            eventBus.publish(new ShipControlStartedEvent());
        }

        if (ship.isDead()) {
            resetControlledShip();
            return;
        }

        if (!client.isPaused() && ship.isSpawned() && ship.getLifeTime() == 0) {
            controlShip(frame);
        }
    }

    @Override
    public boolean input(int key) {
        if (ship == null) {
            return false;
        }

        Engines engines = ship.getModules().getEngines();
        if (key == KEY_W && engines.isEngineAlive(Direction.FORWARD)) {
            ship.addMoveDirection(Direction.FORWARD);
        }

        if (key == KEY_S && engines.isEngineAlive(Direction.BACKWARD)) {
            ship.addMoveDirection(Direction.BACKWARD);
        }

        if (key == KEY_A && engines.isEngineAlive(Direction.RIGHT)) {
            ship.addMoveDirection(Direction.RIGHT);
        }

        if (key == KEY_D && engines.isEngineAlive(Direction.LEFT)) {
            ship.addMoveDirection(Direction.LEFT);
        }

        if (key == KEY_X && engines.isSomeEngineAlive()) {
            ship.addMoveDirection(Direction.STOP);
        }

        return false;
    }

    @Override
    public void release(int key) {
        if (ship == null) {
            return;
        }

        if (key == KEY_W) {
            ship.removeMoveDirection(Direction.FORWARD);
        }

        if (key == KEY_S) {
            ship.removeMoveDirection(Direction.BACKWARD);
        }

        if (key == KEY_A) {
            ship.removeMoveDirection(Direction.RIGHT);
        }

        if (key == KEY_D) {
            ship.removeMoveDirection(Direction.LEFT);
        }

        if (key == KEY_X) {
            ship.removeMoveDirection(Direction.STOP);
        }
    }

    private void controlShip(int frame) {
        localPlayerInputCorrectionHandler.setRenderDelayInFrames(client.getRenderDelayManager().getRenderDelayInFrames());

        Vector2f cameraPosition = camera.getPosition();
        Vector2f mouseWorldPosition = mouse.getWorldPosition(camera);

        client.sendUDPPacket(new PacketPlayerInput(
                client.getRenderDelayManager().getRenderDelayInFrames(),
                frame,
                mouseWorldPosition.x, mouseWorldPosition.y,
                new boolean[]{mouse.isLeftDown(), mouse.isRightDown()},
                new boolean[]{
                        keyboard.isKeyDown(KEY_W),
                        keyboard.isKeyDown(KEY_A),
                        keyboard.isKeyDown(KEY_S),
                        keyboard.isKeyDown(KEY_D),
                        keyboard.isKeyDown(KEY_X),
                },
                cameraPosition.x, cameraPosition.y
        ));

        Body body = ship.getBody();
        if (!body.isAwake()) {
            body.setAwake(true);
        }

        rigidBodyUtils.rotateToVector(ship, mouseWorldPosition, ship.getModules().getEngines().getAngularVelocity());

        ship.getMoveDirections().forEach(ship::move);

        if (mouse.isLeftDown()) {
            ship.shoot(weaponSlot -> weaponSlot.createBullet(true));
        }
    }

    @Override
    public boolean mouseLeftClick() {
        if (guiManager.isActive()) {
            return false;
        }

        if (ship == null) {
            Fixture fixture = selectFixtureWithMouse();
            if (fixture != null && fixture.getBody().getUserData() instanceof Ship ship) {
                eventBus.publish(new SelectShipEvent(ship));
                return true;
            }

            eventBus.publish(new SelectShipEvent(null));
        }

        return false;
    }

    private @Nullable Fixture selectFixtureWithMouse() {
        World world = client.getWorld();
        if (world == null) {
            return null;
        }

        selectedFixture = null;
        float offset = 0.01f;
        Vector2f mousePosition = mouse.getWorldPosition(camera);

        AABB mouseAABB = new AABB(new Vector2(mousePosition.x - offset, mousePosition.y - offset),
                new Vector2(mousePosition.x + offset, mousePosition.y + offset));

        world.getPhysicWorld().queryAABB(fixture -> {
            if (fixture.testPoint(mousePosition.x, mousePosition.y)) {
                selectedFixture = fixture;
                return false;
            }

            return true;
        }, mouseAABB);

        return selectedFixture;
    }

    @Override
    public boolean mouseRightRelease() {
        Fixture fixture = selectFixtureWithMouse();

        if (fixture != null && fixture.getBody().getUserData() instanceof Ship ship) {
            eventBus.publish(new SelectSecondaryShipEvent(ship));
            return true;
        }

        eventBus.publish(new SelectSecondaryShipEvent(null));
        return false;
    }

    public void setShip(Ship ship) {
        if (this.ship != null) {
            this.ship.setCorrectionHandler(
                    new DynamicCorrectionHandler(0.0f, Engine.convertToDeltaTime(0.2f), new CorrectionHandler(),
                            new HistoryCorrectionHandler()));
            this.ship.setControlledByPlayer(false);
        }

        this.ship = ship;

        if (ship != null) {
            ship.setCorrectionHandler(new DynamicCorrectionHandler(0.0f, Engine.convertToDeltaTime(0.2f), localPlayerInputCorrectionHandler,
                    localPlayerInputCorrectionHandler));
            ship.setControlledByPlayer(true);
        }
    }

    public boolean isControllingShip() {
        return ship != null;
    }

    public void resetControlledShip() {
        setShip(null);
        controlledShipId = NOT_CONTROLLED_SHIP_ID;
        localPlayerInputCorrectionHandler.clear();
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> resetControlledShip();
    }
}
package net.bfsr.client.input;

import net.bfsr.client.Client;
import net.bfsr.client.event.gui.SelectShipEvent;
import net.bfsr.client.event.player.SetPlayerShipEvent;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.math.Direction;
import net.bfsr.engine.math.RigidBodyUtils;
import net.bfsr.engine.physics.correction.CorrectionHandler;
import net.bfsr.engine.physics.correction.DynamicCorrectionHandler;
import net.bfsr.engine.physics.correction.HistoryCorrectionHandler;
import net.bfsr.engine.physics.correction.LocalPlayerInputCorrectionHandler;
import net.bfsr.engine.world.World;
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

public class SessionPlayerInputController extends PlayerInputController {
    private final RigidBodyUtils rigidBodyUtils = new RigidBodyUtils();
    private final LocalPlayerInputCorrectionHandler localPlayerInputCorrectionHandler = new LocalPlayerInputCorrectionHandler();

    public SessionPlayerInputController(Client client) {
        super(client);
    }

    @Override
    public void update(int frame) {
        if (guiManager.isActive()) {
            return;
        }

        Ship ship = playerShipManager.getShip();
        if (ship == null) {
            return;
        }

        localPlayerInputCorrectionHandler.setRenderDelayInFrames(client.getRenderDelayManager().getRenderDelayInFrames());

        if (!client.isPaused() && ship.isSpawned() && ship.getLifeTime() == 0) {
            controlShip(ship, frame);
        }
    }

    @Override
    public boolean input(int key) {
        Ship ship = playerShipManager.getShip();
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

        return true;
    }

    @Override
    public void release(int key) {
        Ship ship = playerShipManager.getShip();
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

    private void controlShip(Ship ship, int frame) {
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
                }
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
            eventBus.publish(new SelectShipEvent(ship));
            return true;
        }

        eventBus.publish(new SelectShipEvent(null));
        return false;
    }

    @EventHandler
    public EventListener<SetPlayerShipEvent> event() {
        return event -> {
            Ship oldShip = event.getOldShip();
            Ship ship = event.getShip();

            if (oldShip != null) {
                oldShip.setCorrectionHandler(new DynamicCorrectionHandler(0.0f, Engine.convertToDeltaTime(0.2f),
                        new CorrectionHandler(), new HistoryCorrectionHandler()));
                oldShip.setControlledByPlayer(false);
            }

            if (ship != null) {
                localPlayerInputCorrectionHandler.clear();
                ship.setCorrectionHandler(new DynamicCorrectionHandler(0.0f, Engine.convertToDeltaTime(0.2f),
                        localPlayerInputCorrectionHandler, localPlayerInputCorrectionHandler));
                ship.setControlledByPlayer(true);
            }
        };
    }
}

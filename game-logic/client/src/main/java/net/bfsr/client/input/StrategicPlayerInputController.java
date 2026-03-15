package net.bfsr.client.input;

import net.bfsr.client.Client;
import net.bfsr.client.event.gui.SelectShipEvent;
import net.bfsr.client.gui.GuiJump;
import net.bfsr.engine.world.World;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.client.input.PacketMoveToPoint;
import net.bfsr.network.packet.client.input.PacketToggleWeapon;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import static net.bfsr.engine.input.Keys.KEY_J;
import static net.bfsr.engine.input.Keys.KEY_LEFT_CONTROL;
import static net.bfsr.engine.input.Keys.KEY_Q;

public class StrategicPlayerInputController extends PlayerInputController {
    public StrategicPlayerInputController(Client client) {
        super(client);
    }

    @Override
    public boolean input(int key) {
        Ship ship = playerShipManager.getShip();
        if (ship == null) {
            return false;
        }

        if (keyboard.isKeyDown(KEY_LEFT_CONTROL)) {
            if (key == KEY_J) {
                guiManager.openGui(new GuiJump());
            }
        } else {
            if (key == KEY_Q) {
                client.sendTCPPacket(new PacketToggleWeapon());
            }
        }

        return false;
    }

    @Override
    public boolean mouseLeftClick() {
        if (guiManager.isActive()) {
            return false;
        }

        Ship ship = playerShipManager.getShip();
        if (ship == null) {
            Fixture fixture = selectFixtureWithMouse();
            if (fixture != null && fixture.getBody().getUserData() instanceof Ship selectedShip) {
                eventBus.publish(new SelectShipEvent(selectedShip));
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
    public boolean mouseLeftRelease() {
        Fixture fixture = selectFixtureWithMouse();

        if (fixture != null && fixture.getBody().getUserData() instanceof Ship ship) {
            eventBus.publish(new SelectShipEvent(ship));
            return true;
        }

        eventBus.publish(new SelectShipEvent(null));
        return false;
    }

    public boolean mouseRightRelease() {
        Ship ship = playerShipManager.getShip();
        if (ship == null) {
            return false;
        }

        Vector2f mousePosition = mouse.getWorldPosition(camera);
        client.sendTCPPacket(new PacketMoveToPoint(mousePosition.x, mousePosition.y));
        return true;
    }
}

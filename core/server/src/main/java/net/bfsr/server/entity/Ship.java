package net.bfsr.server.entity;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.component.weapon.WeaponSlotCommon;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.faction.Faction;
import net.bfsr.math.Direction;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import net.bfsr.server.MainServer;
import net.bfsr.server.ai.Ai;
import net.bfsr.server.ai.AiAggressiveType;
import net.bfsr.server.ai.task.AiAttackTarget;
import net.bfsr.server.ai.task.AiSearchTarget;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.packet.common.PacketObjectPosition;
import net.bfsr.server.network.packet.common.PacketShipEngine;
import net.bfsr.server.network.packet.server.*;
import net.bfsr.server.player.PlayerServer;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.util.Random;

public abstract class Ship extends ShipCommon {
    @Getter
    @Setter
    private PlayerServer owner;
    @Getter
    private final Ai ai;
    @Getter
    private Direction lastMoveDir = Direction.STOP;

    protected Ship(WorldServer world, float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, boolean spawned) {
        super(world, world.getNextId(), x, y, rotation, scaleX, scaleY, r, g, b, spawned);
        RotationHelper.angleToVelocity(this.rotation + MathUtils.PI, -jumpSpeed * 6.0f, jumpVelocity);
        this.jumpPosition.set(jumpVelocity.x / 60.0f * (64.0f + scale.x * 0.1f) * -0.5f + x, jumpVelocity.y / 60.0f * (64.0f + scale.y * 0.1f) * -0.5f + y);
        this.effectsColor.set(r, g, b);
        setRotation(this.rotation);
        if (spawned) setSpawned();
        this.ai = new Ai(this);
        this.ai.setAggressiveType(AiAggressiveType.ATTACK);
        this.ai.addTask(new AiSearchTarget(this, 4000.0f));
        this.ai.addTask(new AiAttackTarget(this, 4000.0f));
        world.addShip(this);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketSpawnShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Override
    public void addWeaponToSlot(int i, WeaponSlotCommon slot) {
        super.addWeaponToSlot(i, slot);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipSetWeaponSlot(this, slot), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Override
    protected void updateShip() {
        super.updateShip();

        WorldServer world = (WorldServer) this.world;
        PlayerServer player = world.getPlayer(name);
        if (controlledByPlayer) {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearbyExcept(new PacketObjectPosition(this), position, WorldServer.PACKET_SPAWN_DISTANCE, player);
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipInfo(this), position, WorldServer.PACKET_UPDATE_DISTANCE);
        } else {
            if (destroyingTimer == 0 && ai != null) ai.update();

            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketObjectPosition(this), position, WorldServer.PACKET_SPAWN_DISTANCE);
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipInfo(this), position, WorldServer.PACKET_UPDATE_DISTANCE);
        }

        if (destroyingTimer > 0) {
            sparksTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (sparksTimer <= 0) {
                createSpark();
                sparksTimer = 25;
            }

            destroyingTimer -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
            if (destroyingTimer <= 0) {
                destroyShip();
            }
        }
    }

    @Override
    protected void onMove(Direction direction) {
        if (lastMoveDir != direction)
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipEngine(id, direction.ordinal()), getPosition(), WorldServer.PACKET_UPDATE_DISTANCE);
        lastMoveDir = direction;
    }

    @Override
    protected void onStopMove(Direction direction) {
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipEngine(id, direction.ordinal()), getPosition(), WorldServer.PACKET_UPDATE_DISTANCE);
    }

    @Override
    protected void onHullDamageByCollision(Contact contact, Vector2 normal) {
        if (hull.getHull() <= 0) {
            setDestroying();
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketDestroingShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    @Override
    protected void onHullDamage() {
        if (hull.getHull() <= 0) {
            setDestroying();
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketDestroingShip(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }
    }

    @Override
    protected void createSpark() {
        Random rand = world.getRand();
        Vector2f position = getPosition();
        Vector2f velocity = getVelocity();
        WreckSpawner.spawnDamageDebris((WorldServer) world, 1, position.x - scale.x / 2.5f + rand.nextInt((int) (scale.x / 1.25f)),
                position.y - scale.y / 2.5f + rand.nextInt((int) (scale.y / 1.25f)), velocity.x * 0.1f, velocity.y * 0.1f, 1.0f);
    }

    @Override
    public void destroyShip() {
        super.destroyShip();
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        setDead(true);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipName(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Override
    public void setFaction(Faction faction) {
        super.setFaction(faction);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShipFaction(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    public boolean isBot() {
        return owner == null;
    }
}

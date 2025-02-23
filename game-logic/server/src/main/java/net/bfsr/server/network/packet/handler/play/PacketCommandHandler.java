package net.bfsr.server.network.packet.handler.play;

import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.ai.Ai;
import net.bfsr.command.Command;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.entity.ship.module.hull.HullCell;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketCommand;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.physics.CollisionHandler;
import net.bfsr.world.World;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public class PacketCommandHandler extends PacketHandler<PacketCommand, PlayerNetworkHandler> {
    private final Command[] commands = Command.values();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final ServerGameLogic serverGameLogic = ServerGameLogic.get();
    private final ShipFactory shipFactory = serverGameLogic.getShipFactory();
    private final ShipOutfitter shipOutfitter = serverGameLogic.getShipOutfitter();
    private final ShipRegistry shipRegistry = serverGameLogic.getConfigConverterManager().getConverter(ShipRegistry.class);
    private final AiFactory aiFactory = serverGameLogic.getAiFactory();
    private final CollisionHandler collisionHandler = serverGameLogic.getCollisionHandler();
    private final EntityTrackingManager trackingManager = serverGameLogic.getEntityTrackingManager();

    @Override
    public void handle(PacketCommand packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        Command cmd = commands[packet.getCommand()];
        World world = playerNetworkHandler.getWorld();
        if (cmd == Command.SPAWN_SHIP) {
            String[] args = packet.getArgs();
            int shipId = Integer.parseInt(args[0]);
            float x = Float.parseFloat(args[1]);
            float y = Float.parseFloat(args[2]);

            Faction faction = Faction.get((byte) random.nextInt(Faction.values().length));
            Ai ai = Ai.NO_AI;

            Ship ship = shipFactory.createBot(world, x, y, random.nextFloat() * MathUtils.TWO_PI, faction, shipRegistry.get(shipId), ai);
            world.add(ship, false);
            ship.setSpawned();
        } else if (cmd == Command.DESTROY_SHIP) {
            Ship ship = getShipByArgumentId(world, packet.getArgs()[0]);
            if (ship != null) {
                ship.setDead();
            }
        } else if (cmd == Command.REMOVE_SHIELD) {
            Ship ship = getShipByArgumentId(world, packet.getArgs()[0]);
            if (ship != null) {
                ship.getModules().removeShield();
            }
        } else if (cmd == Command.DISABLE_SHIELD) {
            Ship ship = getShipByArgumentId(world, packet.getArgs()[0]);
            if (ship != null) {
                ship.getModules().disableShield();
            }
        } else if (cmd == Command.ADD_SHIELD) {
            Ship ship = getShipByArgumentId(world, packet.getArgs()[0]);
            if (ship != null) {
                Shield shield = ship.getModules().getShield();
                if (shield == null) {
                    shipOutfitter.addShieldToShip(ship);
                }
            }
        } else if (cmd == Command.ADD_AI) {
            Ship ship = getShipByArgumentId(world, packet.getArgs()[0]);
            if (ship != null) {
                ship.setAi(aiFactory.createAi());
            }
        } else if (cmd == Command.REMOVE_AI) {
            Ship ship = getShipByArgumentId(world, packet.getArgs()[0]);
            if (ship != null) {
                ship.setAi(Ai.NO_AI);
            }
        } else if (cmd == Command.DESTROY_ONE_HULL_CELL) {
            Ship ship = getShipByArgumentId(world, packet.getArgs()[0]);
            if (ship != null) {
                HullCell[][] cells = ship.getModules().getHull().getCells();
                int lengthX = cells.length;
                int lengthY = cells[0].length;
                int count = lengthX * lengthY;
                int x = 0;
                int y = 0;
                int direction = 0;
                int maxX = lengthX - 1;
                int maxY = lengthY - 1;
                int minX = 0;
                int minY = 1;
                float sin = ship.getSin();
                float cos = ship.getCos();

                for (int i = 0; i < count; i++) {
                    HullCell cell = cells[x][y];
                    if (cell.getValue() > 0.0f) {
                        float sizeX = ship.getSizeX();
                        float sizeY = ship.getSizeY();
                        float halfSizeX = sizeX * 0.5f;
                        float halfSizeY = sizeY * 0.5f;
                        float rhombusWidth = sizeX / lengthX;
                        float rhombusHeight = sizeY / lengthY;
                        float halfRhombusWidth = rhombusWidth * 0.5f;
                        float halfRhombusHeight = rhombusHeight * 0.5f;
                        float posX = cell.getColumn() * rhombusWidth - halfSizeX + halfRhombusWidth;
                        float posY = cell.getRow() * rhombusHeight - halfSizeY + halfRhombusHeight;
                        float rotatedX = posX * cos - posY * sin + ship.getX();
                        float rotatedY = posY * cos + posX * sin + ship.getY();

                        collisionHandler.damageHullCell(cell, Float.MAX_VALUE, ship, cells, rotatedX, rotatedY);
                        return;
                    }

                    if (direction == 0) {
                        x++;

                        if (x == maxX) {
                            direction++;
                            maxX--;
                        }
                    } else if (direction == 1) {
                        y++;

                        if (y == maxY) {
                            direction++;
                            maxY--;
                        }
                    } else if (direction == 2) {
                        x--;

                        if (x == minX) {
                            direction++;
                            minX++;
                        }
                    } else {
                        y--;

                        if (y == minY) {
                            direction = 0;
                            minY++;
                        }
                    }
                }
            }
        }
    }

    private @Nullable Ship getShipByArgumentId(World world, String id) {
        int shipId = Integer.parseInt(id);
        RigidBody rigidBody = world.getEntityById(shipId);
        if (rigidBody instanceof Ship ship) {
            return ship;
        }

        return null;
    }
}
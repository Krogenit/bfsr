package net.bfsr.server.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.bfsr.engine.collection.UnorderedArrayList;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.event.entity.RigidBodyRemovedFromWorldEvent;
import net.bfsr.engine.network.packet.Packet;
import net.bfsr.engine.network.packet.common.world.entity.PacketEntityRemove;
import net.bfsr.engine.network.packet.common.world.entity.PacketEntitySpawn;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.server.event.PlayerDisconnectEvent;
import net.bfsr.server.event.PlayerJoinGameEvent;
import net.bfsr.server.network.EntitySyncManager;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.player.Player;
import org.joml.Vector2f;

import java.util.List;

public class EntityTrackingManager {
    private static final float TRACKING_DISTANCE = 600;
    private static final float TRACKING_DISTANCE_SQ = TRACKING_DISTANCE * TRACKING_DISTANCE;

    private final UnorderedArrayList<Player> players = new UnorderedArrayList<>();
    private final Object2ObjectOpenHashMap<Player, IntOpenHashSet> playerEntitiesInRangeMap = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<ObjectOpenHashSet<Player>> entityTrackingByPlayersMap = new Int2ObjectOpenHashMap<>();

    private final NetworkSystem network;
    private final EntitySyncManager entitySyncManager;
    private final EventBus eventBus;

    public EntityTrackingManager(EventBus eventBus, NetworkSystem network) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.network = network;
        this.entitySyncManager = new EntitySyncManager(network);
    }

    private void addPlayer(Player player) {
        players.add(player);
        playerEntitiesInRangeMap.put(player, new IntOpenHashSet(128));
    }

    private void removePlayer(Player player) {
        players.remove(player);
        playerEntitiesInRangeMap.remove(player);
        entityTrackingByPlayersMap.values().forEach(players1 -> players1.remove(player));
    }

    public void update(int tick, List<? extends RigidBody> entities) {
        playerEntitiesInRangeMap.object2ObjectEntrySet().fastForEach(entry -> {
            Player player = entry.getKey();
            Vector2f position = player.getPosition();
            IntOpenHashSet entitiesInRange = entry.getValue();

            for (int i = 0, size = entities.size(); i < size; i++) {
                RigidBody rigidBody = entities.get(i);
                int id = rigidBody.getId();
                float dx = rigidBody.getX() - position.x;
                float dy = rigidBody.getY() - position.y;
                float distSq = dx * dx + dy * dy;

                if (entitiesInRange.contains(id)) {
                    if (distSq > TRACKING_DISTANCE_SQ) {
                        entitiesInRange.remove(id);
                        entityTrackingByPlayersMap.get(id).remove(player);
                        network.sendTCPPacketTo(new PacketEntityRemove(id, tick), player);
                    } else {
                        entitySyncManager.addToSyncQueue(rigidBody, tick, player);
                    }
                } else {
                    if (!rigidBody.isDead() && player.canTrackEntity(rigidBody) && distSq <= TRACKING_DISTANCE_SQ) {
                        entitiesInRange.add(id);
                        ObjectOpenHashSet<Player> playersSet = entityTrackingByPlayersMap.computeIfAbsent(id,
                                key -> new ObjectOpenHashSet<>(16));
                        playersSet.add(player);
                        network.sendTCPPacketTo(new PacketEntitySpawn(rigidBody, tick), player);
                    }
                }
            }

            entitySyncManager.flush(player, tick);
        });
    }

    @EventHandler
    public EventListener<RigidBodyRemovedFromWorldEvent> rigidBodyDeathEvent() {
        return event -> {
            RigidBody rigidBody = event.rigidBody();
            sendPacketToPlayersTrackingEntity(rigidBody.getId(), new PacketEntityRemove(rigidBody.getId(),
                    event.tick()));
            entityTrackingByPlayersMap.remove(rigidBody.getId());
            playerEntitiesInRangeMap.values().forEach(set -> set.remove(rigidBody.getId()));
        };
    }

    @EventHandler
    public EventListener<PlayerJoinGameEvent> playerJoinGameEvent() {
        return event -> addPlayer(event.getPlayer());
    }

    @EventHandler
    public EventListener<PlayerDisconnectEvent> playerDisconnectEvent() {
        return event -> removePlayer(event.getPlayer());
    }

    public void sendPacketToPlayersTrackingEntity(int id, Packet packet) {
        ObjectOpenHashSet<Player> players = entityTrackingByPlayersMap.get(id);
        if (players != null) {
            players.forEach(player -> network.sendUDPPacketTo(packet, player));
        }
    }

    public void sendPacketToPlayersTrackingEntityExcept(int id, Packet packet, Player except) {
        ObjectOpenHashSet<Player> players = entityTrackingByPlayersMap.get(id);
        if (players != null) {
            players.forEach(player -> {
                if (player != except) {
                    network.sendUDPPacketTo(packet, player);
                }
            });
        }
    }

    public void clear() {
        eventBus.unregister(this);
        players.clear();
        playerEntitiesInRangeMap.clear();
        entityTrackingByPlayersMap.clear();
        entitySyncManager.clear();
    }
}
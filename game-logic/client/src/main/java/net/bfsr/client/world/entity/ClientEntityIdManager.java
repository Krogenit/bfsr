package net.bfsr.client.world.entity;

import gnu.trove.map.TIntObjectMap;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.sync.IntegerTickSync;
import net.bfsr.engine.world.entity.EntityIdManager;
import net.bfsr.engine.world.entity.RigidBody;

import java.util.ArrayList;
import java.util.List;

@Log4j2
//TODO: entities
public class ClientEntityIdManager extends EntityIdManager {
    private final Client client = Client.get();

    private final double clientRenderDelayInNanos;
    private final int clientRenderDelayInTicks;

    private final List<RigidBody> entities = new ArrayList<>();

    private final IntegerTickSync idSync = new IntegerTickSync(NetworkHandler.GLOBAL_HISTORY_LENGTH_MILLIS);

    public ClientEntityIdManager(double clientRenderDelayInNanos, int clientRenderDelayInTicks) {
        super(-1);
        this.clientRenderDelayInNanos = clientRenderDelayInNanos;
        this.clientRenderDelayInTicks = clientRenderDelayInTicks;
    }

    @Override
    public void add(RigidBody rigidBody) {
        if (rigidBody.getId() < 0) {
//            entities.add(rigidBody);
        }
    }

    @Override
    public void remove(int index, RigidBody rigidBody) {
//        entities.remove(rigidBody);
    }

    public void addRemoteData(int localId, int tick) {
        idSync.addRemoteData(localId, tick);
    }

    public void addLocalData(int tick) {
        idSync.addLocalData(id, tick);
    }

    @Override
    public void update(double timestamp, int tick) {
        int idCorrection = idSync.correction();
        if (idCorrection != 0) {
            log.info("Correction local ids with value {}, new value {}", idCorrection, id + idCorrection);
        }

        id += idCorrection;
//        idSync.addLocalData(id, tick + clientRenderDelayInTicks);

//        if (idCorrection > 0) {
//            correctEntityIds(idCorrection);
//        }
    }

    private void correctEntityIds(int delta) {
        TIntObjectMap<RigidBody> entitiesById = client.getWorld().getEntityManager().getEntitiesById();

        for (int i = 0; i < entities.size(); i++) {
            RigidBody rigidBody = entities.get(i);
            entitiesById.remove(rigidBody.getId());
            rigidBody.setId(rigidBody.getId() + delta);
            entitiesById.put(rigidBody.getId(), rigidBody);
        }
    }

    public void onClientToServerTimeDiffChange() {
        idSync.clear();
    }

    @Override
    public int getNextId() {
        return id--;
    }

    @Override
    public void clear() {
        super.clear();
        entities.clear();
        idSync.clear();
    }
}
package net.bfsr.client.world.entity;

import gnu.trove.map.TIntObjectMap;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.sync.IntegerSync;
import net.bfsr.engine.world.entity.EntityIdManager;
import net.bfsr.engine.world.entity.RigidBody;

import java.util.ArrayList;
import java.util.List;

@Log4j2
//TODO: entities
public class ClientEntityIdManager extends EntityIdManager {
    private final List<RigidBody> entities = new ArrayList<>();

    private final IntegerSync idSync = new IntegerSync(NetworkHandler.GLOBAL_HISTORY_LENGTH_MILLIS);

    public ClientEntityIdManager() {
        super(-1);
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

    public void addRemoteData(int localId, int frame) {
        idSync.addRemoteData(localId, frame);
    }

    @Override
    public void update(double timestamp, int frame) {
        int idCorrection = idSync.correction();
        if (idCorrection != 0) {
            log.info("Correction local ids with value {}, new value {}", idCorrection, id + idCorrection);
        }

        id += idCorrection;
        idSync.addLocalData(id, frame + Client.get().getClientRenderDelayInFrames());

//        if (idCorrection > 0) {
//            correctEntityIds(idCorrection);
//        }
    }

    private void correctEntityIds(int delta) {
        TIntObjectMap<RigidBody> entitiesById = Client.get().getWorld().getEntityManager().getEntitiesById();

        for (int i = 0; i < entities.size(); i++) {
            RigidBody rigidBody = entities.get(i);
            entitiesById.remove(rigidBody.getId());
            rigidBody.setId(rigidBody.getId() + delta);
            entitiesById.put(rigidBody.getId(), rigidBody);
        }
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
package net.bfsr.client.world.entity;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.sync.IntegerSync;
import net.bfsr.engine.world.entity.EntityIdManager;

@Log4j2
public class ClientEntityIdManager extends EntityIdManager {
    private final Client client;

    private final IntegerSync idSync = new IntegerSync(NetworkHandler.GLOBAL_HISTORY_LENGTH_MILLIS);

    public ClientEntityIdManager(Client client) {
        super(-1);
        this.client = client;
    }

    public void addRemoteData(int localId, int frame) {
        idSync.addRemoteData(localId, frame);
    }

    @Override
    public void update(int frame) {
        int idCorrection = idSync.correction();
        if (idCorrection != 0) {
            log.info("Correction local ids with value {}, new value {}", idCorrection, id + idCorrection);
        }

        id += idCorrection;
        idSync.addLocalData(id, frame + client.getRenderDelayManager().getRenderDelayInFrames());
    }

    @Override
    public int getNextId() {
        return id--;
    }

    @Override
    public void clear() {
        super.clear();
        idSync.clear();
    }
}
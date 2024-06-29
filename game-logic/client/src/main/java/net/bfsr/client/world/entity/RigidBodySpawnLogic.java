package net.bfsr.client.world.entity;

import net.bfsr.client.Core;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.ConfigToDataConverter;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.common.entity.spawn.RigidBodySpawnData;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.MassType;

import java.util.List;

public class RigidBodySpawnLogic implements EntitySpawnLogic<RigidBodySpawnData> {
    @Override
    public void spawn(RigidBodySpawnData spawnData) {
        World world = Core.get().getWorld();
        ConfigToDataConverter<?, ?> converter = ConfigConverterManager.INSTANCE.getConverter(spawnData.getRegistryId());
        GameObjectConfigData configData = (GameObjectConfigData) converter.get(spawnData.getDataId());
        RigidBody rigidBody = new RigidBody(spawnData.getPosX(), spawnData.getPosY(), spawnData.getSin(),
                spawnData.getCos(), configData.getSizeX(), configData.getSizeY(), configData, spawnData.getRegistryId());
        rigidBody.init(world, spawnData.getEntityId());

        List<Convex> convexList = configData.getConvexList();
        Body body = rigidBody.getBody();
        for (int i = 0; i < convexList.size(); i++) {
            Convex convex = convexList.get(i);
            body.addFixture(rigidBody.setupFixture(new BodyFixture(convex)));
        }

        body.setMass(MassType.NORMAL);
        body.setUserData(rigidBody);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
        world.add(rigidBody);
    }
}
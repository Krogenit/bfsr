package net.bfsr.client.world.entity;

import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.ConfigToDataConverter;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.common.entity.spawn.RigidBodySpawnData;
import net.bfsr.world.World;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

import java.util.ArrayList;
import java.util.List;

public class RigidBodySpawnLogic implements EntitySpawnLogic<RigidBodySpawnData> {
    @Override
    public void spawn(RigidBodySpawnData spawnData, World world, ConfigConverterManager configConverterManager, AbstractRenderer renderer) {
        ConfigToDataConverter<?, GameObjectConfigData> converter =
                (ConfigToDataConverter<?, GameObjectConfigData>) configConverterManager.getConverter(spawnData.getRegistryId());
        GameObjectConfigData configData = converter.get(spawnData.getDataId());
        RigidBody rigidBody = new RigidBody(spawnData.getPosX(), spawnData.getPosY(), spawnData.getSin(),
                spawnData.getCos(), configData.getSizeX(), configData.getSizeY(), configData);
        rigidBody.init(world, spawnData.getEntityId());

        List<Shape> shapeList = configData.getShapeList();
        List<Fixture> fixtures = new ArrayList<>(shapeList.size());

        for (int i = 0; i < shapeList.size(); i++) {
            fixtures.add(rigidBody.setupFixture(new Fixture(shapeList.get(i))));
        }

        Body body = rigidBody.getBody();
        body.setFixtures(fixtures);
        body.setUserData(rigidBody);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.005f);
        world.add(rigidBody);
    }
}
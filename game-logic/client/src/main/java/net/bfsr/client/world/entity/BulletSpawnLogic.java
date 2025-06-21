package net.bfsr.client.world.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.ConfigConverterManager;
import net.bfsr.engine.physics.correction.CorrectionHandler;
import net.bfsr.engine.physics.correction.DynamicCorrectionHandler;
import net.bfsr.engine.physics.correction.HistoryCorrectionHandler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.world.World;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.common.entity.spawn.BulletSpawnData;

@Log4j2
@RequiredArgsConstructor
public class BulletSpawnLogic implements EntitySpawnLogic<BulletSpawnData> {
    private final GunRegistry gunRegistry;

    @Override
    public void spawn(BulletSpawnData spawnData, World world, ConfigConverterManager configConverterManager, AbstractRenderer renderer) {
        int clientId = spawnData.getClientId();
        Ship ship = Client.get().getPlayerInputController().getShip();
        if (clientId < 0 && ship != null && spawnData.getOwnerId() == ship.getId()) {
            Bullet bullet = world.getEntityById(clientId);
            if (bullet != null) {
                bullet.setCorrectionHandler(new DynamicCorrectionHandler(0.1f, Engine.convertToDeltaTime(2.0f), new CorrectionHandler(),
                        new HistoryCorrectionHandler()));
                bullet.setId(spawnData.getEntityId());
                return;
            } else {
                log.warn("Bullet with id {} not found in client world", clientId);
            }
        }

        spawnBullet(spawnData, world);
    }

    private void spawnBullet(BulletSpawnData spawnData, World world) {
        GunData gunData = gunRegistry.get(spawnData.getDataId());
        Bullet bullet = new Bullet(spawnData.getPosX(), spawnData.getPosY(), spawnData.getSin(), spawnData.getCos(), gunData,
                world.getEntityById(spawnData.getOwnerId()), gunData.getDamage());
        bullet.init(world, spawnData.getEntityId());
        bullet.setCorrectionHandler(new DynamicCorrectionHandler(0.0f, Engine.convertToDeltaTime(4.0f), new CorrectionHandler(),
                new HistoryCorrectionHandler()));
        world.add(bullet);
    }
}
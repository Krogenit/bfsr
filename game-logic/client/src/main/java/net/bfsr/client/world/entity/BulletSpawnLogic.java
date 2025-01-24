package net.bfsr.client.world.entity;

import lombok.RequiredArgsConstructor;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.network.packet.common.entity.spawn.BulletSpawnData;
import net.bfsr.world.World;

@RequiredArgsConstructor
public class BulletSpawnLogic implements EntitySpawnLogic<BulletSpawnData> {
    private final GunRegistry gunRegistry;

    @Override
    public void spawn(BulletSpawnData spawnData, World world, ConfigConverterManager configConverterManager, AbstractRenderer renderer) {
        GunData gunData = gunRegistry.get(spawnData.getDataId());
        Bullet bullet = new Bullet(spawnData.getPosX(), spawnData.getPosY(), spawnData.getSin(), spawnData.getCos(), gunData,
                world.getEntityById(spawnData.getOwnerId()), gunData.getDamage());
        bullet.init(world, spawnData.getEntityId());
        world.add(bullet);
    }
}
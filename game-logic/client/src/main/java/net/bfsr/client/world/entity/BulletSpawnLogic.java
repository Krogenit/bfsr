package net.bfsr.client.world.entity;

import net.bfsr.client.Core;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.network.packet.common.entity.spawn.BulletSpawnData;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.world.World;

public class BulletSpawnLogic implements EntitySpawnLogic {
    @Override
    public void spawn(EntityPacketSpawnData spawnData) {
        World world = Core.get().getWorld();
        BulletSpawnData bulletSpawnData = (BulletSpawnData) spawnData;
        GunData gunData = GunRegistry.INSTANCE.get(bulletSpawnData.getDataId());
        Bullet bullet = new Bullet(spawnData.getPosX(), spawnData.getPosY(), spawnData.getSin(), spawnData.getCos(), gunData,
                null, gunData.getDamage());
        bullet.init(world, spawnData.getEntityId());
        world.add(bullet);
    }
}
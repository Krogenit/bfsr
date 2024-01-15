package net.bfsr.client.world.entity;

import net.bfsr.client.Core;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.network.packet.common.entity.spawn.BulletSpawnData;
import net.bfsr.world.World;

public class BulletSpawnLogic implements EntitySpawnLogic<BulletSpawnData> {
    @Override
    public void spawn(BulletSpawnData spawnData) {
        World world = Core.get().getWorld();
        GunData gunData = GunRegistry.INSTANCE.get(spawnData.getDataId());
        Bullet bullet = new Bullet(spawnData.getPosX(), spawnData.getPosY(), spawnData.getSin(), spawnData.getCos(), gunData,
                world.getEntityById(spawnData.getOwnerId()), gunData.getDamage());
        bullet.init(world, spawnData.getEntityId());
        bullet.setOnAddedToWorldConsumer((bullet1) -> Core.get().getRenderManager().createRender(bullet1));
        world.add(bullet);
    }
}
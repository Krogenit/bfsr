package net.bfsr.network.packet.common.entity.spawn.connectedobject;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

@Getter
public class WeaponSlotConnectObjectSpawnData extends ConnectedObjectSpawnData {
    protected int id;
    protected Vector2f localPosition;

    @Override
    public ConnectedObject<?> create(ConfigData configData) {
        WeaponSlot weaponSlot = new WeaponSlot((GunData) configData);
        weaponSlot.setId(id);
        weaponSlot.setLocalPosition(localPosition);
        return weaponSlot;
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        id = data.readInt();
        ByteBufUtils.readVector(data, localPosition = new Vector2f());
    }
}

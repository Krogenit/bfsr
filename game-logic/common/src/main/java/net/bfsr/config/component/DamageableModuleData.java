package net.bfsr.config.component;

import lombok.Getter;
import net.bfsr.config.ConfigData;

@Getter
public class DamageableModuleData extends ConfigData {
    private final float hp;

    public DamageableModuleData(DamageableModuleConfig config, String fileName, int id, int registryId) {
        super(fileName, id, registryId);
        this.hp = config.getHp();
    }
}
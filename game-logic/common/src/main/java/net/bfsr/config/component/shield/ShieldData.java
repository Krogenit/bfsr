package net.bfsr.config.component.shield;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.engine.Engine;

@Getter
public class ShieldData extends ConfigData {
    private final float maxShield;
    private final float regenAmount;
    private final int rebuildTimeInTicks;

    ShieldData(ShieldConfig config, String fileName, int id, int registryId) {
        super(fileName, id, registryId);
        this.maxShield = config.maxShield();
        this.regenAmount = Engine.convertToDeltaTime(config.regenInSeconds());
        this.rebuildTimeInTicks = Engine.convertToTicks(config.rebuildTimeInSeconds());
    }
}
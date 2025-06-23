package net.bfsr.config.component.shield;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.ConfigData;

@Getter
public class ShieldData extends ConfigData {
    private final float maxShield;
    private final float regenAmount;
    private final int rebuildTimeInTicks;

    ShieldData(ShieldConfig config, String fileName, int id, int registryId) {
        super(fileName, id, registryId);
        this.maxShield = config.maxShield();
        this.regenAmount = Engine.convertToDeltaTime(config.regenInSeconds());
        this.rebuildTimeInTicks = Engine.convertSecondsToTicks(config.rebuildTimeInSeconds());
    }
}
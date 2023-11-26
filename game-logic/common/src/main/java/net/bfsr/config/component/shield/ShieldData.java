package net.bfsr.config.component.shield;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.engine.util.TimeUtils;

import java.nio.file.Path;

@Getter
public class ShieldData extends ConfigData {
    private final Path texturePath;
    private final float maxShield;
    private final float regenAmount;
    private final float rebuildTimeInTicks;

    ShieldData(ShieldConfig config, String fileName, int id) {
        super(fileName, id);
        this.texturePath = PathHelper.convertPath(config.texture());
        this.maxShield = config.maxShield();
        this.regenAmount = config.regenInSeconds() * TimeUtils.UPDATE_DELTA_TIME;
        this.rebuildTimeInTicks = config.rebuildTimeInSeconds() * TimeUtils.UPDATES_PER_SECOND;
    }
}
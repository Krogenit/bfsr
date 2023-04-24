package net.bfsr.config.component;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.util.PathHelper;

import java.nio.file.Path;

@Getter
public class ShieldData extends ConfigData {
    private final Path texturePath;
    private final float maxShield;
    private final float shieldRegen;
    private final float rebuildTime;

    public ShieldData(ShieldConfig config, int dataIndex) {
        super(config.name(), dataIndex);
        this.texturePath = PathHelper.convertPath(config.texture());
        this.maxShield = config.maxShield();
        this.shieldRegen = config.shieldRegen();
        this.rebuildTime = config.rebuildTime();
    }
}
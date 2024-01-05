package net.bfsr.config.entity.wreck;

import net.bfsr.config.ConfigConverter;
import net.bfsr.config.ConfigToDataConverter;
import net.bfsr.entity.wreck.WreckType;

import java.util.ArrayList;
import java.util.List;

@ConfigConverter
public final class WreckRegistry extends ConfigToDataConverter<WreckConfig, WreckData> {
    public static final WreckRegistry INSTANCE = new WreckRegistry();

    private final List<WreckData>[] wrecksByType;

    private WreckRegistry() {
        super("entity/wreck", WreckConfig.class, (fileName, wreckConfig) -> fileName,
                (config, fileName, index) -> new WreckData(config, index));
        WreckType[] values = WreckType.values();
        wrecksByType = new List[values.length];
        for (int i = 0; i < values.length; i++) {
            wrecksByType[i] = new ArrayList<>();
        }
    }

    @Override
    protected void add(WreckData data, String name) {
        super.add(data, name);
        wrecksByType[data.getType().ordinal()].add(data);
    }

    public WreckData getWreck(WreckType wreckType, int index) {
        return wrecksByType[wreckType.ordinal()].get(index);
    }
}
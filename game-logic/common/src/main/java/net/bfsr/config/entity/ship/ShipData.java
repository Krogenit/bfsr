package net.bfsr.config.entity.ship;

import gnu.trove.map.TMap;
import lombok.Getter;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.config.Vector2fConfigurable;
import net.bfsr.config.component.ModulesPolygonsConfig;
import net.bfsr.engine.Engine;
import net.bfsr.math.Direction;
import org.jbox2d.collision.shapes.Polygon;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ShipData extends GameObjectConfigData {
    private final int destroyTimeInTicks;
    private final Vector4f effectsColor;
    private final Vector2f[] weaponSlotPositions;
    private final Polygon reactorPolygon;
    private final Polygon shieldPolygon;
    private final TMap<Direction, EnginesData> engines;

    public ShipData(ShipConfig shipConfig, String fileName, int id, int registryId) {
        super(shipConfig, fileName, id, registryId);
        this.destroyTimeInTicks = Engine.convertToTicks(shipConfig.getDestroyTimeInSeconds());
        this.effectsColor = convert(shipConfig.getEffectsColor());

        Vector2fConfigurable[] slotPositions = shipConfig.getWeaponSlotPositions();
        this.weaponSlotPositions = new Vector2f[slotPositions.length];
        for (int i = 0; i < slotPositions.length; i++) {
            this.weaponSlotPositions[i] = convert(slotPositions[i]);
        }

        ModulesPolygonsConfig modules = shipConfig.getModules();

        this.reactorPolygon = convertToPolygon(modules.getReactor().getVertices());
        this.shieldPolygon = convertToPolygon(modules.getShield().getVertices());
        this.engines = convert(modules.getEngines(), direction -> direction, this::convert);
    }

    private EnginesData convert(EnginesConfig enginesConfig) {
        List<EngineConfig> configEngines = enginesConfig.getEngines();
        List<EngineData> engines = new ArrayList<>(configEngines.size());

        for (int i = 0; i < configEngines.size(); i++) {
            engines.add(convert(configEngines.get(i)));
        }

        return new EnginesData(engines);
    }

    private EngineData convert(EngineConfig engineConfig) {
        return new EngineData(convert(engineConfig.getPolygons()), convert(engineConfig.getEffectPosition()));
    }
}
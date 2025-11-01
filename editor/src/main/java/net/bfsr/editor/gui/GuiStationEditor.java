package net.bfsr.editor.gui;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.client.renderer.entity.RigidBodyRender;
import net.bfsr.client.renderer.entity.StationRender;
import net.bfsr.config.entity.station.StationConfig;
import net.bfsr.config.entity.station.StationData;
import net.bfsr.config.entity.station.StationRegistry;
import net.bfsr.editor.gui.component.MinimizableHolder;
import net.bfsr.editor.object.station.StationConverter;
import net.bfsr.editor.object.station.StationProperties;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.entity.Station;
import org.mapstruct.factory.Mappers;

@Log4j2
public class GuiStationEditor extends GuiEntityEditor<StationConfig, StationProperties, Station> {
    public GuiStationEditor() {
        super("Stations", Client.get().getConfigConverterManager().getConverter(StationRegistry.class),
                Mappers.getMapper(StationConverter.class), StationConfig.class, StationProperties.class);
    }

    @Override
    protected void createPropertyControls(MinimizableHolder<PropertiesHolder> minimizableHolder) {

    }

    @Override
    protected String getEntityName() {
        return "Station";
    }

    @Override
    protected Station createEntity(int id, StationConfig config) {
        Station station = new Station(new StationData(config, "station", id, configRegistry.getId()));
        station.init(client.getWorld(), -1);
        station.getBody().setActive(false);

        return station;
    }

    @Override
    protected RigidBodyRender createRender(Station station) {
        StationRender render = new StationRender(station) {
            @Override
            public void renderDebug() {
                if (polygonEditMode) {
                    return;
                }

                super.renderDebug();
            }
        };
        render.init();
        return render;
    }
}
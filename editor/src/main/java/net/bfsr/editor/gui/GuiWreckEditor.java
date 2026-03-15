package net.bfsr.editor.gui;

import net.bfsr.client.Client;
import net.bfsr.client.renderer.entity.RigidBodyRender;
import net.bfsr.client.renderer.entity.WreckRender;
import net.bfsr.config.entity.wreck.WreckConfig;
import net.bfsr.config.entity.wreck.WreckData;
import net.bfsr.config.entity.wreck.WreckRegistry;
import net.bfsr.editor.EditorGameLogic;
import net.bfsr.editor.gui.component.MinimizableHolder;
import net.bfsr.editor.object.wreck.WreckConverter;
import net.bfsr.editor.object.wreck.WreckProperties;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.entity.wreck.Wreck;
import org.mapstruct.factory.Mappers;

public class GuiWreckEditor extends GuiEntityEditor<WreckConfig, WreckProperties, Wreck> {
    public GuiWreckEditor() {
        super("Wrecks", Client.get().getConfigConverterManager().getConverter(WreckRegistry.class),
                Mappers.getMapper(WreckConverter.class), WreckConfig.class, WreckProperties.class);
    }

    @Override
    protected void createPropertyControls(MinimizableHolder<PropertiesHolder> minimizableHolder) {

    }

    @Override
    protected String getEntityName() {
        return "Wreck";
    }

    @Override
    protected Wreck createEntity(int id, WreckConfig wreckConfig) {
        Wreck wreck = new Wreck(new ObjectPool<>(Wreck::new));
        WreckData wreckData = new WreckData(wreckConfig, id, configRegistry.getId());
        wreck.init(client.getWorld(), -1, 0.0f, 0.0f, 0.0f, 1.0f, wreckData.getSizeX(), wreckData.getSizeY(), 0.0f, 0.0f, 0.0f,
                Integer.MAX_VALUE, id, false, wreckData);
        wreck.getBody().setActive(false);

        return wreck;
    }

    @Override
    protected RigidBodyRender createRender(Wreck rigidBody) {
        WreckRender wreckRender = new WreckRender(rigidBody, EditorGameLogic.EDITOR_Z_LAYER);
        wreckRender.init();
        return wreckRender;
    }
}

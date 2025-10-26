package net.bfsr.editor.gui.ship;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.client.renderer.entity.RigidBodyRender;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.config.entity.ship.ShipConfig;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.editor.gui.GuiEntityEditor;
import net.bfsr.editor.gui.component.MinimizableHolder;
import net.bfsr.editor.gui.property.PropertyCheckBox;
import net.bfsr.editor.object.ship.ShipConverter;
import net.bfsr.editor.object.ship.ShipProperties;
import net.bfsr.editor.object.ship.TestShip;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.gui.component.CheckBox;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import org.mapstruct.factory.Mappers;

import static net.bfsr.editor.gui.EditorTheme.FONT_SIZE;

@Log4j2
public class GuiShipEditor extends GuiEntityEditor<ShipConfig, ShipProperties, Ship> {
    private boolean showShipSprite = true;
    private boolean showModules = true;

    public GuiShipEditor() {
        super("Ships", Client.get().getConfigConverterManager().getConverter(ShipRegistry.class), Mappers.getMapper(ShipConverter.class),
                ShipConfig.class, ShipProperties.class);
    }

    @Override
    protected void createPropertyControls(MinimizableHolder<PropertiesHolder> minimizableHolder) {
        PropertyCheckBox propertyShowSprite = new PropertyCheckBox(minimizableHolder.getWidth(), elementHeight, "Show ship sprite", 0,
                FONT_SIZE, 0, null, null, new Object[]{showShipSprite}, (object, integer) -> {}, RunnableUtils.EMPTY_RUNNABLE);
        minimizableHolder.add(propertyShowSprite);
        CheckBox checkBox = propertyShowSprite.getCheckBox();
        checkBox.setLeftClickConsumer((mouseX, mouseY) -> {
            checkBox.setChecked(!checkBox.isChecked());
            showShipSprite = checkBox.isChecked();
        });
        PropertyCheckBox propertyShowModules = new PropertyCheckBox(minimizableHolder.getWidth(), elementHeight, "Show ship modules", 0,
                FONT_SIZE, 0, null, null, new Object[]{showModules}, (object, integer) -> {}, RunnableUtils.EMPTY_RUNNABLE);
        minimizableHolder.add(propertyShowModules);
        CheckBox checkBoxModules = propertyShowModules.getCheckBox();
        checkBoxModules.setLeftClickConsumer((mouseX, mouseY) -> {
            checkBoxModules.setChecked(!checkBoxModules.isChecked());
            showModules = checkBoxModules.isChecked();
        });
    }

    @Override
    protected String getEntityName() {
        return "Ship";
    }

    @Override
    protected Ship createEntity(int id, ShipConfig config) {
        TestShip testShip = new TestShip(new ShipData(config, "ship", id, configRegistry.getId()));
        testShip.init(client.getWorld(), -1);
        testShip.getBody().setActive(false);
        testShip.setFaction(Faction.HUMAN);
        testShip.setSpawned();

        try {
            new ShipOutfitter(client.getConfigConverterManager()).outfit(testShip);
        } catch (Exception e) {
            log.error("Can't outfit ship", e);
        }

        return testShip;
    }

    @Override
    protected RigidBodyRender createRender(Ship ship) {
        ShipRender shipRender = new ShipRender(ship) {
            @Override
            public void render() {
                if (!ship.isSpawned()) {
                    return;
                }

                if (showModules) {
                    renderModules();
                }

                if (showShipSprite) {
                    spriteRenderer.addDrawCommand(id, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.ENTITIES_ALPHA);
                    renderShield();
                }

                if (showModules) {
                    renderGunSlots();
                }
            }

            @Override
            public void renderDebug() {
                if (polygonEditMode) {
                    return;
                }

                super.renderDebug();
            }
        };
        shipRender.init();
        return shipRender;
    }
}
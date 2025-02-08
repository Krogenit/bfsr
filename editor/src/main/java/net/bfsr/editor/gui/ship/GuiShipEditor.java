package net.bfsr.editor.gui.ship;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.config.entity.ship.ShipConfig;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.gui.component.MinimizableHolder;
import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.editor.gui.property.PolygonProperty;
import net.bfsr.editor.gui.property.PropertyCheckBox;
import net.bfsr.editor.object.ship.ShipConverter;
import net.bfsr.editor.object.ship.ShipProperties;
import net.bfsr.editor.object.ship.TestShip;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.entity.GameObject;
import net.bfsr.engine.gui.component.CheckBox;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Log4j2
public class GuiShipEditor extends GuiEditor<ShipConfig, ShipProperties> {
    private final Client client = Client.get();
    private TestShip testShip;
    private final ShipConverter converter = Mappers.getMapper(ShipConverter.class);
    private boolean polygonCreationMode;
    private final Label polygonCreationModeString = new Label(font.getFontName(), "Polygon creation mode", fontSize);
    private PolygonProperty polygonProperty;
    private final GameObject polygonObject = new GameObject();
    private boolean lastDebugBoxesMode;
    private boolean showShipSprite = true;

    public GuiShipEditor() {
        super("Ships", Client.get().getConfigConverterManager().getConverter(ShipRegistry.class), Mappers.getMapper(ShipConverter.class),
                ShipConfig.class, ShipProperties.class);

        client.getEntityRenderer().addRender(new Render(polygonObject) {
            @Override
            public void update() {}

            @Override
            public void postWorldUpdate() {}

            @Override
            public void renderDebug() {
                if (!polygonCreationMode) return;

                List<Vector2fPropertiesHolder> vertices = polygonProperty.getVertices();
                if (vertices.isEmpty()) return;

                debugRenderer.addCommand(vertices.size());
                Vector4f color = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
                for (int i = 0; i < vertices.size(); i++) {
                    Vector2fPropertiesHolder vertex = vertices.get(i);
                    debugRenderer.addVertex(vertex.getX() + testShip.getX(), vertex.getY() + testShip.getY(), color);
                }
            }
        });

        addInspectionPanel();
    }

    @Override
    protected void onEntrySelected(InspectionEntry<ShipProperties> entry) {
        ShipProperties properties = entry.getComponentByType(ShipProperties.class);

        if (properties != null) {
            MinimizableHolder<PropertiesHolder> minimizableHolder = propertiesPanel.add(properties, "Ship");
            PropertyCheckBox propertyCheckBox = new PropertyCheckBox(minimizableHolder.getWidth(), elementHeight, "Show ship sprite", 0,
                    fontSize, stringOffsetY, null, null, new Object[]{showShipSprite}, (object, integer) -> {});
            minimizableHolder.add(propertyCheckBox);
            CheckBox checkBox = propertyCheckBox.getCheckBox();
            checkBox.setLeftClickConsumer((mouseX, mouseY) -> {
                checkBox.setChecked(!checkBox.isChecked());
                showShipSprite = checkBox.isChecked();
            });

            if (testShip != null) {
                testShip.setDead();
                client.getEntityRenderer().removeRenderById(testShip.getId());
            }

            try {
                testShip = new TestShip(new ShipData(converter.from(properties), "ship", 0, 0));
                testShip.init(client.getWorld(), -1);
                testShip.setFaction(Faction.HUMAN);
                testShip.setSpawned();

                try {
                    new ShipOutfitter(client.getConfigConverterManager()).outfit(testShip);
                } catch (Exception e) {
                    log.error("Can't outfit ship", e);
                }

                ShipRender render = new ShipRender(testShip) {
                    @Override
                    public void renderAlpha() {
                        if (!ship.isSpawned()) {
                            return;
                        }

                        for (int i = 0; i < moduleRenders.size(); i++) {
                            moduleRenders.get(i).renderAlpha();
                        }

                        if (showShipSprite) {
                            super.renderAlpha();
                        }

                        renderGunSlots();
                    }

                    @Override
                    public void renderDebug() {
                        if (polygonCreationMode) return;
                        super.renderDebug();
                    }
                };
                render.init();
                client.getEntityRenderer().addRender(render);
                render.getMaskTexture().createEmpty();
            } catch (Exception e) {
                log.error("Can't create ship for selected entry", e);
                remove(propertiesPanel);
                selectedEntry = null;
            }
        }
    }

    @Override
    protected void onEntryDeselected() {
        if (testShip != null) {
            testShip.setDead();
        }
    }

    @Override
    public void switchPolygonEditMode(PolygonProperty polygonProperty) {
        polygonCreationMode = !polygonCreationMode;

        if (polygonCreationMode) {
            add(polygonCreationModeString.atTop(-polygonCreationModeString.getWidth() / 2, elementHeight));
            this.polygonProperty = polygonProperty;
            lastDebugBoxesMode = ClientSettings.SHOW_DEBUG_BOXES.getBoolean();
            ClientSettings.SHOW_DEBUG_BOXES.setValue(true);
        } else {
            remove(polygonCreationModeString);
            ClientSettings.SHOW_DEBUG_BOXES.setValue(lastDebugBoxesMode);
        }
    }

    @Override
    public GuiObject mouseLeftRelease(int mouseX, int mouseY) {
        GuiObject child = super.mouseLeftRelease(mouseX, mouseY);

        if (child == null && polygonCreationMode && !inspectionPanel.isMouseHover() &&
                !propertiesPanel.isIntersectsWithMouse(mouseX, mouseY)) {
            Vector2f position = Engine.getMouse().getWorldPosition(renderer.getCamera());
            polygonProperty.addProperty(new Vector2fPropertiesHolder(position.x, position.y));
        }

        return child;
    }

    @Override
    public void remove() {
        super.remove();

        if (testShip != null) {
            testShip.setDead();
        }

        polygonObject.setDead();
        ClientSettings.SHOW_DEBUG_BOXES.setValue(lastDebugBoxesMode);
    }
}
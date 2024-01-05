package net.bfsr.editor.gui.ship;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Core;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.config.entity.ship.ShipConfig;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.editor.gui.property.PolygonProperty;
import net.bfsr.editor.object.ship.ShipConverter;
import net.bfsr.editor.object.ship.ShipProperties;
import net.bfsr.editor.object.ship.TestShip;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.StringObject;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Log4j2
public class GuiShipEditor extends GuiEditor<ShipConfig, ShipProperties> {
    private TestShip testShip;

    private final ShipConverter converter = Mappers.getMapper(ShipConverter.class);

    private boolean polygonCreationMode;
    private final StringObject polygonCreationModeString = new StringObject(fontType, "Polygon creation mode",
            fontSize).compile();
    private PolygonProperty polygonProperty;
    private final GameObject polygonObject = new GameObject();
    private final Render<GameObject> polygonRender = new Render<>(polygonObject) {
        @Override
        public void renderDebug() {
            if (!polygonCreationMode) return;

            List<Vector2fPropertiesHolder> vertices = polygonProperty.getVertices();
            if (vertices.size() == 0) return;

            debugRenderer.addCommand(vertices.size());
            Vector4f color = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
            Vector2f position = testShip.getPosition();
            for (int i = 0; i < vertices.size(); i++) {
                Vector2fPropertiesHolder vertex = vertices.get(i);
                debugRenderer.addVertex(vertex.getX() + position.x, vertex.getY() + position.y, color);
            }
        }
    };
    private boolean lastDebugBoxesMode;

    public GuiShipEditor() {
        super("Ships", ShipRegistry.INSTANCE, Mappers.getMapper(ShipConverter.class), ShipConfig.class, ShipProperties.class);
        Core.get().getRenderManager().addRender(polygonRender);
    }

    @Override
    protected void onEntrySelected(InspectionEntry<ShipProperties> entry) {
        ShipProperties properties = entry.getComponentByType(ShipProperties.class);

        if (properties != null) {
            propertiesPanel.add(properties, "Ship");

            if (testShip != null) {
                testShip.setDead();
                Core.get().getRenderManager().removeRenderById(testShip.getId());
            }

            try {
                testShip = new TestShip(new ShipData(converter.from(properties), "ship", 0));
                testShip.init(Core.get().getWorld(), -1);
                testShip.setFaction(Faction.HUMAN);
                testShip.setSpawned();

                try {
                    ShipOutfitter.get().outfit(testShip);
                } catch (Exception e) {
                    log.error("Can't outfit ship", e);
                }

                Core.get().getRenderManager().addRender(new ShipRender(testShip) {
                    @Override
                    public void renderDebug() {
                        if (polygonCreationMode) return;
                        super.renderDebug();
                    }
                });
            } catch (Exception e) {
                log.error("Can't create ship for selected entry", e);
                propertiesPanel.close();
                selectedEntry = null;
            }
        }
    }

    @Override
    public void switchPolygonEditMode(PolygonProperty polygonProperty) {
        polygonCreationMode = !polygonCreationMode;

        if (polygonCreationMode) {
            registerGuiObject(polygonCreationModeString.atTop(-polygonCreationModeString.getWidth() / 2, elementHeight));
            this.polygonProperty = polygonProperty;
            lastDebugBoxesMode = ClientSettings.SHOW_DEBUG_BOXES.getBoolean();
            ClientSettings.SHOW_DEBUG_BOXES.setValue(true);
        } else {
            unregisterGuiObject(polygonCreationModeString);
            ClientSettings.SHOW_DEBUG_BOXES.setValue(lastDebugBoxesMode);
        }
    }

    @Override
    public boolean onMouseLeftRelease() {
        boolean leftRelease = super.onMouseLeftRelease();

        if (!leftRelease && polygonCreationMode && !inspectionPanel.isMouseHover() && !propertiesPanel.isIntersectsWithMouse()) {
            Vector2f position = Engine.mouse.getWorldPosition(renderer.camera);
            polygonProperty.add(new Vector2fPropertiesHolder(position.x, position.y));
            updatePositions();
        }

        return leftRelease;
    }

    @Override
    public void clear() {
        super.clear();

        if (testShip != null) {
            testShip.setDead();
        }

        polygonObject.setDead();
        ClientSettings.SHOW_DEBUG_BOXES.setValue(lastDebugBoxesMode);
    }
}
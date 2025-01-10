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
import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.editor.gui.property.PolygonProperty;
import net.bfsr.editor.object.ship.ShipConverter;
import net.bfsr.editor.object.ship.ShipProperties;
import net.bfsr.editor.object.ship.TestShip;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
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
    private final Label polygonCreationModeString = new Label(font, "Polygon creation mode", fontSize);
    private PolygonProperty polygonProperty;
    private final GameObject polygonObject = new GameObject();
    private boolean lastDebugBoxesMode;

    public GuiShipEditor() {
        super("Ships", Client.get().getConfigConverterManager().getConverter(ShipRegistry.class), Mappers.getMapper(ShipConverter.class),
                ShipConfig.class, ShipProperties.class);
        Client.get().getRenderManager().addRender(new Render(polygonObject) {
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
            propertiesPanel.add(properties, "Ship");

            if (testShip != null) {
                testShip.setDead();
                Client.get().getRenderManager().removeRenderById(testShip.getId());
            }

            try {
                testShip = new TestShip(new ShipData(converter.from(properties), "ship", 0, 0));
                testShip.init(Client.get().getWorld(), -1);
                testShip.setFaction(Faction.HUMAN);
                testShip.setSpawned();

                try {
                    new ShipOutfitter(Client.get().getConfigConverterManager()).outfit(testShip);
                } catch (Exception e) {
                    log.error("Can't outfit ship", e);
                }

                ShipRender render = new ShipRender(testShip) {
                    @Override
                    public void renderDebug() {
                        if (polygonCreationMode) return;
                        super.renderDebug();
                    }
                };
                render.init();
                Client.get().getRenderManager().addRender(render);
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
    public GuiObject mouseLeftRelease() {
        GuiObject child = super.mouseLeftRelease();

        if (child == null && polygonCreationMode && !inspectionPanel.isMouseHover() && !propertiesPanel.isIntersectsWithMouse()) {
            Vector2f position = Engine.mouse.getWorldPosition(renderer.camera);
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
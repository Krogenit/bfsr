package net.bfsr.editor.gui.ship;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.config.entity.ship.ShipConfig;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.gui.GuiVertex;
import net.bfsr.editor.gui.component.MinimizableHolder;
import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.editor.gui.property.PolygonProperty;
import net.bfsr.editor.gui.property.PropertyCheckBox;
import net.bfsr.editor.object.ship.ShipConverter;
import net.bfsr.editor.object.ship.ShipProperties;
import net.bfsr.editor.object.ship.TestShip;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.entity.GameObject;
import net.bfsr.engine.gui.component.CheckBox;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import org.joml.Vector4f;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.CONTEXT_MENU_STRING_OFFSET_X;
import static net.bfsr.editor.gui.EditorTheme.FONT_SIZE;

@Log4j2
public class GuiShipEditor extends GuiEditor<ShipConfig, ShipProperties> {
    private final Client client = Client.get();
    private TestShip testShip;
    private final ShipConverter converter = Mappers.getMapper(ShipConverter.class);
    private boolean polygonCreationMode;
    private final Label polygonCreationModeString = new Label(font, "Polygon creation mode", FONT_SIZE);
    @Getter
    private PolygonProperty polygonProperty;
    private final GameObject polygonObject = new GameObject();
    private boolean lastDebugBoxesMode;
    private boolean showShipSprite = true;
    private final List<GuiVertex> guiVertices = new ArrayList<>();

    private ShipProperties selectedShipProperties;

    public GuiShipEditor() {
        super("Ships", Client.get().getConfigConverterManager().getConverter(ShipRegistry.class), Mappers.getMapper(ShipConverter.class),
                ShipConfig.class, ShipProperties.class);

        client.getEntityRenderer().addRender(new Render(polygonObject) {
            @Override
            public void update() {}

            @Override
            public void postWorldUpdate() {
                if (!polygonCreationMode) {
                    return;
                }

                List<Vector2fPropertiesHolder> vertices = polygonProperty.getVertices();
                if (vertices.isEmpty()) {
                    aabb.set(0.0f, 0.0f, 0.0f, 0.0f);
                    return;
                }

                Vector2fPropertiesHolder vertex = vertices.get(0);

                float minX = vertex.getX();
                float minY = vertex.getY();
                float maxX = vertex.getX();
                float maxY = vertex.getY();
                for (int i = 1; i < vertices.size(); i++) {
                    vertex = vertices.get(i);

                    if (vertex.getX() < minX) {
                        minX = vertex.getX();
                    }

                    if (vertex.getX() > maxX) {
                        maxX = vertex.getX();
                    }

                    if (vertex.getY() < minY) {
                        minY = vertex.getY();
                    }

                    if (vertex.getY() > maxY) {
                        maxY = vertex.getY();
                    }
                }

                aabb.set(minX, minY, maxX, maxY);
            }

            @Override
            public void renderDebug() {
                if (!polygonCreationMode) {
                    return;
                }

                List<Vector2fPropertiesHolder> vertices = polygonProperty.getVertices();
                if (vertices.isEmpty()) {
                    return;
                }

                debugRenderer.addCommand(vertices.size());
                Vector4f color = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
                for (int i = 0; i < vertices.size(); i++) {
                    Vector2fPropertiesHolder vertex = vertices.get(i);
                    debugRenderer.addVertex(vertex.getX() + testShip.getX(), vertex.getY() + testShip.getY(), color);
                }
            }
        });

        addInspectionPanel();
        propertiesPanel.setChangeValueListener(() -> {
            if (selectedShipProperties != null) {
                createShip(selectedShipProperties);
            }
        });
    }

    @Override
    protected void onEntrySelected(InspectionEntry<ShipProperties> entry) {
        ShipProperties properties = entry.getComponentByType(ShipProperties.class);

        if (properties != null) {
            selectedShipProperties = properties;

            MinimizableHolder<PropertiesHolder> minimizableHolder = propertiesPanel.add(properties, "Ship");
            PropertyCheckBox propertyCheckBox = new PropertyCheckBox(minimizableHolder.getWidth(), elementHeight, "Show ship sprite", 0,
                    FONT_SIZE, 0, null, null, new Object[]{showShipSprite}, (object, integer) -> {}, RunnableUtils.EMPTY_RUNNABLE);
            minimizableHolder.add(propertyCheckBox);
            CheckBox checkBox = propertyCheckBox.getCheckBox();
            checkBox.setLeftClickConsumer((mouseX, mouseY) -> {
                checkBox.setChecked(!checkBox.isChecked());
                showShipSprite = checkBox.isChecked();
            });

            createShip(properties);
        }
    }

    private void createShip(ShipProperties properties) {
        if (testShip != null) {
            testShip.setDead();
            client.getEntityRenderer().removeRenderById(testShip.getId());
        }

        try {
            testShip = new TestShip(new ShipData(converter.from(properties), "ship", 0, 0));
            testShip.init(client.getWorld(), -1);
            testShip.getBody().setActive(false);
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

    @Override
    protected void onEntryDeselected() {
        if (testShip != null) {
            testShip.setDead();
        }

        selectedShipProperties = null;
    }

    @Override
    public void switchPolygonEditMode(PolygonProperty polygonProperty) {
        polygonCreationMode = !polygonCreationMode;

        if (polygonCreationMode) {
            add(polygonCreationModeString.atTop(0, -elementHeight));
            this.polygonProperty = polygonProperty;
            lastDebugBoxesMode = ClientSettings.SHOW_DEBUG_BOXES.getBoolean();
            ClientSettings.SHOW_DEBUG_BOXES.setValue(true);

            List<Vector2fPropertiesHolder> vertices = polygonProperty.getVertices();
            for (int i = 0; i < vertices.size(); i++) {
                Vector2fPropertiesHolder vertex = vertices.get(i);
                GuiVertex guiVertex = new GuiVertex(this, vertex, font, FONT_SIZE, elementHeight, CONTEXT_MENU_STRING_OFFSET_X);
                guiVertices.add(guiVertex);
                addAt(i, guiVertex);
            }
        } else {
            for (int i = 0; i < guiVertices.size(); i++) {
                remove(guiVertices.get(i));
            }

            guiVertices.clear();

            remove(polygonCreationModeString);
            ClientSettings.SHOW_DEBUG_BOXES.setValue(lastDebugBoxesMode);
        }
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

    public void addVertex(int insertIndex, GuiVertex guiVertex) {
        guiVertices.add(guiVertex);
        addAt(insertIndex, guiVertex);
    }

    public void removeVertex(GuiVertex guiVertex) {
        guiVertices.remove(guiVertex);
        remove(guiVertex);
    }
}
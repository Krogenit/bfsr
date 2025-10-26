package net.bfsr.editor.gui;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.client.renderer.entity.DamageableRigidBodyRenderer;
import net.bfsr.client.renderer.entity.RigidBodyRender;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.editor.gui.component.MinimizableHolder;
import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.editor.gui.property.PolygonProperty;
import net.bfsr.editor.gui.property.PropertyCheckBox;
import net.bfsr.editor.object.EditorObjectConverter;
import net.bfsr.editor.object.ObjectProperties;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.editor.property.holder.Vector2fPropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.Config;
import net.bfsr.engine.config.ConfigData;
import net.bfsr.engine.config.ConfigToDataConverter;
import net.bfsr.engine.geometry.GeometryUtils;
import net.bfsr.engine.gui.component.CheckBox;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.input.Keys;
import net.bfsr.engine.renderer.entity.Render;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.engine.world.entity.GameObject;
import net.bfsr.engine.world.entity.RigidBody;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.CONTEXT_MENU_STRING_OFFSET_X;
import static net.bfsr.editor.gui.EditorTheme.FONT_SIZE;

@Log4j2
public abstract class GuiEntityEditor<CONFIG_TYPE extends Config, PROPERTIES_TYPE extends ObjectProperties, ENTITY_TYPE extends RigidBody>
        extends GuiEditor<CONFIG_TYPE, PROPERTIES_TYPE> {
    private final AbstractMouse mouse = Engine.getMouse();
    protected final Client client = Client.get();
    private final Label polygonCreationModeLabel = new Label(font, "Polygon edit mode", FONT_SIZE);
    private final Label debugDamageSystemModeLabel = new Label(font, "Damage system debug mode", FONT_SIZE);

    private final GameObject polygonObject = new GameObject();
    private final DamageSystem damageSystem = new DamageSystem();

    private ENTITY_TYPE testEntity;
    private RigidBodyRender entityRender;

    @Getter
    private PolygonProperty polygonProperty;
    protected boolean polygonEditMode;
    private final List<GuiVertex> guiVertices = new ArrayList<>();

    private boolean lastDebugBoxesMode;

    private boolean debugDamageSystem;
    private float clipPolygonRadius = 0.05f;
    private Polygon clipPolygon;

    private PROPERTIES_TYPE selectedShipProperties;

    public GuiEntityEditor(String inspectionPanelName, ConfigToDataConverter<CONFIG_TYPE, ?> configRegistry,
                           EditorObjectConverter<CONFIG_TYPE, PROPERTIES_TYPE> converter, Class<CONFIG_TYPE> configClass,
                           Class<PROPERTIES_TYPE> propertiesClass) {
        super(inspectionPanelName, configRegistry, converter, configClass, propertiesClass);

        client.getEntityRenderer().addRender(new Render(polygonObject) {
            private final Vector4f color = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);

            @Override
            public void update() {}

            @Override
            public void postWorldUpdate() {
                if (polygonEditMode) {
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
                } else if (debugDamageSystem) {
                    Vector2f mouseWorldPosition = mouse.getWorldPosition(client.getCamera());
                    Coordinate[] coordinates = clipPolygon.getCoordinates();
                    Coordinate vertex = coordinates[0];

                    float minX = (float) vertex.getX() + mouseWorldPosition.x;
                    float minY = (float) vertex.getY() + mouseWorldPosition.y;
                    float maxX = (float) vertex.getX() + mouseWorldPosition.x;
                    float maxY = (float) vertex.getY() + mouseWorldPosition.y;
                    for (int i = 1; i < coordinates.length; i++) {
                        vertex = coordinates[i];

                        float x = (float) vertex.getX() + mouseWorldPosition.x;
                        if (x < minX) {
                            minX = x;
                        }

                        if (x > maxX) {
                            maxX = x;
                        }

                        float y = (float) vertex.getY() + mouseWorldPosition.y;
                        if (y < minY) {
                            minY = y;
                        }

                        if (y > maxY) {
                            maxY = y;
                        }
                    }

                    aabb.set(minX, minY, maxX, maxY);
                }
            }

            @Override
            public void renderDebug() {
                if (polygonEditMode) {
                    List<Vector2fPropertiesHolder> vertices = polygonProperty.getVertices();
                    if (vertices.isEmpty()) {
                        return;
                    }

                    debugRenderer.addCommand(vertices.size());
                    for (int i = 0; i < vertices.size(); i++) {
                        Vector2fPropertiesHolder vertex = vertices.get(i);
                        debugRenderer.addVertex(vertex.getX() + testEntity.getX(), vertex.getY() + testEntity.getY(), color);
                    }
                } else if (debugDamageSystem) {
                    Vector2f mouseWorldPosition = mouse.getWorldPosition(client.getCamera());
                    Coordinate[] coordinates = clipPolygon.getCoordinates();

                    debugRenderer.addCommand(coordinates.length);
                    for (int i = 0; i < coordinates.length; i++) {
                        Coordinate coordinate = coordinates[i];
                        debugRenderer.addVertex((float) coordinate.x + mouseWorldPosition.x,
                                (float) coordinate.y + mouseWorldPosition.y, color);
                    }
                }
            }
        });

        addInspectionPanel();
        propertiesPanel.setChangeValueListener(() -> {
            if (selectedShipProperties != null) {
                createEntity(selectedShipProperties);
            }
        });
    }

    @Override
    protected void onEntrySelected(InspectionEntry<PROPERTIES_TYPE> entry) {
        PROPERTIES_TYPE properties = entry.getComponentByType(propertiesClass);

        if (properties != null) {
            selectedShipProperties = properties;

            MinimizableHolder<PropertiesHolder> minimizableHolder = propertiesPanel.add(properties, getEntityName());
            createPropertyControls(minimizableHolder);

            PropertyCheckBox propertyDebugDamageSystem = new PropertyCheckBox(minimizableHolder.getWidth(), elementHeight,
                    "Debug damage system", 0, FONT_SIZE, 0, null, null, new Object[]{debugDamageSystem},
                    (object, integer) -> {}, RunnableUtils.EMPTY_RUNNABLE);
            minimizableHolder.add(propertyDebugDamageSystem);
            CheckBox debugDamageSystemCheckBox = propertyDebugDamageSystem.getCheckBox();
            debugDamageSystemCheckBox.setLeftClickConsumer((mouseX, mouseY) -> {
                debugDamageSystemCheckBox.setChecked(!debugDamageSystemCheckBox.isChecked());
                debugDamageSystem = debugDamageSystemCheckBox.isChecked();

                if (debugDamageSystem) {
                    add(debugDamageSystemModeLabel.atTop(0, -elementHeight << 1));
                    clipPolygon = createClipPolygon(0, 0);
                } else {
                    remove(debugDamageSystemModeLabel);
                    clipPolygon = null;
                }
            });

            createEntity(properties);
        }
    }

    protected abstract void createPropertyControls(MinimizableHolder<PropertiesHolder> minimizableHolder);

    protected abstract String getEntityName();

    private Polygon createClipPolygon(float x, float y) {
        return GeometryUtils.createCirclePath(x, y, 0, 1, 6, clipPolygonRadius);
    }

    private void createEntity(PROPERTIES_TYPE properties) {
        if (testEntity != null) {
            testEntity.setDead();
            client.getEntityRenderer().removeRenderById(testEntity.getId());
        }

        try {
            CONFIG_TYPE entityConfig = converter.from(properties);
            ConfigData configData = configRegistry.get(entityConfig.getName());
            int id = configData != null ? configData.getId() : 0;
            testEntity = createEntity(id, entityConfig);
            entityRender = createRender(testEntity);
            client.getEntityRenderer().addRender(entityRender);
        } catch (Exception e) {
            log.error("Can't create entity for selected entry", e);
            remove(propertiesPanel);
            selectedEntry = null;
        }
    }

    protected abstract ENTITY_TYPE createEntity(int id, CONFIG_TYPE entityConfig);

    protected abstract RigidBodyRender createRender(ENTITY_TYPE rigidBody);

    @Override
    protected void onEntryDeselected(@Nullable InspectionEntry<PROPERTIES_TYPE> selectedEntry) {
        if (testEntity != null) {
            testEntity.setDead();
        }

        selectedShipProperties = null;

        if (polygonEditMode) {
            switchPolygonEditMode(null);
        }
    }

    @Override
    public void switchPolygonEditMode(PolygonProperty polygonProperty) {
        polygonEditMode = !polygonEditMode;

        if (polygonEditMode) {
            add(polygonCreationModeLabel.atTop(0, -elementHeight));
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

            remove(polygonCreationModeLabel);
            ClientSettings.SHOW_DEBUG_BOXES.setValue(lastDebugBoxesMode);
        }
    }

    @Override
    public @Nullable GuiObject mouseLeftRelease(int mouseX, int mouseY) {
        GuiObject guiObject = super.mouseLeftRelease(mouseX, mouseY);

        if (guiObject == this && debugDamageSystem && testEntity instanceof DamageableRigidBody damageableEntity) {
            Vector2f mouseWorldPosition = mouse.getWorldPosition(client.getCamera());
            float textureClipRadius = clipPolygonRadius;
            int vertices = damageableEntity.getPolygon().getNumPoints();
            damageSystem.damage(damageableEntity, mouseWorldPosition.x, mouseWorldPosition.y,
                    createClipPolygon(mouseWorldPosition.x, mouseWorldPosition.y), textureClipRadius,
                    0, 0, 0, 1, RunnableUtils.EMPTY_RUNNABLE);
            DamageMask damageMask = damageableEntity.getDamageMask();

            int width = damageMask.getWidth();
            int height = damageMask.getHeight();
            ByteBuffer byteBuffer = renderer.createByteBuffer(width * height);
            byteBuffer.put(damageMask.getData()).flip();
            ((DamageableRigidBodyRenderer) entityRender).updateDamageMask(0, 0, width, height, byteBuffer);
            renderer.memFree(byteBuffer);

            int newVertices = damageableEntity.getPolygon().getNumPoints();
            Client.get().getHud().addChatMessage("Vertices count before: " + vertices + ", after: " + newVertices);
        }

        return guiObject;
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, float scrollY) {
        boolean mouseScroll = super.mouseScroll(mouseX, mouseY, scrollY);

        if (!mouseScroll && debugDamageSystem && Engine.getKeyboard().isKeyDown(Keys.KEY_LEFT_CONTROL)) {
            clipPolygonRadius += scrollY * 0.05f;
            if (clipPolygonRadius < 0.01f) {
                clipPolygonRadius = 0.01f;
            }

            clipPolygon = createClipPolygon(0, 0);
            return true;
        }

        return mouseScroll;
    }

    @Override
    public void remove() {
        super.remove();

        if (testEntity != null) {
            testEntity.setDead();
        }

        polygonObject.setDead();
        ClientSettings.SHOW_DEBUG_BOXES.setValue(lastDebugBoxesMode);
    }

    public void addVertex(int insertIndex, GuiVertex guiVertex) {
        guiVertices.add(insertIndex, guiVertex);
        addAt(insertIndex, guiVertex);
    }

    public void removeVertex(GuiVertex guiVertex) {
        guiVertices.remove(guiVertex);
        remove(guiVertex);
    }
}

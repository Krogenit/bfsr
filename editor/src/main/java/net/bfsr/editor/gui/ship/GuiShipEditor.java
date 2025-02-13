package net.bfsr.editor.gui.ship;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.config.entity.ship.ShipConfig;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageSystem;
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
import net.bfsr.engine.Engine;
import net.bfsr.engine.entity.GameObject;
import net.bfsr.engine.gui.component.CheckBox;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.input.Keys;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.entity.Render;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.mapstruct.factory.Mappers;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.CONTEXT_MENU_STRING_OFFSET_X;
import static net.bfsr.editor.gui.EditorTheme.FONT_SIZE;

@Log4j2
public class GuiShipEditor extends GuiEditor<ShipConfig, ShipProperties> {
    private final AbstractMouse mouse = Engine.getMouse();
    private final Client client = Client.get();
    private final ShipConverter converter = Mappers.getMapper(ShipConverter.class);
    private final Label polygonCreationModeLabel = new Label(font, "Polygon creation mode", FONT_SIZE);
    private final Label debugDamageSystemModeLabel = new Label(font, "Damage system debug mode", FONT_SIZE);

    private final GameObject polygonObject = new GameObject();
    private final DamageSystem damageSystem = new DamageSystem();

    private TestShip testShip;
    private ShipRender shipRender;

    @Getter
    private PolygonProperty polygonProperty;
    private boolean polygonCreationMode;
    private final List<GuiVertex> guiVertices = new ArrayList<>();

    private boolean lastDebugBoxesMode;
    private boolean showShipSprite = true;
    private boolean showModules = true;

    private boolean debugDamageSystem;
    private float clipPolygonRadius = 0.5f;
    private Polygon clipPolygon;

    private ShipProperties selectedShipProperties;

    public GuiShipEditor() {
        super("Ships", Client.get().getConfigConverterManager().getConverter(ShipRegistry.class), Mappers.getMapper(ShipConverter.class),
                ShipConfig.class, ShipProperties.class);

        client.getEntityRenderer().addRender(new Render(polygonObject) {
            private final Vector4f color = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);

            @Override
            public void update() {}

            @Override
            public void postWorldUpdate() {
                if (polygonCreationMode) {
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
                if (polygonCreationMode) {
                    List<Vector2fPropertiesHolder> vertices = polygonProperty.getVertices();
                    if (vertices.isEmpty()) {
                        return;
                    }

                    debugRenderer.addCommand(vertices.size());
                    for (int i = 0; i < vertices.size(); i++) {
                        Vector2fPropertiesHolder vertex = vertices.get(i);
                        debugRenderer.addVertex(vertex.getX() + testShip.getX(), vertex.getY() + testShip.getY(), color);
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

            createShip(properties);
        }
    }

    private Polygon createClipPolygon(float x, float y) {
        return damageSystem.createCirclePath(x, y, 0, 1, 12, clipPolygonRadius);
    }

    private void createShip(ShipProperties properties) {
        if (testShip != null) {
            testShip.setDead();
            client.getEntityRenderer().removeRenderById(testShip.getId());
        }

        try {
            testShip = new TestShip(new ShipData(converter.from(properties), "ship", 0, 0));
            testShip.getDamageMask().init();
            testShip.init(client.getWorld(), -1);
            testShip.getBody().setActive(false);
            testShip.setFaction(Faction.HUMAN);
            testShip.setSpawned();

            try {
                new ShipOutfitter(client.getConfigConverterManager()).outfit(testShip);
            } catch (Exception e) {
                log.error("Can't outfit ship", e);
            }

            shipRender = new ShipRender(testShip) {
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
                    if (polygonCreationMode) return;
                    super.renderDebug();
                }
            };
            shipRender.init();
            client.getEntityRenderer().addRender(shipRender);
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

        if (guiObject == this && debugDamageSystem) {
            Vector2f mouseWorldPosition = mouse.getWorldPosition(client.getCamera());
            float textureClipRadius = clipPolygonRadius * 2.0f;
            int vertices = testShip.getPolygon().getNumPoints();
            damageSystem.damage(testShip, mouseWorldPosition.x, mouseWorldPosition.y,
                    createClipPolygon(mouseWorldPosition.x, mouseWorldPosition.y), textureClipRadius,
                    0, 0, 0, 1, RunnableUtils.EMPTY_RUNNABLE);
            DamageMask damageMask = testShip.getDamageMask();

            int width = damageMask.getWidth();
            int height = damageMask.getHeight();
            ByteBuffer byteBuffer = renderer.createByteBuffer(width * height);
            byteBuffer.put(damageMask.getData()).flip();
            shipRender.updateDamageMask(0, 0, width, height, byteBuffer);
            renderer.memFree(byteBuffer);

            int newVertices = testShip.getPolygon().getNumPoints();
            Client.get().getHud().addChatMessage("Vertices count before: " + vertices + ", after: " + newVertices);
        }

        return guiObject;
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, float scrollY) {
        boolean mouseScroll = super.mouseScroll(mouseX, mouseY, scrollY);

        if (!mouseScroll && debugDamageSystem && Engine.getKeyboard().isKeyDown(Keys.KEY_LEFT_CONTROL)) {
            clipPolygonRadius += scrollY / 2.0f;
            if (clipPolygonRadius < 0.1f) {
                clipPolygonRadius = 0.1f;
            }

            clipPolygon = createClipPolygon(0, 0);
            return true;
        }

        return mouseScroll;
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
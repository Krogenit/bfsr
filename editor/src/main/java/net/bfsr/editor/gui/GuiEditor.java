package net.bfsr.editor.gui;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Core;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.config.Config;
import net.bfsr.config.ConfigLoader;
import net.bfsr.config.ConfigToDataConverter;
import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.editor.gui.inspection.InspectionPanel;
import net.bfsr.editor.gui.property.PolygonProperty;
import net.bfsr.editor.gui.property.PropertiesPanel;
import net.bfsr.editor.object.EditorObjectConverter;
import net.bfsr.editor.object.ObjectProperties;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.object.AbstractGuiObject;
import net.bfsr.engine.gui.object.GuiObjectWithSubObjects;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.engine.util.RunnableUtils;
import org.joml.Vector2f;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButtonColors;
import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

@Log4j2
public abstract class GuiEditor<CONFIG_TYPE extends Config, PROPERTIES_TYPE extends ObjectProperties> extends Gui {
    protected final Core core = Core.get();

    private final int propertiesContainerWidth = 450;
    protected final int elementHeight = 20;

    private final int leftPanelWidth = 300;

    protected final FontType fontType = EditorTheme.FONT_TYPE;
    protected final int fontSize = 13;
    private final int stringXOffset = 4;
    private final int stringYOffset = 0;
    private final int contextMenuStringXOffset = 8;

    private final boolean prevFollowCameraOptionValue;

    protected InspectionEntry<PROPERTIES_TYPE> selectedEntry;
    private final List<InspectionEntry<PROPERTIES_TYPE>> entries = new ArrayList<>();
    protected final InspectionPanel<PROPERTIES_TYPE> inspectionPanel;
    protected final PropertiesPanel propertiesPanel = new PropertiesPanel(
            this, propertiesContainerWidth, fontType, fontSize, stringXOffset, stringYOffset, contextMenuStringXOffset
    );

    private final ConfigToDataConverter<CONFIG_TYPE, ?> configRegistry;
    private final EditorObjectConverter<CONFIG_TYPE, PROPERTIES_TYPE> converter;
    private final Class<CONFIG_TYPE> configClass;
    private final Class<PROPERTIES_TYPE> propertiesClass;

    protected GuiEditor(String inspectionPanelName, ConfigToDataConverter<CONFIG_TYPE, ?> configRegistry,
                        EditorObjectConverter<CONFIG_TYPE, PROPERTIES_TYPE> converter, Class<CONFIG_TYPE> configClass,
                        Class<PROPERTIES_TYPE> propertiesClass) {
        this.inspectionPanel = new InspectionPanel<>(this, inspectionPanelName, leftPanelWidth, fontType, fontSize,
                stringYOffset);
        this.configRegistry = configRegistry;
        this.converter = converter;
        this.configClass = configClass;
        this.propertiesClass = propertiesClass;
        this.prevFollowCameraOptionValue = ClientSettings.CAMERA_FOLLOW_PLAYER.getBoolean();
        ClientSettings.CAMERA_FOLLOW_PLAYER.setValue(false);
        Vector2f position = renderer.camera.getPosition();
        renderer.camera.move(-position.x, -position.y);
    }

    @Override
    protected void initElements() {
        initInspectionPanel(0, 0);
        propertiesPanel.initElements();
        load();
    }

    private void initInspectionPanel(int x, int y) {
        inspectionPanel.setRightClickSupplier(() -> {
            if (!inspectionPanel.isMouseHover()) return false;
            Vector2f mousePos = Engine.mouse.getPosition();
            int x1 = (int) mousePos.x;
            int y1 = (int) mousePos.y;

            String name = "Create Folder";
            Button createEntryButton =
                    new Button(null, x1, y1, fontType.getStringCache().getStringWidth(name, fontSize) + contextMenuStringXOffset,
                            elementHeight, name, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT,
                            RunnableUtils.EMPTY_RUNNABLE);
            createEntryButton.setOnMouseClickRunnable(() -> {
                InspectionEntry<PROPERTIES_TYPE> entry = inspectionPanel.createEntry();
                inspectionPanel.addSubObject(entry);
                inspectionPanel.updatePositions();
            });

            y1 += elementHeight;

            name = "Create Object";
            Button createEffectButton =
                    new Button(null, x1, y1, fontType.getStringCache().getStringWidth(name, fontSize) + contextMenuStringXOffset,
                            elementHeight, name, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT,
                            RunnableUtils.EMPTY_RUNNABLE);
            createEffectButton.setOnMouseClickRunnable(() -> {
                InspectionEntry<PROPERTIES_TYPE> entry = createEntry();
                inspectionPanel.addSubObject(entry);
                inspectionPanel.updatePositions();
            });

            openContextMenu(setupContextMenuButtonColors(createEntryButton), setupContextMenuButtonColors(createEffectButton));
            return true;
        });
        inspectionPanel.setEntryRightClickSupplier((inspectionEntry) -> {
            if (!inspectionEntry.isIntersectsWithMouse()) return false;
            Vector2f mousePos = Engine.mouse.getPosition();
            int x1 = (int) mousePos.x;
            int y1 = (int) mousePos.y;
            String addString = "Create Entry";
            Button createEntryButton = new Button(
                    null, x1, y1, fontType.getStringCache().getStringWidth(addString, fontSize) + contextMenuStringXOffset,
                    elementHeight, addString, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT,
                    () -> {
                        InspectionEntry<PROPERTIES_TYPE> childEntry = inspectionPanel.createEntry();
                        inspectionEntry.addSubObject(childEntry);
                        inspectionEntry.maximize();
                        inspectionPanel.updatePositions();
                    }
            );

            y1 += elementHeight;

            addString = "Create Effect";
            Button createEffectButton = new Button(
                    null, x1, y1, fontType.getStringCache().getStringWidth(addString, fontSize) + contextMenuStringXOffset,
                    elementHeight, addString, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT,
                    () -> {
                        InspectionEntry<PROPERTIES_TYPE> inspectionHolder = createEntry();
                        inspectionEntry.addSubObject(inspectionHolder);
                        inspectionEntry.maximize();
                        inspectionPanel.updatePositions();
                    }
            );
            y1 += elementHeight;

            addString = "Remove";
            Button removeButton = new Button(
                    null, x1, y1, fontType.getStringCache().getStringWidth(addString, fontSize) + contextMenuStringXOffset,
                    elementHeight, addString, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT,
                    () -> {
                        GuiObjectWithSubObjects parent = inspectionEntry.getParent();
                        parent.removeSubObject(inspectionEntry);
                        remove(inspectionEntry);
                        inspectionPanel.updatePositions();
                    }
            );

            openContextMenu(setupContextMenuButtonColors(createEntryButton), setupContextMenuButtonColors(createEffectButton),
                    setupContextMenuButtonColors(removeButton));
            return true;
        });
        inspectionPanel.setOnSelectConsumer(this::selectEntry);
        inspectionPanel.initElements(x, y);
        y -= elementHeight;
        inspectionPanel.addBottomButton(x, y, "Save All", this::saveAll);
        y -= elementHeight;
        inspectionPanel.addBottomButton(x, y, "Add", () -> {
            inspectionPanel.addSubObject(createEntry());
            inspectionPanel.updatePositions();
        });
    }

    private void load() {
        List<CONFIG_TYPE> configs = new ArrayList<>();
        ConfigLoader.loadFromFiles(configRegistry.getFolder(), configClass, (path, fileName, config) -> configs.add(config));

        configs.stream().sorted(Comparator.comparingInt(CONFIG_TYPE::getTreeIndex)).forEach(config -> {
            String editorPath = config.getPath();
            String fileName = config.getName();

            PROPERTIES_TYPE properties = converter.to(config);

            if (editorPath != null && !editorPath.isEmpty()) {
                addToEntry(buildEntryPath(editorPath), properties, fileName);
            } else {
                InspectionEntry<PROPERTIES_TYPE> entry = inspectionPanel.findEntry(fileName);
                if (entry != null) {
                    entry.addComponent(properties);
                    entries.add(entry);
                } else {
                    inspectionPanel.addSubObject(createEntry(fileName, properties));
                }
            }
        });

        inspectionPanel.sortFolders();
        inspectionPanel.updatePositions();
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (key == KEY_ESCAPE) {
            Core.get().closeGui();
        }

        return input;
    }

    @Override
    public boolean onMouseLeftClick() {
        boolean result = super.onMouseLeftClick();
        inspectionPanel.onMouseLeftClick();
        return result;
    }

    @Override
    public boolean onMouseLeftRelease() {
        boolean leftRelease = inspectionPanel.onMouseLeftRelease();

        if (super.onMouseLeftRelease()) {
            leftRelease = true;
        }

        return leftRelease;
    }

    @Override
    public void update() {
        super.update();
        inspectionPanel.update();
    }

    public void updatePositions() {
        inspectionPanel.updatePositions();
        propertiesPanel.updatePropertiesPositions();
    }

    @Override
    public void render() {
        super.render();
        inspectionPanel.render();
    }

    private InspectionEntry<PROPERTIES_TYPE> buildEntryPath(String editorPath) {
        InspectionEntry<PROPERTIES_TYPE> parent = null;
        String[] strings = editorPath.split("/");
        for (int i = 0; i < strings.length; i++) {
            String path = strings[i];
            InspectionEntry<PROPERTIES_TYPE> inspectionEntry;

            if (parent == null) {
                inspectionEntry = inspectionPanel.findEntry(path);
            } else {
                inspectionEntry = inspectionPanel.findEntry(parent, path);
            }

            if (inspectionEntry == null) {
                inspectionEntry = inspectionPanel.createEntry();
                inspectionEntry.setName(path);
                if (parent == null) {
                    inspectionPanel.addSubObject(inspectionEntry);
                } else {
                    parent.addSubObject(inspectionEntry);
                }
            }

            parent = inspectionEntry;
        }

        return parent;
    }

    private void addToEntry(InspectionEntry<PROPERTIES_TYPE> parent,
                            PROPERTIES_TYPE properties, String name) {
        boolean entryNotFound = true;
        List<AbstractGuiObject> subObjects = parent.getSubObjects();
        for (int i = 0; i < subObjects.size(); i++) {
            InspectionEntry<PROPERTIES_TYPE> inspectionEntry =
                    (InspectionEntry<PROPERTIES_TYPE>) subObjects.get(i);
            if (inspectionEntry.getName().equals(name)) {
                entryNotFound = false;
                inspectionEntry.addComponent(properties);
                entries.add(inspectionEntry);
                break;
            }
        }

        if (entryNotFound) {
            parent.addSubObject(createEntry(name, properties));
        }
    }

    private InspectionEntry<PROPERTIES_TYPE> createEntry() {
        PROPERTIES_TYPE properties;

        try {
            properties = propertiesClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        properties.setDefaultValues();
        return createEntry("object", properties);
    }

    private InspectionEntry<PROPERTIES_TYPE> createEntry(String name, PROPERTIES_TYPE properties) {
        InspectionEntry<PROPERTIES_TYPE> entry = inspectionPanel.createEntry(name, properties);
        entries.add(entry);
        return entry;
    }

    private void saveAll() {
        try {
            for (int i = 0; i < entries.size(); i++) {
                InspectionEntry<PROPERTIES_TYPE> entry = entries.get(i);
                save(entry);
            }
        } catch (Exception e) {
            log.error("Failed to save entries", e);
        }

        try {
            Files.walkFileTree(configRegistry.getFolder(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String filePath = PathHelper.convertToLocalPath(configRegistry.getFolder(), file);
                    if (entries.stream().noneMatch(inspectionHolder -> {
                        PROPERTIES_TYPE properties = inspectionHolder.getComponentByType(propertiesClass);
                        return filePath.equals(properties.getFullPath());
                    })) {
                        Files.delete(file);
                        configRegistry.remove(filePath);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    File[] listFiles = dir.toFile().listFiles();
                    if (listFiles == null || listFiles.length == 0) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Failed to delete files", e);
        }
    }

    private void save(InspectionEntry<PROPERTIES_TYPE> entry) {
        GuiObjectWithSubObjects parent = entry.getParent();
        String editorPath = "";
        while (parent instanceof InspectionEntry<?> inspectionEntry) {
            editorPath = inspectionEntry.getName() + (editorPath.isEmpty() ? editorPath : "/" + editorPath);
            parent = inspectionEntry.getParent();
        }

        propertiesPanel.applyProperties();
        PROPERTIES_TYPE properties = entry.getComponentByType(propertiesClass);
        if (properties != null) {
            properties.setName(entry.getName());
            properties.setPath(editorPath);
            properties.setTreeIndex(entry.getParent().getSubObjects().indexOf(entry));
            String fileName = editorPath.isEmpty() ? entry.getName() : editorPath + "/" + entry.getName();

            CONFIG_TYPE config = converter.from(properties);
            if (configRegistry.get(fileName) == null) {
                configRegistry.add(editorPath, fileName, config);
            }
            Path file = configRegistry.getFolder().resolve(fileName + ".json");
            file.toFile().mkdirs();
            ConfigLoader.save(file, config, configClass);
        }
    }

    private void remove(InspectionEntry<PROPERTIES_TYPE> entry) {
        propertiesPanel.close();
        selectedEntry = null;
        entries.remove(entry);
        entry.getParent().removeSubObject(entry);
        PROPERTIES_TYPE properties = entry.getComponentByType(propertiesClass);
        if (properties != null) {
            configRegistry.remove(properties.getFullPath());
        }
    }

    private void selectEntry(InspectionEntry<PROPERTIES_TYPE> entry) {
        if (selectedEntry != null) {
            propertiesPanel.applyProperties();
        }

        propertiesPanel.close();
        selectedEntry = entry;

        if (entry == null) {
            return;
        }

        propertiesPanel.open(() -> save(entry), () -> remove(entry));
        onEntrySelected(entry);
    }

    protected abstract void onEntrySelected(InspectionEntry<PROPERTIES_TYPE> entry);

    public void switchPolygonEditMode(PolygonProperty polygonProperty) {}

    @Override
    public boolean isAllowCameraZoom() {
        return !propertiesPanel.isIntersectsWithMouse() && !inspectionPanel.isIntersectsWithMouse();
    }

    @Override
    public void clear() {
        super.clear();
        ClientSettings.CAMERA_FOLLOW_PLAYER.setValue(prevFollowCameraOptionValue);
    }
}
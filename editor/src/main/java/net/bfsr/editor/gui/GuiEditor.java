package net.bfsr.editor.gui;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
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
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.engine.util.RunnableUtils;
import org.jetbrains.annotations.Nullable;
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

import static net.bfsr.editor.gui.EditorTheme.BACKGROUND_COLOR;
import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButton;
import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

@Log4j2
public abstract class GuiEditor<CONFIG_TYPE extends Config, PROPERTIES_TYPE extends ObjectProperties> extends Gui {
    private final int propertiesContainerWidth = 450;
    protected final int elementHeight = 20;

    private final int leftPanelWidth = 300;

    protected final Font font = EditorTheme.FONT_TYPE;
    protected final int fontSize = 13;
    private final int stringOffsetX = 4;
    private final int stringOffsetY = 0;
    private final int contextMenuStringOffsetX = 8;

    private final boolean prevFollowCameraOptionValue;

    protected @Nullable InspectionEntry<PROPERTIES_TYPE> selectedEntry;
    private final List<InspectionEntry<PROPERTIES_TYPE>> entries = new ArrayList<>();
    protected final InspectionPanel<PROPERTIES_TYPE> inspectionPanel;
    protected final PropertiesPanel propertiesPanel = new PropertiesPanel(
            propertiesContainerWidth, Engine.renderer.getScreenHeight() - elementHeight, font, fontSize, stringOffsetX,
            stringOffsetY, contextMenuStringOffsetX
    );

    private final ConfigToDataConverter<CONFIG_TYPE, ?> configRegistry;
    private final EditorObjectConverter<CONFIG_TYPE, PROPERTIES_TYPE> converter;
    private final Class<CONFIG_TYPE> configClass;
    private final Class<PROPERTIES_TYPE> propertiesClass;

    protected GuiEditor(String inspectionPanelName, ConfigToDataConverter<CONFIG_TYPE, ?> configRegistry,
                        EditorObjectConverter<CONFIG_TYPE, PROPERTIES_TYPE> converter, Class<CONFIG_TYPE> configClass,
                        Class<PROPERTIES_TYPE> propertiesClass) {
        this.inspectionPanel = new InspectionPanel<>(this, inspectionPanelName, leftPanelWidth, Engine.renderer.getScreenHeight(), font,
                fontSize, stringOffsetY);
        this.configRegistry = configRegistry;
        this.converter = converter;
        this.configClass = configClass;
        this.propertiesClass = propertiesClass;
        this.prevFollowCameraOptionValue = ClientSettings.CAMERA_FOLLOW_PLAYER.getBoolean();
        ClientSettings.CAMERA_FOLLOW_PLAYER.setValue(false);
        Vector2f position = renderer.camera.getPosition();
        renderer.camera.move(-position.x, -position.y);

        addInspectionPanel();

        propertiesPanel.setAllColors(BACKGROUND_COLOR.x, BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w)
                .setHeightFunction((width, height) -> height)
                .atTopRight(-propertiesPanel.getWidth(), 0);

        load();
    }

    private void addInspectionPanel() {
        add(inspectionPanel.setOnSelectConsumer(this::selectEntry)
                .setAllColors(BACKGROUND_COLOR.x, BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w)
                .setHeightFunction((integer, integer2) -> Engine.renderer.getScreenHeight())
                .setRightClickRunnable(() -> {
                    Vector2f mousePos = Engine.mouse.getPosition();
                    int x1 = (int) mousePos.x;
                    int y1 = (int) mousePos.y;

                    String name = "Create Folder";
                    Button createEntryButton =
                            new Button(x1, y1, font.getGlyphsBuilder().getWidth(name, fontSize) + contextMenuStringOffsetX,
                                    elementHeight, name, font, fontSize, stringOffsetX, stringOffsetY, StringOffsetType.DEFAULT,
                                    RunnableUtils.EMPTY_RUNNABLE);
                    createEntryButton.setLeftReleaseRunnable(() -> inspectionPanel.add(createEntry()));

                    y1 += elementHeight;

                    name = "Create Object";
                    Button createEffectButton =
                            new Button(x1, y1, font.getGlyphsBuilder().getWidth(name, fontSize) + contextMenuStringOffsetX,
                                    elementHeight, name, font, fontSize, stringOffsetX, stringOffsetY, StringOffsetType.DEFAULT,
                                    RunnableUtils.EMPTY_RUNNABLE);
                    createEffectButton.setLeftReleaseRunnable(() -> inspectionPanel.add(createObject()));

                    Client.get().getGuiManager().openContextMenu(
                            setupContextMenuButton(createEntryButton), setupContextMenuButton(createEffectButton));
                }));

        int x = 0;
        int y = -elementHeight;
        inspectionPanel.addBottomButton(x, y, "Save All", this::saveAll);
        y -= elementHeight;
        inspectionPanel.addBottomButton(x, y, "Add", () -> inspectionPanel.add(createObject()));
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
                    inspectionPanel.add(createEntry(fileName, properties));
                }
            }
        });

        inspectionPanel.sortFolders();
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (key == KEY_ESCAPE) {
            Client.get().closeGui();
        }

        return input;
    }

    @Override
    public boolean mouseMove(float x, float y) {
        boolean mouseMove = false;
        for (int i = 0; i < guiObjects.size(); i++) {
            if (guiObjects.get(i).mouseMove(x, y)) {
                mouseMove = true;
            }
        }

        return mouseMove;
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
                inspectionEntry = createEntry();
                inspectionEntry.setName(path);
                if (parent == null) {
                    inspectionPanel.add(inspectionEntry);
                } else {
                    parent.add(inspectionEntry);
                }
            }

            parent = inspectionEntry;
        }

        return parent;
    }

    private void addToEntry(InspectionEntry<PROPERTIES_TYPE> parent, PROPERTIES_TYPE properties, String name) {
        List<GuiObject> guiObjects = parent.getGuiObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            InspectionEntry<PROPERTIES_TYPE> inspectionEntry = (InspectionEntry<PROPERTIES_TYPE>) guiObjects.get(i);
            if (inspectionEntry.getName().equals(name)) {
                inspectionEntry.addComponent(properties);
                entries.add(inspectionEntry);
                return;
            }
        }

        parent.add(createEntry(name, properties));
    }

    private InspectionEntry<PROPERTIES_TYPE> createObject() {
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
        InspectionEntry<PROPERTIES_TYPE> entry = createEntry(name);
        entry.addComponent(properties);
        entries.add(entry);
        return entry;
    }

    private InspectionEntry<PROPERTIES_TYPE> createEntry(String name) {
        ScrollPane scrollPane = inspectionPanel.getScrollPane();
        InspectionEntry<PROPERTIES_TYPE> entry = new InspectionEntry<>(inspectionPanel, scrollPane.getWidth() - scrollPane.getScrollWidth(),
                elementHeight, name, font, fontSize, stringOffsetY);
        entry.setRightClickRunnable(() -> {
            Vector2f mousePos = Engine.mouse.getPosition();
            int x1 = (int) mousePos.x;
            int y1 = (int) mousePos.y;
            String addString = "Create Entry";
            Button createEntryButton = new Button(x1, y1,
                    font.getGlyphsBuilder().getWidth(addString, fontSize) + contextMenuStringOffsetX,
                    elementHeight, addString, font, fontSize, stringOffsetX, stringOffsetY, StringOffsetType.DEFAULT,
                    () -> {
                        InspectionEntry<PROPERTIES_TYPE> childEntry = createEntry();
                        entry.add(childEntry);
                        entry.tryMaximize();
                    }
            );

            y1 += elementHeight;

            addString = "Create Object";
            Button createEffectButton = new Button(x1, y1,
                    font.getGlyphsBuilder().getWidth(addString, fontSize) + contextMenuStringOffsetX,
                    elementHeight, addString, font, fontSize, stringOffsetX, stringOffsetY, StringOffsetType.DEFAULT,
                    () -> {
                        InspectionEntry<PROPERTIES_TYPE> inspectionHolder = createEntry();
                        entry.add(inspectionHolder);
                        entry.tryMaximize();
                    }
            );
            y1 += elementHeight;

            addString = "Remove";
            Button removeButton = new Button(x1, y1,
                    font.getGlyphsBuilder().getWidth(addString, fontSize) + contextMenuStringOffsetX,
                    elementHeight, addString, font, fontSize, stringOffsetX, stringOffsetY, StringOffsetType.DEFAULT,
                    () -> remove(entry)
            );

            Client.get().getGuiManager().openContextMenu(setupContextMenuButton(createEntryButton),
                    setupContextMenuButton(createEffectButton),
                    setupContextMenuButton(removeButton));
        });
        return entry;
    }

    private InspectionEntry<PROPERTIES_TYPE> createEntry() {
        return createEntry("Entry");
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
        GuiObject parent = entry.getParent();
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
            properties.setTreeIndex(entry.getParent().getGuiObjects().indexOf(entry));
            String fileName = editorPath.isEmpty() ? entry.getName() : editorPath + "/" + entry.getName();

            CONFIG_TYPE config = converter.from(properties);
            if (configRegistry.get(fileName) == null) {
                configRegistry.add(editorPath, fileName, config);
            }
            Path folder = configRegistry.getFolder();
            Path path = folder.resolve(fileName + ".json");
            folder.toFile().mkdirs();
            ConfigLoader.save(path, config, configClass);
            log.info("Config {} successfully saved", fileName);
        }
    }

    private void remove(InspectionEntry<PROPERTIES_TYPE> entry) {
        inspectionPanel.removeEntry(entry);
        remove(propertiesPanel);
        selectedEntry = null;
        entries.remove(entry);
        PROPERTIES_TYPE properties = entry.getComponentByType(propertiesClass);
        if (properties != null) {
            configRegistry.remove(properties.getFullPath());
        }
    }

    private void selectEntry(InspectionEntry<PROPERTIES_TYPE> entry) {
        if (selectedEntry != null) {
            propertiesPanel.applyProperties();
        }

        selectedEntry = entry;

        if (entry == null) {
            remove(propertiesPanel);
            onEntryDeselected();
            return;
        }

        addIfAbsent(propertiesPanel);

        propertiesPanel.open(() -> save(entry), () -> remove(entry));
        onEntrySelected(entry);
    }

    protected abstract void onEntrySelected(InspectionEntry<PROPERTIES_TYPE> entry);

    protected void onEntryDeselected() {}

    public void switchPolygonEditMode(PolygonProperty polygonProperty) {}

    @Override
    public void clear() {
        super.clear();
        ClientSettings.CAMERA_FOLLOW_PLAYER.setValue(prevFollowCameraOptionValue);
    }
}
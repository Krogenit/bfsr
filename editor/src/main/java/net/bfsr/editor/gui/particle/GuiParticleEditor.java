package net.bfsr.editor.gui.particle;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Core;
import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectConfig;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.renderer.Render;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.config.ConfigLoader;
import net.bfsr.editor.ConfigurableGameObject;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.gui.control.Pausable;
import net.bfsr.editor.gui.control.PauseButton;
import net.bfsr.editor.gui.control.PlayButton;
import net.bfsr.editor.gui.control.Playble;
import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.editor.gui.inspection.InspectionPanel;
import net.bfsr.editor.gui.property.PropertiesPanel;
import net.bfsr.editor.particle.ParticleEffectConverter;
import net.bfsr.editor.particle.ParticleEffectProperties;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.object.AbstractGuiObject;
import net.bfsr.engine.gui.object.GuiObjectWithSubObjects;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.entity.GameObject;
import org.joml.Vector2f;
import org.mapstruct.factory.Mappers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.bfsr.editor.gui.ColorScheme.*;
import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

@Log4j2
public class GuiParticleEditor extends GuiEditor implements Playble, Pausable {
    private final Core core = Core.get();
    private InspectionEntry<ParticleEffectProperties> selectedEntry;
    private final ConfigurableGameObject gameObject = new ConfigurableGameObject();
    private final GameObject textureObject = new GameObject();
    private final Render<GameObject> testRender = new Render<>(textureObject) {
        @Override
        public void renderAlpha() {
            if (particleEffect != null && playing && texture != null) {
                Vector2f position = object.getPosition();
                Vector2f scale = object.getSize();
                spriteRenderer.add(position.x, position.y, scale.x, scale.y, color.x, color.y, color.z, color.w, texture,
                        BufferType.ENTITIES_ALPHA);
            }
        }
    };

    private final int leftPanelWidth = 300;
    private final int topPanelHeight = 28;
    private final int propertiesContainerWidth = 450;
    private final int elementHeight = 20;
    private final int fontSize = 13;
    private final int stringXOffset = 4;
    private final int stringYOffset = 3;
    private final FontType fontType = FontType.CONSOLA;
    private boolean playing;
    private final List<InspectionEntry<ParticleEffectProperties>> particleEffects = new ArrayList<>();
    private final int contextMenuStringXOffset = 8;
    private final InspectionPanel<ParticleEffectProperties> inspectionPanel = new InspectionPanel<>(
            this, "Particle Effects", leftPanelWidth, fontType, fontSize, stringYOffset
    );
    private final PropertiesPanel propertiesPanel = new PropertiesPanel(
            this, propertiesContainerWidth, fontType, fontSize, stringXOffset, stringYOffset, contextMenuStringXOffset
    );
    private final SpawnAccumulator spawnAccumulator = new SpawnAccumulator();
    private ParticleEffect particleEffect;
    private final ParticleEffectConverter converter = Mappers.getMapper(ParticleEffectConverter.class);

    @Override
    protected void initElements() {
        int x = 0;
        int y = 0;

        int controlButtonsSize = 28;

        registerGuiObject(setupButtonColors(new PlayButton(this, controlButtonsSize, controlButtonsSize)).atTop(x, y));
        x += controlButtonsSize;
        registerGuiObject(setupButtonColors(new PauseButton(this, controlButtonsSize, controlButtonsSize)).atTop(x, y));

        x = 0;
        y = 0;

        initInspectionPanel(x, y);
        propertiesPanel.initElements();
        initGameObject();
        initParticleEffects();
        Core.get().getRenderManager().addRender(testRender);
    }

    private void initGameObject() {
        gameObject.setDefaultValues();
        gameObject.init();
    }

    private void initInspectionPanel(int x, int y) {
        inspectionPanel.setRightClickSupplier(() -> {
            if (!inspectionPanel.isMouseHover()) return false;
            Vector2f mousePos = Engine.mouse.getPosition();
            int x1 = (int) mousePos.x;
            int y1 = (int) mousePos.y;

            String name = "Create Entry";
            Button createEntryButton =
                    new Button(null, x1, y1, fontType.getStringCache().getStringWidth(name, fontSize) + contextMenuStringXOffset,
                            elementHeight, name, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT,
                            RunnableUtils.EMPTY_RUNNABLE);
            createEntryButton.setOnMouseClickRunnable(() -> {
                InspectionEntry<ParticleEffectProperties> entry = inspectionPanel.createEntry();
                inspectionPanel.addSubObject(entry);
                inspectionPanel.updatePositions();
            });

            y1 += elementHeight;

            name = "Create Effect";
            Button createEffectButton =
                    new Button(null, x1, y1, fontType.getStringCache().getStringWidth(name, fontSize) + contextMenuStringXOffset,
                            elementHeight, name, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT,
                            RunnableUtils.EMPTY_RUNNABLE);
            createEffectButton.setOnMouseClickRunnable(() -> {
                InspectionEntry<ParticleEffectProperties> particleEffect = createParticleEffectEntry();
                inspectionPanel.addSubObject(particleEffect);
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
                        InspectionEntry<ParticleEffectProperties> childEntry = inspectionPanel.createEntry();
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
                        InspectionEntry<ParticleEffectProperties> inspectionHolder = createParticleEffectEntry();
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
        inspectionPanel.initElements(x, y, this::saveAll, this::createParticleEffectEntry);
    }

    private void initParticleEffects() {
        List<ParticleEffectConfig> allEffects = ParticleEffectsRegistry.INSTANCE.getAllConfigs();
        allEffects.stream().sorted(Comparator.comparingInt(ParticleEffectConfig::getTreeIndex)).forEach(particleEffect -> {
            String editorPath = particleEffect.getEditorPath();
            ParticleEffectProperties particleEffectProperties = converter.to(particleEffect);

            if (editorPath != null && !editorPath.isEmpty()) {
                addParticleEffectToEntry(buildEntryPath(editorPath), particleEffectProperties);
            } else {
                InspectionEntry<ParticleEffectProperties> entry = inspectionPanel.findEntry(particleEffect.getName());
                if (entry != null) {
                    entry.addComponent(particleEffectProperties);
                } else {
                    inspectionPanel.addSubObject(createParticleEffectEntry(particleEffectProperties));
                }
            }
        });

        inspectionPanel.sortFolders();
        inspectionPanel.updatePositions();
    }

    private InspectionEntry<ParticleEffectProperties> buildEntryPath(String editorPath) {
        InspectionEntry<ParticleEffectProperties> parent = null;
        String[] strings = editorPath.split("/");
        for (int i = 0; i < strings.length; i++) {
            String path = strings[i];
            InspectionEntry<ParticleEffectProperties> inspectionEntry;

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

    private void addParticleEffectToEntry(InspectionEntry<ParticleEffectProperties> parent,
                                          ParticleEffectProperties particleEffect) {
        boolean entryNotFound = true;
        List<AbstractGuiObject> subObjects = parent.getSubObjects();
        for (int i = 0; i < subObjects.size(); i++) {
            InspectionEntry<ParticleEffectProperties> inspectionEntry =
                    (InspectionEntry<ParticleEffectProperties>) subObjects.get(i);
            if (inspectionEntry.getName().equals(particleEffect.getName())) {
                entryNotFound = false;
                inspectionEntry.addComponent(particleEffect);
                particleEffects.add(inspectionEntry);
                break;
            }
        }

        if (entryNotFound) {
            parent.addSubObject(createParticleEffectEntry(particleEffect));
        }
    }

    private InspectionEntry<ParticleEffectProperties> createParticleEffectEntry() {
        ParticleEffectProperties particleEffect = new ParticleEffectProperties();
        particleEffect.setDefaultValues();
        return createParticleEffectEntry(particleEffect);
    }

    private InspectionEntry<ParticleEffectProperties> createParticleEffectEntry(ParticleEffectProperties particleEffect) {
        InspectionEntry<ParticleEffectProperties> entry = inspectionPanel.createEntry(particleEffect.getName(), particleEffect);
        particleEffects.add(entry);
        return entry;
    }

    private void saveAll() {
        try {
            for (int i = 0; i < particleEffects.size(); i++) {
                save(particleEffects.get(i));
            }
        } catch (Exception e) {
            log.error("Failed to save particle effects", e);
        }

        Path folder = ParticleEffectsRegistry.INSTANCE.getFolder();
        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String filePath = PathHelper.getFileNameWithoutExtension(
                            file.toString().replace(folder.toString(), "").replace(File.separator, "/").substring(1));
                    if (particleEffects.stream().noneMatch(inspectionHolder -> {
                        ParticleEffectProperties particleEffect =
                                inspectionHolder.getComponentByType(ParticleEffectProperties.class);
                        return particleEffect.getPath().equals(filePath);
                    })) {
                        Files.delete(file);
                        ParticleEffectsRegistry.INSTANCE.remove(filePath);
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
            log.error("Failed to delete particle effects", e);
        }
    }

    private void save(InspectionEntry<ParticleEffectProperties> buttonObjectHolder) {
        GuiObjectWithSubObjects parent = buttonObjectHolder.getParent();
        String editorPath = "";
        while (parent instanceof InspectionEntry<?> minimizableGuiObject) {
            editorPath = minimizableGuiObject.getName() + (editorPath.isEmpty() ? editorPath : "/" + editorPath);
            parent = minimizableGuiObject.getParent();
        }

        propertiesPanel.applyProperties();
        ParticleEffectProperties particleEffect = buttonObjectHolder.getComponentByType(ParticleEffectProperties.class);
        particleEffect.setEditorPath(editorPath);
        particleEffect.setTreeIndex(buttonObjectHolder.getParent().getSubObjects().indexOf(buttonObjectHolder));

        ParticleEffectConfig particleEffectConfig = converter.from(particleEffect);
        if (ParticleEffectsRegistry.INSTANCE.get(particleEffect.getPath()) == null) {
            ParticleEffectsRegistry.INSTANCE.add(particleEffectConfig);
        }
        Path folder = ParticleEffectsRegistry.INSTANCE.getFolder();
        Path effectFolder = folder.resolve(editorPath);
        effectFolder.toFile().mkdirs();
        ConfigLoader.save(effectFolder.resolve(particleEffect.getName() + ".json"), particleEffectConfig,
                ParticleEffectConfig.class);
    }

    private void remove(InspectionEntry<ParticleEffectProperties> buttonObjectHolder) {
        propertiesPanel.close();
        selectedEntry = null;
        particleEffects.remove(buttonObjectHolder);
        ParticleEffectProperties particleEffect = buttonObjectHolder.getComponentByType(ParticleEffectProperties.class);
        if (particleEffect != null) {
            ParticleEffectsRegistry.INSTANCE.remove(particleEffect.getPath());
        }
    }

    private void selectEntry(InspectionEntry<ParticleEffectProperties> buttonObjectHolder) {
        if (buttonObjectHolder == null) {
            if (selectedEntry != null) {
                propertiesPanel.applyProperties();
                selectedEntry = null;
            }

            propertiesPanel.close();

            return;
        }

        ParticleEffectProperties particleEffectProperties = buttonObjectHolder.getComponentByType(ParticleEffectProperties.class);

        if (selectedEntry != null) {
            propertiesPanel.applyProperties();
        }

        propertiesPanel.close();

        selectedEntry = buttonObjectHolder;

        propertiesPanel.open(() -> save(buttonObjectHolder), () -> remove(buttonObjectHolder));
        propertiesPanel.add(gameObject, gameObject.getName());

        if (particleEffectProperties != null) {
            propertiesPanel.add(particleEffectProperties, "Particle Effect");
            spawnAccumulator.resetTime();
            particleEffect = new ParticleEffect(converter.from(particleEffectProperties), 0);
            findChild(particleEffect, selectedEntry);
        } else {
            particleEffect = null;
        }
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

        if (selectedEntry != null && playing && !core.isPaused()) {
            gameObject.init();
            textureObject.setPosition(gameObject.getPosX(), gameObject.getPosY());
            textureObject.setSize(gameObject.getSizeX(), gameObject.getSizeY());
            testRender.setTexture(gameObject.getTexture());
            propertiesPanel.applyProperties();
            if (particleEffect != null) {
                particleEffect.applyConfig(converter.from(selectedEntry.getComponentByType(ParticleEffectProperties.class)));
                if (particleEffect.getSpawnOverTime() > 0 && particleEffect.getSpawnTime() == 0) {
                    spawnAccumulator.resetTime();
                }
                particleEffect.init();
                particleEffect.debug(gameObject.getPosX(), gameObject.getPosY(), gameObject.getSizeX(), gameObject.getSizeY(),
                        0.0f, 1.0f,
                        gameObject.getVelocityX(), gameObject.getVelocityY(), spawnAccumulator);
            }
        }
    }

    private void findChild(ParticleEffect particleEffect, InspectionEntry<ParticleEffectProperties> inspectionEntry) {
        particleEffect.clearChildEffects();
        List<AbstractGuiObject> subObjects = inspectionEntry.getSubObjects();
        for (int i = 0; i < subObjects.size(); i++) {
            InspectionEntry<ParticleEffectProperties> childEntry = (InspectionEntry<ParticleEffectProperties>) subObjects.get(i);
            ParticleEffectProperties componentByType = childEntry.getComponentByType(ParticleEffectProperties.class);
            if (componentByType != null) {
                ParticleEffect childParticleEffect = new ParticleEffect(converter.from(componentByType), 0);
                childParticleEffect.init();
                particleEffect.addChild(childParticleEffect);
                findChild(childParticleEffect, childEntry);
            }
        }
    }

    @Override
    public void updatePositions() {
        inspectionPanel.updatePositions();
        propertiesPanel.updatePropertiesPositions();
    }

    @Override
    public void render() {
        if (propertiesPanel.hasComponents()) {
            propertiesPanel.render();
            guiRenderer.add(inspectionPanel.getWidth(), 0, width, topPanelHeight, BACKGROUND_COLOR.x,
                    BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w);
        } else {
            guiRenderer.add(inspectionPanel.getWidth(), 0, width, topPanelHeight, BACKGROUND_COLOR.x,
                    BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w);
        }
        guiRenderer.add(0, 0, inspectionPanel.getWidth(), height, BACKGROUND_COLOR.x, BACKGROUND_COLOR.y,
                BACKGROUND_COLOR.z, BACKGROUND_COLOR.w);
        super.render();

        inspectionPanel.render();
    }

    @Override
    public void setPlaying(boolean value) {
        if (particleEffect != null) {
            if (playing) {
                particleEffect.clear();
            } else {
                spawnAccumulator.resetTime();
            }
        }

        if (value) {
            ClientSettings.CAMERA_FOLLOW_PLAYER.setValue(false);
            renderer.camera.getPosition().set(0);
        }

        this.playing = value;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void setPause(boolean value) {
        if (particleEffect != null && core.isPaused()) {
            spawnAccumulator.resetTime();
        }

        core.setPaused(value);
    }

    @Override
    public boolean isPaused() {
        return core.isPaused();
    }

    @Override
    public boolean isAllowCameraZoom() {
        return !propertiesPanel.isIntersectsWithMouse() && !inspectionPanel.isIntersectsWithMouse();
    }

    @Override
    public void clear() {
        super.clear();
        textureObject.setDead();
    }
}
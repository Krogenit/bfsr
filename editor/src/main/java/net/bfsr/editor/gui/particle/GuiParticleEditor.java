package net.bfsr.editor.gui.particle;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.TextureObject;
import net.bfsr.client.gui.AbstractGuiObject;
import net.bfsr.client.gui.GuiObjectWithSubObjects;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.particle.ParticleEffect;
import net.bfsr.client.particle.ParticleEffectsRegistry;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import net.bfsr.client.settings.Option;
import net.bfsr.client.util.PathHelper;
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
import net.bfsr.editor.world.EditorWorld;
import net.bfsr.util.RunnableUtils;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static net.bfsr.editor.gui.ColorScheme.*;

@Log4j2
public class GuiParticleEditor extends GuiEditor implements Playble, Pausable {
    private InspectionEntry<ParticleEffect> selectedEntry;
    private final ConfigurableGameObject gameObject = new ConfigurableGameObject();
    private final TextureObject textureObject = new TextureObject();

    private final int leftPanelWidth = 300;
    private final int topPanelHeight = 28;
    private final int propertiesContainerWidth = 450;
    private final int elementHeight = 20;
    private final int fontSize = 13;
    private final int stringXOffset = 4;
    private final int stringYOffset = 3;
    private final FontType fontType = FontType.CONSOLA;
    private boolean playing;
    private final List<InspectionEntry<ParticleEffect>> particleEffects = new ArrayList<>();
    private final int contextMenuStringXOffset = 8;
    private final InspectionPanel<ParticleEffect> inspectionPanel = new InspectionPanel<>(this, "Particle Effects", leftPanelWidth, fontType, fontSize, stringYOffset);
    private final PropertiesPanel propertiesPanel = new PropertiesPanel(this, propertiesContainerWidth, fontType, fontSize, stringXOffset, stringYOffset, contextMenuStringXOffset);

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
    }

    private void initGameObject() {
        gameObject.setDefaultValues();
        gameObject.init();
    }

    private void initInspectionPanel(int x, int y) {
        inspectionPanel.setRightClickSupplier(() -> {
            if (!inspectionPanel.isMouseHover()) return false;
            Vector2f mousePos = Mouse.getPosition();
            int x1 = (int) mousePos.x;
            int y1 = (int) mousePos.y;

            String name = "Create Entry";
            Button createEntryButton = new Button(null, x1, y1, fontType.getStringCache().getStringWidth(name, fontSize) + contextMenuStringXOffset, elementHeight,
                    name, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            createEntryButton.setOnMouseClickRunnable(() -> {
                InspectionEntry<ParticleEffect> entry = inspectionPanel.createEntry();
                inspectionPanel.addSubObject(entry);
                inspectionPanel.updatePositions();
            });

            y1 += elementHeight;

            name = "Create Effect";
            Button createEffectButton = new Button(null, x1, y1, fontType.getStringCache().getStringWidth(name, fontSize) + contextMenuStringXOffset, elementHeight,
                    name, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            createEffectButton.setOnMouseClickRunnable(() -> {
                InspectionEntry<ParticleEffect> particleEffect = createParticleEffectEntry();
                inspectionPanel.addSubObject(particleEffect);
                inspectionPanel.updatePositions();
            });

            openContextMenu(setupContextMenuButtonColors(createEntryButton), setupContextMenuButtonColors(createEffectButton));
            return true;
        });
        inspectionPanel.setEntryRightClickSupplier((inspectionEntry) -> {
            if (!inspectionEntry.isIntersectsWithMouse()) return false;
            Vector2f mousePos = Mouse.getPosition();
            int x1 = (int) mousePos.x;
            int y1 = (int) mousePos.y;
            String addString = "Create Entry";
            Button createEntryButton = new Button(null, x1, y1, fontType.getStringCache().getStringWidth(addString, fontSize) + contextMenuStringXOffset, elementHeight,
                    addString, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            createEntryButton.setOnMouseClickRunnable(() -> {
                InspectionEntry<ParticleEffect> childEntry = inspectionPanel.createEntry();
                inspectionEntry.addSubObject(childEntry);
                inspectionEntry.maximize();
                inspectionPanel.updatePositions();
            });

            y1 += elementHeight;

            addString = "Create Effect";
            Button createEffectButton = new Button(null, x1, y1, fontType.getStringCache().getStringWidth(addString, fontSize) + contextMenuStringXOffset, elementHeight,
                    addString, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            createEffectButton.setOnMouseClickRunnable(() -> {
                InspectionEntry<ParticleEffect> inspectionHolder = createParticleEffectEntry();
                inspectionEntry.addSubObject(inspectionHolder);
                inspectionEntry.maximize();
                inspectionPanel.updatePositions();
            });

            y1 += elementHeight;

            addString = "Remove";
            Button removeButton = new Button(null, x1, y1, fontType.getStringCache().getStringWidth(addString, fontSize) + contextMenuStringXOffset, elementHeight,
                    addString, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            removeButton.setOnMouseClickRunnable(() -> {
                GuiObjectWithSubObjects parent = inspectionEntry.getParent();
                parent.removeSubObject(inspectionEntry);
                remove(inspectionEntry);
                inspectionPanel.updatePositions();
            });

            openContextMenu(setupContextMenuButtonColors(createEntryButton), setupContextMenuButtonColors(createEffectButton), setupContextMenuButtonColors(removeButton));
            return true;
        });
        inspectionPanel.setOnSelectConsumer(this::selectEntry);
        inspectionPanel.initElements(x, y, this::saveAll, this::createParticleEffectEntry);
    }

    private void initParticleEffects() {
        Collection<ParticleEffect> allEffects = ParticleEffectsRegistry.INSTANCE.getAllEffects();
        allEffects.stream().sorted(Comparator.comparingInt(ParticleEffect::getTreeIndex)).forEach(particleEffect -> {
            String editorPath = particleEffect.getEditorPath();
            if (editorPath != null && !editorPath.isEmpty()) {
                addParticleEffectToEntry(buildEntryPath(editorPath), particleEffect);
            } else {
                inspectionPanel.addSubObject(createParticleEffectEntry(particleEffect));
            }
        });

        inspectionPanel.updatePositions();
    }

    private InspectionEntry<ParticleEffect> buildEntryPath(String editorPath) {
        InspectionEntry<ParticleEffect> parent = null;
        String[] strings = editorPath.split("/");
        for (int i = 0; i < strings.length; i++) {
            String path = strings[i];
            InspectionEntry<ParticleEffect> inspectionEntry;

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

    private void addParticleEffectToEntry(InspectionEntry<ParticleEffect> parent, ParticleEffect particleEffect) {
        boolean findEntry = false;
        List<AbstractGuiObject> subObjects = parent.getSubObjects();
        for (int i = 0; i < subObjects.size(); i++) {
            InspectionEntry<ParticleEffect> inspectionEntry = (InspectionEntry<ParticleEffect>) subObjects.get(i);
            if (inspectionEntry.getName().equals(particleEffect.getName())) {
                findEntry = true;
                inspectionEntry.addComponent(particleEffect);
                particleEffects.add(inspectionEntry);
                break;
            }
        }

        if (!findEntry) {
            parent.addSubObject(createParticleEffectEntry(particleEffect));
        }
    }

    private InspectionEntry<ParticleEffect> createParticleEffectEntry() {
        ParticleEffect particleEffect = new ParticleEffect();
        particleEffect.setDefaultValues();
        return createParticleEffectEntry(particleEffect);
    }

    private InspectionEntry<ParticleEffect> createParticleEffectEntry(ParticleEffect particleEffect) {
        InspectionEntry<ParticleEffect> entry = inspectionPanel.createEntry(particleEffect.getName(), particleEffect);
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

        File folder = ParticleEffectsRegistry.INSTANCE.getEffectsFolder();
        try {
            Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String filePath = PathHelper.getFileNameWithoutExtension(file.toString().replace(folder.getPath(), "").replace(File.separator, "/").substring(1));
                    if (particleEffects.stream().noneMatch(inspectionHolder -> {
                        ParticleEffect particleEffect = inspectionHolder.getComponentByType(ParticleEffect.class);
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

    private void save(InspectionEntry<ParticleEffect> buttonObjectHolder) {
        GuiObjectWithSubObjects parent = buttonObjectHolder.getParent();
        String editorPath = "";
        while (parent instanceof InspectionEntry<?> minimizableGuiObject) {
            editorPath = minimizableGuiObject.getName() + (editorPath.isEmpty() ? editorPath : "/" + editorPath);
            parent = minimizableGuiObject.getParent();
        }

        propertiesPanel.applyProperties();
        ParticleEffect particleEffect = buttonObjectHolder.getComponentByType(ParticleEffect.class);
        particleEffect.setEditorPath(editorPath);
        particleEffect.setTreeIndex(buttonObjectHolder.getParent().getSubObjects().indexOf(buttonObjectHolder));
        ParticleEffectsRegistry.INSTANCE.add(particleEffect);
        File folder = ParticleEffectsRegistry.INSTANCE.getEffectsFolder();
        File effectFolder = new File(folder, Paths.get(editorPath).toString());
        effectFolder.mkdirs();
        ConfigLoader.save(new File(effectFolder, particleEffect.getName() + ".json"), particleEffect, ParticleEffect.class);
    }

    private void remove(InspectionEntry<ParticleEffect> buttonObjectHolder) {
        propertiesPanel.close();
        selectedEntry = null;
        particleEffects.remove(buttonObjectHolder);
        ParticleEffect particleEffect = buttonObjectHolder.getComponentByType(ParticleEffect.class);
        if (particleEffect != null) {
            ParticleEffectsRegistry.INSTANCE.remove(particleEffect.getPath());
        }
    }

    private void selectEntry(InspectionEntry<ParticleEffect> buttonObjectHolder) {
        if (buttonObjectHolder == null) {
            if (selectedEntry != null) {
                propertiesPanel.applyProperties();
                selectedEntry = null;
            }

            propertiesPanel.close();

            return;
        }

        ParticleEffect particleEffect = buttonObjectHolder.getComponentByType(ParticleEffect.class);

        if (selectedEntry != null) {
            propertiesPanel.applyProperties();
        }

        propertiesPanel.close();

        selectedEntry = buttonObjectHolder;

        propertiesPanel.open(() -> save(buttonObjectHolder), () -> remove(buttonObjectHolder));
        propertiesPanel.add(gameObject, gameObject.getName());

        if (particleEffect != null) {
            propertiesPanel.add(particleEffect, "Particle Effect");
            particleEffect.create();
        }
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            Core.get().setCurrentGui(null);
        }
    }

    @Override
    public boolean onMouseLeftClick() {
        boolean result = super.onMouseLeftClick();
        inspectionPanel.onMouseLeftClick();
        return result;
    }

    @Override
    public void onMouseLeftRelease() {
        inspectionPanel.onMouseLeftRelease();
        super.onMouseLeftRelease();
    }

    @Override
    public void update() {
        super.update();
        inspectionPanel.update();

        if (selectedEntry != null && playing && !Core.get().isPaused()) {
            ((EditorWorld) Core.get().getWorld()).setTestObject(textureObject);
            gameObject.init();
            textureObject.setPosition(gameObject.getPosX(), gameObject.getPosY());
            textureObject.setScale(gameObject.getSizeX(), gameObject.getSizeY());
            textureObject.setTexture(gameObject.getTexture());
            propertiesPanel.applyProperties();
            ParticleEffect particleEffect = selectedEntry.getComponentByType(ParticleEffect.class);
            if (particleEffect != null) {
                findChild(particleEffect, selectedEntry);
                particleEffect.init();
                particleEffect.emit(gameObject.getPosX(), gameObject.getPosY(), gameObject.getSizeX(), gameObject.getSizeY(), gameObject.getVelocityX(), gameObject.getVelocityY());
            }
        }
    }

    private void findChild(ParticleEffect particleEffect, InspectionEntry<ParticleEffect> inspectionEntry) {
        particleEffect.clearChildEffects();
        List<AbstractGuiObject> subObjects = inspectionEntry.getSubObjects();
        for (int i = 0; i < subObjects.size(); i++) {
            InspectionEntry<ParticleEffect> childEntry = (InspectionEntry<ParticleEffect>) subObjects.get(i);
            ParticleEffect componentByType = childEntry.getComponentByType(ParticleEffect.class);
            if (componentByType != null) {
                particleEffect.addChild(componentByType);
                findChild(componentByType, childEntry);
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
            GUIRenderer.get().add(inspectionPanel.getWidth(), 0, width, topPanelHeight, BACKGROUND_COLOR.x, BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w);
        } else {
            GUIRenderer.get().add(inspectionPanel.getWidth(), 0, width, topPanelHeight, BACKGROUND_COLOR.x, BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w);
        }
        GUIRenderer.get().add(0, 0, inspectionPanel.getWidth(), height, BACKGROUND_COLOR.x, BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w);
        super.render();

        inspectionPanel.render();
    }

    @Override
    public void setPlaying(boolean value) {
        if (selectedEntry != null) {
            ParticleEffect particleEffect = selectedEntry.getComponentByType(ParticleEffect.class);
            if (particleEffect != null) {
                if (playing) {
                    particleEffect.clear();
                } else {
                    particleEffect.create();
                }
            }
        }

        if (value) {
            Option.CAMERA_FOLLOW_PLAYER.setValue(false);
            Core.get().getRenderer().getCamera().getPosition().set(0);
        }

        this.playing = value;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void setPause(boolean value) {
        if (selectedEntry != null && Core.get().isPaused()) {
            ParticleEffect particleEffect = selectedEntry.getComponentByType(ParticleEffect.class);
            if (particleEffect != null) {
                particleEffect.create();
            }
        }
        Core.get().setPaused(value);
    }

    @Override
    public boolean isPaused() {
        return Core.get().isPaused();
    }

    @Override
    public boolean isAllowCameraZoom() {
        return !propertiesPanel.isMouseHover() && !inspectionPanel.isMouseHover();
    }

    @Override
    public void clear() {
        super.clear();
        ((EditorWorld) Core.get().getWorld()).setTestObject(null);
    }
}
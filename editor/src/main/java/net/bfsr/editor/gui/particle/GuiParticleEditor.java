package net.bfsr.editor.gui.particle;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.entity.ship.ShipHumanSmall0;
import net.bfsr.client.gui.GuiObjectWithSubObjects;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.particle.ParticleEffect;
import net.bfsr.client.particle.ParticleEffectsRegistry;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import net.bfsr.client.util.PathHelper;
import net.bfsr.config.ConfigLoader;
import net.bfsr.editor.ConfigurableGameObject;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.gui.control.Pausable;
import net.bfsr.editor.gui.control.PauseButton;
import net.bfsr.editor.gui.control.PlayButton;
import net.bfsr.editor.gui.control.Playble;
import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.editor.gui.inspection.InspectionMinimizableGuiObject;
import net.bfsr.editor.gui.inspection.InspectionPanel;
import net.bfsr.editor.gui.property.PropertiesPanel;
import net.bfsr.util.RunnableUtils;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.bfsr.editor.gui.ColorScheme.*;

@Log4j2
public class GuiParticleEditor extends GuiEditor implements Playble, Pausable {
    private ParticleEffect selectedParticleEffect;
    private final ConfigurableGameObject gameObject = new ConfigurableGameObject();

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
    private final PropertiesPanel propertiesPanel = new PropertiesPanel(this, propertiesContainerWidth, fontType, fontSize, stringYOffset);
    private final Ship testShip = new ShipHumanSmall0(Core.get().getWorld(), -1, 0, 0, 0);

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

        gameObject.setDefaultValues();
        testShip.init();
        testShip.setSpawned();
        gameObject.setSizeX(testShip.getScale().x);
        gameObject.setSizeY(testShip.getScale().y);
        initParticleEffects();
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
        for (ParticleEffect particleEffect : allEffects) {
            InspectionEntry<ParticleEffect> particleEffectHolder = createParticleEffectEntry(particleEffect);

            String editorPath = particleEffect.getEditorPath();
            InspectionEntry<ParticleEffect> parent = null;
            if (editorPath != null && !editorPath.isEmpty()) {
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

                if (parent != null) {
                    parent.addSubObject(particleEffectHolder);
                }
            } else {
                inspectionPanel.addSubObject(particleEffectHolder);
            }
        }

        inspectionPanel.updatePositions();
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
        } catch (Exception e) {
            log.error("Failed to delete particle effects", e);
        }
    }

    private void save(InspectionEntry<ParticleEffect> buttonObjectHolder) {
        GuiObjectWithSubObjects parent = buttonObjectHolder.getParent();
        String editorPath = "";
        while (parent instanceof InspectionMinimizableGuiObject<?> minimizableGuiObject) {
            editorPath = minimizableGuiObject.getName() + (editorPath.isEmpty() ? editorPath : "/" + editorPath);
            parent = minimizableGuiObject.getParent();
        }

        propertiesPanel.applyProperties();
        ParticleEffect particleEffect = buttonObjectHolder.getComponentByType(ParticleEffect.class);
        particleEffect.setEditorPath(editorPath);
        ParticleEffectsRegistry.INSTANCE.add(particleEffect);
        File folder = ParticleEffectsRegistry.INSTANCE.getEffectsFolder();
        File effectFolder = new File(folder, Paths.get(editorPath).toString());
        effectFolder.mkdirs();
        ConfigLoader.save(new File(effectFolder, particleEffect.getName() + ".json"), particleEffect, ParticleEffect.class);
    }

    private void remove(InspectionEntry<ParticleEffect> buttonObjectHolder) {
        propertiesPanel.close();
        selectedParticleEffect = null;
        particleEffects.remove(buttonObjectHolder);
        ParticleEffect particleEffect = buttonObjectHolder.getComponentByType(ParticleEffect.class);
        if (particleEffect != null) {
            ParticleEffectsRegistry.INSTANCE.remove(particleEffect.getPath());
        }
    }

    private void selectEntry(InspectionEntry<ParticleEffect> buttonObjectHolder) {
        if (buttonObjectHolder == null) {
            if (selectedParticleEffect != null) {
                propertiesPanel.applyProperties();
                selectedParticleEffect = null;
            }

            propertiesPanel.close();

            return;
        }

        ParticleEffect particleEffect = buttonObjectHolder.getComponentByType(ParticleEffect.class);

        if (selectedParticleEffect != null) {
            propertiesPanel.applyProperties();
        }

        propertiesPanel.close();

        selectedParticleEffect = particleEffect;

        propertiesPanel.initElements(() -> save(buttonObjectHolder), () -> remove(buttonObjectHolder));
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
    public void update() {
        super.update();
        inspectionPanel.update();
        testShip.setLifeTime(0);
    }

    @Override
    public void updatePositions() {
        inspectionPanel.updatePositions();
        propertiesPanel.updatePropertiesPositions();
    }

    @Override
    public void render() {
        if (propertiesPanel.hasComponents()) {
            if (selectedParticleEffect != null && playing && !Core.get().isPaused()) {
                testShip.setPosition(gameObject.getPosX(), gameObject.getPosY());
                testShip.setScale(gameObject.getSizeX(), gameObject.getSizeY());
                testShip.setVelocity(gameObject.getVelocityX(), gameObject.getVelocityY());
                propertiesPanel.applyProperties();
                selectedParticleEffect.init();
                selectedParticleEffect.emit(gameObject.getPosX(), gameObject.getPosY(), gameObject.getSizeX(), gameObject.getSizeY(), gameObject.getVelocityX(), gameObject.getVelocityY());
            }
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
        if (selectedParticleEffect != null) {
            if (playing) {
                selectedParticleEffect.clear();
            } else {
                selectedParticleEffect.create();
            }
        }

        this.playing = value;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void setPause(boolean value) {
        if (selectedParticleEffect != null && Core.get().isPaused()) selectedParticleEffect.create();
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
        testShip.setDead();
    }
}
package net.bfsr.editor.gui.particle;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Core;
import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectConfig;
import net.bfsr.client.config.particle.ParticleEffectsRegistry;
import net.bfsr.client.particle.SpawnAccumulator;
import net.bfsr.client.renderer.Render;
import net.bfsr.editor.ConfigurableGameObject;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.gui.control.Pausable;
import net.bfsr.editor.gui.control.PauseButton;
import net.bfsr.editor.gui.control.PlayButton;
import net.bfsr.editor.gui.control.Playble;
import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.editor.object.particle.ParticleEffectConverter;
import net.bfsr.editor.object.particle.ParticleEffectProperties;
import net.bfsr.engine.gui.object.AbstractGuiObject;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.entity.GameObject;
import org.joml.Vector2f;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.BACKGROUND_COLOR;
import static net.bfsr.editor.gui.EditorTheme.setupButtonColors;

@Log4j2
public class GuiParticleEditor extends GuiEditor<ParticleEffectConfig, ParticleEffectProperties> implements Playble, Pausable {
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

    private final int topPanelHeight = 28;
    private boolean playing;

    private final SpawnAccumulator spawnAccumulator = new SpawnAccumulator();
    private ParticleEffect particleEffect;
    private final ParticleEffectConverter converter = Mappers.getMapper(ParticleEffectConverter.class);

    public GuiParticleEditor() {
        super("Particle Effects", ParticleEffectsRegistry.INSTANCE, Mappers.getMapper(ParticleEffectConverter.class),
                ParticleEffectConfig.class, ParticleEffectProperties.class);
    }

    @Override
    protected void initElements() {
        super.initElements();
        int x = 0;
        int y = 0;

        int controlButtonsSize = 28;

        registerGuiObject(setupButtonColors(new PlayButton(this, controlButtonsSize, controlButtonsSize)).atTop(x, y));
        x += controlButtonsSize;
        registerGuiObject(setupButtonColors(new PauseButton(this, controlButtonsSize, controlButtonsSize)).atTop(x, y));

        initGameObject();
        Core.get().getRenderManager().addRender(testRender);
    }

    private void initGameObject() {
        gameObject.setDefaultValues();
        gameObject.init();
    }

    @Override
    protected void onEntrySelected(InspectionEntry<ParticleEffectProperties> entry) {
        ParticleEffectProperties particleEffectProperties = entry.getComponentByType(ParticleEffectProperties.class);
        propertiesPanel.add(gameObject, "Game Object");

        if (particleEffectProperties != null) {
            propertiesPanel.add(particleEffectProperties, "Particle Effect");
            spawnAccumulator.resetTime();
            particleEffect = new ParticleEffect(converter.from(particleEffectProperties), "particle_effect", 0);
            findChild(particleEffect, selectedEntry);
        } else {
            particleEffect = null;
        }
    }

    private void findChild(ParticleEffect particleEffect, InspectionEntry<ParticleEffectProperties> entry) {
        particleEffect.clearChildEffects();
        List<AbstractGuiObject> subObjects = entry.getSubObjects();
        for (int i = 0; i < subObjects.size(); i++) {
            InspectionEntry<ParticleEffectProperties> childEntry = (InspectionEntry<ParticleEffectProperties>) subObjects.get(i);
            ParticleEffectProperties componentByType = childEntry.getComponentByType(ParticleEffectProperties.class);
            if (componentByType != null) {
                ParticleEffect childParticleEffect = new ParticleEffect(converter.from(componentByType), "particle_effect", 0);
                childParticleEffect.init();
                particleEffect.addChild(childParticleEffect);
                findChild(childParticleEffect, childEntry);
            }
        }
    }

    @Override
    public void update() {
        super.update();

        if (selectedEntry != null && playing && !core.isPaused()) {
            gameObject.init();
            textureObject.setPosition(gameObject.getPosX(), gameObject.getPosY());
            textureObject.setSize(gameObject.getSizeX(), gameObject.getSizeY());
            testRender.setTexture(gameObject.getTexture());
            propertiesPanel.applyProperties();
            if (particleEffect != null) {
                ParticleEffectProperties propertiesHolder = selectedEntry.getComponentByType(ParticleEffectProperties.class);
                particleEffect.applyConfig(converter.from(propertiesHolder));
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

    @Override
    public void render() {
        guiRenderer.add(inspectionPanel.getWidth(), 0, width, topPanelHeight, BACKGROUND_COLOR.x,
                BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w);

        super.render();
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
    public void clear() {
        super.clear();
        textureObject.setDead();
    }
}
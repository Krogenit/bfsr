package net.bfsr.editor.gui.particle;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.client.config.particle.ParticleEffect;
import net.bfsr.client.config.particle.ParticleEffectConfig;
import net.bfsr.editor.ConfigurableGameObject;
import net.bfsr.editor.gui.GuiEditor;
import net.bfsr.editor.gui.control.Pausable;
import net.bfsr.editor.gui.control.PauseButton;
import net.bfsr.editor.gui.control.PlayButton;
import net.bfsr.editor.gui.control.Playble;
import net.bfsr.editor.gui.inspection.InspectionEntry;
import net.bfsr.editor.object.particle.ParticleEffectConverter;
import net.bfsr.editor.object.particle.ParticleEffectProperties;
import net.bfsr.engine.entity.GameObject;
import net.bfsr.engine.entity.SpawnAccumulator;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.entity.Render;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.BACKGROUND_COLOR;

@Log4j2
public class GuiParticleEditor extends GuiEditor<ParticleEffectConfig, ParticleEffectProperties> implements Playble, Pausable {
    private final Client client = Client.get();
    private final ConfigurableGameObject gameObject = new ConfigurableGameObject();
    private final GameObject textureObject = new GameObject();
    private final Render testRender = new Render(textureObject) {
        @Override
        public void render() {
            if (particleEffect != null && playing) {
                spriteRenderer.addDrawCommand(id, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX, BufferType.ENTITIES_ALPHA);
            }
        }
    };
    private final int topPanelHeight = 28;
    private boolean playing;
    private final SpawnAccumulator spawnAccumulator = new SpawnAccumulator();
    private @Nullable ParticleEffect particleEffect;
    private final ParticleEffectConverter converter = Mappers.getMapper(ParticleEffectConverter.class);

    public GuiParticleEditor() {
        super("Particle Effects", Client.get().getParticleEffectsRegistry(), Mappers.getMapper(ParticleEffectConverter.class),
                ParticleEffectConfig.class, ParticleEffectProperties.class);

        int x = 0;
        int y = 0;
        int controlButtonsSize = 28;

        add(new Rectangle(width, topPanelHeight).atTopLeft(inspectionPanel.getWidth(), 0).setAllColors(BACKGROUND_COLOR.x,
                        BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w).setWidthFunction((integer, integer2) -> this.width)
                .setHeightFunction((integer, integer2) -> topPanelHeight));

        add(new PlayButton(this, controlButtonsSize, controlButtonsSize).atTop(x, y));
        x += controlButtonsSize;
        add(new PauseButton(this, controlButtonsSize, controlButtonsSize).atTop(x, y));

        gameObject.setDefaultValues();
        gameObject.init();

        testRender.init();
        client.getEntityRenderer().addRender(testRender);

        addInspectionPanel();
    }

    @Override
    protected void onEntrySelected(InspectionEntry<ParticleEffectProperties> entry) {
        ParticleEffectProperties particleEffectProperties = entry.getComponentByType(ParticleEffectProperties.class);
        propertiesPanel.add(gameObject, "Game Object");

        if (particleEffectProperties != null) {
            propertiesPanel.add(particleEffectProperties, "Particle Effect");
            spawnAccumulator.resetTime();
            particleEffect = new ParticleEffect(converter.from(particleEffectProperties), "particle_effect", 0, 0,
                    client.getParticleManager());
            findChild(particleEffect, selectedEntry);
        } else {
            particleEffect = null;
        }
    }

    private void findChild(ParticleEffect particleEffect, InspectionEntry<ParticleEffectProperties> entry) {
        particleEffect.clearChildEffects();
        List<GuiObject> guiObjects = entry.getGuiObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            InspectionEntry<ParticleEffectProperties> childEntry = (InspectionEntry<ParticleEffectProperties>) guiObjects.get(i);
            ParticleEffectProperties componentByType = childEntry.getComponentByType(ParticleEffectProperties.class);
            if (componentByType != null) {
                ParticleEffect childParticleEffect = new ParticleEffect(converter.from(componentByType), "particle_effect", 0, 0,
                        client.getParticleManager());
                childParticleEffect.init();
                particleEffect.addChild(childParticleEffect);
                findChild(childParticleEffect, childEntry);
            }
        }
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);

        if (selectedEntry != null && playing && !client.isPaused()) {
            gameObject.init();
            textureObject.setPosition(gameObject.getPosX(), gameObject.getPosY());
            textureObject.setSize(gameObject.getSizeX(), gameObject.getSizeY());
            testRender.setTexture(gameObject.getTexture());
            testRender.setSize(gameObject.getSizeX(), gameObject.getSizeY());
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
        if (particleEffect != null && client.isPaused()) {
            spawnAccumulator.resetTime();
        }

        client.setPaused(value);
    }

    @Override
    public boolean isPaused() {
        return client.isPaused();
    }

    @Override
    public void remove() {
        super.remove();
        textureObject.setDead();
    }
}
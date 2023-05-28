package net.bfsr.editor.particle;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.ConfigurableSound;
import net.bfsr.editor.gui.property.PropertyGuiElementType;
import net.bfsr.editor.property.PropertiesHolder;
import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.event.ChangeNameEventListener;
import net.bfsr.engine.renderer.particle.RenderLayer;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.PathHelper;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ParticleEffectProperties implements PropertiesHolder {
    private String name;
    @Property(elementType = PropertyGuiElementType.ARRAY, arrayElementType = PropertyGuiElementType.FILE_SELECTOR,
            arrayElementName = "assets/client/texture")
    private List<String> texturePaths;
    @Property
    private float spawnOverTime;
    @Property(name = "spawnCount", fieldsAmount = 2)
    private int minSpawnCount, maxSpawnCount;
    @Property(name = "position", fieldsAmount = 4)
    private float minPosX, minPosY, maxPosX, maxPosY;
    @Property(name = "velocity", fieldsAmount = 4)
    private float minVelocityX, minVelocityY, maxVelocityX, maxVelocityY;
    @Property(name = "angle", fieldsAmount = 2)
    private float minAngle, maxAngle;
    @Property(name = "angularVelocity", fieldsAmount = 2)
    private float minAngularVelocity, maxAngularVelocity;
    @Property(name = "size", fieldsAmount = 4)
    private float minSizeX, minSizeY, maxSizeX, maxSizeY;
    @Property(name = "sizeVelocity", fieldsAmount = 2)
    private float minSizeVelocity, maxSizeVelocity;
    @Property(name = "color", fieldsAmount = 4)
    private float r, g, b, a;
    @Property(name = "alphaVelocity", fieldsAmount = 2)
    private float minAlphaVelocity, maxAlphaVelocity;
    @Property(elementType = PropertyGuiElementType.CHECK_BOX)
    private boolean isAlphaFromZero;
    @Property(elementType = PropertyGuiElementType.COMBO_BOX)
    private RenderLayer renderLayer;
    @Property(elementType = PropertyGuiElementType.ARRAY)
    private List<ConfigurableSound> soundEffects;
    @Property(name = "srcSizeMultiplayer", fieldsAmount = 2)
    private float sourceSizeXMultiplier, sourceSizeYMultiplier;
    @Property(name = "srcVelocityMultiplayer", fieldsAmount = 2)
    private float sourceVelocityXMultiplier, sourceVelocityYMultiplier;
    private String editorPath;
    private int treeIndex;

    @Override
    public void setDefaultValues() {
        setName("Particle Effect");
        texturePaths = new ArrayList<>();
        texturePaths.add(PathHelper.convertToLocalPath(TextureRegister.particleShipEngineBack.getPath()));
        minSpawnCount = maxSpawnCount = 1;
        setColor(1.0f, 1.0f, 1.0f, 1.0f);
        minAlphaVelocity = 0.5f;
        maxAlphaVelocity = 0.5f;
        minSizeX = 10.0f;
        minSizeY = 10.0f;
        maxSizeX = 10.0f;
        maxSizeY = 10.0f;
        renderLayer = RenderLayer.DEFAULT_ADDITIVE;
        soundEffects = new ArrayList<>();
        sourceSizeXMultiplier = 1.0f;
        sourceSizeYMultiplier = 1.0f;
        sourceVelocityXMultiplier = 1.0f;
        sourceVelocityYMultiplier = 1.0f;
    }

    public void setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public void registerChangeNameEventListener(ChangeNameEventListener listener) {

    }

    @Override
    public void clearListeners() {

    }

    public String getPath() {
        return editorPath != null ? editorPath.isEmpty() ? name : editorPath + "/" + name : "";
    }
}
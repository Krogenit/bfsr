package net.bfsr.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.engine.util.PathHelper;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.List;

@AllArgsConstructor
@Getter
public class ConfigData {
    private final String name;
    private final int dataIndex;

    protected Vector2[] convertVertices(Vector2fConfigurable[] configurableVertices) {
        Vector2[] vertices = new Vector2[configurableVertices.length];
        for (int i = 0; i < vertices.length; i++) {
            Vector2fConfigurable configurableVertex = configurableVertices[i];
            vertices[i] = new Vector2(configurableVertex.x(), configurableVertex.y());
        }

        return vertices;
    }

    protected Vector2f convert(Vector2fConfigurable size) {
        return new Vector2f(size.x(), size.y());
    }

    protected Vector4f convert(ColorConfigurable effectsColor) {
        return new Vector4f(effectsColor.r(), effectsColor.g(), effectsColor.b(), effectsColor.a());
    }

    protected SoundData[] convert(List<ConfigurableSound> configurableSounds) {
        return convert(configurableSounds.toArray(new ConfigurableSound[0]));
    }

    protected SoundData[] convert(ConfigurableSound[] configurableSounds) {
        SoundData[] sounds = new SoundData[configurableSounds.length];
        for (int i = 0; i < configurableSounds.length; i++) {
            ConfigurableSound configurableSound = configurableSounds[i];
            sounds[i] = new SoundData(PathHelper.convertPath(configurableSound.getPath()), configurableSound.getVolume());
        }

        return sounds;
    }
}
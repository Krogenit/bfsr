package net.bfsr.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dyn4j.geometry.Vector2;

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
}
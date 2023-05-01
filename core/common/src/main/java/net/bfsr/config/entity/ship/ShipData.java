package net.bfsr.config.entity.ship;

import clipper2.core.PathD;
import clipper2.core.PointD;
import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.config.Vector2fConfigurable;
import net.bfsr.util.PathHelper;
import net.bfsr.util.TimeUtils;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.decompose.SweepLine;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.file.Path;
import java.util.List;

@Getter
public class ShipData extends ConfigData {
    private static final SweepLine SWEEP_LINE = new SweepLine();

    private final Vector2f size;
    private final int destroyTimeInTicks;
    private final Path texture;
    private final Path damageTexture;
    private final Vector4f effectsColor;
    private final PathD contour;
    private final List<Convex> convexList;
    private final Vector2f[] weaponSlotPositions;

    public ShipData(ShipConfig shipConfig, int dataIndex) {
        super(shipConfig.name(), dataIndex);
        this.size = convert(shipConfig.size());
        this.destroyTimeInTicks = (int) (shipConfig.destroyTimeInSeconds() * TimeUtils.UPDATES_PER_SECOND);
        this.texture = PathHelper.convertPath(shipConfig.texture());
        this.damageTexture = PathHelper.convertPath(shipConfig.damageTexture());
        this.effectsColor = convert(shipConfig.effectsColor());

        Vector2fConfigurable[] vertices = shipConfig.vertices();
        this.contour = new PathD(vertices.length);
        for (int i = 0; i < vertices.length; i++) {
            Vector2fConfigurable vertex = vertices[i];
            this.contour.add(new PointD(vertex.x(), vertex.y()));
        }

        this.convexList = SWEEP_LINE.decompose(convertVertices(vertices));

        Vector2fConfigurable[] slotPositions = shipConfig.weaponSlotPositions();
        this.weaponSlotPositions = new Vector2f[slotPositions.length];
        for (int i = 0; i < slotPositions.length; i++) {
            this.weaponSlotPositions[i] = convert(slotPositions[i]);
        }
    }
}
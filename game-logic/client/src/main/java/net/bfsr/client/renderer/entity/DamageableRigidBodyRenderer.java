package net.bfsr.client.renderer.entity;

import lombok.Getter;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.math.RotationHelper;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;

import java.nio.ByteBuffer;

@Getter
public class DamageableRigidBodyRenderer extends RigidBodyRender {
    private static final Vector4f CONTOUR_COLOR = new Vector4f(1.0f, 0.6f, 0.4f, 1.0f);
    private static final Vector4f CONTOUR_OFFSET_COLOR = new Vector4f(1.0f, 0.6f, 0.4f, 0.6f);

    protected final DamageMaskTexture maskTexture;
    protected final DamageableRigidBody damageableRigidBody;

    DamageableRigidBodyRenderer(AbstractTexture texture, DamageableRigidBody object, float r, float g, float b, float a) {
        super(texture, object, r, g, b, a);
        this.damageableRigidBody = object;

        DamageMask mask = object.getMask();
        maskTexture = new DamageMaskTexture(mask.getWidth(), mask.getHeight());
        maskTexture.createEmpty();
    }

    DamageableRigidBodyRenderer(AbstractTexture texture, DamageableRigidBody object) {
        this(texture, object, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void update() {
        super.update();
        maskTexture.updateEffects();
    }

    public void updateDamageMask(int x, int y, int width, int height, ByteBuffer byteBuffer) {
        maskTexture.upload(x, y, width, height, byteBuffer);
    }

    @Override
    public void renderAlpha() {
        float sin = rigidBody.getSin();
        float cos = rigidBody.getCos();
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, rigidBody.getX(), rigidBody.getY(), lastSin, lastCos,
                sin, cos, rigidBody.getSizeX(), rigidBody.getSizeY(), color.x, color.y, color.z, color.w, texture, maskTexture,
                BufferType.ENTITIES_ALPHA);
    }

    @Override
    public void renderDebug() {
        super.renderDebug();
        Polygon polygon = damageableRigidBody.getPolygon();
        Vector2f interpolatedPosition = new Vector2f(
                lastPosition.x + (object.getX() - lastPosition.x) * renderer.getInterpolation(),
                lastPosition.y + (object.getY() - lastPosition.y) * renderer.getInterpolation());
        float sin = lastSin + (rigidBody.getSin() - lastSin) * renderer.getInterpolation();
        float cos = lastCos + (rigidBody.getCos() - lastCos) * renderer.getInterpolation();

        CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
        renderRing(interpolatedPosition.x, interpolatedPosition.y, sin, cos, coordinateSequence);

        int numInteriorRing = polygon.getNumInteriorRing();
        for (int i = 0; i < numInteriorRing; i++) {
            renderRing(interpolatedPosition.x, interpolatedPosition.y, sin, cos,
                    polygon.getInteriorRingN(i).getCoordinateSequence());
        }

        renderRingOffset(interpolatedPosition.x, interpolatedPosition.y, sin, cos, polygon);
    }

    private void renderRing(float x, float y, float sin, float cos, CoordinateSequence coordinateSequence) {
        int size = coordinateSequence.size() - 1;
        debugRenderer.addCommand(size);
        for (int i1 = 0; i1 < size; i1++) {
            Coordinate pointD = coordinateSequence.getCoordinate(i1);
            debugRenderer.addVertex(x + RotationHelper.rotateX(sin, cos, (float) pointD.x, (float) pointD.y),
                    y + RotationHelper.rotateY(sin, cos, (float) pointD.x, (float) pointD.y), CONTOUR_COLOR);
        }
    }

    private void renderRingOffset(float x, float y, float sin, float cos, Polygon polygon) {
        Polygon polygon1 = (Polygon) BufferOp.bufferOp(polygon, DamageSystem.BUFFER_DISTANCE, DamageSystem.BUFFER_PARAMETERS);
        CoordinateSequence coordinates = polygon1.getExteriorRing().getCoordinateSequence();
        int size = coordinates.size() - 1;

        debugRenderer.addCommand(size);
        for (int i1 = 0; i1 < size; i1++) {
            Coordinate pointD = coordinates.getCoordinate(i1);
            debugRenderer.addVertex(x + RotationHelper.rotateX(sin, cos, (float) pointD.x, (float) pointD.y),
                    y + RotationHelper.rotateY(sin, cos, (float) pointD.x, (float) pointD.y), CONTOUR_OFFSET_COLOR);
        }
    }

    @Override
    public void clear() {
        maskTexture.delete();
    }
}
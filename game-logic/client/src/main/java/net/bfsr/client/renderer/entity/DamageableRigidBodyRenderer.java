package net.bfsr.client.renderer.entity;

import clipper2.core.*;
import clipper2.offset.ClipperOffset;
import clipper2.offset.EndType;
import clipper2.offset.JoinType;
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

import java.nio.ByteBuffer;

public class DamageableRigidBodyRenderer<T extends DamageableRigidBody<?>> extends RigidBodyRender<T> {
    private static final Vector4f CONTOUR_COLOR = new Vector4f(1.0f, 0.6f, 0.4f, 1.0f);
    private static final Vector4f CONTOUR_OFFSET_COLOR = new Vector4f(1.0f, 0.6f, 0.4f, 0.6f);

    @Getter
    private final DamageMaskTexture maskTexture;

    DamageableRigidBodyRenderer(AbstractTexture texture, T object) {
        this(texture, object, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    DamageableRigidBodyRenderer(AbstractTexture texture, T object, float r, float g, float b, float a) {
        super(texture, object, r, g, b, a);

        DamageMask mask = object.getMask();
        maskTexture = new DamageMaskTexture(mask.getWidth(), mask.getHeight());
        maskTexture.createEmpty();
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
        Vector2f position = object.getPosition();
        float sin = object.getSin();
        float cos = object.getCos();
        Vector2f scale = object.getSize();
        spriteRenderer.addToRenderPipeLineSinCos(lastPosition.x, lastPosition.y, position.x, position.y, lastSin, lastCos,
                sin, cos, scale.x, scale.y, color.x, color.y, color.z, color.w, texture, maskTexture, BufferType.ENTITIES_ALPHA);
    }

    @Override
    public void renderDebug() {
        super.renderDebug();
        PathsD contours = object.getContours();
        if (contours != null) {
            Vector2f position = object.getPosition();
            Vector2f interpolatedPosition = new Vector2f(
                    lastPosition.x + (position.x - lastPosition.x) * renderer.getInterpolation(),
                    lastPosition.y + (position.y - lastPosition.y) * renderer.getInterpolation());
            float sin = lastSin + (object.getSin() - lastSin) * renderer.getInterpolation();
            float cos = lastCos + (object.getCos() - lastCos) * renderer.getInterpolation();

            for (int i = 0; i < contours.size(); i++) {
                PathD pathD = contours.get(i);
                debugRenderer.addCommand(pathD.size());
                for (int i1 = 0; i1 < pathD.size(); i1++) {
                    PointD pointD = pathD.get(i1);
                    debugRenderer.addVertex(
                            interpolatedPosition.x + RotationHelper.rotateX(sin, cos, (float) pointD.x, (float) pointD.y),
                            interpolatedPosition.y + RotationHelper.rotateY(sin, cos, (float) pointD.x, (float) pointD.y),
                            CONTOUR_COLOR
                    );
                }

                if (i == 0) {
                    ClipperOffset clipperOffset = new ClipperOffset();
                    Path64 path64 = new Path64(pathD.size());
                    for (int i1 = 0; i1 < pathD.size(); i1++) {
                        path64.add(new Point64(pathD.get(i1), DamageSystem.SCALE));
                    }

                    clipperOffset.AddPath(path64, JoinType.Miter, EndType.Polygon);
                    Path64 solution = clipperOffset.Execute(0.25f * DamageSystem.SCALE).get(0);
                    debugRenderer.addCommand(solution.size());
                    for (int i2 = 0, path64Size = solution.size(); i2 < path64Size; i2++) {
                        Point64 pointD = solution.get(i2);
                        float x = (float) (pointD.x * DamageSystem.INV_SCALE);
                        float y = (float) (pointD.y * DamageSystem.INV_SCALE);
                        debugRenderer.addVertex(interpolatedPosition.x + RotationHelper.rotateX(sin, cos, x, y),
                                interpolatedPosition.y + RotationHelper.rotateY(sin, cos, x, y), CONTOUR_OFFSET_COLOR);
                    }
                }
            }
        }
    }

    @Override
    public void clear() {
        maskTexture.delete();
    }
}
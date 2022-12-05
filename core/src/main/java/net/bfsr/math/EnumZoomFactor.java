package net.bfsr.math;

import org.joml.Matrix4f;

import java.util.function.Supplier;

public enum EnumZoomFactor {
    Default(Transformation::getDefaultViewMatrix), Background(Transformation::getBackgroundViewMatrix), Gui(Transformation::getGuiViewMatrix);

    private final Supplier<Matrix4f> matrix4fSupplier;

    EnumZoomFactor(Supplier<Matrix4f> matrix4fSupplier) {
        this.matrix4fSupplier = matrix4fSupplier;
    }

    public Matrix4f getMatrix() {
        return matrix4fSupplier.get();
    }
}

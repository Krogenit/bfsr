#version 450
#include "common.glsl"

layout(location = 0) in vec4 in_PositionUV;

out Data {
    vec2 textureCoord;
} out_Data;

uniform mat4 modelMatrix;

layout(std140, binding = UBO_CAMERA_MATRIX) uniform ProjectionViewMatrix {
    mat4 matrix;
} ub_PrjectionViewMatrix;

void main() {
    gl_Position = ub_PrjectionViewMatrix.matrix * modelMatrix * vec4(in_PositionUV.xy, 0.0, 1.0);
    out_Data.textureCoord = in_PositionUV.zw;
}
#version 450
#include "common.glsl"

layout(location = 0) in vec2 in_Position;
layout(location = 1) in vec4 in_Color;

out Data {
    vec4 color;
} out_Data;

layout(std140, binding = UBO_CAMERA_MATRIX) uniform ProjectionViewMatrix {
    mat4 matrix;
} ub_PrjectionViewMatrix;

void main() {
    gl_Position = ub_PrjectionViewMatrix.matrix * vec4(in_Position.xy, 0.0, 1.0);
    out_Data.color = in_Color;
}
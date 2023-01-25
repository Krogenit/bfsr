#version 450
#include "common.glsl"

layout(location = 0) in vec4 in_PositionUV;

out Data {
    vec2 textureCoord;
} out_Data;

uniform mat4 modelMatrix;
uniform vec2 uv_scale;
uniform vec2 uv_offset;

layout(std140, binding = UBO_PROJECTION_MATRIX) uniform ProjectionMatrix {
    mat4 matrix;
} ub_PrjectionMatrix;

layout(std140, binding = UBO_VIEW_MATRIX) uniform ViewMatrix {
    mat4 matrix;
} ub_ViewMatrix;

void main() {
    gl_Position = ub_PrjectionMatrix.matrix * ub_ViewMatrix.matrix * modelMatrix * vec4(in_PositionUV.xy, 0.0, 1.0);
    out_Data.textureCoord = in_PositionUV.zw * uv_scale + uv_offset;
}
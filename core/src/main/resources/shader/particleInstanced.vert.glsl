#version 450
#include "common.glsl"

layout(location = 0) in vec4 in_PositionUV;
layout(location = 1) in mat4 in_ModelMatrix;
layout(location = 5) in vec4 in_Color;
layout(location = 6) in uvec2 in_TextureHandle;

out Data {
    vec2 textureCoords;
    vec4 color;
    flat uvec2 textureHandle;
} out_Data;

layout(std140, binding = UBO_PROJECTION_MATRIX) uniform ProjectionMatrix {
    mat4 matrix;
} ub_PrjectionMatrix;

layout(std140, binding = UBO_VIEW_MATRIX) uniform ViewMatrix {
    mat4 matrix;
} ub_ViewMatrix;

void main() {
    gl_Position = ub_PrjectionMatrix.matrix * ub_ViewMatrix.matrix * in_ModelMatrix * vec4(in_PositionUV.xy, 0.0, 1.0);
    out_Data.color = in_Color;
    out_Data.textureCoords = in_PositionUV.zw;
    out_Data.textureHandle = in_TextureHandle;
}
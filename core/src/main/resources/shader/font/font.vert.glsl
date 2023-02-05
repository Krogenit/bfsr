#version 450
#include "common.glsl"

layout(location = 0) in vec4 in_PositionUV;
layout(location = 1) in vec4 in_Color;

out Data {
    vec2 textureCoord;
    vec4 color;
    flat uvec2 textureHandle;
} out_Data;

layout(std430, binding = 0) readonly buffer Textures {
    uvec2 textureHandles[];
} sb_Texture;

layout(std140, binding = UBO_CAMERA_MATRIX) uniform ProjectionViewMatrix {
    mat4 matrix;
} ub_PrjectionViewMatrix;

uniform mat4 modelMatrix;

void main() {
    gl_Position = ub_PrjectionViewMatrix.matrix * modelMatrix * vec4(in_PositionUV.xy, 0.0, 1.0);
    out_Data.textureCoord = in_PositionUV.zw;
    out_Data.color = in_Color;
    out_Data.textureHandle = sb_Texture.textureHandles[gl_VertexID / 4];
}
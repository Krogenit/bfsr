#version 450
#include "common.glsl"

layout(location = 0) in vec4 in_PositionUV;

out Data {
    vec2 textureCoords;
    vec4 color;
    flat uvec2 textureHandle;
    flat bool useTexture;
} out_Data;

layout(std140, binding = UBO_CAMERA_MATRIX) uniform ProjectionViewMatrix {
    mat4 matrix;
} ub_PrjectionViewMatrix;

layout(std430, binding = 0) readonly buffer Materials {
    Material materials[];
} sb_Material;

void main() {
    gl_Position = ub_PrjectionViewMatrix.matrix * vec4(in_PositionUV.xy, 0.0, 1.0);
    Material material = sb_Material.materials[gl_VertexID / 4];
    out_Data.color = material.color;
    out_Data.textureCoords = in_PositionUV.zw;
    out_Data.textureHandle = material.textureHandle;
    out_Data.useTexture = material.useTexture;
}
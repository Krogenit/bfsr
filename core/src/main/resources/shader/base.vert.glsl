#version 450
#include "common.glsl"

layout(location = 0) in vec4 in_PositionUV;

out Data {
    vec2 textureCoords;
    vec4 color;
    flat uvec2 textureHandle;
} out_Data;

layout(std140, binding = UBO_CAMERA_MATRIX) uniform ProjectionViewMatrix {
    mat4 matrix;
} ub_PrjectionViewMatrix;

layout(std430, binding = 0) readonly buffer Materials {
    ColorAndTexture materials[];
} sb_Material;

void main() {
    gl_Position = ub_PrjectionViewMatrix.matrix * vec4(in_PositionUV.xy, 0.0, 1.0);
    out_Data.color = sb_Material.materials[gl_VertexID / 4].color;
    out_Data.textureCoords = in_PositionUV.zw;
    out_Data.textureHandle = sb_Material.materials[gl_VertexID / 4].textureHandle;
}
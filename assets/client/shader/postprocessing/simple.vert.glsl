#version 460
#extension GL_ARB_shading_language_include: enable
#include "/common/common.glsl"

layout (location = 0) in vec4 positionsUV;

layout (location = 0) out data {
    vec2 textureCoords;
    vec4 color;
    flat uvec2 textureHandle;
};

layout (std430, binding = SSBO_MODEL_DATA) readonly buffer modelDataBuffer {
    ModelData modelData[];
};

layout (std430, binding = SSBO_MATERIAL_DATA) readonly buffer materialsBuffer {
    Material materials[];
};

void main() {
    ModelData modelData = modelData[gl_BaseInstance];
    Material material = materials[gl_BaseInstance];

    float x = modelData.x;
    float y = modelData.y;

    float width = modelData.width;
    float height = modelData.height;

    gl_Position = vec4(positionsUV.x * width + x, positionsUV.y * height + y, 0.0, 0.5);

    textureCoords = positionsUV.zw;
    color = material.color;
    textureHandle = material.textureHandle;
}
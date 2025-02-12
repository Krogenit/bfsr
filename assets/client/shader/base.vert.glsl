#version 460
#extension GL_ARB_shading_language_include: enable
#include "/common/common.glsl"

layout (location = 0) in vec4 in_PositionUV;

layout (location = 0) out data {
    vec2 textureCoords;
    vec4 color;
    flat int materialType;
    flat uvec2 textureHandle;
    flat uvec2 maskTextureHandle;
    flat float fireAmount;
    flat float fireUVAnimation;
};

layout (std140, binding = UBO_PROJECTION_MATRIX) uniform projectionMatrixBuffer {
    mat4 projectionMatrix;
};
layout (std140, binding = UBO_INTERPOLATION) uniform interpolationBuffer {
    float interpolation;
};
layout (std140, binding = UBO_VIEW_DATA) uniform viewDataBuffer {
    ViewData viewData;
};

layout (std430, binding = SSBO_MODEL_DATA) readonly buffer modelDataBuffer {
    ModelData modelData[];
};
layout (std430, binding = SSBO_LAST_UPDATE_MODEL_DATA) readonly buffer lastUpdateModelDataBuffer {
    ModelData lastUpdateModelData[];
};
layout (std430, binding = SSBO_MATERIAL_DATA) readonly buffer materialsBuffer {
    Material materials[];
};
layout (std430, binding = SSBO_LAST_UPDATE_MATERIAL_DATA) readonly buffer lastUpdateMaterialsBuffer {
    LastUpdateMaterial lastUpdateMaterials[];
};

void main() {
    ModelData lastModelData = lastUpdateModelData[gl_BaseInstance];
    ModelData modelData = modelData[gl_BaseInstance];
    LastUpdateMaterial lastMaterialData = lastUpdateMaterials[gl_BaseInstance];
    Material material = materials[gl_BaseInstance];

    float x = lastModelData.x + (modelData.x - lastModelData.x) * interpolation;
    float y = lastModelData.y + (modelData.y - lastModelData.y) * interpolation;

    float cameraX = viewData.x;
    float cameraY = viewData.y;
    float cameraZoom = viewData.zoom;
    float zoomFactor = material.zoomFactor;

    if (zoomFactor < 1.0) {
        cameraX *= zoomFactor;
        cameraY *= zoomFactor;

        float maxMinDiff = MAX_CAMERA_ZOOM - MIN_CAMERA_ZOOM;
        cameraZoom = MIN_CAMERA_ZOOM + log(cameraZoom) * zoomFactor * 0.05f * maxMinDiff;
    }

    float width = (lastModelData.width + (modelData.width - lastModelData.width) * interpolation);
    float height = (lastModelData.height + (modelData.height - lastModelData.height) * interpolation);

    float sin = lastModelData.sin + (modelData.sin - lastModelData.sin) * interpolation;
    float cos = lastModelData.cos + (modelData.cos - lastModelData.cos) * interpolation;

    float vx = in_PositionUV.x * cos * width - in_PositionUV.y * sin * height + x + cameraX;
    float vy = in_PositionUV.y * cos * height + in_PositionUV.x * sin * width + y + cameraY;

    gl_Position = projectionMatrix * vec4(vx * cameraZoom, vy * cameraZoom, 0.0, 1.0);

    textureCoords = in_PositionUV.zw;
    color = material.color;
    materialType = material.materialType;
    textureHandle = material.textureHandle;
    maskTextureHandle = material.maskTextureHandle;
    fireAmount = material.fireAmount;
    fireUVAnimation = material.fireUVAnimation;
}
#version 460
#include "common.glsl"

layout (location = 0) in vec4 in_PositionUV;

out Data {
    vec2 textureCoords;
    vec4 color;
    flat uvec2 textureHandle;
    flat bool useTexture;
    flat bool useMask;
    flat uvec2 maskTextureHandle;
    flat float fireAmount;
    flat float fireUVAnimation;
    flat bool font;
} out_Data;

layout (std140, binding = UBO_PROJECTION_MATRIX) uniform ProjectionViewMatrix {
    mat4 matrix;
} ub_PrjectionViewMatrix;

layout (std140, binding = UBO_INTERPOLATION) uniform Interpolation {
    float interpolation;
} ub_Interpolation;

layout (std140, binding = UBO_VIEW_DATA) uniform ViewDataUniform {
    ViewData viewData;
} ub_ViewData;

layout (std430, binding = 0) readonly buffer ModelDatas {
    ModelData modelData[];
} sb_ModelData;

layout (std430, binding = 1) readonly buffer LastUpdateModelDatas {
    ModelData modelData[];
} sb_LastUpdateModelData;

layout (std430, binding = 2) readonly buffer Materials {
    Material materials[];
} sb_Material;

layout (std430, binding = 3) readonly buffer LastUpdateMaterials {
    LastUpdateMaterial materials[];
} sb_LastUpdateMaterial;

void main() {
    ModelData lastModelData = sb_LastUpdateModelData.modelData[gl_BaseInstance];
    ModelData modelData = sb_ModelData.modelData[gl_BaseInstance];
    LastUpdateMaterial lastMaterialData = sb_LastUpdateMaterial.materials[gl_BaseInstance];
    Material material = sb_Material.materials[gl_BaseInstance];

    float interpolation = ub_Interpolation.interpolation;

    float x = lastModelData.x + (modelData.x - lastModelData.x) * interpolation;
    float y = lastModelData.y + (modelData.y - lastModelData.y) * interpolation;

    float cameraX = ub_ViewData.viewData.x;
    float cameraY = ub_ViewData.viewData.y;
    float cameraZoom = ub_ViewData.viewData.zoom;
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

    gl_Position = ub_PrjectionViewMatrix.matrix * vec4(vx * cameraZoom, vy * cameraZoom, 0.0, 1.0);

    out_Data.textureCoords = in_PositionUV.zw;
    out_Data.color = material.color;
    out_Data.textureHandle = material.textureHandle;
    out_Data.useTexture = material.useTexture;
    out_Data.useMask = material.useMask;
    out_Data.maskTextureHandle = material.maskTextureHandle;
    out_Data.fireAmount = material.fireAmount;
    out_Data.fireUVAnimation = material.fireUVAnimation;
    out_Data.font = material.font;
}
#version 460
#extension GL_ARB_shading_language_include: enable
#include "/common/common.glsl"

layout (location = 0) in vec2 in_Position;
layout (location = 1) in vec4 in_Color;

layout (location = 0) out colorData {
    vec4 color;
};

layout (std140, binding = UBO_PROJECTION_MATRIX) uniform projectionMatrixBuffer {
    mat4 projectionMatrix;
};
layout (std140, binding = UBO_VIEW_DATA) uniform viewDataBuffer {
    ViewData viewData;
} ub_ViewData;

void main() {
    float cameraZoom = ub_ViewData.viewData.zoom;
    float x = in_Position.x + ub_ViewData.viewData.x;
    float y = in_Position.y + ub_ViewData.viewData.y;

    gl_Position = projectionMatrix * vec4(x * cameraZoom, y * cameraZoom, 0.0, 1.0);
    color = in_Color;
}
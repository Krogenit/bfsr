#version 450
#include "common.glsl"

layout (location = 0) in vec2 in_Position;
layout (location = 1) in vec4 in_Color;

out Data {
    vec4 color;
} out_Data;

layout (std140, binding = UBO_PROJECTION_MATRIX) uniform ProjectionViewMatrix {
    mat4 matrix;
} ub_PrjectionViewMatrix;

layout (std140, binding = UBO_VIEW_DATA) uniform ViewDataUniform {
    ViewData viewData;
} ub_ViewData;

void main() {
    float cameraZoom = ub_ViewData.viewData.zoom;
    float x = in_Position.x + ub_ViewData.viewData.x;
    float y = in_Position.y + ub_ViewData.viewData.y;

    gl_Position = ub_PrjectionViewMatrix.matrix * vec4(x * cameraZoom, y * cameraZoom, 0.0, 1.0);
    out_Data.color = in_Color;
}
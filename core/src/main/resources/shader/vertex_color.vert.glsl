#version 450
#extension GL_ARB_shading_language_include : enable
#include "common.glsl"

in layout(location=0)  vec2 pos;
in layout(location=1)  vec4 color;

layout(binding=USB_VIEW, std140) uniform viewBuffer {
    ViewData view;
};

out Data {
    vec4 color;
} OUT;

void main() {
    OUT.color = color;
    gl_Position = view.projectionMatrix * view.viewMatrix * view.modelMatrix * vec4(pos, 0.0, 1.0);
}
#version 450
#extension GL_ARB_shading_language_include : enable
#include "common.glsl"

in layout(location=0)  vec4 posAtTexCoord;
in layout(location=1)  vec4 color;

layout(binding=USB_VIEW, std140) uniform viewBuffer {
    ViewData view;
};

out Data {
    vec4 color;
    vec2 texCoord;
} OUT;

void main() {
    OUT.color = color;
    OUT.texCoord = posAtTexCoord.zw;
    gl_Position = view.projectionMatrix * view.viewMatrix * view.modelMatrix * vec4(posAtTexCoord.xy, 0.0, 1.0);
}
#version 450
#extension GL_ARB_shading_language_include : enable
#include "common.glsl"

in Data {
    vec4 color;
} IN;

layout(location=0, index=0) out vec4 out_Color;

void main() {
    out_Color = IN.color;
}

#version 450
#extension GL_ARB_shading_language_include : enable
#include "common.glsl"

layout(binding=0) uniform sampler2D texDiffuse;

in Data {
    vec4 color;
    vec2 texCoord;
} IN;

layout(location=0, index=0) out vec4 out_Color;

void main() {
    out_Color = texture(texDiffuse, IN.texCoord) * IN.color;
}

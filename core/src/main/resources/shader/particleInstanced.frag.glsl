#version 450
#extension GL_ARB_bindless_texture : enable

layout(location = 0) out vec4 out_Color;

in Data {
    vec2 textureCoords;
    vec4 color;
    flat uvec2 textureHandle;
} in_Data;

void main() {
    out_Color = texture(sampler2D(in_Data.textureHandle), in_Data.textureCoords) * in_Data.color;
}
#version 450
#extension GL_ARB_bindless_texture : enable

layout(location = 0) out vec4 out_Color;

in Data {
    vec2 textureCoords;
    vec4 color;
    flat uvec2 textureHandle;
    flat bool useTexture;
} in_Data;

void main() {
    out_Color = in_Data.useTexture ? texture(sampler2D(in_Data.textureHandle), in_Data.textureCoords) * in_Data.color : in_Data.color;
}
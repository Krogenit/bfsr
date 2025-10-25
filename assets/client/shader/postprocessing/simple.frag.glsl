#version 460
#extension GL_ARB_bindless_texture: enable

layout (location = 0) in data {
    vec2 textureCoords;
    vec4 color;
    flat uvec2 textureHandle;
};

layout (location = 0) out vec4 out_Color;

void main() {
    out_Color = color * texture(sampler2D(textureHandle), textureCoords);
}
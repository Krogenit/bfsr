#version 450

layout(location = 0) out vec4 out_Color;

in Data {
    vec2 textureCoord;
} in_Data;

uniform sampler2D textureOpaque;
uniform vec4 color;
uniform bool useTexture;

void main() {
    if (useTexture) {
        out_Color = texture(textureOpaque, in_Data.textureCoord) * color;
    } else {
        out_Color = color;
    }
}
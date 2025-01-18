#version 460

layout (location = 0) in colorData {
    vec4 color;
};

layout (location = 0) out vec4 out_Color;

void main() {
    out_Color = color;
}
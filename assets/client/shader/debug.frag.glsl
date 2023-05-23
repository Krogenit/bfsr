#version 450

layout(location = 0) out vec4 out_Color;

in Data {
    vec4 color;
} in_Data;

void main() {
    out_Color = in_Data.color;
}
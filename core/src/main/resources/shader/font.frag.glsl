#version 330

in vec2 textureCoords0;

out vec4 fragColor;

uniform vec4 color;
uniform sampler2D fontAtlas;

void main()
{
	fragColor = vec4(color.rgb, texture(fontAtlas, textureCoords0).a * color.a);
}
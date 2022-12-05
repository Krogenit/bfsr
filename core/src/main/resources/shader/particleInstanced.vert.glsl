#version 330

layout (location = 0) in vec2 position;
in mat4 modelViewMat;
in vec4 texOffsets;
in float blendFactor;
in vec4 colorIn;

out vec2 textureCoords0;
out vec2 textureCoords1;
out float blend;
out vec4 particleColor;

uniform mat4 orthoMat;
uniform float numberOfRows;

uniform bool animatedTexture;

void main()
{
    gl_Position = orthoMat * modelViewMat * vec4(position, 0.0, 1.0);
	vec2 textureCoords = position +  vec2(0.5, 0.5);
	textureCoords.y = 1.0 - textureCoords.y;
	particleColor = colorIn;
	
	if(animatedTexture) {
		vec2 textureCoordsNew = textureCoords / numberOfRows;
		textureCoords0 = textureCoordsNew + texOffsets.xy;
		textureCoords1 = textureCoordsNew + texOffsets.zw;
		blend = blendFactor;
	} else {
		textureCoords0 = textureCoords;
		textureCoords1 = textureCoords;
		blend = 0;
	}
	
}
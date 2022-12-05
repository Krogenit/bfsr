#version 330

layout (location = 0) in vec2 position;
layout (location = 1) in vec2 textureCoords;

out vec2 textureCoords0;
out vec2 textureCoords1;
out float blend;

uniform mat4 orthoMat;
uniform mat4 modelViewMat;

uniform vec4 texOffset;
uniform vec2 texCoordInfo;

uniform bool animatedTexture;

void main()
{
    gl_Position = orthoMat * modelViewMat * vec4(position, 0.0, 1.0);
	
	if(animatedTexture) {
		vec2 textureCoordsNew = textureCoords / texCoordInfo.x;
		textureCoords0 = textureCoordsNew + texOffset.xy;
		textureCoords1 = textureCoordsNew + texOffset.zw;
		blend = texCoordInfo.y;
	} else {
		textureCoords0 = textureCoords;
		textureCoords1 = textureCoords;
		blend = 0;
	}
	
}
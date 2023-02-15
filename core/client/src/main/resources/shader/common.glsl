struct Material {
    vec4 color;
    uvec2 textureHandle;
    bool useTexture;
    bool padding;
};

#define UBO_CAMERA_MATRIX 0
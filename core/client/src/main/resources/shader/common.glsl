struct Material {
    vec4 color;
    uvec2 textureHandle;
    bool useTexture;
    bool useMask;
    uvec2 maskTextureHandle;
    float fireAmount;
    float fireUVAnimation;
};

#define UBO_CAMERA_MATRIX 0
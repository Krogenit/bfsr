#define UBO_PROJECTION_MATRIX 0
#define UBO_INTERPOLATION 1
#define UBO_VIEW_DATA 2

#define MIN_CAMERA_ZOOM 2.0
#define MAX_CAMERA_ZOOM 30.0

struct Material {
    vec4 color;
    uvec2 textureHandle;
    bool useTexture;
    bool useMask;
    uvec2 maskTextureHandle;
    float fireAmount;
    float fireUVAnimation;
    bool font;
    float zoomFactor;
    bool padding1;
    bool padding2;
};

struct LastUpdateMaterial {
    vec4 color;
    float fireAmount;
    float fireUVAnimation;
    bool padding0;
    bool padding1;
};

struct ModelData {
    float x;
    float y;
    float sin;
    float cos;
    float width;
    float height;
};

struct ViewData {
    float x;
    float y;
    float zoom;
};
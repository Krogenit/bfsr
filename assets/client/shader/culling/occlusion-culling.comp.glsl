#version 460
#extension GL_ARB_shading_language_include: enable
#include "/common/common.glsl"
#include "/common/occlusion-culling-common.glsl"

layout (local_size_x = 1, local_size_y = 1, local_size_z = 1) in;

layout (std140, binding = UBO_VIEW_DATA) uniform viewDataBuffer {
    ViewData viewData;
};

layout (std430, binding = FRUSTUM_CULL_SSBO_MODEL_DATA) readonly buffer modelDataBuffer {
    ModelData modelData[];
};
layout (std430, binding = FRUSTUM_CULL_SSBO_MATERIAL_DATA) readonly buffer materialBuffer {
    Material materials[];
};
layout (std430, binding = FRUSTUM_CULL_SSBO_DRAW_COMMANDS) buffer drawCommandBuffer {
    int drawCommands[];
};

//uvec3	gl_NumWorkGroups	        number of work groups that have been dispatched set by glDispatchCompute()
//uvec3	gl_WorkGroupSize	        size of the work group (local size) operated on defined with layout
//uvec3	gl_WorkGroupID	            index of the work group currently being operated on
//uvec3	gl_LocalInvocationID	    index of the current work item in the work group
//uvec3	gl_GlobalInvocationID	    global index of the current work item (gl_WorkGroupID * gl_WorkGroupSize + gl_LocalInvocationID)
//uint	gl_LocalInvocationIndex	    1d index representation of gl_LocalInvocationID
//                                  (gl_LocalInvocationID.z * gl_WorkGroupSize.x * gl_WorkGroupSize.y +
//                                   gl_LocalInvocationID.y * gl_WorkGroupSize.x + gl_LocalInvocationID.x)

void main() {
    uint globalThreadID = gl_GlobalInvocationID.x;
    uint commandIndex = globalThreadID * COMMANDSTRIDE;
    int instanceId = drawCommands[commandIndex + 4];

    float zoom = viewData.zoom;

    Material material = materials[instanceId];

    ModelData modelData = modelData[instanceId];
    float x = modelData.x + viewData.x;
    float y = modelData.y + viewData.y;

    float invertedHalfZoom = 1.0 / zoom * 0.5;
    float viewHalfWidth = viewData.width * invertedHalfZoom;
    float viewHalfHeight = viewData.height * invertedHalfZoom;

    if (material.materialType == FONT) {
        float glyphWidth = modelData.width * zoom;
        float glyphHeight = modelData.height * zoom;
        if (x <= viewHalfWidth && y <= viewHalfHeight
        && x + glyphWidth >= -viewHalfWidth && y + glyphHeight >= -viewHalfHeight) {
            drawCommands[globalThreadID * COMMANDSTRIDE + 1] = 1;
        } else {
            drawCommands[globalThreadID * COMMANDSTRIDE + 1] = 0;
        }
    } else {
        float halfModelWidth = modelData.width * 0.5;
        float halfModelHeight = modelData.height * 0.5;
        if (x - halfModelWidth < viewHalfWidth && y - halfModelHeight < viewHalfHeight
        && x + halfModelWidth >= -viewHalfWidth && y + halfModelHeight >= -viewHalfHeight) {
            drawCommands[commandIndex + 1] = 1;
        } else {
            drawCommands[commandIndex + 1] = 0;
        }
    }
}

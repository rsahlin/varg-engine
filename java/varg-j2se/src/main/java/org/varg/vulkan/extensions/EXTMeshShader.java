package org.varg.vulkan.extensions;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.vulkan.Queue;

/**
 * VK_EXT_mesh_shader extension
 * 
 *
 */
public abstract class EXTMeshShader<Q extends Queue> {

    private final PhysicalDeviceMeshShaderFeaturesEXT meshShaderFeatures;
    private final PhysicalDeviceMeshShaderPropertiesEXT meshShaderProperties;

    protected EXTMeshShader(@NonNull PhysicalDeviceMeshShaderFeaturesEXT meshShaderFeatures, @NonNull PhysicalDeviceMeshShaderPropertiesEXT meshShaderProperties) {
        this.meshShaderFeatures = meshShaderFeatures;
        this.meshShaderProperties = meshShaderProperties;
    }

    public abstract void drawMeshTasksEXT(Q queue, int groupCountX, int groupCountY, int groupCountZ);

    public abstract void drawMeshTasksIndirectCountEXT(Q queue, long buffer, int offset, long countBuffer,
            int countBufferOffset, int maxDrawCount, int stride);

    public abstract void drawMeshTasksIndirectEXT(Q queue, long buffer, int offset, int drawCount, int stride);

    public final PhysicalDeviceMeshShaderFeaturesEXT getMeshShaderFeatures() {
        return meshShaderFeatures;
    }

    public final PhysicalDeviceMeshShaderPropertiesEXT getMeshShaderProperties() {
        return meshShaderProperties;
    }
}

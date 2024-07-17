package org.varg.lwjgl3.vulkan.extensions;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.lwjgl3.vulkan.LWJGLVulkan12Queue;
import org.varg.vulkan.extensions.EXTMeshShader;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderFeaturesEXT;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderPropertiesEXT;

public class LWJGL3EXTMeshShader extends EXTMeshShader<LWJGLVulkan12Queue> {

    public LWJGL3EXTMeshShader(@NonNull PhysicalDeviceMeshShaderFeaturesEXT meshShaderFeatures, @NonNull PhysicalDeviceMeshShaderPropertiesEXT meshShaderProperties) {
        super(meshShaderFeatures, meshShaderProperties);
    }

    @Override
    public void drawMeshTasksEXT(LWJGLVulkan12Queue queue, int groupCountX, int groupCountY, int groupCountZ) {
        org.lwjgl.vulkan.EXTMeshShader.vkCmdDrawMeshTasksEXT(queue.getCommandBuffer(), groupCountX, groupCountY, groupCountZ);
    }

    @Override
    public void drawMeshTasksIndirectCountEXT(LWJGLVulkan12Queue queue, long buffer, int offset, long countBuffer, int countBufferOffset, int maxDrawCount, int stride) {
        org.lwjgl.vulkan.EXTMeshShader.vkCmdDrawMeshTasksIndirectCountEXT(queue.getCommandBuffer(), buffer, offset, countBuffer, countBufferOffset, maxDrawCount, stride);
    }

    @Override
    public void drawMeshTasksIndirectEXT(LWJGLVulkan12Queue queue, long buffer, int offset, int drawCount, int stride) {
        org.lwjgl.vulkan.EXTMeshShader.vkCmdDrawMeshTasksIndirectEXT(queue.getCommandBuffer(), buffer, offset, drawCount, stride);
    }

}

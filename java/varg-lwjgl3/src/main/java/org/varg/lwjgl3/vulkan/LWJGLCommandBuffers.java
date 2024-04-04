
package org.varg.lwjgl3.vulkan;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.varg.vulkan.CommandBuffer;
import org.varg.vulkan.CommandBuffers;
import org.varg.vulkan.structs.QueueFamilyProperties;

class LWJGLCommandBuffers extends CommandBuffers<VkCommandBuffer> {

    public static class LWJGLCommandBuffer extends CommandBuffer<VkCommandBuffer> {
        LWJGLCommandBuffer(VkCommandBuffer commandBuffer) {
            super(commandBuffer);
        }
    }

    LWJGLCommandBuffers(long pool, QueueFamilyProperties family, CommandBuffer<VkCommandBuffer>[] commands) {
        super(pool, family, commands);
    }

}

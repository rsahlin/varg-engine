
package org.varg.lwjgl3.vulkan;

import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;

/**
 * LWJGL command pool for vulkan record commands.
 *
 */
class LWJGLCommandPool {

    CommandPool<VkRenderPassBeginInfo> renderPassBeginPool = new CommandPool<VkRenderPassBeginInfo>(
            VkRenderPassBeginInfo.class);
    CommandPool<VkCommandBufferBeginInfo> commandBufferBeginPool = new CommandPool<VkCommandBufferBeginInfo>(
            VkCommandBufferBeginInfo.class);
    CommandPool<VkImageMemoryBarrier.Buffer> imageMemoryBarrierPool =
            new BufferCommandPool<VkImageMemoryBarrier.Buffer>(
                    VkImageMemoryBarrier.class, 1);
    CommandPool<VkSubmitInfo> submitInfoPool = new CommandPool<VkSubmitInfo>(VkSubmitInfo.class);

    CommandPool<VkClearValue.Buffer> clearValuePool = new BufferCommandPool<VkClearValue.Buffer>(VkClearValue.class, 2);

    CommandPool<VkViewport.Buffer> viewportPool = new BufferCommandPool<VkViewport.Buffer>(VkViewport.class, 1);

    CommandPool<VkRect2D.Buffer> rect2DPool = new BufferCommandPool<VkRect2D.Buffer>(VkRect2D.class, 1);

    protected LWJGLCommandPool() {
    }

    VkRenderPassBeginInfo getRenderPassBegin() {
        return renderPassBeginPool.fetchCommand();
    }

    VkCommandBufferBeginInfo getCommandBufferBegin() {
        return commandBufferBeginPool.fetchCommand();
    }

    VkImageMemoryBarrier.Buffer getImageMemoryBarrier() {
        return imageMemoryBarrierPool.fetchCommand();
    }

    VkSubmitInfo getSubmit() {
        return submitInfoPool.fetchCommand();
    }

    VkClearValue.Buffer getClearValue() {
        return clearValuePool.fetchCommand();
    }

    VkViewport.Buffer getViewport() {
        return viewportPool.fetchCommand();
    }

    VkRect2D.Buffer getRect2D() {
        return rect2DPool.fetchCommand();
    }

    void releaseCommands() {
        renderPassBeginPool.releaseCommands();
        commandBufferBeginPool.releaseCommands();
        imageMemoryBarrierPool.releaseCommands();
        submitInfoPool.releaseCommands();
        clearValuePool.releaseCommands();
        viewportPool.releaseCommands();
        rect2DPool.releaseCommands();
    }

}

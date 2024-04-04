
package org.varg.vulkan;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.CommandBuffers.Category;
import org.varg.vulkan.Vulkan10.Filter;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.IndexType;
import org.varg.vulkan.Vulkan10.PipelineBindPoint;
import org.varg.vulkan.Vulkan10.PipelineStageFlagBit;
import org.varg.vulkan.descriptor.DescriptorSet;
import org.varg.vulkan.image.Image;
import org.varg.vulkan.image.ImageMemoryBarrier;
import org.varg.vulkan.image.ImageSubresourceLayers;
import org.varg.vulkan.image.ImageSubresourceRange;
import org.varg.vulkan.memory.ImageMemory;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.pipeline.Pipeline;
import org.varg.vulkan.pipeline.PipelineLayout;
import org.varg.vulkan.renderpass.RenderPassBeginInfo;
import org.varg.vulkan.structs.DependencyInfo;
import org.varg.vulkan.structs.PushConstantRange;
import org.varg.vulkan.structs.QueryPool;
import org.varg.vulkan.structs.Rect2D;
import org.varg.vulkan.structs.Semaphore;
import org.varg.vulkan.structs.TimelineSemaphore;
import org.varg.vulkan.structs.TimelineSemaphore.TimelineSemaphoreSubmitInfo;

/**
 * The vulkan queue handling - this is the high level functionality to record commands on a queue, execute and
 * submit them and wait for a queue to become idle.
 * 
 * Implementations of Queue shall take care of creation and descruction of platform command objects, such as
 * VkRenderPassBeginInfo
 * 
 * This class shall have no dependencies to extensions
 *
 */
public abstract class Queue {

    public enum State {
        INITIAL(0),
        // State after a call to queueBegin() - command buffer begin command is inserted on queue
        RECORDING(1),
        // State after a command buffer end command is inserted on queue
        EXECUTABLE(2),
        // Queue has been submitted
        PENDING(3),
        INVALID(4);

        public final int value;

        State(int value) {
            this.value = value;
        }

    }

    public static final int UPDATE_BUFFER_MAX_BYTES = 65536;

    /**
     * Begins command recording on the queue - may be called multiple times.
     */
    public abstract void queueBegin();

    /**
     * Ends recording of commandbuffers on the queue
     */
    public abstract void queueEnd();

    /**
     * Submits previously recorded commands
     * 
     */
    public abstract void queueSubmit(Semaphore waitSemaphores, Semaphore signalSemaphores,
            PipelineStageFlagBit[] dstStageFlags);

    /**
     * Submits previously recorded commands
     * 
     */
    public abstract void queueSubmit(Category id, TimelineSemaphoreSubmitInfo submitInfo, TimelineSemaphore wait,
            TimelineSemaphore signal);

    /**
     * Waits for the queue to finish - if in recording state {@link #queueEnd()} is called
     * command idle is submitted and command transitioned to state idle
     * 
     * @param queue
     */
    public abstract void queueWaitIdle();

    /**
     * Resets the querypool
     * 
     * @param queryPool
     * @param firstQuery
     * @param queryCount
     */
    public abstract void cmdResetQueryPool(QueryPool queryPool, int firstQuery, int queryCount);

    /**
     * records an imagememorybarrier on the command queue
     * 
     * @param barrier
     */
    public abstract void cmdPipelineBarrier(ImageMemoryBarrier barrier, Vulkan10.PipelineStageFlagBit[] srcStageMask,
            Vulkan10.PipelineStageFlagBit[] dstStageMask);

    /**
     * records a beginrenderpass command on the queue
     * 
     * @param beginInfo
     */
    public abstract void cmdBeginRenderPass(RenderPassBeginInfo beginInfo);

    /**
     * Records a bind pipeline command on the queue
     * 
     * @param pipeline
     * @param bindPoint
     */
    public abstract void cmdBindPipeline(Pipeline pipeline, PipelineBindPoint bindPoint);

    public abstract void cmdBindDescriptorSets(PipelineLayout pipelineLayout, DescriptorSet descriptorSet,
            PipelineBindPoint bindPoint, int firstSet,
            IntBuffer dynamicOffset);

    public abstract void cmdSetViewport(Rect2D viewport, float minDepth, float maxDepth);

    public abstract void cmdSetScissor(Rect2D scissor);

    public abstract void cmdBindVertexBuffers(int firstBinding, LongBuffer pBuffers, LongBuffer pOffsets);

    public abstract void cmdBindIndexBuffer(long buffer, long offset, IndexType indexType);

    public abstract void cmdPushConstants(PipelineLayout layout, PushConstantRange pushRange, ByteBuffer buffer);

    public abstract void cmdDraw(int vertexCount, int instanceCount, int firstVertex, int firstInstance);

    public abstract void cmdDrawIndexed(int indexCount, int instanceCount, int firstIndex, int vertexOffset,
            int firstInstance);

    public abstract void cmdDrawIndirect(IndirectDrawCalls drawCalls);

    public abstract void cmdDispatch(int sizeX, int sizeY, int sizeZ);

    /**
     * Blits one imagesubresource layer
     * 
     * @param srcImage
     * @param dstImage
     * @param srcLayer
     * @param dstLayer
     * @param regions
     * @param filter
     */
    public abstract void cmdBlitImage(Image srcImage, Image dstImage, ImageSubresourceLayers srcLayer,
            ImageSubresourceLayers dstLayer, Filter filter);

    public abstract void cmdPipelineBarrier(Image image, ImageLayout oldLayout, ImageLayout newLayout,
            ImageSubresourceRange subresourceRange);

    public abstract void cmdPipelineBarrier2(DependencyInfo depInfo);

    public abstract void cmdBufferMemoryBarrier2(long srcStageMask, long srcAccessMask, long dstStageMask,
            long dstAccessMask, MemoryBuffer buffer);

    public abstract void cmdImageMemoryBarrier2(long srcStageMask, long srcAccessMask, long dstStageMask,
            long dstAccessMask, Image image, ImageSubresourceRange resourceRange);

    /**
     * records an endrenderpass command on the queue
     */
    public abstract void cmdEndRenderPass();

    /**
     * Issues vkCmdCopyBufferToImage, copying from source buffer to destination
     * 
     * @param source
     * @param destination
     * @param sublayer
     */
    public abstract void cmdCopyBufferToImage(MemoryBuffer source, ImageMemory destination,
            ImageSubresourceLayers subLayer);

    /**
     * Copy from source to destination
     * 
     * @param source
     * @param destination
     * @param sourceOffset
     * @param destOffset
     * @param size
     */
    public abstract void cmdCopyBuffer(MemoryBuffer source, MemoryBuffer destination, int sourceOffset, int destOffset,
            long size);

    /**
     * Copies a region or whole image to buffer
     * 
     * @param source
     * @param srcLayout
     * @param destination
     * @param subresourceLayers Array of layers
     */
    public abstract void cmdCopyImageToBuffer(Image source, ImageLayout srcLayout, MemoryBuffer destination,
            ImageSubresourceLayers... subresourceLayers);

    /**
     * Does a direct buffer update - this shall only be used for small direct buffer updates.
     * Max size is 65536 - size to copy MUST be a multiple of 4
     * 
     * @param source
     * @param destination
     * @param destOffset
     */
    public abstract void cmdUpdateBuffer(ByteBuffer source, MemoryBuffer destination, int destOffset);

    protected abstract State getState();

    /**
     * Checks the current layout of the miplevel in the image, of not equal to layout then a pipline barrier command is
     * issued to transition the layout.
     * 
     * @param image
     * @param layout
     * @param subLayer
     */
    public void transitionToLayout(Image image, ImageLayout layout, ImageSubresourceLayers subLayer) {
        ImageLayout currentLayout = image.getLayout(subLayer);
        if (currentLayout == null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + ", no initial layout for sublayer with miplevel "
                            + subLayer.mipLevel + ", base arraylayer " + subLayer.baseArrayLayer);
        }
        if (currentLayout != layout) {
            // queueBegin();
            cmdPipelineBarrier(image, currentLayout, layout, new ImageSubresourceRange(subLayer));
            // queueEnd();
        }
    }

}

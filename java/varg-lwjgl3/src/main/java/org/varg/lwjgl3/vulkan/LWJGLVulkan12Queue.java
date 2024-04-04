package org.varg.lwjgl3.vulkan;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkBufferMemoryBarrier2;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageMemoryBarrier2;
import org.lwjgl.vulkan.VkImageSubresourceLayers;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkOffset3D;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;
import org.varg.vulkan.CommandBuffer;
import org.varg.vulkan.CommandBuffers;
import org.varg.vulkan.CommandBuffers.Category;
import org.varg.vulkan.IndirectDrawCalls;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.AccessFlagBit;
import org.varg.vulkan.Vulkan10.ImageAspectFlagBit;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.PipelineBindPoint;
import org.varg.vulkan.Vulkan10.PipelineStageFlagBit;
import org.varg.vulkan.Vulkan10.SubpassContents;
import org.varg.vulkan.Vulkan13.AccessFlagBits2;
import org.varg.vulkan.Vulkan13.PipelineStateFlagBits2;
import org.varg.vulkan.VulkanBackend;
import org.varg.vulkan.cmd.ClearColorValue;
import org.varg.vulkan.descriptor.DescriptorSet;
import org.varg.vulkan.image.Image;
import org.varg.vulkan.image.ImageMemoryBarrier;
import org.varg.vulkan.image.ImageSubresourceLayers;
import org.varg.vulkan.image.ImageSubresourceRange;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.memory.ImageMemory;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.pipeline.Pipeline;
import org.varg.vulkan.pipeline.PipelineLayout;
import org.varg.vulkan.renderpass.ClearValue;
import org.varg.vulkan.renderpass.RenderPassBeginInfo;
import org.varg.vulkan.renderpass.SubpassDescription2;
import org.varg.vulkan.structs.BufferMemoryBarrier2;
import org.varg.vulkan.structs.DependencyInfo;
import org.varg.vulkan.structs.Extent3D;
import org.varg.vulkan.structs.ImageMemoryBarrier2;
import org.varg.vulkan.structs.Offset3D;
import org.varg.vulkan.structs.PushConstantRange;
import org.varg.vulkan.structs.QueryPool;
import org.varg.vulkan.structs.Rect2D;
import org.varg.vulkan.structs.Semaphore;
import org.varg.vulkan.structs.TimelineSemaphore;
import org.varg.vulkan.structs.TimelineSemaphore.TimelineSemaphoreSubmitInfo;

/**
 * Handles queue states using CommandBuffers, handles pool of vulkan platform command objects needed to record
 * commands on the queue.
 *
 */
public class LWJGLVulkan12Queue extends Queue {

    public static final int MAX_DESCRIPTOR_SETS = 10;
    private final VkQueue queue;
    private final CommandBuffers<VkCommandBuffer> pool;
    LWJGLCommandPool commandPool = new LWJGLCommandPool();
    VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc();
    VkSubmitInfo submitInfo = VkSubmitInfo.calloc();

    IntBuffer dstWaitStageMask = Buffers.createIntBuffer(1);
    LongBuffer descriptorSets = Buffers.createLongBuffer(MAX_DESCRIPTOR_SETS);

    public LWJGLVulkan12Queue(VkQueue queue, CommandBuffers<VkCommandBuffer> pool) {
        if (queue == null || pool == null || queue.address() == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.pool = pool;
        this.queue = queue;
    }

    @Override
    public void queueBegin() {
        if (!pool.hasCommandBuffer()) {
            VkCommandBuffer commandBuffer = pool.transitionToState(State.RECORDING).commandBuffer;
            insertBeginCommand(commandBuffer);
        } else {
            CommandBuffer<VkCommandBuffer> cmd = pool.getCommandBuffer();
            if (cmd.getState() != State.RECORDING & cmd.getState() != State.EXECUTABLE & cmd.getState()
                    != State.PENDING) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + cmd.getState());
            }
            if (cmd.getState() != State.RECORDING) {
                pool.transitionToState(State.RECORDING);
            }
        }
    }

    private void insertBeginCommand(VkCommandBuffer commandBuffer) {
        VkCommandBufferBeginInfo beginInfo = commandPool.getCommandBufferBegin();
        beginInfo.sType(VK12.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(0)
                .pInheritanceInfo(null);
        VulkanBackend.assertResult(VK12.vkBeginCommandBuffer(commandBuffer, beginInfo));
    }

    @Override
    public void queueEnd() {
        if (pool.hasCommandBuffer()) {
            pool.transitionToState(State.EXECUTABLE);
        }
    }

    @Override
    public void queueSubmit(Semaphore waitSemaphores, Semaphore signalSemaphores, PipelineStageFlagBit[] dstStageFlags) {
        dstWaitStageMask.put(0, BitFlags.getFlagsValue(dstStageFlags));
        CommandBuffer<VkCommandBuffer> commandBuffer = pool.transitionToState(State.PENDING);
        PointerBuffer pointerBuffer = MemoryUtil.memAllocPointer(1);
        VkSubmitInfo submit = commandPool.getSubmit();
        submit.sType(VK12.VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(MemoryUtil.NULL)
                .pWaitDstStageMask(dstWaitStageMask)
                .pCommandBuffers(pointerBuffer.put(0, commandBuffer.commandBuffer));
        if (waitSemaphores != null) {
            submit.waitSemaphoreCount(1)
                    .pWaitSemaphores(waitSemaphores.getSemaphoreBuffer());
        } else {
            submit.waitSemaphoreCount(0);
        }
        if (signalSemaphores != null) {
            submit.pSignalSemaphores(signalSemaphores.getSemaphoreBuffer());
        } else {
            submit.pSignalSemaphores(null);
        }
        VulkanBackend.assertResult(VK12.vkEndCommandBuffer(commandBuffer.commandBuffer));
        VulkanBackend.assertResult(VK12.vkQueueSubmit(queue, submit, VK12.VK_NULL_HANDLE));
        pointerBuffer.free();
    }

    @Override
    public void cmdResetQueryPool(QueryPool queryPool, int firstQuery, int queryCount) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdResetQueryPool(cmd, queryPool.getHandle(), firstQuery, queryCount);
    }

    @Override
    public void queueSubmit(Category id, TimelineSemaphoreSubmitInfo timelineSubmit, TimelineSemaphore wait, TimelineSemaphore signal) {

        CommandBuffer<VkCommandBuffer> commandBuffer = pool.transitionToState(State.PENDING);

        PointerBuffer pointerBuffer = MemoryUtil.memAllocPointer(1);

        VkSubmitInfo info = VkSubmitInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(timelineSubmit.getPointer())
                .waitSemaphoreCount(1)
                .pWaitSemaphores(wait.getSemaphoreBuffer())
                .pSignalSemaphores(signal.getSemaphoreBuffer())
                .pCommandBuffers(pointerBuffer.put(0, commandBuffer.commandBuffer));

        VulkanBackend.assertResult(VK12.vkEndCommandBuffer(commandBuffer.commandBuffer));
        VulkanBackend.assertResult(VK12.vkQueueSubmit(queue, info, VK12.VK_NULL_HANDLE));
    }

    @Override
    public void queueWaitIdle() {
        if (pool.hasCommandBuffer()) {
            if (getState() == State.RECORDING) {
                pool.transitionToState(State.EXECUTABLE);
            }
            if (getState() == State.EXECUTABLE) {
                // Submit and switch to pending state.
                queueSubmit(null, null, null);
            }
            if (getState() != State.PENDING) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Invalid state: " + getState());
            }
            VK12.vkQueueWaitIdle(queue);
            pool.transitionToState(State.INITIAL);
            commandPool.releaseCommands();
        }
    }

    @Override
    public void cmdBindPipeline(Pipeline pipeline, PipelineBindPoint bindPoint) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdBindPipeline(cmd, bindPoint.value, pipeline.getPipeline());
    }

    @Override
    public void cmdBindDescriptorSets(PipelineLayout pipelineLayout, DescriptorSet descriptorSet,
            PipelineBindPoint bindPoint, int firstSet,
            IntBuffer dynamicOffset) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdBindDescriptorSets(cmd, bindPoint.value, pipelineLayout.getPipelineLayout(), firstSet,
                descriptorSet.getDescriptorSetBuffer(), dynamicOffset);
    }

    @Override
    public void cmdBindIndexBuffer(long buffer, long offset,
            org.varg.vulkan.Vulkan10.IndexType indexType) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdBindIndexBuffer(cmd, buffer, offset, indexType.value);
    }

    @Override
    public void cmdPipelineBarrier(ImageMemoryBarrier barrier, Vulkan10.PipelineStageFlagBit[] srcStageMask,
            Vulkan10.PipelineStageFlagBit[] dstStageMask) {

        ImageSubresourceRange subresourceRange = barrier.subresourceRange;
        VkImageMemoryBarrier.Buffer imageMemoryBarrier = commandPool.getImageMemoryBarrier();
        imageMemoryBarrier.sType(VK12.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .pNext(MemoryUtil.NULL)
                .srcAccessMask(barrier.getSrcAccessMaskValue())
                .dstAccessMask(barrier.getDstAccessMaskValue())
                .oldLayout(barrier.oldLayout.value)
                .newLayout(barrier.newLayout.value)
                .srcQueueFamilyIndex(0)
                .dstQueueFamilyIndex(0)
                .image(barrier.image.pointer)
                .subresourceRange(it -> it
                        .aspectMask(subresourceRange.getAspectMaskValue())
                        .baseMipLevel(subresourceRange.baseMipLevel)
                        .levelCount(subresourceRange.levelCount)
                        .baseArrayLayer(subresourceRange.baseArrayLayer)
                        .layerCount(subresourceRange.layerCount));

        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdPipelineBarrier(cmd, BitFlags.getFlagsValue(srcStageMask), BitFlags.getFlagsValue(dstStageMask), 0,
                null, null, imageMemoryBarrier);
    }

    @Override
    public void cmdSetViewport(Rect2D viewport, float minDepth, float maxDepth) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VkViewport.Buffer viewportBuffer = commandPool.getViewport();
        viewportBuffer.width(viewport.extent.width)
                .height(viewport.extent.height)
                .x(viewport.offset.x)
                .y(viewport.offset.y)
                .minDepth(minDepth)
                .maxDepth(maxDepth);
        VK12.vkCmdSetViewport(cmd, 0, viewportBuffer);
    }

    @Override
    public void cmdSetScissor(Rect2D scissor) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VkRect2D.Buffer scissorBuffer = commandPool.getRect2D();
        scissorBuffer.extent(it -> it
                .width(scissor.extent.width)
                .height(scissor.extent.height))
                .offset(it -> it
                        .x(scissor.offset.x)
                        .y(scissor.offset.y));
        VK12.vkCmdSetScissor(cmd, 0, scissorBuffer);
    }

    @Override
    public void cmdBindVertexBuffers(int firstBinding, LongBuffer pBuffers, LongBuffer pOffsets) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdBindVertexBuffers(cmd, firstBinding, pBuffers, pOffsets);
    }

    @Override
    public void cmdPushConstants(PipelineLayout layout, PushConstantRange pushRange, ByteBuffer buffer) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdPushConstants(cmd, layout.getPipelineLayout(), pushRange.stageFlagsValue, pushRange.offset, buffer);
    }

    @Override
    public void cmdDraw(int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdDraw(cmd, vertexCount, instanceCount, firstVertex, firstInstance);
    }

    @Override
    public void cmdDrawIndexed(int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdDrawIndexed(cmd, indexCount, instanceCount, firstIndex, vertexOffset, firstInstance);
    }

    private void cmdDrawIndexedIndirect(IndirectDrawCalls drawCalls) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        for (IndexType indexType : IndexType.values()) {
            int count = drawCalls.getIndexedInstanceCount(indexType);
            if (count > 0) {
                switch (indexType) {
                    case BYTE:
                        cmdBindIndexBuffer(drawCalls.getIndexBufferPointer(indexType), 0,
                                Vulkan10.IndexType.VK_INDEX_TYPE_UINT8_EXT);
                        break;
                    case SHORT:
                        cmdBindIndexBuffer(drawCalls.getIndexBufferPointer(indexType), 0,
                                Vulkan10.IndexType.VK_INDEX_TYPE_UINT16);
                        break;
                    case INT:
                        cmdBindIndexBuffer(drawCalls.getIndexBufferPointer(indexType), 0,
                                Vulkan10.IndexType.VK_INDEX_TYPE_UINT32);
                        break;
                    default:
                        throw new IllegalArgumentException(indexType.name());
                }
                VK12.vkCmdDrawIndexedIndirect(cmd, drawCalls.getIndirectMemory().getPointer(),
                        drawCalls.getIndirectIndexedMemoryOffset(indexType), count,
                        drawCalls.getIndexedCommandSize() * Integer.BYTES);
            }
        }
    }

    @Override
    public void cmdDrawIndirect(IndirectDrawCalls drawCalls) {
        if (drawCalls.hasIndices()) {
            cmdDrawIndexedIndirect(drawCalls);
        }
        if (drawCalls.arrayInstanceCount > 0) {
            VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
            VK12.vkCmdDrawIndirect(cmd, drawCalls.getIndirectMemory().getPointer(), drawCalls
                    .getIndirectArrayMemoryOffset(), drawCalls.arrayInstanceCount,
                    drawCalls.getCommandSize()
                            * Integer.BYTES);
        }
    }

    @Override
    public void cmdDispatch(int sizeX, int sizeY, int sizeZ) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdDispatch(cmd, sizeX, sizeY, sizeZ);
    }

    @Override
    public void cmdBeginRenderPass(RenderPassBeginInfo beginInfo) {
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VkClearValue.Buffer clearValue = commandPool.getClearValue();
        int index = 0;
        for (ClearValue cv : beginInfo.pClearValues) {
            ImageView attachment = beginInfo.framebuffer.createInfo.pAttachments[index];
            ClearColorValue color = cv.getClearColor();
            switch (index) {
                case SubpassDescription2.COLOR_ATTACHMENT_INDEX:
                    switch (attachment.image.getFormat()) {
                        // The clear color order is always RGBA, where index 0 is R, 1 is G etc.
                        case VK_FORMAT_R8G8B8A8_UNORM:
                        case VK_FORMAT_R8G8B8A8_SRGB:
                        case VK_FORMAT_A2R10G10B10_UNORM_PACK32:
                        case VK_FORMAT_R16G16B16A16_SFLOAT:
                        case VK_FORMAT_B8G8R8A8_UNORM:
                        case VK_FORMAT_B10G11R11_UFLOAT_PACK32:
                        case VK_FORMAT_A2B10G10R10_UNORM_PACK32:
                            clearValue.get(index).color()
                                    .float32(0, color.float32[0])
                                    .float32(1, color.float32[1])
                                    .float32(2, color.float32[2])
                                    .float32(3, color.float32[3]);
                            break;
                        case VK_FORMAT_R8G8B8A8_SINT:
                            clearValue.get(index).color()
                                    .int32(0, color.int32[0])
                                    .int32(1, color.int32[1])
                                    .int32(2, color.int32[2])
                                    .int32(3, color.int32[3]);
                            break;
                        case VK_FORMAT_R8G8B8_UINT:
                            clearValue.get(index).color()
                                    .uint32(0, color.uint32[0])
                                    .uint32(1, color.uint32[1])
                                    .uint32(2, color.uint32[2])
                                    .uint32(3, color.uint32[3]);
                            break;

                        default:
                            throw new IllegalArgumentException(
                                    ErrorMessage.NOT_IMPLEMENTED.message + attachment.image.getFormat());
                    }
                    break;
                case SubpassDescription2.DEPTH_ATTACHMENT_INDEX:
                    if (cv.depthStencil != null) {
                        clearValue.get(index).depthStencil()
                                .depth(cv.depthStencil.depth)
                                .stencil(cv.depthStencil.stencil);
                    }
                    break;
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + index);
            }
            index++;
        }
        clearValue.position(0);

        VkRenderPassBeginInfo renderPassInfo = commandPool.getRenderPassBegin();
        renderPassInfo.sType(VK12.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                .pNext(MemoryUtil.NULL)
                .renderPass(beginInfo.renderPass.handle)
                .framebuffer(beginInfo.framebuffer.handle)
                .renderArea(ra -> ra
                        .offset(it -> it
                                .x(beginInfo.renderArea.offset.x)
                                .y(beginInfo.renderArea.offset.y))
                        .extent(it -> it
                                .width(beginInfo.renderArea.extent.width)
                                .height(beginInfo.renderArea.extent.height)))
                .pClearValues(clearValue);

        VK12.vkCmdBeginRenderPass(cmd, renderPassInfo, SubpassContents.VK_SUBPASS_CONTENTS_INLINE.value);

    }

    @Override
    public void cmdEndRenderPass() {
        CommandBuffer<VkCommandBuffer> commandBuffer = pool.getCommandBuffer();
        VK12.vkCmdEndRenderPass(commandBuffer.commandBuffer);
    }

    @Override
    public void cmdCopyBufferToImage(MemoryBuffer source, ImageMemory destination, ImageSubresourceLayers subLayer) {
        Extent3D imageSize = destination.getImage().getExtent();

        int width = imageSize.width;
        int height = imageSize.height;
        VkExtent3D extent = VkExtent3D.calloc();
        VkOffset3D offset = VkOffset3D.calloc()
                .set(0, 0, 0);
        extent.set(width, height, 1);

        transitionToLayout(destination.getImage(), ImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, subLayer);
        VkImageSubresourceLayers subresource = VkImageSubresourceLayers.calloc()
                .aspectMask(ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT.value)
                .baseArrayLayer(subLayer.baseArrayLayer)
                .mipLevel(subLayer.mipLevel)
                .layerCount(subLayer.layerCount);

        VkBufferImageCopy.Buffer imageCopy = VkBufferImageCopy.calloc(1)
                .bufferOffset(0)
                .bufferRowLength(0)
                .bufferImageHeight(0)
                .imageOffset(offset)
                .imageExtent(extent)
                .imageSubresource(subresource);

        VkCommandBuffer command = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdCopyBufferToImage(command, source.getPointer(), destination.getImage().pointer,
                destination.getImage().getLayout(subLayer).value, imageCopy);

        ImageSubresourceRange range = new ImageSubresourceRange(subLayer);
        cmdImageMemoryBarrier2(PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                AccessFlagBits2.VK_ACCESS_2_MEMORY_READ_BIT.value,
                PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                AccessFlagBits2.VK_ACCESS_2_MEMORY_WRITE_BIT.value, destination.getImage(), range);

        offset.free();
        subresource.free();
        extent.free();
        imageCopy.free();
    }

    @Override
    public void cmdCopyBuffer(MemoryBuffer source, MemoryBuffer destination, int sourceOffset, int destOffset,
            long size) {
        VkCommandBuffer command = pool.getCommandBuffer().commandBuffer;
        VkBufferCopy.Buffer region = VkBufferCopy.calloc(1);
        region.size(size)
                .srcOffset(sourceOffset)
                .dstOffset(destOffset);
        VK12.vkCmdCopyBuffer(command, source.getPointer(), destination.getPointer(), region);
        region.free();
    }

    @Override
    public void cmdUpdateBuffer(ByteBuffer source, MemoryBuffer destination, int destOffset) {
        if (source.remaining() > 65536) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + ", buffer size too large for update: " + source.remaining());
        }
        VkCommandBuffer command = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdUpdateBuffer(command, destination.getPointer(), destOffset, source);
    }

    private VkOffset3D.Buffer createOffset3D(Offset3D[] offsets) {
        VkOffset3D.Buffer vkOffsets = VkOffset3D.calloc(offsets.length);
        for (int i = 0; i < offsets.length; i++) {
            vkOffsets.get(i).set(offsets[i].x, offsets[i].y, offsets[i].z);
        }
        return vkOffsets;
    }

    private VkImageSubresourceLayers setSubresourceLayer(VkImageSubresourceLayers subresource,
            ImageSubresourceLayers subLayer) {
        subresource.baseArrayLayer(subLayer.baseArrayLayer)
                .mipLevel(subLayer.mipLevel)
                .layerCount(subLayer.layerCount)
                .aspectMask(BitFlags.getFlagsValue(subLayer.aspectMask));
        return subresource;
    }

    @Override
    public void cmdBlitImage(Image srcImage, Image dstImage, ImageSubresourceLayers srcLayer,
            ImageSubresourceLayers dstLayer, org.varg.vulkan.Vulkan10.Filter filter) {

        int width = srcImage.getExtent().width;
        int height = srcImage.getExtent().height;
        transitionToLayout(srcImage, ImageLayout.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, srcLayer);
        transitionToLayout(dstImage, ImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, dstLayer);
        VkImageSubresourceLayers.Buffer subresource = VkImageSubresourceLayers.calloc(2);
        VkImageBlit.Buffer blitRegions = VkImageBlit.calloc(1);
        Offset3D[] srcOffsets = new Offset3D[] { new Offset3D(),
                new Offset3D(width >> srcLayer.mipLevel, height >> srcLayer.mipLevel, 1) };
        Offset3D[] dstOffsets = new Offset3D[] { new Offset3D(),
                new Offset3D(width >> dstLayer.mipLevel, height >> dstLayer.mipLevel, 1) };

        VkImageBlit vkBlit = blitRegions.get(0);
        vkBlit.srcSubresource(setSubresourceLayer(subresource.get(0), srcLayer));
        vkBlit.dstSubresource(setSubresourceLayer(subresource.get(1), dstLayer));
        VkOffset3D.Buffer vkSrcOffsets = createOffset3D(srcOffsets);
        VkOffset3D.Buffer vkDstOffsets = createOffset3D(dstOffsets);
        vkBlit.srcOffsets(vkSrcOffsets);
        vkBlit.dstOffsets(vkDstOffsets);
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK12.vkCmdBlitImage(cmd, srcImage.pointer, srcImage.getLayout(srcLayer).value, dstImage.pointer,
                dstImage.getLayout(dstLayer).value, blitRegions, filter.value);
        subresource.free();
        blitRegions.free();
    }

    @Override
    public void cmdPipelineBarrier(Image image, ImageLayout oldLayout, ImageLayout newLayout,
            ImageSubresourceRange subresourceRange) {
        AccessFlagBit[] srcAccess = new AccessFlagBit[] { AccessFlagBit.VK_ACCESS_MEMORY_WRITE_BIT };
        AccessFlagBit[] dstAccess = new AccessFlagBit[] { AccessFlagBit.VK_ACCESS_MEMORY_WRITE_BIT };
        ImageMemoryBarrier barrier = new ImageMemoryBarrier(srcAccess, dstAccess, oldLayout, newLayout, 0, 0,
                image, subresourceRange);
        cmdPipelineBarrier(barrier,
                PipelineStageFlagBit.getBitFlags(PipelineStageFlagBit.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT),
                PipelineStageFlagBit.getBitFlags(PipelineStageFlagBit.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT));
        barrier.updateLayout();
    }

    @Override
    protected State getState() {
        if (!pool.hasCommandBuffer()) {
            return State.INITIAL;
        }
        CommandBuffer<VkCommandBuffer> cmd = pool.getCommandBuffer();
        return cmd != null ? cmd.getState() : null;
    }

    /**
     * Internal method do not use!!!
     * 
     * @return
     */
    public VkQueue getQueue() {
        return queue;
    }

    /**
     * Internal method do not use!!!
     * 
     * @param category
     * @return
     */
    public VkCommandBuffer getCommandBuffer() {
        return pool.getCommandBuffer().commandBuffer;
    }

    @Override
    public void cmdBufferMemoryBarrier2(long srcStageMask, long srcAccessMask, long dstStageMask,
            long dstAccessMask, MemoryBuffer buffer) {

        VkBufferMemoryBarrier2.Buffer vkBufferBarrier = VkBufferMemoryBarrier2.calloc(1)
                .sType(VK13.VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER_2)
                .pNext(MemoryUtil.NULL)
                .srcStageMask(srcStageMask)
                .dstStageMask(dstStageMask)
                .srcAccessMask(srcAccessMask)
                .dstAccessMask(dstAccessMask)
                .srcQueueFamilyIndex(0)
                .dstQueueFamilyIndex(0)
                .buffer(buffer.getPointer())
                .offset(0)
                .size(buffer.size);

        VkDependencyInfo vkDepInfo = VkDependencyInfo.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_DEPENDENCY_INFO)
                .pNext(MemoryUtil.NULL)
                .dependencyFlags(0);

        vkDepInfo.pBufferMemoryBarriers(vkBufferBarrier);
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK13.vkCmdPipelineBarrier2(cmd, vkDepInfo);
        free(vkDepInfo, vkBufferBarrier);
    }

    @Override
    public void cmdImageMemoryBarrier2(long srcStageMask, long srcAccessMask, long dstStageMask,
            long dstAccessMask, Image image, ImageSubresourceRange imageRange) {

        VkImageSubresourceRange vkRange = VkImageSubresourceRange.calloc()
                .aspectMask(imageRange.getAspectMaskValue())
                .baseArrayLayer(imageRange.baseArrayLayer)
                .baseMipLevel(imageRange.baseMipLevel)
                .layerCount(imageRange.layerCount)
                .levelCount(imageRange.levelCount);

        VkImageMemoryBarrier2.Buffer vkImageBarrier = VkImageMemoryBarrier2.calloc(1)
                .sType(VK13.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER_2)
                .pNext(MemoryUtil.NULL)
                .srcStageMask(srcStageMask)
                .dstStageMask(dstStageMask)
                .srcAccessMask(srcAccessMask)
                .dstAccessMask(dstAccessMask)
                .srcQueueFamilyIndex(0)
                .dstQueueFamilyIndex(0)
                .image(image.pointer)
                .subresourceRange(vkRange);

        VkDependencyInfo vkDepInfo = VkDependencyInfo.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_DEPENDENCY_INFO)
                .pNext(MemoryUtil.NULL)
                .dependencyFlags(0);

        vkDepInfo.pImageMemoryBarriers(vkImageBarrier);
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK13.vkCmdPipelineBarrier2(cmd, vkDepInfo);
        free(vkDepInfo, vkImageBarrier, vkRange);
    }

    @Override
    public void cmdPipelineBarrier2(DependencyInfo depInfo) {

        VkBufferMemoryBarrier2.Buffer vkBufferBarrier = null;
        VkImageMemoryBarrier2.Buffer vkImageBarrier = null;
        VkDependencyInfo vkDepInfo = VkDependencyInfo.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_DEPENDENCY_INFO)
                .pNext(MemoryUtil.NULL)
                .dependencyFlags(BitFlags.getFlagsValue(depInfo.dependencyFlags));

        ImageMemoryBarrier2 imageBarrier = depInfo.getImageMemoryBarriers();
        if (imageBarrier != null) {
            vkImageBarrier = VkImageMemoryBarrier2.calloc(1)
                    .sType(VK13.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER_2)
                    .pNext(MemoryUtil.NULL)
                    .srcStageMask(imageBarrier.getSrcStageMask())
                    .dstStageMask(imageBarrier.getDestStageMask())
                    .srcAccessMask(imageBarrier.getSrcAccessMask())
                    .dstAccessMask(imageBarrier.getDstAccessMask())
                    .oldLayout(imageBarrier.oldLayout.value)
                    .newLayout(imageBarrier.newLayout.value)
                    .srcQueueFamilyIndex(imageBarrier.srcQueueFamilyIndex)
                    .dstQueueFamilyIndex(imageBarrier.dstQueueFamilyIndex);

            vkDepInfo.pImageMemoryBarriers(vkImageBarrier);
        }

        BufferMemoryBarrier2 bufferBarrier = depInfo.getBufferMemoryBarriers();
        if (bufferBarrier != null) {
            MemoryBuffer buffer = bufferBarrier.getBuffer();
            vkBufferBarrier = VkBufferMemoryBarrier2.calloc(1)
                    .sType(VK13.VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER_2)
                    .pNext(MemoryUtil.NULL)
                    .srcStageMask(bufferBarrier.getSrcStageMask())
                    .dstStageMask(bufferBarrier.getDestStageMask())
                    .srcAccessMask(bufferBarrier.getSrcAccessMask())
                    .dstAccessMask(bufferBarrier.getDstAccessMask())
                    .srcQueueFamilyIndex(bufferBarrier.srcQueueFamilyIndex)
                    .dstQueueFamilyIndex(bufferBarrier.dstQueueFamilyIndex)
                    .buffer(buffer.getPointer())
                    .offset(0)
                    .size(buffer.size);
            vkDepInfo.pBufferMemoryBarriers(vkBufferBarrier);
        }
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VK13.vkCmdPipelineBarrier2(cmd, vkDepInfo);
        free(vkDepInfo, vkImageBarrier, vkBufferBarrier);
    }

    @Override
    public void cmdCopyImageToBuffer(Image source, ImageLayout srcLayout, MemoryBuffer destination,
            ImageSubresourceLayers... subresourceLayers) {

        // From spec:
        // srcImageLayout must be VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_GENERAL,
        // or VK_IMAGE_LAYOUT_SHARED_PRESENT_KHR
        if (srcLayout != ImageLayout.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL &&
                srcLayout != ImageLayout.VK_IMAGE_LAYOUT_GENERAL && srcLayout
                        != ImageLayout.VK_IMAGE_LAYOUT_SHARED_PRESENT_KHR) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid layout: " + srcLayout);
        }
        VkCommandBuffer cmd = pool.getCommandBuffer().commandBuffer;
        VkBufferImageCopy.Buffer vkCopy = VkBufferImageCopy.calloc(subresourceLayers.length);
        VkOffset3D vkOffset = VkOffset3D.calloc()
                .x(0)
                .y(0)
                .z(0);
        Extent3D imageExtent = source.getExtent();
        VkExtent3D vkExtent = VkExtent3D.calloc()
                .depth(imageExtent.depth);
        VkImageSubresourceLayers vkSubresource = VkImageSubresourceLayers.calloc();
        for (ImageSubresourceLayers subLayer : subresourceLayers) {

            ImageLayout currentLayout = source.getLayout(subLayer);
            if (currentLayout != srcLayout) {
                transitionToLayout(source, srcLayout, subLayer);
            }
            int sizeDivisor = (int) Math.pow(2, subLayer.mipLevel);
            vkExtent.width(imageExtent.width / sizeDivisor)
                    .height(imageExtent.height / sizeDivisor);

            vkSubresource.baseArrayLayer(subLayer.baseArrayLayer)
                    .aspectMask(BitFlags.getFlagsValue(subLayer.aspectMask))
                    .mipLevel(subLayer.mipLevel)
                    .layerCount(subLayer.layerCount);

            vkCopy.get()
                    .bufferOffset(0)
                    .bufferRowLength(0)
                    .bufferImageHeight(0)
                    .imageOffset(vkOffset)
                    .imageExtent(vkExtent)
                    .imageSubresource(vkSubresource);
        }
        vkCopy.clear();
        VK12.vkCmdCopyImageToBuffer(cmd, source.pointer, srcLayout.value, destination.getPointer(), vkCopy);

    }

    private void free(NativeResource... structs) {
        if (structs != null) {
            for (NativeResource s : structs) {
                if (s != null) {
                    s.free();
                }
            }
        }
    }

}

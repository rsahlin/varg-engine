package org.varg.lwjgl3.vulkan;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.data.VertexBuffer;
import org.gltfio.gltf2.JSONBuffer;
import org.gltfio.gltf2.JSONBufferView;
import org.gltfio.gltf2.JSONBufferView.Target;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.ktximageio.ktx.ImageBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkBindBufferMemoryInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkMappedMemoryRange;
import org.lwjgl.vulkan.VkMemoryAllocateFlagsInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.uniform.BindBuffer;
import org.varg.uniform.BindBuffer.BufferState;
import org.varg.uniform.DescriptorBuffers;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;
import org.varg.vulkan.Vulkan10.ImageAspectFlagBit;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.ImageTiling;
import org.varg.vulkan.Vulkan10.MemoryPropertyFlagBit;
import org.varg.vulkan.Vulkan12;
import org.varg.vulkan.Vulkan13.AccessFlagBits2;
import org.varg.vulkan.Vulkan13.PipelineStateFlagBits2;
import org.varg.vulkan.VulkanBackend;
import org.varg.vulkan.image.Image;
import org.varg.vulkan.image.ImageCreateInfo;
import org.varg.vulkan.image.ImageSubresourceLayers;
import org.varg.vulkan.image.ImageSubresourceRange;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.ImageMemory;
import org.varg.vulkan.memory.Memory;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.memory.VertexMemory;
import org.varg.vulkan.memory.VertexMemory.Mode;
import org.varg.vulkan.structs.DeviceLimits;
import org.varg.vulkan.structs.FormatProperties;
import org.varg.vulkan.structs.PhysicalDeviceMemoryProperties;
import org.varg.vulkan.structs.PhysicalDeviceMemoryProperties.MemoryType;
import org.varg.vulkan.structs.SubresourceLayout;

public class LWJGLVulkan12MemoryAllocator implements DeviceMemory {

    private final VkDevice deviceInstance;
    private final PhysicalDeviceMemoryProperties deviceMemoryProperties;
    private final LongBuffer lb = Buffers.createLongBuffer(1);
    private final PointerBuffer pointer = MemoryUtil.memAllocPointer(1);
    private final HashSet<Memory> memoryAllocations = new HashSet<Memory>();
    private final HashMap<Long, MemoryBuffer> boundBuffers = new HashMap<Long, MemoryBuffer>();
    private final HashMap<Long, Set<ImageMemory>> boundImageMemory = new HashMap<Long, Set<ImageMemory>>();
    private final DeviceLimits limits;
    private MemoryBuffer transferStagingBuffer = null;

    static class SourceBuffers {

        HashMap<Integer, HashSet<Integer>> sourceOffsets = new HashMap<Integer, HashSet<Integer>>();

        void add(int bufferIndex, int offset) {
            HashSet<Integer> offsets = sourceOffsets.get(bufferIndex);
            if (offsets == null) {
                offsets = new HashSet<Integer>();
            }
            offsets.add(offset);
            sourceOffsets.put(bufferIndex, offsets);
        }

        HashSet<Integer> getList(int bufferIndex) {
            HashSet<Integer> offsets = sourceOffsets.get(bufferIndex);
            return offsets;
        }

        Set<Integer> getKeys() {
            return sourceOffsets.keySet();
        }
    }

    protected LWJGLVulkan12MemoryAllocator(LWJGL3Vulkan12Backend backend) {
        if (backend == null) {
            throw new IllegalAccessError(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        deviceInstance = backend.getLogicalDevice().getDeviceInstance();
        limits = backend.getLogicalDevice().getPhysicalDeviceProperties().getLimits();
        deviceMemoryProperties = backend.getMemoryProperties();
        int maxTexture = limits.getMaxImageDimension2D();
    }

    /**
     * Creates the stagingbuffer to be used as source transfer (BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
     * 
     * @param sizeInBytes
     * @param current
     * @return
     */
    private MemoryBuffer createTransferStagingBuffer(int sizeInBytes) {
        if (transferStagingBuffer != null && transferStagingBuffer.size >= sizeInBytes) {
            return transferStagingBuffer;
        } else if (transferStagingBuffer != null) {
            unbindBuffer(transferStagingBuffer);
            freeMemory(transferStagingBuffer.getBoundMemory());
        }
        transferStagingBuffer = createStagingBuffer(sizeInBytes, BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_SRC_BIT.value);
        return transferStagingBuffer;
    }

    private MemoryBuffer createStagingBuffer(int sizeInBytes, int usageFlags) {
        long start = System.currentTimeMillis();
        MemoryBuffer stagingBuf = createBuffer(sizeInBytes, usageFlags);
        Memory staging = allocateMemory(stagingBuf.size, BitFlags.getFlagsValue(
                MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT));
        bindBufferMemory(stagingBuf, staging, 0L);
        Logger.d(getClass(), "Allocated staging buffer with size: " + staging.size + ", usageflags " + BitFlags.toString(usageFlags, BufferUsageFlagBit.values()) + ", in " + (System.currentTimeMillis() - start)
                + " millis");
        return stagingBuf;
    }

    private long bindBufferMemory(Memory memory, MemoryBuffer... buffers) {
        return bindBufferMemory(memory, 0, buffers);
    }

    @Override
    public long bindBufferMemory(Memory memory, long byteOffset, MemoryBuffer... buffers) {
        for (MemoryBuffer buffer : buffers) {
            if (buffer != null) {
                bindBufferMemory(buffer, memory, byteOffset);
                byteOffset += buffer.allocationSize;
            }
        }
        return byteOffset;
    }

    @Override
    public void bindBufferMemory(MemoryBuffer buffer, Memory memory, long memoryOffset) {
        long ptr = memory.pointer + memoryOffset;
        if (boundBuffers.containsKey(ptr)) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Buffer already bound: " + ptr);
        }
        if (memoryOffset % buffer.alignment != 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Memory offset does not match alignment: " + memoryOffset + ", " + buffer.alignment + " ("
                    + memoryOffset % buffer.alignment + ")");
        }
        if ((buffer.usage & Vulkan10.BufferUsageFlagBit.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT.value) != 0 && (memory.allocateFlags & Vulkan12.MemoryAllocateFlagBits.VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT.value) == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Buffer created with VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,"
                    + "memory must be allocated with VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT");
        }

        VkBindBufferMemoryInfo.Buffer bindInfo = VkBindBufferMemoryInfo.calloc(1)
                .sType(VK12.VK_STRUCTURE_TYPE_BIND_BUFFER_MEMORY_INFO)
                .buffer(buffer.getPointer())
                .memory(memory.pointer)
                .memoryOffset(memoryOffset)
                .pNext(MemoryUtil.NULL);
        int result = VK12.vkBindBufferMemory2(deviceInstance, bindInfo);
        VulkanBackend.assertResult(result);
        buffer.bindMemory(memory, memoryOffset);
        Logger.d(getClass(),
                "Bound buffer to memory, size " + buffer.size + "(" + buffer.allocationSize + ") at offset: " + memoryOffset + ", usageflags " + BitFlags.toString(buffer.usage, BufferUsageFlagBit.values()));
        boundBuffers.put(memory.pointer + memoryOffset, buffer);
    }

    @Override
    public void freeBuffer(MemoryBuffer... buffers) {
        Memory bound = null;
        for (MemoryBuffer buffer : buffers) {
            if (buffer != null) {
                bound = buffer.getBoundMemory();
                if (bound.pointer != buffer.getBoundMemory().pointer) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Different memory pointers");
                }
                unbindBuffer(buffer);
                if (bound != null && memoryAllocations.contains(bound)) {
                    freeMemory(bound);
                }
            }
        }
    }

    @Override
    public void freeBuffer(BindBuffer... buffers) {
        Memory bound = null;
        for (BindBuffer buffer : buffers) {
            if (buffer != null) {
                if (bound == null) {
                    bound = buffer.getBuffer().getBoundMemory();
                }
                if (bound.pointer != buffer.getBuffer().getBoundMemory().pointer) {
                    throw new IllegalArgumentException(
                            ErrorMessage.INVALID_VALUE.message + "Different memory pointers");
                }
                unbindBuffer(buffer.getBuffer());
            }
        }
        if (bound != null) {
            freeMemory(bound);
        }
    }

    private void unbindBuffer(MemoryBuffer buffer) {
        if (buffer.getBoundOffset() == Constants.NO_VALUE) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Buffer already unbound");
        }
        Memory memory = buffer.getBoundMemory();
        long ptr = memory.pointer + buffer.getBoundOffset();
        if (!boundBuffers.containsKey(ptr)) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "No buffer bound to "
                    + memory.pointer + " at offset " + buffer.getBoundOffset());
        }
        buffer.unbind();
        boundBuffers.remove(ptr);
    }

    @Override
    public ImageMemory allocateImageMemory(ImageCreateInfo createInfo, MemoryPropertyFlagBit... memoryFlags) {
        long start = System.currentTimeMillis();
        Image img = createImage(createInfo);

        VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc();
        VK12.vkGetImageMemoryRequirements(deviceInstance, img.pointer, memRequirements);
        Logger.d(getClass(),
                "Memory size " + memRequirements.size() + ", memory alignment " + memRequirements.alignment()
                        + " memory type bits " + memRequirements.memoryTypeBits() + ", arrayLayers: "
                        + createInfo.arrayLayers + ", miplevels: " + createInfo.mipLevels
                        + ", depth: " + createInfo.extent.depth);
        int memoryTypeIndex = deviceMemoryProperties.getMemoryTypeIndex(BitFlags.getFlagsValue(memoryFlags));
        MemoryType mt = deviceMemoryProperties.getMemoryType(memoryTypeIndex);
        Memory memory = allocateMemory(memRequirements.size(), mt.flags);
        ImageMemory image = new ImageMemory(createInfo.extent.width, createInfo.extent.height, createInfo.format,
                0, memRequirements.size(), img, memory);
        bindImage(image);
        Logger.d(getClass(),
                "Created image with size: " + createInfo.extent.width + ", " + createInfo.extent.height + ", format "
                        + Vulkan10.Format.get(createInfo.formatValue) + ", " + createInfo.samples
                        + ", usage " + BitFlags.toString(createInfo.usage) + ", flags "
                        + BitFlags.toString(createInfo.flags)
                        + ", isBound " + image.isBound()
                        + ", " + (System.currentTimeMillis() - start) + " millis");
        return image;
    }

    private Image createImage(ImageCreateInfo createInfo) {
        VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc().sType(VK12.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(BitFlags.getFlagsValue(createInfo.flags))
                .imageType(createInfo.imageType.value)
                .mipLevels(createInfo.mipLevels)
                .arrayLayers(createInfo.arrayLayers)
                .format(createInfo.formatValue)
                .tiling(createInfo.tiling.value)
                .initialLayout(createInfo.initialLayout.value)
                .usage(BitFlags.getFlagsValue(createInfo.usage))
                .samples((int) createInfo.samples.getValue())
                .sharingMode(createInfo.sharingMode.value)
                .pQueueFamilyIndices(null);

        imageInfo.extent().width(createInfo.extent.width);
        imageInfo.extent().height(createInfo.extent.height);
        imageInfo.extent().depth(createInfo.extent.depth);
        lb.position(0);
        VulkanBackend.assertResult(VK12.vkCreateImage(deviceInstance, imageInfo, null, lb));
        long imagePointer = lb.get(0);
        SubresourceLayout resourceLayout = null;
        if (createInfo.arrayLayers > 1 || createInfo.mipLevels > 1) {
            if (createInfo.tiling == ImageTiling.VK_IMAGE_TILING_LINEAR) {
                resourceLayout = LWJGL3Vulkan12Backend.getSubresourceLayout(deviceInstance, imagePointer);
            }
        }
        return new Image(imagePointer, createInfo, resourceLayout);
    }

    private void bindImage(ImageMemory image) {
        int result = VK12.vkBindImageMemory(deviceInstance, image.getImage().pointer,
                image.getMemory().pointer, image.offset);
        VulkanBackend.assertResult(result);
        image.setBound(true);
        Set<ImageMemory> boundMemory = boundImageMemory.get(image.getMemory().pointer);
        if (boundMemory == null) {
            boundMemory = new HashSet<ImageMemory>();
            boundImageMemory.put(image.getMemory().pointer, boundMemory);
        }
        if (boundMemory.contains(image)) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already bound " + image);
        }
        boundMemory.add(image);
    }

    private void unbindImage(ImageMemory image) {
        Set<ImageMemory> boundMemory = boundImageMemory.get(image.getMemory().pointer);
        if (boundMemory == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Not bound " + image);
        }
        boundMemory.remove(image);
    }

    @Override
    public long mapMemory(Memory memory, long offset) {
        if ((memory.memoryProperties & VK12.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Memory must have VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT");
        }
        if (memory.isMapped()) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", memory is mapped");
        }
        VulkanBackend.assertResult(VK12.vkMapMemory(deviceInstance, memory.pointer, offset, memory.size, VulkanBackend.RESERVED_FOR_FUTURE_USE, pointer));
        int align = (int) ((pointer.get(0) - offset) % limits.getMinMemoryMapAlignment());
        if (align != 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid mapped memory ptr, does not fit with minMemoryMapAlignment " + limits.getMinMemoryMapAlignment());
        }
        memory.setMapped(pointer.get(0));
        return memory.getMapped();
    }

    @Override
    public void unmapMemory(Memory memory) {
        if (!memory.isMapped()) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", memory is not mapped");
        }
        VkMappedMemoryRange mappedRange = VkMappedMemoryRange.calloc().sType(VK10.VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
                .memory(memory.pointer)
                .offset(0)
                .size(memory.size)
                .pNext(MemoryUtil.NULL);
        VK12.vkInvalidateMappedMemoryRanges(deviceInstance, mappedRange);
        VK12.vkUnmapMemory(deviceInstance, memory.pointer);
        memory.setMapped(0);
    }

    private void copyImage(ByteBuffer sourceImage, Vulkan10.Format sourceFormat, int width, int height, int rowPitch,
            long destPointer, Vulkan10.Format destFormat) {
        switch (sourceFormat) {
            case VK_FORMAT_B8G8R8_UNORM:
            case VK_FORMAT_B8G8R8_SRGB:
                switch (destFormat) {
                    case VK_FORMAT_R8G8B8_UNORM:
                    case VK_FORMAT_R8G8B8_SRGB:
                        copyReverseBGR(sourceImage, width, height, destPointer);
                        break;
                    case VK_FORMAT_R8G8B8A8_UNORM:
                    case VK_FORMAT_R8G8B8A8_SRGB:
                        copyReverseBGRToRGBA(sourceImage, width, height, destPointer);
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + ", for src "
                                + sourceFormat + " to dst " + destFormat);
                }
                break;
            case VK_FORMAT_B8G8R8A8_UNORM:
            case VK_FORMAT_B8G8R8A8_SRGB:
                switch (destFormat) {
                    case VK_FORMAT_R8G8B8A8_UNORM:
                    case VK_FORMAT_R8G8B8A8_SRGB:
                        copyReverseBGRA(sourceImage, width, height, destPointer);
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + ", for src "
                                + sourceFormat + " to dst " + destFormat);
                }
                break;
            case VK_FORMAT_A8B8G8R8_UNORM_PACK32:
            case VK_FORMAT_A8B8G8R8_SRGB_PACK32:
                switch (destFormat) {
                    case VK_FORMAT_R8G8B8A8_UNORM:
                    case VK_FORMAT_R8G8B8A8_SRGB:
                        copyReverseABGR(sourceImage, width, height, destPointer);
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + ", for src "
                                + sourceFormat + " to dst " + destFormat);
                }
                break;
            case VK_FORMAT_R8G8B8_UNORM:
            case VK_FORMAT_R8G8B8_SRGB:
                switch (destFormat) {
                    case VK_FORMAT_R8G8B8A8_UNORM:
                    case VK_FORMAT_R8G8B8A8_SRGB:
                        copyRGBToRGBA(sourceImage, width, height, destPointer);
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + ", for src "
                                + sourceFormat + " to dst " + destFormat);
                }
                break;
            case VK_FORMAT_R32G32B32_SFLOAT:
                switch (destFormat) {
                    case VK_FORMAT_R32G32B32A32_SFLOAT:
                        copyRGBToRGBA(sourceImage.asFloatBuffer(), width, height, destPointer);
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + ", for src "
                                + sourceFormat + " to dst " + destFormat);
                }
                break;
            case VK_FORMAT_R16G16B16_SFLOAT:
                switch (destFormat) {
                    case VK_FORMAT_R16G16B16A16_SFLOAT:
                        copyRGBToRGBA(sourceImage.asShortBuffer(), width, height, destPointer);
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + ", for src "
                                + sourceFormat + " to dst " + destFormat);
                }
                break;
            case VK_FORMAT_R8G8B8A8_SRGB:
                switch (destFormat) {
                    case VK_FORMAT_R8G8B8A8_UNORM:
                        ByteBuffer buffer = MemoryUtil.memByteBuffer(destPointer,
                                (width * height * sourceFormat.sizeInBytes));
                        buffer.put(sourceImage);
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + ", for src "
                                + sourceFormat + " to dst " + destFormat);
                }
                break;
            case VK_FORMAT_R8G8_SRGB:
                switch (destFormat) {
                    case VK_FORMAT_R8G8_UNORM:
                        ByteBuffer buffer = MemoryUtil.memByteBuffer(destPointer,
                                (width * height * sourceFormat.sizeInBytes));
                        buffer.put(sourceImage);
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + ", for src "
                                + sourceFormat + " to dst " + destFormat);
                }
                // Mandatory formats
            case VK_FORMAT_R8_UNORM:
            case VK_FORMAT_R8G8_UNORM:
            case VK_FORMAT_R8G8B8A8_UNORM:
            case VK_FORMAT_R32G32B32A32_SFLOAT:
            case VK_FORMAT_R16G16B16A16_SFLOAT:
            case VK_FORMAT_R16G16B16A16_UNORM:
                ByteBuffer buffer = MemoryUtil.memByteBuffer(destPointer, (width * height * sourceFormat.sizeInBytes));
                buffer.put(sourceImage);
                break;
            default:
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_VALUE.message + ", source format " + sourceFormat + ", destination format "
                                + destFormat);

        }

    }

    private void copyReverseBGR(ByteBuffer sourceImage, int width, int height, long destPointer) {
        ByteBuffer buffer = MemoryUtil.memByteBuffer(destPointer, (width * height * 3));
        byte[] line = new byte[width * 3];
        byte[] dest = new byte[width * 3];
        for (int y = 0; y < height; y++) {
            sourceImage.get(line);
            int index = 0;
            for (int x = 0; x < width; x++) {
                dest[index] = line[index + 2];
                dest[index + 1] = line[index + 1];
                dest[index + 2] = line[index];
                index += 3;
            }
            buffer.put(dest);
        }
    }

    private void copyRGBToRGBA(ShortBuffer sourceImage, int width, int height, long destPointer) {
        ShortBuffer buffer = MemoryUtil.memByteBuffer(destPointer, (width * height * Short.BYTES * 4)).asShortBuffer();
        short[] line = new short[width * 3];
        short[] dest = new short[width * 4];
        for (int y = 0; y < height; y++) {
            sourceImage.get(line);
            int srcIndex = 0;
            int dstIndex = 0;
            for (int x = 0; x < width; x++) {
                dest[dstIndex] = line[srcIndex];
                dest[dstIndex + 1] = line[srcIndex + 1];
                dest[dstIndex + 2] = line[srcIndex + 2];
                dstIndex += 4;
                srcIndex += 3;
            }
            buffer.put(dest);
        }
    }

    private void copyRGBToRGBA(FloatBuffer sourceImage, int width, int height, long destPointer) {
        FloatBuffer buffer = MemoryUtil.memByteBuffer(destPointer, (width * height * Float.BYTES * 4)).asFloatBuffer();
        float[] line = new float[width * 3];
        float[] dest = new float[width * 4];
        for (int y = 0; y < height; y++) {
            sourceImage.get(line);
            int srcIndex = 0;
            int dstIndex = 0;
            for (int x = 0; x < width; x++) {
                dest[dstIndex] = line[srcIndex];
                dest[dstIndex + 1] = line[srcIndex + 1];
                dest[dstIndex + 2] = line[srcIndex + 2];
                dstIndex += 4;
                srcIndex += 3;
            }
            buffer.put(dest);
        }
    }

    private void copyReverseBGRA(ByteBuffer sourceImage, int width, int height, long destPointer) {
        ByteBuffer buffer = MemoryUtil.memByteBuffer(destPointer, (width * height * 4));
        byte[] line = new byte[width * 4];
        byte[] dest = new byte[width * 4];
        for (int y = 0; y < height; y++) {
            sourceImage.get(line);
            int index = 0;
            for (int x = 0; x < width; x++) {
                dest[index] = line[index + 2];
                dest[index + 1] = line[index + 1];
                dest[index + 2] = line[index];
                dest[index + 3] = line[index + 3];
                index += 4;
            }
            buffer.put(dest);
        }
    }

    private void copyReverseBGRToRGBA(ByteBuffer sourceImage, int width, int height, long destPointer) {
        ByteBuffer buffer = MemoryUtil.memByteBuffer(destPointer, (width * height * 4));
        byte[] line = new byte[width * 3];
        byte[] dest = new byte[width * 4];
        for (int y = 0; y < height; y++) {
            sourceImage.get(line);
            int srcIndex = 0;
            int dstIndex = 0;
            for (int x = 0; x < width; x++) {
                dest[dstIndex] = line[srcIndex + 2];
                dest[dstIndex + 1] = line[srcIndex + 1];
                dest[dstIndex + 2] = line[srcIndex];
                dstIndex += 4;
                srcIndex += 3;
            }
            buffer.put(dest);
        }
    }

    private void copyRGBToRGBA(ByteBuffer sourceImage, int width, int height, long destPointer) {
        ByteBuffer buffer = MemoryUtil.memByteBuffer(destPointer, (width * height * 4));
        byte[] line = new byte[width * 3];
        byte[] dest = new byte[width * 4];
        for (int y = 0; y < height; y++) {
            sourceImage.get(line);
            int srcIndex = 0;
            int dstIndex = 0;
            for (int x = 0; x < width; x++) {
                dest[dstIndex] = line[srcIndex];
                dest[dstIndex + 1] = line[srcIndex + 1];
                dest[dstIndex + 2] = line[srcIndex + 2];
                dstIndex += 4;
                srcIndex += 3;
            }
            buffer.put(dest);
        }
    }

    private void copyReverseABGR(ByteBuffer sourceImage, int width, int height, long destPointer) {
        ByteBuffer buffer = MemoryUtil.memByteBuffer(destPointer, (width * height * 4));
        byte[] line = new byte[width * 4];
        byte[] dest = new byte[width * 4];
        for (int y = 0; y < height; y++) {
            sourceImage.get(line);
            int index = 0;
            for (int x = 0; x < width; x++) {
                dest[index] = line[index + 3];
                dest[index + 1] = line[index + 2];
                dest[index + 2] = line[index + 1];
                dest[index + 3] = line[index];
                index += 4;
            }
            buffer.put(dest);
        }
    }

    @Override
    public MemoryBuffer copyFromDeviceMemory(MemoryBuffer memory, int count, Queue queue) {
        if ((memory.getBoundMemory().memoryProperties & VK12.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) != 0) {
            throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
        }
        int size = count < 0 ? (int) memory.size : count;
        MemoryBuffer tempMemory = createStagingBuffer(size, BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT.value);
        queue.cmdCopyBuffer(memory, tempMemory, 0, 0, size);
        return tempMemory;
    }

    @Override
    public MemoryBuffer copyFromDeviceMemory(Image image, ImageAspectFlagBit flagBit, Queue queue) {
        ImageSubresourceLayers subLayers = new ImageSubresourceLayers(flagBit, 0, 0, image.getArrayLayers());
        return copyFromDeviceMemory(image, flagBit, subLayers, queue);
    }

    @Override
    public MemoryBuffer copyFromDeviceMemory(Image image, ImageAspectFlagBit flagBit, ImageSubresourceLayers subLayers, Queue queue) {
        // Size of mip 0
        int divisor = subLayers.mipLevel == 0 ? 1 : 4 * subLayers.mipLevel;
        SubresourceLayout subLayout = image.getSubresourceLayout();
        if (subLayout == null) {
            subLayout = LWJGL3Vulkan12Backend.getSubresourceLayout(deviceInstance, image.pointer);
        }
        long byteSize = subLayout.rowPitch != 0 ? subLayout.rowPitch * image.getExtent().height : subLayout.size;
        int size = (int) (byteSize / divisor);

        MemoryBuffer tempMemory = createStagingBuffer(size, BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT.value);
        queue.cmdCopyImageToBuffer(image, ImageLayout.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, tempMemory, subLayers);
        ImageSubresourceRange range = new ImageSubresourceRange();
        queue.cmdImageMemoryBarrier2(PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                AccessFlagBits2.VK_ACCESS_2_MEMORY_READ_BIT.value,
                PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                AccessFlagBits2.VK_ACCESS_2_MEMORY_WRITE_BIT.value, image, range);
        return tempMemory;
    }

    @Override
    public void copyFromHostAvailableMemory(MemoryBuffer memory, ByteBuffer buffer, Queue queue) {
        long destPointer = mapMemory(memory.getBoundMemory(), 0);
        long minAlignment = limits.getMinMemoryMapAlignment();
        if (destPointer % minAlignment != 0) {
            throw new IllegalArgumentException();
        }
        ByteBuffer deviceBuffer = MemoryUtil.memByteBuffer(destPointer, buffer.remaining());
        buffer.put(deviceBuffer);
        unmapMemory(memory.getBoundMemory());
    }

    @Override
    public void copyToBuffer(MemoryBuffer memory, ByteBuffer buffer) {
        long destPointer = mapMemory(memory.getBoundMemory(), memory.getBoundOffset());
        try {
            ByteBuffer deviceBuffer = MemoryUtil.memByteBuffer(destPointer, (int) memory.size);
            buffer.put(deviceBuffer);
        } finally {
            unmapMemory(memory.getBoundMemory());
        }
    }

    @Override
    public void copyToBuffer(ImageMemory image, ByteBuffer buffer) {
        long size = image.getMemory().size;
        long destPointer = mapMemory(image.getMemory(), 0);
        try {
            ByteBuffer deviceBuffer = MemoryUtil.memByteBuffer(destPointer, (int) size);
            buffer.put(deviceBuffer);
        } finally {
            unmapMemory(image.getMemory());
        }
    }

    @Override
    public Memory allocateMemory(long sizeInBytes, int memoryProperties) {
        return allocateMemory(sizeInBytes, memoryProperties, 0, 0);
    }

    @Override
    public Memory allocateMemory(long sizeInBytes, int memoryProperties, int allocateFlags, int deviceMask) {
        long start = System.currentTimeMillis();
        VkMemoryAllocateFlagsInfo flagsInfo = null;
        if (allocateFlags > 0) {
            flagsInfo = VkMemoryAllocateFlagsInfo.calloc()
                    .deviceMask(deviceMask)
                    .flags(allocateFlags)
                    .sType(VK11.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_FLAGS_INFO)
                    .pNext(MemoryUtil.NULL);
        }
        int memoryTypeIndex = deviceMemoryProperties.getMemoryTypeIndex(memoryProperties);
        MemoryType mt = deviceMemoryProperties.getMemoryType(memoryTypeIndex);
        VkMemoryAllocateInfo allocateInfo = VkMemoryAllocateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                .pNext(flagsInfo != null ? flagsInfo.address() : MemoryUtil.NULL)
                .allocationSize(sizeInBytes)
                .memoryTypeIndex(memoryTypeIndex);
        try {
            lb.position(0);
            VulkanBackend.assertResult(VK12.vkAllocateMemory(deviceInstance, allocateInfo, null, lb));
            Memory memory = new Memory(allocateInfo.allocationSize(), mt.flags, lb.get(0), allocateFlags);
            if (memoryAllocations.contains(memory)) {
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_VALUE.message + "Already contains pointer: " + memory.pointer);
            }
            memoryAllocations.add(memory);
            Logger.d(getClass(),
                    "Allocated memory with size: " + sizeInBytes + ", properties: "
                            + BitFlags.toString(mt.flags) + ", " + (System.currentTimeMillis() - start)
                            + " millis");
            return memory;
        } finally {
            allocateInfo.free();
            if (flagsInfo != null) {
                flagsInfo.free();
            }
        }

    }

    @Override
    public MemoryBuffer createBuffer(long size, int usage) {
        // Make sure size is compatible with copy size.
        size = DeviceMemory.getPaddedBufferSize(size);
        VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .size(size)
                .usage(usage)
                .sharingMode(VK12.VK_SHARING_MODE_EXCLUSIVE);

        lb.position(0);
        VulkanBackend.assertResult(VK12.vkCreateBuffer(deviceInstance, bufferCreateInfo, null, lb));
        long bufferPointer = lb.get(0);
        VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc();
        VK12.vkGetBufferMemoryRequirements(deviceInstance, bufferPointer, memoryRequirements);
        long allocateSize = memoryRequirements.size();
        MemoryBuffer result = new MemoryBuffer(bufferPointer, size, memoryRequirements.alignment(), allocateSize,
                usage);
        Logger.d(getClass(), "Created memorybuffer " + result);
        bufferCreateInfo.free();
        return result;
    }

    @Override
    public void copyToDeviceMemory(ImageBuffer sourceImage, FormatProperties sourceProperties,
            ImageMemory destinationImage, FormatProperties destinationProperties, ImageSubresourceLayers subLayer,
            Queue queue) {
        if (destinationImage.getImage().getTiling() == ImageTiling.VK_IMAGE_TILING_LINEAR) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE + "Tiling must be optimal");
        } else {
            int destSize = sourceImage.width * sourceImage.height * sourceImage.faceCount
                    * destinationImage.format.sizeInBytes;
            createTransferStagingBuffer(destSize);
            // Copy from source image to staging buffer
            directoCopyToHostVisibleMemory(sourceImage, transferStagingBuffer, destinationImage.format);
            queue.queueBegin();
            // Copy from staging buffer to image memory (device)
            queue.cmdCopyBufferToImage(transferStagingBuffer, destinationImage, subLayer);
            queue.queueWaitIdle();
        }
    }

    @Override
    public void directoCopyToHostVisibleMemory(ImageBuffer sourceImage, MemoryBuffer destinationBuffer,
            Vulkan10.Format destinationFormat) {
        long start = System.currentTimeMillis();
        if ((destinationBuffer.usage & BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_SRC_BIT.value)
                != BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_SRC_BIT.value) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message
                    + "MemoryBuffer must have usage flag VK_BUFFER_USAGE_TRANSFER_SRC_BIT");
        }
        if ((destinationBuffer.getBoundMemory().memoryProperties &
                MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT.value) == 0) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + " Destination buffer must have memory property flag "
                            + MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT.getBitName());
        }
        int width = sourceImage.width;
        int height = sourceImage.height * sourceImage.faceCount;
        int destSize = width * height * destinationFormat.sizeInBytes;
        if (destinationBuffer.size < destSize) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Destination too small: "
                    + destinationBuffer.size + ", needs to be " + destSize);
        }
        long destPointer = mapMemory(destinationBuffer.getBoundMemory(), destinationBuffer.getBoundOffset());
        sourceImage.getBuffer().rewind();
        Vulkan10.Format format = Vulkan10.Format.get(sourceImage.getFormat().value);
        copyImage(sourceImage.getBuffer(), format, width, height,
                width * (sourceImage.format.sizeInBytes), destPointer, destinationFormat);
        unmapMemory(destinationBuffer.getBoundMemory());
        Logger.d(getClass(), "Copy " + width + ", " + height + " * " + sourceImage.faceCount
                + " faces to devicememory took "
                + (System.currentTimeMillis() - start) + " millis");
    }

    @Override
    public int updateBuffer(ByteBuffer buffer, MemoryBuffer memory, Queue queue) {
        return updateBuffer(buffer, memory, 0, queue);
    }

    @Override
    public int updateBuffer(ByteBuffer buffer, MemoryBuffer memory, int destOffset, Queue queue) {
        buffer.mark();
        int size = buffer.remaining();
        if (size > Queue.UPDATE_BUFFER_MAX_BYTES) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Buffer too large for update: "
                    + size);
        }
        if (size % DeviceMemory.MIN_BUFFER_DATASIZE != 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Size must be multiple of " + DeviceMemory.MIN_BUFFER_DATASIZE);
        }
        queue.cmdUpdateBuffer(buffer, memory, destOffset);
        queue.cmdBufferMemoryBarrier2(PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                AccessFlagBits2.VK_ACCESS_2_MEMORY_READ_BIT.value,
                PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                AccessFlagBits2.VK_ACCESS_2_MEMORY_WRITE_BIT.value, memory);
        return size;
    }

    @Override
    public void updateBuffers(ByteBuffer[] sourceBuffers, MemoryBuffer[] deviceBuffers, Queue queue) {
        if (sourceBuffers != null) {
            for (int i = 0; i < sourceBuffers.length; i++) {
                if (sourceBuffers[i] != null) {
                    // When vertexbuffers are created the size is padded to multiple of minimum buffer size to
                    // be compatible with copy commands.
                    ByteBuffer byteBuffer = sourceBuffers[i];
                    if (byteBuffer.remaining() <= Queue.UPDATE_BUFFER_MAX_BYTES) {
                        updateBuffer(byteBuffer, deviceBuffers[i], 0, queue);
                    } else {
                        // When buffer is updated by queue the bytesize must be aligned to 4.
                        copyToDeviceMemory(byteBuffer, deviceBuffers[i], 0, queue);
                        queue.queueBegin();
                    }
                }
            }
        }
    }

    @Override
    public void updateBuffers(MemoryBuffer deviceBuffer, Queue queue, ByteBuffer... sourceBuffers) {
        if (sourceBuffers != null) {
            int offset = 0;
            for (int i = 0; i < sourceBuffers.length; i++) {
                if (sourceBuffers[i] != null) {
                    ByteBuffer byteBuffer = sourceBuffers[i];
                    if (byteBuffer.remaining() <= Queue.UPDATE_BUFFER_MAX_BYTES) {
                        offset += updateBuffer(byteBuffer, deviceBuffer, offset, queue);
                    } else {
                        // When buffer is updated by queue the bytesize must be aligned to 4.
                        offset += copyToDeviceMemory(byteBuffer, deviceBuffer, offset, queue);
                        queue.queueBegin();
                    }
                }
            }
        }
    }

    @Override
    public int copyToDeviceMemory(ByteBuffer buffer, MemoryBuffer memory, Queue queue) {
        return copyToDeviceMemory(buffer, memory, 0, queue);
    }

    @Override
    public int copyToDeviceMemory(ByteBuffer buffer, MemoryBuffer memory, int destOffset, Queue queue) {
        buffer.mark();
        int size = buffer.remaining();
        if ((memory.getBoundMemory().memoryProperties & VK12.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) != 0) {
            mapMemory(memory.getBoundMemory(), memory.getBoundOffset());
            long destPointer = memory.getBoundMemory().getMapped();
            ByteBuffer deviceBuffer = MemoryUtil.memByteBuffer(destPointer, (int) memory.size);
            deviceBuffer.position(destOffset);
            deviceBuffer.put(buffer);
            unmapMemory(memory.getBoundMemory());
        } else {
            if (size <= Queue.UPDATE_BUFFER_MAX_BYTES) {
                Logger.d(getClass(), "Buffer size smaller than 65536 - consider using updateBuffer()");
            }
            createTransferStagingBuffer(size);
            long destPointer = mapMemory(transferStagingBuffer.getBoundMemory(), 0);
            long minAlignment = limits.getMinMemoryMapAlignment();
            if (destPointer % minAlignment != 0) {
                throw new IllegalArgumentException();
            }
            ByteBuffer deviceBuffer = MemoryUtil.memByteBuffer(destPointer, size);
            deviceBuffer.position(0);
            deviceBuffer.put(buffer);
            buffer.reset();
            queue.queueBegin();
            queue.cmdCopyBuffer(transferStagingBuffer, memory, 0, destOffset, size);
            queue.cmdBufferMemoryBarrier2(PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                    AccessFlagBits2.VK_ACCESS_2_MEMORY_READ_BIT.value,
                    PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                    AccessFlagBits2.VK_ACCESS_2_MEMORY_WRITE_BIT.value, memory);
            queue.queueWaitIdle();
            unmapMemory(transferStagingBuffer.getBoundMemory());
        }
        return size;
    }

    @Override
    public void allocateStagingBuffer(int size) {
        if (transferStagingBuffer != null) {
            if (transferStagingBuffer.size < size) {
                freeStagingBuffer();
            } else {
                return;
            }
        }
        transferStagingBuffer = createStagingBuffer(size, BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_SRC_BIT.value);
    }

    @Override
    public void freeStagingBuffer() {
        if (transferStagingBuffer != null) {
            unbindBuffer(transferStagingBuffer);
            freeMemory(transferStagingBuffer.getBoundMemory());
            transferStagingBuffer = null;
        }
    }

    @Override
    public void freeMemory(@NonNull Memory memory) {
        if (!memoryAllocations.contains(memory)) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Not allocated here "
                    + memory.pointer);
        }
        VK12.vkFreeMemory(deviceInstance, memory.pointer, null);
        Logger.d(getClass(), "Freed memory with size " + memory.size + ", pointer " + memory.pointer);
        memoryAllocations.remove(memory);
    }

    @Override
    public void freeImageMemory(ImageMemory imageMemory) {
        if (!memoryAllocations.contains(imageMemory.getMemory())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Not allocated here");
        }
        unbindImage(imageMemory);
        VK12.vkFreeMemory(deviceInstance, imageMemory.getMemory().pointer, null);
        memoryAllocations.remove(imageMemory.getMemory());
    }

    @Override
    public long getAllocatedMemorySize() {
        long result = 0;
        for (Memory memory : memoryAllocations) {
            result += memory.size;
        }
        return result;
    }

    @Override
    public Memory[] getAllocatedMemory() {
        return memoryAllocations.toArray(new Memory[0]);
    }

    @Override
    public ImageMemory[] getAllocatedImageMemory() {
        ArrayList<ImageMemory> images = new ArrayList<ImageMemory>();
        for (Set<ImageMemory> boundMemory : boundImageMemory.values()) {
            images.addAll(boundMemory);
        }
        return images.toArray(new ImageMemory[0]);
    }

    private MemoryBuffer[] allocateVertexMemory(int bufferUsage, JSONBuffer... buffers) {
        MemoryBuffer[] result = new MemoryBuffer[buffers.length];
        int index = 0;
        for (JSONBuffer buffer : buffers) {
            result[0] = createBuffer(buffer.getByteLength(), bufferUsage);
            long size = result[index].allocationSize + result[index].alignment - (result[index].allocationSize
                    % result[index].alignment);
            Memory vertexMemory = allocateMemory(size,
                    BitFlags.getFlagsValue(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));
            bindBufferMemory(vertexMemory, result[index]);
        }
        return result;
    }

    @Override
    public VertexMemory allocateVertexMemory(int indexUsage, VertexBuffer[] indexBuffers, int vertexUsage,
            VertexBuffer[] vertexBuffers) {
        MemoryBuffer[] indexArray = internalAllocate(indexUsage, indexBuffers);
        MemoryBuffer[] attributeArray = internalAllocate(vertexUsage, vertexBuffers);
        return new VertexMemory(indexArray, indexBuffers, attributeArray, vertexBuffers);
    }

    private MemoryBuffer[] internalAllocate(int usage, VertexBuffer[] buffers) {
        MemoryBuffer[] result = null;
        if (buffers != null && buffers.length > 0) {
            result = new MemoryBuffer[buffers.length];
            for (int index = 0; index < buffers.length; index++) {
                VertexBuffer buffer = buffers[index];
                if (buffer != null) {
                    MemoryBuffer memBuff = createBuffer(buffer.sizeInBytes, usage);
                    Memory vertexMemory = allocateMemory(memBuff.allocationSize,
                            BitFlags.getFlagsValue(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));
                    bindBufferMemory(memBuff, vertexMemory, 0);
                    result[index] = memBuff;
                }
            }
        }
        return result;
    }

    @Override
    public VertexMemory allocateVertexMemory(int bufferUsage, JSONBuffer[] indexBuffers,
            JSONBuffer[] attributeBuffers) {
        MemoryBuffer[] attributeArray = new MemoryBuffer[attributeBuffers.length];
        MemoryBuffer[] indexArray = new MemoryBuffer[indexBuffers.length];
        long totalSize = 0;
        for (int index = 0; index < attributeBuffers.length; index++) {
            JSONBuffer buffer = attributeBuffers[index];
            if (buffer != null) {
                MemoryBuffer memBuff = createBuffer(buffer.getByteLength(), bufferUsage);
                totalSize += memBuff.allocationSize + memBuff.alignment - (memBuff.allocationSize % memBuff.alignment);
                attributeArray[index] = memBuff;
            }
        }
        for (int index = 0; index < indexArray.length; index++) {
            JSONBuffer buffer = indexBuffers[index];
            if (buffer != null) {
                MemoryBuffer memBuff = createBuffer(buffer.getByteLength(), bufferUsage
                        | BufferUsageFlagBit.VK_BUFFER_USAGE_INDEX_BUFFER_BIT.value);
                totalSize += memBuff.allocationSize + memBuff.alignment - (memBuff.allocationSize % memBuff.alignment);
                indexArray[index] = memBuff;
            }
        }
        Memory vertexMemory = allocateMemory(totalSize,
                BitFlags.getFlagsValue(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));
        long offset = bindBufferMemory(vertexMemory, attributeArray);
        bindBufferMemory(vertexMemory, offset, indexArray);
        return new VertexMemory(indexArray, attributeArray, indexBuffers, attributeBuffers);

    }

    private VertexMemory allocateVertexMemory(int bufferUsage, JSONBufferView... bufferViews) {
        if (bufferViews == null) {
            return null;
        }
        MemoryBuffer[] result = new MemoryBuffer[bufferViews.length];
        long totalSize = 0;
        int index = 0;
        for (JSONBufferView bufferView : bufferViews) {
            Target target = bufferView.getTarget();
            if (target != null) {
                if (target == Target.ELEMENT_ARRAY_BUFFER) {
                    bufferUsage |= BufferUsageFlagBit.VK_BUFFER_USAGE_INDEX_BUFFER_BIT.value;
                }
                result[index] = createBuffer(bufferView.getByteLength(), bufferUsage);
                totalSize += result[index].allocationSize + result[index].alignment - (result[index].allocationSize
                        % result[index].alignment);
            }
            index++;
        }
        Memory vertexMemory = allocateMemory(totalSize,
                BitFlags.getFlagsValue(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));
        bindBufferMemory(vertexMemory, result);
        return new VertexMemory(result, bufferViews);
    }

    @Override
    public VertexMemory allocateVertexMemory(Mode mode, int bufferUsage, JSONBufferView... bufferViews) {
        if (bufferViews == null) {
            return null;
        }
        switch (mode) {
            case BUFFERVIEWS:
                return allocateVertexMemory(bufferUsage, bufferViews);
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", mode");
        }
    }

    @Override
    public void logMemory() {
        Logger.d(getClass(), "Total allocated memory: " + getAllocatedMemorySize());
        for (Memory memory : getAllocatedMemory()) {
            Logger.d(getClass(), memory.toString());
        }
    }

    @Override
    public Vulkan10.Format getMemoryFormat(ImageBuffer image) {
        // Se mandatory formats :
        // https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#features-required-format-support
        Vulkan10.Format format = Vulkan10.Format.get(image.format.value);
        switch (format) {
            case VK_FORMAT_B8G8R8_UNORM:
            case VK_FORMAT_R8G8B8_UNORM:
                // TODO - check if device supports VK_FORMAT_R8G8B8_UNORM
                return Vulkan10.Format.VK_FORMAT_R8G8B8A8_UNORM;
            case VK_FORMAT_R16G16B16A16_UNORM:
                return Vulkan10.Format.VK_FORMAT_R16G16B16A16_UNORM;
            case VK_FORMAT_B8G8R8_SRGB:
            case VK_FORMAT_R8G8B8_SRGB:
                // TODO - check if device supports VK_FORMAT_R8G8B8_SRGB
                return Vulkan10.Format.VK_FORMAT_R8G8B8A8_SRGB;
            case VK_FORMAT_B8G8R8A8_UNORM:
            case VK_FORMAT_A8B8G8R8_UNORM_PACK32:
                return Vulkan10.Format.VK_FORMAT_R8G8B8A8_UNORM;
            case VK_FORMAT_B8G8R8A8_SRGB:
                return Vulkan10.Format.VK_FORMAT_R8G8B8A8_SRGB;
            case VK_FORMAT_R8_UNORM:
            case VK_FORMAT_R8G8_UNORM:
            case VK_FORMAT_R8G8B8A8_UNORM:
            case VK_FORMAT_R8G8B8A8_SRGB:
                return format;
            case VK_FORMAT_R32G32B32_SFLOAT:
                return Vulkan10.Format.VK_FORMAT_R32G32B32A32_SFLOAT;
            case VK_FORMAT_R16G16B16_SFLOAT:
                return Vulkan10.Format.VK_FORMAT_R16G16B16A16_SFLOAT;
            default:
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_VALUE.message + ", not supported " + image.format);

        }
    }

    @Override
    public void uploadBuffers(Queue queue, @NonNull DescriptorBuffers buffers,
            @NonNull DescriptorSetTarget... descriptorSetTargets) {
        for (DescriptorSetTarget target : descriptorSetTargets) {
            BindBuffer buffer = buffers.getBuffer(target);
            uploadBuffer(queue, buffer);
        }
    }

    @Override
    public void uploadBuffer(Queue queue, BindBuffer buffer) {
        if (buffer != null && buffer.getState() == BufferState.updated) {
            queue.queueBegin();
            if (buffer.getBuffer().size <= Queue.UPDATE_BUFFER_MAX_BYTES) {
                updateBuffer(buffer.getBackingBuffer(), buffer.getBuffer(), queue);
            } else {
                copyToDeviceMemory(buffer.getBackingBuffer(),
                        buffer.getBuffer(), queue);
                queue.queueBegin();
            }
            buffer.setState(BufferState.copiedToDevice);
        }
    }

}

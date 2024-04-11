
package org.varg.vulkan.memory;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.data.VertexBuffer;
import org.gltfio.gltf2.JSONBuffer;
import org.gltfio.gltf2.JSONBufferView;
import org.ktximageio.ktx.ImageBuffer;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.uniform.BindBuffer;
import org.varg.uniform.DescriptorBuffers;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.ImageAspectFlagBit;
import org.varg.vulkan.Vulkan10.MemoryPropertyFlagBit;
import org.varg.vulkan.image.Image;
import org.varg.vulkan.image.ImageCreateInfo;
import org.varg.vulkan.image.ImageSubresourceLayers;
import org.varg.vulkan.memory.VertexMemory.Mode;
import org.varg.vulkan.structs.FormatProperties;

/**
 * Allocator for device memory and methods for copying to device memory.
 *
 */
public interface DeviceMemory {

    /**
     * Returns the padded buffersize to align to MIN_BUFFER_DATASIZE
     * 
     * @param size
     * @return
     */
    static long getPaddedBufferSize(long size) {
        return size + ((size % DeviceMemory.MIN_BUFFER_DATASIZE) == 0 ? 0 : DeviceMemory.MIN_BUFFER_DATASIZE
                - (size % DeviceMemory.MIN_BUFFER_DATASIZE));
    }

    /**
     * Allocated buffer size should factor of this, regardless of device alignment.
     */
    int MIN_BUFFER_DATASIZE = 4;

    /**
     * Creates a buffer of requested size for the specified usage, the created buffer size will be >= size.
     * Size of created buffer can be read from {@link MemoryBuffer#size}
     * 
     * @param size
     * @param usage
     * @return
     */
    MemoryBuffer createBuffer(long size, int usage);

    /**
     * Binds the buffer to memory
     * This shall be the only entrypoint to bind a buffer to memory!
     * 
     * @param buffer
     * @param memory May be null to allocate memory
     * @param memoryOffset
     */
    void bindBufferMemory(MemoryBuffer buffer, Memory memory, long memoryOffset);

    /**
     * Binds the buffer to memory
     * This shall be the only entrypoint to bind a buffer to memory!
     * 
     * @param memoryOffset
     */
    long bindBufferMemory(Memory memory, long memoryOffset, MemoryBuffer... memoryBuffers);

    /**
     * Frees the memory that has been allocated with #allocateMemory
     * 
     * @param memory
     * @throws IllegalArgumentException If memory was not allocated by this devicememory, has already been freed
     * or is bound to a buffer.
     */
    void freeMemory(Memory memory);

    /**
     * Allocates and binds vertex buffer memory
     * 
     * @return
     */
    VertexMemory allocateVertexMemory(int indexUsage, VertexBuffer[] indexBuffers, int vertexUsage, VertexBuffer[] vertexBuffers);

    /**
     * Allocates memory, creates buffers and binds to the memory - use this to allocate vertex memory.
     * 
     * @param bufferViews
     * @return Bound memorybuffers with memory allocated to fit
     */
    VertexMemory allocateVertexMemory(Mode mode, int bufferUsage, JSONBufferView... bufferViews);

    /**
     * Allocates memory, creates buffers and binds to the memory - use this to allocate vertex memory.
     * 
     * @return Bound memorybuffers with memory allocated to fit
     */
    VertexMemory allocateVertexMemory(int bufferUsage, JSONBuffer[] indexBuffers, JSONBuffer[] vertexBuffers);

    /**
     * Frees the buffers and memory that is allocted for the buffers
     * If array contains more than one BufferMemory they must be connected to the same Memory
     * 
     * @param buffer
     * @throws IllegalArgumentException If buffer is not bound to memory or has already been freed
     */
    void freeBuffer(MemoryBuffer... buffers);

    /**
     * Frees the buffers and memory
     * 
     * @param buffers
     * @throws IllegalArgumentException If buffer is not bound to memory or has already been freed
     */
    void freeBuffer(BindBuffer... buffers);

    /**
     * Allocates memory with the specified size in bytes and memory flags
     * 
     * @param sizeInBytes
     * @param memoryProperties
     * @return
     */
    Memory allocateMemory(long sizeInBytes, int memoryProperties);

    /**
     * Allocates memory with the specified size in byte, memory flags and uses the allocateFlag and deviceMask
     * 
     * @param sizeInBytes
     * @param memoryProperties
     * @param allocateFlag
     * @param deviceMask
     * @return
     */
    Memory allocateMemory(long sizeInBytes, int memoryProperties, int allocateFlag, int deviceMask);

    /**
     * Allocates and binds image memory, the allocated memory must be freed by calling
     * {@link #freeImageMemory(ImageMemory)}
     * when it is not needed anymore.
     * 
     * @param createInfo
     * @param properties
     * @return
     */
    ImageMemory allocateImageMemory(ImageCreateInfo createInfo, MemoryPropertyFlagBit... properties);

    /**
     * Free the image memory allocated by calling {@link #allocateImageMemory(ImageCreateInfo, MemoryPropertyFlagBit[])}
     * After this method returns it is an error to use the imageMemory.
     * 
     * @param imageMemory
     * @throws IllegalArgumentException If imageMemory was not allocated by this devicememory or has already been freed
     */
    void freeImageMemory(ImageMemory imageMemory);

    /**
     * Host map the memory and return pointer to it
     * 
     * @param memory
     * @param offset is a zero-based byte offset from the beginning of the memory object.
     * @return pointer to the mapped memory
     * @throws RuntimeException If the memory could not be mapped
     */
    long mapMemory(Memory memory, long offset);

    /**
     * Unmap the specified memory when access to it is not needed anymore
     * The mapped memory should be invalidated and/or flushed as needed so that it is available.
     * 
     * @param memory
     */
    void unmapMemory(Memory memory);

    /**
     * Pre - allocates stagingbuffer,to be used in copy methods
     * 
     * @param size
     */
    void allocateStagingBuffer(int size);

    /**
     * Frees the allocated staging buffer
     */
    void freeStagingBuffer();

    /**
     * Copies the image to device memory
     * Stagingbuffer is allocated, a direct copy from source image to staging buffer is done.
     * If queue is not recording, queueBegin() is called.
     * Then the staging buffer is copied to destination (device) and wait for idle so that staging buffer can
     * be freed or re-used
     * Queue will be idle after this method returns.
     * 
     * @param sourceImage
     * @param sourceProperties
     * @param destinationImage
     * @param destinationProperties
     * @param subLayer
     * @param queue
     */
    void copyToDeviceMemory(ImageBuffer sourceImage, FormatProperties sourceProperties,
            ImageMemory destinationImage, FormatProperties destinationProperties, ImageSubresourceLayers subLayer,
            Queue queue);

    /**
     * Copy image from the ImageBuffer to MemoryBuffer that is in host visible memory.
     * The MemoryBuffer must have VK_BUFFER_USAGE_TRANSFER_SRC_BIT set and the memory must be allocated with
     * VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT flag.
     * Source image and destination buffer MUST have the same bitdepth - but may have different component layout and
     * count.
     * The copy will be done immediately by mapping destination buffer.
     * 
     * @param sourceImage The image data to copy
     * @param destinationBuffer MemoryBuffer to copy image data into - must have VK_BUFFER_USAGE_TRANSFER_SRC_BIT
     * @param destinationFormat Destination format - this MUST match the source image in number of components and size
     * in bytes but may have different component layout eg BGR - RGB
     * @throws IllegalArgumentException If memory does not have usage bit VK_BUFFER_USAGE_TRANSFER_SRC_BIT
     */
    void directoCopyToHostVisibleMemory(ImageBuffer sourceImage, MemoryBuffer destinationBuffer,
            Vulkan10.Format destinationFormat);

    /**
     * Copies the ByteBuffer, starting at the current position, to device buffer memory
     * This will copy using a staging buffer that his host visible, then copy from staging buffer to destination
     * memory.
     * Queue shall be in recording state - will be in pending state when method returns.
     * 
     * @param buffer
     * @param memory
     * @param queue
     */
    int copyToDeviceMemory(ByteBuffer buffer, MemoryBuffer memory, Queue queue);

    /**
     * Copies the ByteBuffer, starting at the current position, to device buffer memory
     * This will copy using a staging buffer that his host visible, then copy from staging buffer to destination
     * memory.
     * Queue shall be in recording state - will be in pending state when method returns.
     * 
     * @param buffer
     * @param memory
     * @param destination offset
     * @param queue
     */
    int copyToDeviceMemory(ByteBuffer buffer, MemoryBuffer memory, int destOffset, Queue queue);

    /**
     * Copies buffer to device memory using updateBuffer() - size in bytes MUST be < 65536
     * Does not synchronize or wait for idle - queue must be in recording state
     * 
     * @param buffer Source of data to copy - copies remaining bytes
     * @param memory Destination
     * @param queue
     */
    int updateBuffer(ByteBuffer buffer, MemoryBuffer memory, Queue queue);

    /**
     * Copies buffer to device memory using updateBuffer() - size in bytes MUST be < 65536
     * Does not synchronize or wait for idle - queue must be in recording state
     * 
     * @param buffer Source of data to copy - copies remaining bytes
     * @param memory Destination
     * @param destination offset in bytes
     * @param queue
     */
    int updateBuffer(ByteBuffer buffer, MemoryBuffer memory, int destOffset, Queue queue);

    /**
     * Copies the source buffers to device memory, using update or copy depending on size
     * 
     * @param sourceBuffers
     * @param deviceBuffers
     * @param queue
     */
    void updateBuffers(ByteBuffer[] sourceBuffers, MemoryBuffer[] deviceBuffers, Queue queue);

    /**
     * Copies one or more bytebuffers to one device memorybuffer, using update or copy depending on size
     * 
     * @param deviceBuffer
     * @param queue
     * @param sourceBuffers One or more source buffers.
     */
    void updateBuffers(MemoryBuffer deviceBuffer, Queue queue, ByteBuffer... sourceBuffers);

    /**
     * Copies memory from device buffer memory to the destination buffer
     * 
     * @param memory
     * @param buffer
     */
    void copyToBuffer(MemoryBuffer memory, ByteBuffer buffer);

    /**
     * Copies from device image memory to the destination buffer
     */
    void copyToBuffer(ImageMemory image, ByteBuffer buffer);

    /**
     * Copies from device memory to staging buffer.
     * This staging buffer is then returned and the result will be available after queue is submitted and idle.
     * The caller MUST free the returned MemoryBuffer when done.
     * Queue must be in recording state
     * 
     * @param memory The memory that will be copied
     * @param count Number of bytes to copy - or -1 to copy the whole buffer
     * @param queue Must be in recording state
     * @return Staging buffer that is available after synchronization (for instance wait idle)
     */
    MemoryBuffer copyFromDeviceMemory(MemoryBuffer memory, int count, Queue queue);

    /**
     * Copies miplevel 0, arraylayer 0, from device memory to staging buffer.
     * This staging buffer is then returned and the result will be available after queue is submitted and idle.
     * The caller MUST free the returned MemoryBuffer when done.
     * Queue must be in recording state
     * 
     * @param image
     * @param flagBit
     * @param queue Must be in recording state
     * @return Staging buffer, result will be available after queue is idle or similar.
     */
    MemoryBuffer copyFromDeviceMemory(Image image, ImageAspectFlagBit flagBit, Queue queue);

    /**
     * Copies the subresourcelayers from device memory to staging buffer
     * This staging buffer is then returned and the result will be available after queue is submitted and idle.
     * The caller MUST free the returned MemoryBuffer when done.
     * Queue must be in recording state
     * 
     * @param image
     * @param flagBit
     * @param subLayers
     * @param queue
     * @return Staging buffer, result will be available after queue is idle or similar.
     */
    MemoryBuffer copyFromDeviceMemory(Image image, ImageAspectFlagBit flagBit, ImageSubresourceLayers subLayers,
            Queue queue);

    /**
     * Copies host available memory, without any synchronization, to bytebuffer.
     * Memory must be bound.
     * 
     * @param memory
     * @param buffer
     * @param queue
     * @return
     */
    void copyFromHostAvailableMemory(MemoryBuffer memory, ByteBuffer buffer, Queue queue);

    /**
     * Returns the total size of allocated memory in bytes, that is not freed.
     * 
     * @return Current allocation size in bytes
     */
    long getAllocatedMemorySize();

    /**
     * Returns a shallow copy of allocated memory, this will include all memory types both for buffers and images - DO
     * NOT MODIFY
     * This method is for debugging and statistics ONLY
     * 
     * @return
     */
    Memory[] getAllocatedMemory();

    /**
     * Returns an array of the allocated image memory - DO NOT MODIFY
     * This method is for debugging and statistics ONLY
     * 
     * @return
     */
    ImageMemory[] getAllocatedImageMemory();

    /**
     * Logs the allocated memory
     */
    void logMemory();

    /**
     * Returns the memory format that shall be used when allocating memory for the ImageBuffer
     * 
     * @param image
     * @return
     */
    Vulkan10.Format getMemoryFormat(ImageBuffer image);

    /**
     * Internal method
     * TODO Shall this be hidden?
     * If buffer is not in state BufferState.updated then the backing uniform buffer is uploaded to device memory.
     * Call this method to update a buffer where the contents of the backing buffer has been updated.
     * 
     * It is an error to call this method in an ongoing renderpass.
     * 
     * @param buffer
     */
    void uploadBuffer(Queue queue, BindBuffer buffer);

    /**
     * 
     * @param buffers
     * @param descriptorSetTargets
     */
    void uploadBuffers(Queue queue, @NonNull DescriptorBuffers buffers,
            @NonNull DescriptorSetTarget... descriptorSetTargets);

}

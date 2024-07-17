package org.varg.vulkan.memory;

import java.nio.ByteBuffer;

import org.gltfio.data.VertexBuffer;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.vertex.BindVertexBuffers;

/**
 * Collection of vulkan memory, memorybuffer(s) and offsets - this is a mapping to GPU storage
 */
public class VertexMemory {

    private MemoryBuffer[] memoryBuffers;
    private ByteBuffer[] buffers;
    private int[] offsets;
    private ByteBuffer[] indexBuffers;
    private MemoryBuffer[] indexMemory;
    private BindVertexBuffers bindBuffers;
    private BindVertexBuffers bindIndexBuffers;

    public VertexMemory(MemoryBuffer[] indexMemory, VertexBuffer[] indexSourceBuffers, MemoryBuffer[] attributeBuffers, VertexBuffer[] sourceBuffers) {
        if (attributeBuffers.length != sourceBuffers.length) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Arrays must match in size");
        }
        this.memoryBuffers = attributeBuffers;
        this.buffers = new ByteBuffer[sourceBuffers.length];
        for (int i = 0; i < sourceBuffers.length; i++) {
            this.buffers[i] = sourceBuffers[i] != null ? sourceBuffers[i].getAsReadOnlyBuffer() : null;
        }
        if (indexMemory != null) {
            this.indexMemory = indexMemory;
            this.indexBuffers = new ByteBuffer[indexSourceBuffers.length];
            for (int i = 0; i < indexMemory.length; i++) {
                this.indexBuffers[i] = indexSourceBuffers[i] != null ? indexSourceBuffers[i].getAsReadOnlyBuffer()
                        : null;
            }
        }
        createBindBuffers();
    }

    /**
     * Returns the buffers to bind vertex data before issuing drawcalls.
     * 
     * @return
     */
    public BindVertexBuffers getBindVertexBuffers() {
        return bindBuffers;
    }

    /**
     * Returns the indexbuffers to bind before issuing drawcalls, or null if arrayed drawing.
     * 
     * @return
     */
    public BindVertexBuffers getBindIndexBuffers() {
        return bindIndexBuffers;
    }

    /**
     * Creates the buffers needed to bind vertex and index memory before issuing drawcalls
     */
    private void createBindBuffers() {
        MemoryBuffer[] buffers = getMemoryBuffers();
        MemoryBuffer[] indexBuffers = getIndexMemoryBuffers();
        bindBuffers = new BindVertexBuffers(0, buffers, new long[buffers.length]);
        bindIndexBuffers = null;
        if (indexBuffers != null && indexBuffers.length > 0) {
            bindIndexBuffers = new BindVertexBuffers(0, indexBuffers, new long[indexBuffers.length]);
        }
    }

    /**
     * Returns the array of offset
     * 
     * @return
     */
    public int[] getOffsets() {
        return offsets;
    }

    /**
     * Returns the array with memorybuffer
     * 
     * @return
     */
    public MemoryBuffer[] getMemoryBuffers() {
        return memoryBuffers;
    }

    /**
     * Returns the array of bytebuffers
     * 
     * @return
     */
    public ByteBuffer[] getBuffers() {
        return buffers;
    }

    /**
     * Returns the array with indexbuffers
     * 
     * @return
     */
    public ByteBuffer[] getIndexBuffers() {
        return indexBuffers;
    }

    /**
     * Returns the index memory device memory buffers
     * 
     * @return
     */
    public MemoryBuffer[] getIndexMemoryBuffers() {
        return indexMemory;
    }

    /**
     * Returns the offset into the buffer memory for the buffer view index.
     * 
     * @param bufferViewIndex
     * @return
     */
    public int getMemoryBufferOffset(int bufferViewIndex) {
        return offsets != null ? offsets[bufferViewIndex] : 0;
    }

    /**
     * Frees the device memory
     * 
     * @param deviceMemory
     */
    public void freeDeviceMemory(DeviceMemory deviceMemory) {
        if (memoryBuffers != null) {
            deviceMemory.freeBuffer(memoryBuffers);
            memoryBuffers = null;
        }
        if (indexMemory != null) {
            deviceMemory.freeBuffer(indexMemory);
            indexMemory = null;
        }
    }

}

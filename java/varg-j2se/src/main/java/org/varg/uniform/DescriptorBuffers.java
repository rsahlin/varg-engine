package org.varg.uniform;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.lib.ErrorMessage;
import org.varg.gltf.VulkanMesh;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.renderer.GltfRenderer;
import org.varg.shader.Shader;
import org.varg.uniform.BindBuffer.BufferState;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.memory.Memory;

/**
 * Base class to handle (uniform) storage buffers that are connected to shaders using descriptorset.
 * There is one memory allocation per DescriptorBuffers with each added BindBuffer must share that memory.
 *
 */
public abstract class DescriptorBuffers<S extends Shader<?>> {

    public static final int VEC4_SIZE_IN_BYTES = 4 * Float.BYTES;
    public static final int MAT4_SIZE_IN_BYTES = 4 * 4 * Float.BYTES;
    public static final int F16VEC4_SIZE_IN_BYTES = 4 * Short.BYTES;

    private HashMap<DescriptorSetTarget, BindBuffer> bindBuffersMap = new HashMap<DescriptorSetTarget, BindBuffer>();
    private HashMap<DescriptorSetTarget, float[]> backingBuffersMap = new HashMap<DescriptorSetTarget, float[]>();
    PushConstants pushConstants;
    private Memory allocatedMemory;
    private ByteBuffer backingByteBuffer;

    /**
     * Sets the dynamic storage data into the backing buffers as needed,
     * ie data that must be updated on a per frame basis.
     * Call this before storage buffers are uploaded to device.
     * 
     * @param source The source of data
     * @return The descriptorsettarget buffers that have been updated and needs to be copied to device.
     */
    public abstract DescriptorSetTarget[] setDynamicStorage(S source);

    /**
     * Copies static uniform data from float[] array to backing buffer for all uniform buffers
     * - call this method to update backing buffer before uploading to device uniform store.
     * This method only needs to be called once before upload to device memory.
     * 
     * @param glTF
     * @param renderer
     */
    public abstract void setStaticStorage(VulkanRenderableScene glTF, GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer);

    /**
     * Returns the setcount for the specified target, this is used when descriptorset layout bindings are created
     * 
     * @param target The target to get number of sets for, must be a valid target
     * @return
     * @throws IllegalArgumentException If target is invalid
     */
    public abstract int getSetCount(DescriptorSetTarget target);

    /**
     * Adds the buffer for the specified target, throws an exception if buffers for target are present.
     * Buffer can be fetched by calling {@link #getBuffer(DescriptorSetTarget)}
     * 
     * @param target
     * @param dynamicCount Number of elements for the target - may be queried by calling
     * {@link #getDynamicCount()}
     * @param buffer
     * @param backingFloatData Optional backing float array data, can be used to store data in backing buffer.
     * @throws IllegalArgumentException If buffers for target is already present, if target or buffers is null,
     * if sizes of backing buffer does not match with backingFloatData
     */
    public void addBuffer(DescriptorSetTarget target, int dynamicCount, BindBuffer buffer, float[] backingFloatData) {
        if (target == null || buffer == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        if (bindBuffersMap.containsKey(target)) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already contains buffers for " + target);
        }
        if (backingFloatData != null && buffer.getBackingBuffer() != null && buffer.getBackingBuffer().capacity() != backingFloatData.length * Float.BYTES) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Size of backing buffer does not match ("
                    + buffer.getBackingBuffer().capacity() + ") ("
                    + backingFloatData.length * Float.BYTES + ")");
        }
        if (allocatedMemory == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Allocated memory must be set.");
        }
        if (buffer.getBuffer().getBoundMemory().pointer != allocatedMemory.pointer) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Allocated memory does not match bound memory pointer");
        }
        bindBuffersMap.put(target, buffer);
        backingBuffersMap.put(target, backingFloatData);
    }

    /**
     * Adds the buffer containing toplevel acceleration structures.
     * This will add the bindbuffer to the
     * 
     * @param target
     * @param buffer
     */
    public void addRayTracingToplevelAS(DescriptorSetTarget target, BindBuffer buffer) {
        if (target == null || buffer == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        if (bindBuffersMap.containsKey(target)) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already contains buffers for " + target);
        }
        bindBuffersMap.put(target, buffer);
    }

    /**
     * Sets the allocated memory for descriptorbuffers, throws exception if already set
     * 
     * @param memory
     * @param byteBuffer
     */
    public void setAllocatedMemory(Memory memory, ByteBuffer byteBuffer) {
        if (allocatedMemory != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Allocated memory already set.");
        }
        if (byteBuffer.limit() != memory.size) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Limit of backing buffer does not match memory size");
        }
        allocatedMemory = memory;
        this.backingByteBuffer = byteBuffer;
    }

    /**
     * Returns the allocated memory set by calling {@link #setAllocatedMemory(Memory)}
     * 
     * @return
     */
    public Memory getAllocatedMemory() {
        return allocatedMemory;
    }

    /**
     * Returns the backing byte buffer at position 0
     */
    public ByteBuffer getBackingByteBuffer() {
        return backingByteBuffer.position(0);
    }

    /**
     * Fetch float array uniform data, if specified when adding the buffer
     * 
     * @param target
     * @return float array or null
     */
    public final float[] getFloatData(DescriptorSetTarget target) {
        if (target == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        return backingBuffersMap.get(target);
    }

    /**
     * Stores an array of floats in the uniform storage buffer, call this to store for instance an array of
     * matrices. Use this method when buffer does NOT use dynamic offsets.
     * The backing store can then be updated to device memory by getting the buffer from
     * {@link #getBuffer(DescriptorSetTarget)}
     * 
     * @param target
     * @param destOffset Offset, in floats, in destination buffer where data is stored.
     * @param floatData optional float array to use as source - if null or zero length the array passed when
     * adding the buffer will be used
     */
    public int storeFloatData(DescriptorSetTarget target, int destOffset, float[]... floatData) {
        if (floatData == null || floatData.length == 0) {
            floatData = new float[][] { backingBuffersMap.get(target) };
        }
        BindBuffer buffer = getBuffer(target);
        return storeFloatData(buffer, destOffset, floatData);
    }

    /**
     * Stores byte data in the backing buffer
     * 
     * @param target
     * @param destOffet Destination offset in bytes
     * @param data
     */
    public int storeByteData(DescriptorSetTarget target, int destOffet, byte[]... data) {
        BindBuffer buffer = getBuffer(target);
        return storeByteData(buffer, destOffet, data);
    }

    /**
     * Stores an array of shorts
     * 
     * @param target
     * @param destOffset
     * @param shortData
     */
    public int storeShortData(DescriptorSetTarget target, int destOffset, short[]... shortData) {
        BindBuffer buffer = getBuffer(target);
        return storeShortData(buffer, destOffset, shortData);

    }

    /**
     * Stores an array of shorts
     * 
     * @param buffer
     * @param destOffset Destination offset in shorts
     * @param shortData
     */
    public int storeShortData(BindBuffer buffer, int destOffset, short[]... shortData) {
        buffer.setState(BufferState.updated);
        return buffer.storeShortData(destOffset, shortData);
    }

    /**
     * Stores the float data in the buffer at destOffset, buffer state is set to updated
     * 
     * @param buffer
     * @param destOffset Float offset in destination
     * @param floatData
     */
    public int storeFloatData(BindBuffer buffer, int destOffset, float[]... floatData) {
        buffer.setState(BufferState.updated);
        return buffer.storeFloatData(destOffset, floatData);
    }

    /**
     * Stores the byte data in the buffer at destOffset, buffer state is set to updated
     * 
     * @param buffer
     * @param destOffset byte offset in destination
     * @param data
     */
    public int storeByteData(BindBuffer buffer, int destOffset, byte[]... data) {
        buffer.setState(BufferState.updated);
        return buffer.storeByteData(destOffset, data);
    }

    /**
     * Stores the byte data in the buffer at destOffset, buffer state is set to updated
     * 
     * @param target
     * @param destOffset byte offset in destination
     * @param data
     */
    public int storeByteData(DescriptorSetTarget target, int destOffset, ByteBuffer data) {
        BindBuffer buffer = getBuffer(target);
        buffer.setState(BufferState.updated);
        return buffer.storeByteData(destOffset, data);
    }

    /**
     * Stores int data at destOffset
     * 
     * @param target
     * @param destOffset Int offset in destination
     * @param data
     * @return
     */
    public int storeIntData(DescriptorSetTarget target, int destOffset, int[]... data) {
        BindBuffer buffer = getBuffer(target);
        buffer.setState(BufferState.updated);
        return buffer.storeIntData(destOffset, data);
    }

    /**
     * Stores an array of floats in the backing storage buffer, call this to store for instance an array of
     * matrices. Use this method when storage buffer or uniform uses dynamic offsets.
     * The backing store can then be updated to device memory by getting the buffer from
     * {@link #getBuffer(DescriptorSetTarget)}
     * 
     * @param target
     * @param destOffset Offset, in floats, in destination buffer where data is stored.
     * @param floatData optional float array to use as source - if null or zero length the array passed when adding the
     * buffer will be used.
     */
    public void storeDataDynamicOffsets(DescriptorSetTarget target, float[]... floatData) {
        if (floatData == null || floatData.length == 0) {
            float[] data = getFloatData(target);
            if (data != null) {
                floatData = new float[][] { data };
            }
        }
        BindBuffer buffers = getBuffer(target);
        if (buffers.getDynamicSize() == 0) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + "Buffer does not use dynamic offsets");
        }
        if (buffers.getElementIndex() > buffers.getMaxElementIndex()) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Not enough dynamic offsets ("
                    + (buffers.getMaxElementIndex() + 1) + ")");
        }
        buffers.setState(BufferState.updated);
        buffers.storeFloatDataDynamicOffset(floatData);
    }

    /**
     * Returns the buffer for the target, or null if not added
     * 
     * @param target
     * @return The buffer or null
     */
    public BindBuffer getBuffer(DescriptorSetTarget target) {
        return bindBuffersMap.get(target);
    }

    /**
     * Returns an array with all buffers that are added
     * Internal method - DO NOT MODIFY returned array
     * 
     * @return
     */
    public BindBuffer[] getBuffers() {
        return bindBuffersMap.values().toArray(new BindBuffer[0]);
    }

    /**
     * Restarts dynamic offsets for all buffers that use it.
     */
    public void restartDynamicOffsets() {
        for (BindBuffer buffer : getBuffers()) {
            if (buffer.getDynamicSize() > 0) {
                buffer.restartElements();
            }
        }
    }

    /**
     * Sets the pushconstants - call this from pipeline creation - internal method - DO NOT USE
     * 
     * @param pushConstants
     */
    public void setPushConstants(PushConstants pushConstants) {
        if (pushConstants == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        if (this.pushConstants != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message);
        }
        this.pushConstants = pushConstants;
    }

    /**
     * Returns the pushconstants, or null if none have been set by calling {@link #setPushConstants(PushConstants)}
     * 
     * @return
     */
    public PushConstants getPushConstants() {
        return pushConstants;
    }

    /**
     * Update the pushconstants for the primitive
     * 
     * @param primitive
     */
    public void updatePushConstants(JSONPrimitive primitive, int sceneGraphIndex) {
        pushConstants.setPushConstants(primitive, sceneGraphIndex);
    }

    /**
     * Call after memory has been freed to destroy object
     */
    public void destroy() {
        bindBuffersMap.clear();
        backingBuffersMap.clear();
        backingByteBuffer = null;
        allocatedMemory = null;
        pushConstants = null;
    }

}

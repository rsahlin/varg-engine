
package org.varg.uniform;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.memory.MemoryBuffer;

/**
 * The data needed to record a CmdBindDescriptorSets
 *
 */
public class BindBuffer {

    /**
     * The buffer and memory
     */
    private final MemoryBuffer buffer;
    private final ByteBuffer backingByteBuffer;
    private final FloatBuffer backingFloatBuffer;
    private final int backingBufferByteCapacity;
    private final int backingBufferByteOffset;
    /**
     * Holds dynamic offsets
     */
    private final IntBuffer dynamicOffsets;
    /**
     * Only used when dynamic offset is used - size in bytes of each storage
     */
    private final int dynamicSize;
    /**
     * If dynamic offsets are used this is the index to the current element
     */
    private int elementIndex = 0;

    /**
     * Max dynamic element index
     */
    private int maxElementIndex = Constants.NO_VALUE;

    /**
     * Set to true when the descriptorset has been updated - NOTE this is NOT the same as copying the data
     * to the graphics device.
     */
    private boolean descriptorSetUpdated = false;

    private BufferState state = BufferState.created;

    public enum BufferState {
        created(),
        updated(),
        copiedToDevice();
    }

    public BindBuffer(MemoryBuffer buffer, ByteBuffer backingBuffer, int dynamicOffsetCount) {
        if (buffer == null || backingBuffer == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.buffer = buffer;
        // backingByteBuffer = backingBuffer;
        // Make sure bytebuffer size matches that of the memorybuffer (NOT the memory size)
        backingBufferByteOffset = backingBuffer.position();
        backingBufferByteCapacity = (int) buffer.size;
        backingBuffer.limit(backingBuffer.position() + backingBufferByteCapacity);
        backingByteBuffer = backingBuffer.slice().order(backingBuffer.order());
        // Make sure bytebuffer size matches that of the memorybuffer (NOT the memory size)
        if (backingBuffer.remaining() < backingBufferByteCapacity) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Backing byte buffer is too small, remaining: " + backingBuffer.remaining() + ", but needs "
                    + backingBufferByteCapacity);
        }

        backingFloatBuffer = backingBuffer != null ? backingBuffer.asFloatBuffer() : null;
        if (dynamicOffsetCount > 0) {
            this.dynamicOffsets = Buffers.createIntBuffer(dynamicOffsetCount);
            maxElementIndex = dynamicOffsetCount - 1;
            dynamicSize = backingBuffer.remaining() / dynamicOffsetCount;
            for (int i = 0; i < dynamicOffsetCount; i++) {
                dynamicOffsets.put(i * dynamicSize);
            }
        } else {
            dynamicOffsets = null;
            dynamicSize = 0;
        }
    }

    /**
     * Returns the memory buffer containing the data
     * 
     * @return
     */
    public MemoryBuffer getBuffer() {
        return buffer;
    }

    /**
     * Returns the backing buffer, positioned at backingBufferByteOffset and limit set to the size of the bound buffer.
     * 
     * @return
     */
    public ByteBuffer getBackingBuffer() {
        backingByteBuffer.position(0);
        return backingByteBuffer.limit((int) buffer.size);
    }

    /**
     * Returns the backing buffer, positioned at offset and limit set to offset + byteCount
     * 
     * @param offset
     * @param byteCount
     * @return
     */
    public ByteBuffer getBackingBuffer(int offset, int byteCount) {
        backingByteBuffer.limit(offset + byteCount);
        return backingByteBuffer.position(offset);
    }

    /**
     * Stores the float arrays in the float buffer at the position
     * Use this for instance to store multiple matrices where each matrix is a float array.
     * 
     * @param floatPosition in floats where the data is stored
     * @return The next position
     */
    int storeFloatData(int floatPosition, float[]... floatData) {
        if (getDynamicSize() != 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Buffer uses dynamic offsets");
        }
        backingFloatBuffer.position(floatPosition);
        for (float[] floats : floatData) {
            backingFloatBuffer.put(floats);
            floatPosition += floats.length;
        }
        return backingFloatBuffer.position();
    }

    /**
     * Fetches array of float data from floatPosition
     * 
     * @param floatPosition
     * @param destination
     */
    void getFloatData(int floatPosition, float[]... destination) {
        if (getDynamicSize() != 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Buffer uses dynamic offsets");
        }
        backingFloatBuffer.position(floatPosition);
        for (float[] floats : destination) {
            backingFloatBuffer.get(floats);
        }
    }

    /**
     * Stores float data at the dynamic float offset position
     * 
     * @param floatData
     */
    void storeFloatDataDynamicOffset(float[]... floatData) {
        if (dynamicSize == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message
                    + "Buffer does not use dynamic offsets.");
        }
        int size = 0;
        backingFloatBuffer.position(getDynamicFloatOffset());
        for (float[] floats : floatData) {
            size += floats.length;
            if (size << 2 > getDynamicSize()) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Dynamic data too large, "
                        + (size << 2) + ", dynamic size = " + getDynamicSize());
            }
            backingFloatBuffer.put(floats);
        }
    }

    /**
     * Stores the byte arrays in the byte buffer at the position
     * 
     * @param position in byte where the data is stored
     */
    int storeByteData(int position, byte[]... data) {
        if (getDynamicSize() != 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Buffer uses dynamic offsets");
        }
        backingByteBuffer.position(position);
        for (byte[] bytes : data) {
            backingByteBuffer.put(bytes);
        }
        return backingByteBuffer.position();
    }

    /**
     * Stores the byte arrays in the byte buffer at the position
     * 
     * @param position in bytes where the data is stored
     */
    int storeByteData(int position, ByteBuffer data) {
        if (getDynamicSize() != 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Buffer uses dynamic offsets");
        }
        backingByteBuffer.position(position);
        backingByteBuffer.put(data);
        return backingByteBuffer.position();
    }

    /**
     * 
     * @param position
     * @param intData Int position where data is written
     * @return
     */
    int storeIntData(int intPosition, int[]... intData) {
        if (getDynamicSize() != 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Buffer uses dynamic offsets");
        }
        backingByteBuffer.position(intPosition * Integer.BYTES);
        IntBuffer destination = backingByteBuffer.asIntBuffer();
        for (int[] ints : intData) {
            destination.put(ints);
        }
        return destination.position();
    }

    /**
     * Stores the short arrays in the backing buffer at (short) position
     * 
     * @param shortPosition Short position in backing buffer where data is written.
     * @param shortData
     */
    int storeShortData(int shortPosition, short[]... shortData) {
        if (getDynamicSize() != 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Buffer uses dynamic offsets");
        }
        backingByteBuffer.position(shortPosition * Short.BYTES);
        ShortBuffer destination = backingByteBuffer.asShortBuffer();
        for (short[] shorts : shortData) {
            destination.put(shorts);
        }
        return destination.position();
    }

    /**
     * Returns an intbuffer with dynamic offsets for the current element
     * 
     * @return
     */
    public IntBuffer getDynamicOffsets() {
        if (dynamicOffsets != null) {
            dynamicOffsets.position(elementIndex);
            dynamicOffsets.limit(elementIndex + 1);
            return dynamicOffsets;
        }
        return null;
    }

    /**
     * Returns the size in bytes of each dynamic set
     * 
     * @return
     */
    public int getDynamicSize() {
        return dynamicSize;
    }

    /**
     * Sets the state of the updated flag
     * 
     * @param updated
     */
    public void setDescriptorsetUpdated(boolean updated) {
        this.descriptorSetUpdated = updated;
    }

    /**
     * Returns true of the descritprset (layout) has been updated
     * 
     * @return
     */
    public boolean isDescriptorSetUpdated() {
        return descriptorSetUpdated;
    }

    /**
     * Returns the state of the buffer
     * 
     * @return
     */
    public BufferState getState() {
        return state;
    }

    /**
     * Sets the state of the buffer
     * 
     * @param state
     */
    public void setState(BufferState state) {
        this.state = state;
    }

    /**
     * Call this to set the dynamic element index to 0
     */
    public void restartElements() {
        if (dynamicSize == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message
                    + "Buffer does not use dynamic offsets");
        }
        elementIndex = 0;
    }

    /**
     * Returns the current dynamic elements index
     * 
     * @return
     */
    public int getDynamicIndex() {
        return elementIndex;
    }

    /**
     * Update dynamic element index
     */
    public void nextElement() {
        elementIndex++;
    }

    /**
     * Sets the element index to 0
     */
    public void resetElement() {
        elementIndex = 0;
    }

    /**
     * Returns the dynamic element count
     * 
     * @return The number of elements
     */
    public int getDynamicCount() {
        return dynamicOffsets != null ? dynamicOffsets.capacity() : 0;
    }

    /**
     * Returns the offset, in floats, to the current element - use this to update the data in buffer
     * 
     * @return
     */
    public int getDynamicFloatOffset() {
        return (elementIndex * getDynamicSize()) >> 2;
    }

    @Override
    public String toString() {
        return "Buffer size " + buffer.size + ", alignment " + buffer.alignment + "\n" +
                "Dynamic offsets: " + Buffers.toString(dynamicOffsets, 0, 0, 0);
    }

    /**
     * Internal method - do NOT use
     * 
     * @return
     */
    protected int getElementIndex() {
        return elementIndex;
    }

    /**
     * Internal method - do NOT use
     * 
     * @return
     */
    protected int getMaxElementIndex() {
        return maxElementIndex;
    }
}

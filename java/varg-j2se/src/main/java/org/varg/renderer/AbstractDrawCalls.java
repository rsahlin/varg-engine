package org.varg.renderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.gltfio.gltf2.JSONMaterial;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;

/**
 * Collection of data that enables the issuing of multiple drawcalls, it is considered that uniforms and textures
 * needed to render the scene are already bound.
 * 
 * The organization of drawcalls are done based on vertex attribute bindings and gpu pipeline, ie the drawcalls
 * that can be issued with the same pipeline.
 * 
 */
public abstract class AbstractDrawCalls {

    /**
     * The backing bytebuffer containing the data for drawcalls
     */
    private ByteBuffer indirectCommandBuffer;
    private IntBuffer indirectCommands;

    /**
     * Buffers for the indices used for index drawing - one for each indextype (byte, short, int)
     * or null if not used
     */
    private ByteBuffer[] indexByteBuffers;
    /**
     * Buffer for indirect draw commands
     */
    protected ByteBuffer[] indirectIndexCommandBuffers;

    /**
     * Number of draw instances for array drawing
     */
    public final int arrayInstanceCount;
    /**
     * Number of draw instances for indexed drawing - may be null
     */
    protected final int[] indexedInstanceCount;
    // Todo - figure out some other way of keeping track of material properties - used for pipeline creation and shader compilation
    public final int attributeHash;
    public final Attributes[] attributes;
    public final DrawMode drawMode;
    public final int firstInstance;
    protected final JSONMaterial material;

    /**
     * Used when adding indirect calls
     */
    protected int currentArrayIndex = 0;
    protected int[] currentIndicesIndex = new int[IndexType.values().length];
    protected int currentVertexOffset = 0;
    protected final float[] arrayMinMax;
    protected final float[][] indexedMinMax = new float[IndexType.values().length][];

    /**
     * Returns the number of ints for one draw (array) command
     * 
     * @return Number of ints per draw command
     */
    public abstract int getCommandSize();

    /**
     * Returns the number of ints for one indexed draw command
     * 
     * @return
     */
    public abstract int getIndexedCommandSize();

    /**
     * Returns the total command usage for array and indexed drawcommands, number of ints
     * 
     * @return Number of int commands needed for all drawcalls.
     */
    public abstract int getCommandBufferSize();

    protected AbstractDrawCalls(int attributeHash, Attributes[] attributes, JSONMaterial material, DrawMode drawMode, int arrayInstanceCount, int[] indexedInstanceCount, int[] indicesCount, int firstInstance) {
        this.firstInstance = firstInstance;
        this.attributeHash = attributeHash;
        this.attributes = attributes;
        this.material = material;
        this.drawMode = drawMode;
        this.arrayInstanceCount = arrayInstanceCount;
        this.indexedInstanceCount = indexedInstanceCount;
        this.arrayMinMax = new float[6 * arrayInstanceCount];
        this.indexedMinMax[IndexType.BYTE.index] = new float[6 * indexedInstanceCount[0]];
        this.indexedMinMax[IndexType.SHORT.index] = new float[6 * indexedInstanceCount[1]];
        this.indexedMinMax[IndexType.INT.index] = new float[6 * indexedInstanceCount[2]];
        if (arrayInstanceCount > 0) {
            indirectCommandBuffer = Buffers.createByteBuffer(arrayInstanceCount * getCommandSize() * Integer.BYTES);
            indirectCommands = indirectCommandBuffer.asIntBuffer();
        }
        createIndexBuffers(indicesCount);
        for (IndexType type : IndexType.values()) {
            int count = indexedInstanceCount[type.index];
            if (count > 0) {
                if (indirectIndexCommandBuffers == null) {
                    indirectIndexCommandBuffers = new ByteBuffer[IndexType.values().length];
                }
                indirectIndexCommandBuffers[type.index] = Buffers.createByteBuffer(count * getIndexedCommandSize() * Integer.BYTES);
            }
        }
    }

    public float[] getIndexedMinMax(IndexType indexType) {
        return indexedMinMax[indexType.index];
    }

    /**
     * Returns the instance count (drawcount) for the indexed type
     * 
     * @param indexType, null to return all indexed instancecounts
     * @return
     */
    public int getIndexedInstanceCount(IndexType indexType) {
        if (indexType == null) {
            return indexedInstanceCount != null ? indexedInstanceCount[0] + indexedInstanceCount[1] + indexedInstanceCount[2] : 0;
        } else {
            return indexedInstanceCount != null ? indexedInstanceCount[indexType.index] : 0;
        }
    }

    /**
     * Returns the indirect byte buffer or null
     * 
     * @return
     */
    protected ByteBuffer getIndirectByteBuffer() {
        return indirectCommandBuffer != null ? indirectCommandBuffer.position(0) : null;
    }

    /**
     * Returns the indexedbuffer for the type, or null
     * 
     * @param indexType
     * @return
     */
    protected ByteBuffer getIndexIndirectByteBuffer(IndexType indexType) {
        if (indirectIndexCommandBuffers != null) {
            return indirectIndexCommandBuffers[indexType.index] != null ? indirectIndexCommandBuffers[indexType.index]
                    .position(0) : null;
        }
        return null;
    }

    private void createIndexBuffers(int[] indicesCount) {
        if (indicesCount != null) {
            for (IndexType type : IndexType.values()) {
                int count = indicesCount[type.index];
                if (count > 0) {
                    if (indexByteBuffers == null) {
                        indexByteBuffers = new ByteBuffer[IndexType.values().length];
                    }
                    indexByteBuffers[type.index] = Buffers.createByteBuffer(count * type.dataType.size);
                }
            }
        }
    }

    /**
     * Adds data for an indirect drawcall
     * 
     * @param indirectCall Array size must match command size
     * 
     */
    public void addIndirectDraw(int... indirectCall) {
        int index = 0;
        int size = getCommandSize();
        if (indirectCall.length != size) {
            throw new IllegalArgumentException();
        }
        indirectCommands.position(currentArrayIndex * size);
        indirectCommands.put(indirectCall, index, size);
        currentArrayIndex++;
    }

    /**
     * Returns the index bytebuffer for IndexTypes
     * 
     * @return
     */
    public ByteBuffer[] getIndexByteBuffers() {
        return indexByteBuffers;
    }

    /**
     * Returns true if drawcall uses indexed drawing.
     * 
     * @return
     */
    public boolean hasIndices() {
        return (indexedInstanceCount[0] + indexedInstanceCount[1] + indexedInstanceCount[2]) > 0;
    }

    /**
     * Adds indirectdraw command
     * 
     * @param type The type of index list (byte, short or int)
     * @param indirectCall Array with drawcall command data
     */
    public void addIndexedIndirectDraw(IndexType type, int... indirectCall) {
        int size = getIndexedCommandSize();
        if (indirectCall.length != size) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "command size does not match");
        }
        indirectIndexCommandBuffers[type.index].position(currentIndicesIndex[type.index] * size * Integer.BYTES);
        IntBuffer indirect = indirectIndexCommandBuffers[type.index].asIntBuffer();
        indirect.put(indirectCall);
        currentIndicesIndex[type.index]++;
    }

    /**
     * Returns the indirect indexedcommands, or null
     * 
     * @param type
     * @return
     */
    public int[] getInderectIndexedDrawCommands(IndexType type) {
        return getIndirectCommands(indirectIndexCommandBuffers[type.index], getIndexedCommandSize(), getIndexedInstanceCount(type));
    }

    private int[] getIndirectCommands(ByteBuffer cmdBuffer, int cmdSize, int instanceCount) {
        int[] result = null;
        if (cmdBuffer != null) {
            cmdBuffer.position(0);
            result = new int[cmdSize * instanceCount];
            cmdBuffer.asIntBuffer().get(result);
        }
        return result;
    }

    /**
     * Returns the indirect array drawcommands, or null
     * 
     * @return
     */
    public int[] getIndirectArrayDrawCommands() {
        return getIndirectCommands(indirectCommandBuffer, getCommandSize(), arrayInstanceCount);
    }

}

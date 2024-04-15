package org.varg.vulkan;

import java.nio.ByteBuffer;

import org.gltfio.data.FlattenedScene.PrimitiveSorter;
import org.gltfio.gltf2.AttributeSorter;
import org.gltfio.gltf2.JSONMaterial.AlphaMode;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.lib.ErrorMessage;
import org.varg.gltf.VulkanPrimitive;
import org.varg.gltf.VulkanScene;
import org.varg.renderer.AbstractDrawCalls;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.pipeline.PipelineVertexInputState;
import org.varg.vulkan.vertex.BindVertexBuffers;
import org.varg.vulkan.vertex.VertexInputAttributeDescription;
import org.varg.vulkan.vertex.VertexInputBindingDescription;

/**
 * The data needed to create a number of drawcalls to a low level graphics API that share the same vertex attributes
 */
public class IndirectDrawCalls extends AbstractDrawCalls implements IndirectDrawing {

    /**
     * INDEXED:
     * indexCount
     * instanceCount
     * firstIndex
     * vertexOffset
     * firstInstance
     * 
     * ARRAY:
     * vertexCount,
     * instanceCount,
     * firstVertex,
     * firstInstance
     */

    /**
     * Number of ints for an indirect draw command
     */
    public static final int DRAW_INDIRECT_COMMAND_SIZE = 4;
    /**
     * Number of ints for an indexed indirect draw command
     */
    public static final int DRAW_INDEXED_INDIRECT_COMMAND_SIZE = 5;

    protected MemoryBuffer vkIndirectBuffer;
    // Base memory offset
    protected int vkIndirectBufferOffset;
    // Offsets for indexed drawing
    protected int[] vkIndexOffsets;
    // Offset for array drawing
    protected int vkArrayIndexOffset;
    protected BindVertexBuffers indexBuffers;
    protected BindVertexBuffers vertexBuffers;

    private static transient VertexInputAttributeDescription[] inputs;

    public IndirectDrawCalls(PrimitiveSorter primitives, BindVertexBuffers indexBuffers, BindVertexBuffers vertexBuffers, int firstInstance) {
        super(primitives.attributeHash, primitives.sortedAttributes, primitives.textureChannels, primitives.mode, primitives.alphaMode, primitives.getArrayPrimitiveCount(), primitives.getIndexedPrimitiveCount(),
                primitives.getIndicesCount(), firstInstance);
        this.indexBuffers = indexBuffers;
        this.vertexBuffers = vertexBuffers;
    }

    @Override
    public int getCommandSize() {
        return DRAW_INDIRECT_COMMAND_SIZE;
    }

    @Override
    public int getIndexedCommandSize() {
        return DRAW_INDEXED_INDIRECT_COMMAND_SIZE;
    }

    /**
     * Sets the indirect buffer (device memory)
     * 
     * @param srcVkIndirectBuffer
     * @param bufferOffset Offset into buffer where commands start
     */
    public void setCommandMemoryBuffer(MemoryBuffer srcVkIndirectBuffer, int bufferOffset) {
        if (this.vkIndirectBuffer != null) {
            throw new IllegalArgumentException();
        }
        this.vkIndirectBuffer = srcVkIndirectBuffer;
        this.vkIndirectBufferOffset = bufferOffset;
    }

    /**
     * Use when adding indirect drawcalls
     * 
     * @return
     */
    public int getCurrentIndicesIndex(IndexType type) {
        return currentIndicesIndex[type.index];
    }

    /**
     * Use when adding indirect drawcalls
     * 
     * @return
     */
    public int getCurrentVertexOffset() {
        return currentVertexOffset;
    }

    /**
     * Adds to the value of current vertex offset, use when adding indirect calls
     * 
     * @param vertexOffset
     */
    public void addVertexOffset(int vertexOffset) {
        currentVertexOffset += vertexOffset;
    }

    @Override
    public BindVertexBuffers getVertexBuffers() {
        return vertexBuffers;
    }

    /**
     * Returns the indirect device memory
     * 
     * @return
     */
    public MemoryBuffer getIndirectMemory() {
        return vkIndirectBuffer;
    }

    /**
     * Returns the memory offset to indexed drawcommands - if no indexed drawing is used this will return 0
     * 
     * @param type
     * @return
     */
    public long getIndirectIndexedMemoryOffset(IndexType type) {
        return vkIndexOffsets != null ? vkIndexOffsets[type.index] : 0;
    }

    /**
     * Returns the memory offset to array drawcommands
     * 
     * @return
     */
    public long getIndirectArrayMemoryOffset() {
        return vkArrayIndexOffset;
    }

    @Override
    public void drawIndirect(Queue queue) {
        queue.cmdBindVertexBuffers(0, vertexBuffers.getBuffers(), vertexBuffers.getOffsets());
        queue.cmdDrawIndirect(this);
    }

    @Override
    public int getPipelineHash() {
        return JSONPrimitive.getPipelineHash(attributeHash, textureChannels, drawMode, alphaMode);
    }

    private void copyToDevice(DeviceMemory deviceMemory, Queue queue, ByteBuffer source, MemoryBuffer destination, int offset) {
        if (source != null) {
            queue.queueBegin();
            if (source.remaining() <= Queue.UPDATE_BUFFER_MAX_BYTES) {
                deviceMemory.updateBuffer(source, destination, offset, queue);
            } else {
                deviceMemory.copyToDeviceMemory(source, destination, offset, queue);
            }
        }
    }

    @Override
    public int copyToDevice(DeviceMemory deviceMemory, Queue queue) {
        /**
         * Copy the drawcommands to device memory - copy indexed drawcommands first, using offsets
         */
        int offset = vkIndirectBufferOffset;
        int size = 0;
        if (indirectIndexCommandBuffers != null) {
            vkIndexOffsets = new int[IndexType.values().length];
            // Has indexed drawcommands
            ByteBuffer source = null;
            for (IndexType indexType : IndexType.values()) {
                source = getIndexIndirectByteBuffer(indexType);
                if (source != null) {
                    vkIndexOffsets[indexType.index] = offset;
                    size = source.remaining();
                    copyToDevice(deviceMemory, queue, source, vkIndirectBuffer, offset);
                    offset += size;
                }
            }
        }
        ByteBuffer source = getIndirectByteBuffer();
        if (source != null) {
            vkArrayIndexOffset = offset;
            size = source.remaining();
            copyToDevice(deviceMemory, queue, source, vkIndirectBuffer, offset);
            offset += size;
        }
        return size;
    }

    @Override
    public int getCommandBufferSize() {
        return arrayInstanceCount * getCommandSize() + (indexedInstanceCount[IndexType.BYTE.index]
                + indexedInstanceCount[IndexType.SHORT.index]
                + indexedInstanceCount[IndexType.INT.index]) * getIndexedCommandSize();
    }

    @Override
    public PrimitiveVertexInputState getInputState() {
        return new PrimitiveVertexInputState(createBindings(), drawMode);
    }

    private PipelineVertexInputState createBindings() {
        return IndirectDrawCalls.createBindings(attributes, textureChannels);
    }

    public static PipelineVertexInputState createBindings(Attributes[] sortedAttributes, Channel[] textureChannels) {
        VertexInputAttributeDescription[] vertexInputs =
                new VertexInputAttributeDescription[sortedAttributes.length];
        VertexInputBindingDescription[] vertexBindings =
                new VertexInputBindingDescription[vertexInputs.length];
        for (int i = 0; i < vertexInputs.length; i++) {
            Attributes attribute = AttributeSorter.GLTF_SORT_ORDER[i];
            if (sortedAttributes[i] != null) {
                vertexInputs[i] = getInput(i);
                vertexBindings[i] = VulkanPrimitive.getInputBinding(i);
            } else {
                switch (attribute) {
                    case BITANGENT:
                    case TANGENT:
                        vertexInputs[i] = getInput(i);
                        vertexBindings[i] = VulkanPrimitive.getInputBinding(i);
                    case NORMAL:
                    case TEXCOORD_0:
                    case TEXCOORD_1:
                    case POSITION:
                    case COLOR_0:
                        vertexInputs[i] = null;
                        vertexBindings[i] = VulkanPrimitive.getInputBinding(i);
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + attribute);
                }
            }
        }
        return new PipelineVertexInputState(AttributeSorter.GLTF_SORT_ORDER,
                vertexBindings, vertexInputs, textureChannels);
    }

    public static VertexInputAttributeDescription getInput(int binding) {
        if (inputs == null) {
            inputs = new VertexInputAttributeDescription[AttributeSorter.GLTF_SORT_ORDER.length];
            for (int i = 0; i < inputs.length; i++) {
                Vulkan10.Format format = VulkanScene.getDefaultFormat(AttributeSorter.GLTF_SORT_ORDER[i]);
                inputs[i] = new VertexInputAttributeDescription(i, i, format, 0);
            }
        }
        return inputs[binding];
    }

    @Override
    public Attributes[] getAttributes() {
        return attributes;
    }

    @Override
    public Channel[] getTextureChannels() {
        return textureChannels;
    }

    /**
     * Returns the indexbuffer pointer for the type
     * 
     * @param indexType
     * @return
     */
    public long getIndexBufferPointer(IndexType indexType) {
        return indexBuffers.getBuffers().get(indexType.index);
    }

    @Override
    public DrawMode getDrawMode() {
        return drawMode;
    }

    @Override
    public AlphaMode getAlphaMode() {
        return alphaMode;
    }

}

package org.varg.gltf;

import org.gltfio.gltf2.AttributeSorter;
import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.stream.PrimitiveStream;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.vertex.BindVertexBuffers;
import org.varg.vulkan.vertex.VertexInputAttributeDescription;

public class VulkanStreamingPrimitive extends VulkanPrimitive {

    private int[] vertexBindingIndexes;
    private Attributes[] attributes;
    private int vertexCount;
    private int indicesCount;
    private int materialIndex;

    private int[] sortedInputs;
    private Attributes[] sortedAttributes;

    public VulkanStreamingPrimitive(PrimitiveStream stream, int primitiveIndex) {
        materialIndex = stream.getMaterialIndex();
        vertexBindingIndexes = stream.getVertexBindingIndexes();
        vertexCount = stream.getVertexCount();
        indicesCount = stream.getIndicesCount();
        attributes = stream.getAttributes();
        this.streamVertexIndex = primitiveIndex;
        sortInputs();
    }

    private void sortInputs() {
        Attributes[] sortOrder = AttributeSorter.getInstance().getSortOrder();
        sortedInputs = new int[sortOrder.length];
        sortedAttributes = AttributeSorter.getInstance().sortAttributes(attributes);
        for (int i = 0; i < attributes.length; i++) {
            int index = AttributeSorter.getInstance().getLocation(attributes[i]);
            sortedInputs[index] = vertexBindingIndexes[i];
        }
    }

    protected void createBindBuffers(MemoryBuffer vertexBuffer, int[] offsets,
            VertexInputAttributeDescription[] inputs) {
        MemoryBuffer[] boundBuffers = new MemoryBuffer[inputs.length];
        long[] boundOffsets = new long[boundBuffers.length];
        for (int i = firstBinding; i < inputs.length; i++) {
            JSONAccessor accessor = inputs[i] != null ? getAccessor(sortedAttributes[i]) : null;
            if (accessor != null) {
                boundBuffers[i] = vertexBuffer;
                boundOffsets[i] = offsets[accessor.getBufferViewIndex()] + accessor.getByteOffset();
            } else {
                boundBuffers[i] = null;
                boundOffsets[i] = 0;
            }
        }
        bindBuffers = new BindVertexBuffers(firstBinding, boundBuffers, boundOffsets);
    }

}

package org.varg.gltf;

import org.gltfio.gltf2.AttributeSorter;
import org.gltfio.gltf2.stream.PrimitiveStream;

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

}

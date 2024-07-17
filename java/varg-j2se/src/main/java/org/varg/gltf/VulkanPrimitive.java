
package org.varg.gltf;

import java.util.HashMap;

import org.gltfio.gltf2.AttributeSorter;
import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.vertex.BindVertexBuffers;
import org.varg.vulkan.vertex.VertexInputBindingDescription;
import org.varg.vulkan.vertex.VertexInputBindingDescription.VertexInputRate;

public class VulkanPrimitive extends JSONPrimitive {

    protected transient BindVertexBuffers bindBuffers;
    protected transient int firstBinding = 0;
    private transient int pipelineHash;
    private transient MemoryBuffer indicesBuffer;
    private transient int indicesOffset;

    private static transient VertexInputBindingDescription[] bindings;

    protected VulkanPrimitive() {
    }

    protected VulkanPrimitive(JSONGltf glTF, DrawMode mode, int materialIndex, int indicesIndex,
            HashMap<Attributes, Integer> attributeMap) {
        super(glTF, mode, materialIndex, indicesIndex, attributeMap);
    }

    @Override
    public void resolveTransientValues() {
        super.resolveTransientValues();
    }

    /**
     * Returns the buffer used for indices or null if array mode.
     * 
     * @return
     */
    public MemoryBuffer getIndicesBuffer() {
        return indicesBuffer;
    }

    /**
     * Returns the offset in the indicesbuffer
     * 
     * @return
     */
    public int getIndicesOffset() {
        return indicesOffset;
    }

    /**
     * Sets the buffer with indices and offset into it
     * 
     * @param indices
     * @param offset
     */
    public void setIndices(MemoryBuffer indices, int offset) {
        indicesBuffer = indices;
        indicesOffset = offset;
    }

    /**
     * Returns the BindVertexBuffers data needed to bind vertex buffers.
     * 
     * @return
     */
    public BindVertexBuffers getBindBuffers() {
        return bindBuffers;
    }

    /**
     * Returns the sorted array of accessors - if an accessor is not declared it will be null.
     * 
     * @return
     */
    public JSONAccessor[] getAccessors() {
        if (accessors == null) {
            accessors = sortAccessors(AttributeSorter.GLTF_SORT_ORDER);
        }
        return accessors;
    }

    public static VertexInputBindingDescription getInputBinding(int binding) {
        if (bindings == null) {
            bindings = new VertexInputBindingDescription[AttributeSorter.GLTF_SORT_ORDER.length];
            for (int i = 0; i < bindings.length; i++) {
                switch (AttributeSorter.GLTF_SORT_ORDER[i]) {
                    case POSITION:
                    case NORMAL:
                    case COLOR_0:
                        bindings[i] = new VertexInputBindingDescription(i, 12, VertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX);
                        break;
                    case TANGENT:
                        bindings[i] = new VertexInputBindingDescription(i, 16, VertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX);
                        break;
                    case TEXCOORD_0:
                    case TEXCOORD_1:
                    case TEXCOORD_2:
                        bindings[i] = new VertexInputBindingDescription(i, 8, VertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX);
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid attribute " + AttributeSorter.GLTF_SORT_ORDER[i]);
                }
            }
        }
        return bindings[binding];
    }

}

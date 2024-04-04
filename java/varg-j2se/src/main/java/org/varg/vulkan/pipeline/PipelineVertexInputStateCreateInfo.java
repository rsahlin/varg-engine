
package org.varg.vulkan.pipeline;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.vertex.VertexInputAttributeDescription;
import org.varg.vulkan.vertex.VertexInputBindingDescription;

/**
 * Wrapper for VkPipelineVertexInputStateCreateInfo
 * 
 * For every binding specified by each element of pVertexAttributeDescriptions, a VkVertexInputBindingDescription must
 * exist in pVertexBindingDescriptions with the same value of binding
 * All elements of pVertexBindingDescriptions must describe distinct binding numbers
 * All elements of pVertexAttributeDescriptions must describe distinct attribute locations *
 */
public class PipelineVertexInputStateCreateInfo {

    // Reserved for future use - VkPipelineVertexInputStateCreateFlags
    final VertexInputBindingDescription[] vertexBindingDescriptions;
    final VertexInputAttributeDescription[] vertexAttributeDescriptions;

    public PipelineVertexInputStateCreateInfo(@NonNull VertexInputBindingDescription[] vertexBinding,
            @NonNull VertexInputAttributeDescription[] vertexInput) {
        if (vertexBinding == null || vertexInput == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE + " null");
        }
        vertexBindingDescriptions = vertexBinding;
        vertexAttributeDescriptions = vertexInput;
    }

    /**
     * Returns the array if vertexbinding descriptions
     * This array is always the same size - avoid null elements to maximize layout compatibility
     * 
     * @return
     */
    public VertexInputBindingDescription[] getVertexBindingDescriptions() {
        return vertexBindingDescriptions;
    }

    /**
     * Returns the array of vertex input attribute descriptions - this array is always the same size.
     * Unused elements are null
     * 
     * @return
     */
    public VertexInputAttributeDescription[] getVertexAttributeDescriptions() {
        return vertexAttributeDescriptions;
    }

    /**
     * Returns the number of used vertex inputs
     * 
     * @return
     */
    public int getVertexInputCount() {
        int count = 0;
        for (VertexInputAttributeDescription vertexInput : vertexAttributeDescriptions) {
            count += vertexInput != null ? 1 : 0;
        }
        return count;
    }

    /**
     * Returns the number of vertex bindings
     * 
     * @return
     */
    public int getVertexBindingCount() {
        int count = 0;
        for (VertexInputBindingDescription vertexBinding : vertexBindingDescriptions) {
            count += vertexBinding != null ? 1 : 0;
        }
        return count;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(vertexAttributeDescriptions);
        result = prime * result + Arrays.hashCode(vertexBindingDescriptions);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PipelineVertexInputStateCreateInfo other = (PipelineVertexInputStateCreateInfo) obj;
        if (!Arrays.equals(vertexAttributeDescriptions, other.vertexAttributeDescriptions)) {
            return false;
        }
        if (!Arrays.equals(vertexBindingDescriptions, other.vertexBindingDescriptions)) {
            return false;
        }
        return true;
    }

}

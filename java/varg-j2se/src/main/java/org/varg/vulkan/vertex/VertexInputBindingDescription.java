
package org.varg.vulkan.vertex;

import java.util.Comparator;

/**
 * Wrapper for VkVertexInputBindingDescription
 */
public class VertexInputBindingDescription {

    public static class VertexInputBindingDescriptionSorter implements Comparator<VertexInputBindingDescription> {
        @Override
        public int compare(VertexInputBindingDescription a, VertexInputBindingDescription b) {
            return a.hashCode() - b.hashCode();
        }
    }

    public enum VertexInputRate {
        VK_VERTEX_INPUT_RATE_VERTEX(0),
        VK_VERTEX_INPUT_RATE_INSTANCE(1),
        VK_VERTEX_INPUT_RATE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        VertexInputRate(int value) {
            this.value = value;
        }
    };

    public VertexInputBindingDescription(int binding, int stride, VertexInputRate inputRate) {
        this.binding = binding;
        this.stride = stride;
        this.inputRate = inputRate;
    }

    final int binding;
    final int stride;
    final VertexInputRate inputRate;

    public final int getBinding() {
        return binding;
    }

    public final int getStride() {
        return stride;
    }

    public final VertexInputRate getInputRate() {
        return inputRate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + binding;
        result = prime * result + ((inputRate == null) ? 0 : inputRate.value);
        result = prime * result + stride;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VertexInputBindingDescription other = (VertexInputBindingDescription) obj;
        if (binding != other.binding) {
            return false;
        } else if (inputRate != other.inputRate) {
            return false;
        } else if (stride != other.stride) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Binding: " + binding + ", Stride: " + stride + ", InputRate: " + inputRate;
    }

}

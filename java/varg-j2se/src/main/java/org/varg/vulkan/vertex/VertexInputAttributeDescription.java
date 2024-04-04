
package org.varg.vulkan.vertex;

import java.util.Comparator;

import org.varg.vulkan.Vulkan10;

public class VertexInputAttributeDescription {

    public static class VertexInputAttributeDescriptionSorter implements Comparator<VertexInputAttributeDescription> {
        @Override
        public int compare(VertexInputAttributeDescription a, VertexInputAttributeDescription b) {
            return a.hashCode() - b.hashCode();
        }
    }

    public VertexInputAttributeDescription(int binding, int location, Vulkan10.Format format, int offset) {
        this.binding = binding;
        this.location = location;
        this.format = format;
        this.offset = offset;
    }

    public final int binding;
    public final int location;
    public final Vulkan10.Format format;
    public final int offset;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + binding;
        result = prime * result + ((format == null) ? 0 : format.value);
        result = prime * result + location;
        result = prime * result + offset;
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
        VertexInputAttributeDescription other = (VertexInputAttributeDescription) obj;
        if (binding != other.binding) {
            return false;
        }
        if (format != other.format) {
            return false;
        }
        if (location != other.location) {
            return false;
        }
        if (offset != other.offset) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Location: " + location + ", binding: " + binding + ", format: " + format + ", offset: " + offset;
    }

}

package org.varg.vulkan.descriptor;

import java.nio.LongBuffer;

import org.gltfio.lib.ErrorMessage;

/**
 * Collection of data to update an acceleration structure descriptorset
 */
public class AccelerationStructureDescriptorInfo extends DescriptorBufferInfo {

    public final LongBuffer handles;

    public AccelerationStructureDescriptorInfo(LongBuffer handles) {
        if (handles == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.handles = handles;
    }

    @Override
    public String toString() {
        return "AccelerationStructure with " + handles.remaining() + " handles.";
    }

}

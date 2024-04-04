
package org.varg.vulkan.descriptor;

import org.gltfio.gltf2.JSONBufferView;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.DescriptorType;

public class WriteDescriptorSet {

    DescriptorSet dstSet;
    int dstBinding;
    int dstArrayElement;
    int descriptorCount;
    DescriptorType descriptorType;
    DescriptorImageInfo pImageInfo;
    DescriptorBufferInfo pBufferInfo;
    JSONBufferView pTexelBufferView;

    public WriteDescriptorSet(DescriptorSet descriptorSet, DescriptorType descriptorType, int descriptorCount,
            DescriptorImageInfo pImageInfo) {
        if (descriptorSet == null || descriptorType == null || pImageInfo == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        if (descriptorCount <= 0) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + "Descriptorcount = " + descriptorCount);
        }
    }

}

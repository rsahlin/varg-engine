
package org.varg.vulkan.descriptor;

import org.gltfio.lib.ErrorMessage;

public class DescriptorSetAllocateInfo {
    final DescriptorSetLayout[] pSetLayouts;

    public DescriptorSetAllocateInfo(DescriptorSetLayout[] pSetLayouts) {
        if (pSetLayouts == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE + "Null");
        }
        this.pSetLayouts = pSetLayouts;
    }
}

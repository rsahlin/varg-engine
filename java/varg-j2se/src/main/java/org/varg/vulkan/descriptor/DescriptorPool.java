
package org.varg.vulkan.descriptor;

import org.gltfio.lib.ErrorMessage;

public class DescriptorPool {

    private final long pDescriptorPool;

    public DescriptorPool(long pDescriptorPool) {
        if (pDescriptorPool == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE + "Null");
        }
        this.pDescriptorPool = pDescriptorPool;
    }

    /**
     * Returns the pointer to the descriptorpool
     * 
     * @return
     */
    public long getDescriptorPool() {
        return pDescriptorPool;
    }
}

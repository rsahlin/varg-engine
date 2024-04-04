
package org.varg.vulkan.pipeline;

import java.nio.LongBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;

public class PipelineLayout {

    private final LongBuffer pipelineLayout = Buffers.createLongBuffer(1);
    private final PipelineLayoutCreateInfo createInfo;

    public PipelineLayout(long pipelineLayout, PipelineLayoutCreateInfo createInfo) {
        if (pipelineLayout == 0 || createInfo == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "0 or null");
        }
        this.pipelineLayout.put(pipelineLayout);
        this.createInfo = createInfo;
    }

    /**
     * Returns the pipeline layout native pointer
     * 
     * @return
     */
    public long getPipelineLayout() {
        return pipelineLayout.get(0);
    }

    /**
     * Returns the buffer holding the pipeline layout native pointer, buffer positioned at 0
     * 
     * @return
     */
    public LongBuffer getPipelineLayoutBuffer() {
        pipelineLayout.position(0);
        return pipelineLayout;
    }
}

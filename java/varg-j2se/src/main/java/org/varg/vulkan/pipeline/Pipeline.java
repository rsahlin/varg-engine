
package org.varg.vulkan.pipeline;

import java.nio.LongBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;

/**
 * A pipeline instance that can be used.
 *
 */
public class Pipeline {

    private final LongBuffer pipelineBuffer = Buffers.createLongBuffer(1);
    private final PipelineLayout layout;

    protected Pipeline(long pipeline, PipelineLayout layout) {
        if (pipeline == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + " pipeline is 0");
        }
        pipelineBuffer.put(pipeline);
        this.layout = layout;
    }

    /**
     * Returns the pipeline native pointer
     * 
     * @return
     */
    public long getPipeline() {
        return pipelineBuffer.get(0);
    }

    /**
     * Returns the pipeline layout
     * 
     * @return
     */
    public PipelineLayout getLayout() {
        return layout;
    }

}

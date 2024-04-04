
package org.varg.vulkan.pipeline;

import org.gltfio.lib.ErrorMessage;
import org.varg.shader.ComputeShader;
import org.varg.vulkan.descriptor.DescriptorSet;

/**
 *
 */
public class ComputePipeline extends Pipeline {

    private final ComputePipelineCreateInfo createInfo;
    private final ComputeShader computeShader;
    private DescriptorSet descriptorSet;

    public ComputePipeline(ComputePipelineCreateInfo createInfo, long pipeline, ComputeShader computeShader) {
        super(pipeline, createInfo.getPipelineLayout());
        if (computeShader == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.createInfo = createInfo;
        this.computeShader = computeShader;
    }

}

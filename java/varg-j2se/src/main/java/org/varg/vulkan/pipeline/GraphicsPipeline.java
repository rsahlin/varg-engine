
package org.varg.vulkan.pipeline;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A graphics pipeline instance.
 * 
 * This is used in runtime when geometry shall be recorded to Vulkan.
 *
 */
public class GraphicsPipeline extends Pipeline {

    public GraphicsPipeline(@NonNull GraphicsPipelineCreateInfo createInfo, long pipeline) {
        super(pipeline, createInfo.getPipelineLayout());
    }

}

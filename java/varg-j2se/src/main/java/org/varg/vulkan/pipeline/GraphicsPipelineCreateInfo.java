
package org.varg.vulkan.pipeline;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.extensions.KHRFragmentShadingRate.PipelineFragmentShadingRateStateCreateInfoKHR;

/**
 * Wrapper for VkGraphicsPipelineCreateInfo
 *
 */
public class GraphicsPipelineCreateInfo extends PipelineCreateInfo {

    int stageCount;
    /**
     * Not used if stage is task/mesh shader
     */
    final PipelineVertexInputStateCreateInfo vertexInputState;
    /**
     * Not used if stage is task/mesh shader
     */
    final PipelineInputAssemblyStateCreateInfo inputAssemblyState;

    PipelineTesselationStateCreateInfo tesselationState;
    final PipelineViewportStateCreateInfo viewportState;
    final PipelineRasterizationStateCreateInfo rasterizationState;
    final PipelineMultisampleStateCreateInfo multisampleState;
    final PipelineDepthStencilStateCreateInfo depthStencilState;
    final PipelineColorBlendStateCreateInfo colorBlendState;
    PipelineDynamicStateCreateInfo dynamicState;
    private PipelineFragmentShadingRateStateCreateInfoKHR shadingRateCreateInfo;

    public GraphicsPipelineCreateInfo(PipelineShaderStageCreateInfo[] stages,
            PipelineVertexInputStateCreateInfo vertexInputState,
            PipelineInputAssemblyStateCreateInfo inputAssemblyState, PipelineViewportStateCreateInfo viewportState,
            PipelineRasterizationStateCreateInfo rasterizationState,
            PipelineMultisampleStateCreateInfo multisampleState,
            PipelineDepthStencilStateCreateInfo depthStencilState, PipelineColorBlendStateCreateInfo colorBlendState,
            PipelineLayout layout) {
        super(null, stages, layout, -1, null);
        if (rasterizationState == null || multisampleState == null || depthStencilState == null || colorBlendState
                == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.vertexInputState = vertexInputState;
        this.inputAssemblyState = inputAssemblyState;
        this.viewportState = viewportState;
        this.rasterizationState = rasterizationState;
        this.multisampleState = multisampleState;
        this.depthStencilState = depthStencilState;
        this.colorBlendState = colorBlendState;
    }

    /**
     * Sets the fragmentshadingrate create info - use when fragment shading rate extension is enabled
     * 
     * @param srcShadingRateCreateInfo
     */
    public void setFragmentShadingRateCreateInfo(PipelineFragmentShadingRateStateCreateInfoKHR srcShadingRateCreateInfo) {
        this.shadingRateCreateInfo = srcShadingRateCreateInfo;
    }

    /**
     * Returns the fragmentshadingrate createinfo or null if not used
     * 
     * @return
     */
    public PipelineFragmentShadingRateStateCreateInfoKHR getFragmentShadingRateCreateInfo() {
        return shadingRateCreateInfo;
    }

    /**
     * Returns the vertex input state createinfo
     * 
     * @return
     */
    public PipelineVertexInputStateCreateInfo getVertexInputState() {
        return vertexInputState;
    }

    /**
     * Returns the input assemblystate createinfo
     * 
     * @return
     */
    public PipelineInputAssemblyStateCreateInfo getInputAssemblyState() {
        return inputAssemblyState;
    }

    /**
     * Returns the rasterizationstate createinfo
     * 
     * @return
     */
    public PipelineRasterizationStateCreateInfo getRasterizationState() {
        return rasterizationState;
    }

    /**
     * Returns the multisamplestate createinfo
     * 
     * @return
     */
    public PipelineMultisampleStateCreateInfo getMultisampleState() {
        return multisampleState;
    }

    /**
     * Returns the depthpencilstate createinfo
     * 
     * @return
     */
    public PipelineDepthStencilStateCreateInfo getDepthStencilState() {
        return depthStencilState;
    }

    /**
     * Returns the colorblendstate createinfo
     * 
     * @return
     */
    public PipelineColorBlendStateCreateInfo getColorBlendState() {
        return colorBlendState;
    }

    @Override
    public String toString() {
        String shadingRateStr =
                shadingRateCreateInfo != null ? ", FragmentShadingRate: " + shadingRateCreateInfo.fragmentSize : "";
        return "Multisamplestate:" + (multisampleState != null ? multisampleState.toString()
                : " NULL") + shadingRateStr;
    }
}

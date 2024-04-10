
package org.varg.vulkan.pipeline;

/**
 * Wrapper for VkPipelineDynamicStateCreateInfo
 *
 */
public class PipelineDynamicStateCreateInfo {

    public enum DynamicState {
        VK_DYNAMIC_STATE_VIEWPORT(0),
        VK_DYNAMIC_STATE_SCISSOR(1),
        VK_DYNAMIC_STATE_LINE_WIDTH(2),
        VK_DYNAMIC_STATE_DEPTH_BIAS(3),
        VK_DYNAMIC_STATE_BLEND_CONSTANTS(4),
        VK_DYNAMIC_STATE_DEPTH_BOUNDS(5),
        VK_DYNAMIC_STATE_STENCIL_COMPARE_MASK(6),
        VK_DYNAMIC_STATE_STENCIL_WRITE_MASK(7),
        VK_DYNAMIC_STATE_STENCIL_REFERENCE(8),
        VK_DYNAMIC_STATE_VIEWPORT_W_SCALING_NV(1000087000),
        VK_DYNAMIC_STATE_DISCARD_RECTANGLE_EXT(1000099000),
        VK_DYNAMIC_STATE_SAMPLE_LOCATIONS_EXT(1000143000),
        VK_DYNAMIC_STATE_VIEWPORT_SHADING_RATE_PALETTE_NV(1000164004),
        VK_DYNAMIC_STATE_VIEWPORT_COARSE_SAMPLE_ORDER_NV(1000164006),
        VK_DYNAMIC_STATE_EXCLUSIVE_SCISSOR_NV(1000205001),
        VK_DYNAMIC_STATE_LINE_STIPPLE_EXT(1000259000),
        VK_DYNAMIC_STATE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        DynamicState(int value) {
            this.value = value;
        }
    }

    // Reserved for future use - VkPipelineDynamicStateCreateFlags flags;
    DynamicState[] dynamicStates;
}

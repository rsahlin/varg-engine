
package org.varg.vulkan.renderpass;

import org.varg.vulkan.Vulkan10.AccessFlagBit;
import org.varg.vulkan.Vulkan10.DependencyFlagBit;
import org.varg.vulkan.Vulkan10.PipelineStageFlagBit;

/**
 * Wrapper for VkSubpassDependency
 *
 */
public class SubpassDependency {
    int srcSubpass;
    int dstSubpass;
    PipelineStageFlagBit[] srcStageMask;
    PipelineStageFlagBit[] dstStageMask;
    AccessFlagBit[] srcAccessMask;
    AccessFlagBit[] dstAccessMask;
    DependencyFlagBit[] dependencyFlags;

}

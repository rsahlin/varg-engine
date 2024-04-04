
package org.varg.vulkan.structs;

import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Vulkan10.ShaderStageFlagBit;

/**
 * Wrapper for VkPushConstantRange
 *
 */
public class PushConstantRange {

    private final ShaderStageFlagBit[] stageFlags;
    public final int offset;
    public final int size;
    public final int stageFlagsValue;

    public PushConstantRange(ShaderStageFlagBit[] stageFlags, int offset, int size) {
        this.stageFlags = stageFlags;
        this.offset = offset;
        this.size = size;
        this.stageFlagsValue = BitFlags.getFlagsValue(stageFlags);
    }

}


package org.varg.vulkan.pipeline;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.PipelineCreateFlagBit;

public class PipelineCreateInfo {

    final PipelineCreateFlagBit[] flags;
    final PipelineShaderStageCreateInfo[] stages;
    final PipelineLayout layout;
    final int basePipelineIndex;
    final Pipeline basePipelineHandle;

    public PipelineCreateInfo(PipelineCreateFlagBit[] flags, @NonNull PipelineShaderStageCreateInfo[] stages,
            @NonNull PipelineLayout layout, int basePipelineIndex, Pipeline basePipelineHandle) {
        if (stages == null || layout == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.flags = flags;
        this.stages = stages;
        this.layout = layout;
        this.basePipelineIndex = basePipelineIndex;
        this.basePipelineHandle = basePipelineHandle;
    }

    /**
     * Returns the array of shaderstage createinfo
     * 
     * @return
     */
    public PipelineShaderStageCreateInfo[] getStages() {
        return stages;
    }

    /**
     * Returns the pipeline layout
     * 
     * @return
     */
    public PipelineLayout getPipelineLayout() {
        return layout;
    }

    /**
     * Returns the value for the create flags
     * 
     * @return
     */
    public int getCreateFlagsValue() {
        return BitFlags.getFlagsValue(flags);
    }

}


package org.varg.vulkan.descriptor;

import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Vulkan10.DescriptorType;
import org.varg.vulkan.Vulkan10.ShaderStageFlagBit;
import org.varg.vulkan.structs.Sampler;

public class DescriptorSetLayoutBinding {

    final int binding;
    final DescriptorType descriptorType;
    final int descriptorCount;
    final ShaderStageFlagBit[] stageFlags;
    final Sampler[] immutableSamplers;

    public DescriptorSetLayoutBinding(int binding, DescriptorType descriptorType, int descriptorCount, ShaderStageFlagBit[] stageFlags, Sampler[] immutableSamplers) {
        this.binding = binding;
        this.descriptorType = descriptorType;
        this.descriptorCount = descriptorCount;
        this.stageFlags = stageFlags;
        this.immutableSamplers = immutableSamplers;
    }

    /**
     * Returns the layout binding
     * 
     * @return
     */
    public int getBinding() {
        return binding;
    }

    /**
     * Returns the descriptorcount
     * 
     * @return
     */
    public int getDescriptorCount() {
        return descriptorCount;
    }

    /**
     * Returns the value of the ShaderStageFlagBit set bits
     * 
     * @return
     */
    public int getFlagsValue() {
        return BitFlags.getFlagsValue(stageFlags);
    }

    /**
     * Returns the descriptor type
     * 
     * @return
     */
    public DescriptorType getDescriptorType() {
        return descriptorType;
    }

    /**
     * Returns the stageflags
     * 
     * @return
     */
    public ShaderStageFlagBit[] getStageFlags() {
        return stageFlags;
    }

    @Override
    public String toString() {
        return "Binding: " + binding + ", type: " + descriptorType + ", count: " + descriptorCount + ", stageflags: "
                + BitFlags.toString(stageFlags);
    }

}

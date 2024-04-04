
package org.varg.vulkan.structs;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.BorderColor;
import org.varg.vulkan.Vulkan10.CompareOp;
import org.varg.vulkan.Vulkan10.Filter;
import org.varg.vulkan.Vulkan10.SamplerAddressMode;
import org.varg.vulkan.Vulkan10.SamplerCreateFlagBits;
import org.varg.vulkan.Vulkan10.SamplerMipmapMode;

public class SamplerCreateInfo {

    public final SamplerCreateFlagBits[] flags;
    public final Filter magFilter;
    public final Filter minFilter;
    public final SamplerMipmapMode mipmapMode;
    public final SamplerAddressMode addressModeU;
    public final SamplerAddressMode addressModeV;
    public final SamplerAddressMode addressModeW;
    public final float mipLodBias;
    private boolean anisotropyEnable;
    private float maxAnisotropy;
    public final boolean compareEnable;
    public final CompareOp compareOp;
    public final float minLod;
    public final float maxLod;
    public final BorderColor borderColor;
    public final boolean unnormalizedCoordinates;

    public SamplerCreateInfo(Filter magFilter, Filter minFilter, SamplerMipmapMode mipmapMode, SamplerAddressMode uMode,
            SamplerAddressMode vMode, float minLod, float maxLod, float mipLodBias) {
        this.flags = new SamplerCreateFlagBits[0];
        this.magFilter = magFilter;
        this.minFilter = minFilter;
        this.mipmapMode = mipmapMode;
        this.addressModeU = uMode;
        this.addressModeV = vMode;
        this.addressModeW = SamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT;
        this.mipLodBias = mipLodBias;
        this.anisotropyEnable = false;
        this.maxAnisotropy = 1f;
        this.compareEnable = false;
        this.compareOp = CompareOp.VK_COMPARE_OP_NEVER;
        this.minLod = minLod;
        this.maxLod = maxLod;
        this.borderColor = BorderColor.VK_BORDER_COLOR_INT_TRANSPARENT_BLACK;
        this.unnormalizedCoordinates = false;
    }

    /**
     * Sets the max anisotropy - must first be checked aginst DeviceLimits
     * If maxAnisotropy is >= 1 then anisotropyEnable is set to true
     * 
     * @param maxAnisotropy
     */
    public void setMaxAnisotropy(float maxAnisotropy) {
        this.maxAnisotropy = maxAnisotropy;
        if (maxAnisotropy >= 1) {
            if (unnormalizedCoordinates) {
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_VALUE.message + "UnnormalizedCoordinates must be false");
            }
            anisotropyEnable = true;
        } else {
            anisotropyEnable = false;
        }
    }

    public SamplerCreateInfo(SamplerCreateFlagBits[] flags,
            Filter magFilter,
            Filter minFilter,
            SamplerMipmapMode mipmapMode,
            SamplerAddressMode addressModeU,
            SamplerAddressMode addressModeV,
            SamplerAddressMode addressModeW,
            float mipLodBias,
            boolean anisotropyEnable,
            float maxAnisotropy,
            boolean compareEnable,
            CompareOp compareOp,
            float minLod,
            float maxLod,
            BorderColor borderColor,
            boolean unnormalizedCoordinates) {

        this.flags = flags;
        this.magFilter = magFilter;
        this.minFilter = minFilter;
        this.mipmapMode = mipmapMode;
        this.addressModeU = addressModeU;
        this.addressModeV = addressModeV;
        this.addressModeW = addressModeV;
        this.mipLodBias = mipLodBias;
        this.anisotropyEnable = anisotropyEnable;
        this.maxAnisotropy = maxAnisotropy;
        this.compareEnable = compareEnable;
        this.compareOp = compareOp;
        this.minLod = minLod;
        this.maxLod = maxLod;
        this.borderColor = borderColor;
        this.unnormalizedCoordinates = unnormalizedCoordinates;
    }

    /**
     * Returns the max supported anisotropy for this sampler
     * 
     * @return
     */
    public float getMaxAnisotropy() {
        return maxAnisotropy;
    }

    /**
     * Returns true if sampler supports anisotropy
     * 
     * @return
     */
    public boolean isAnisotropyEnable() {
        return anisotropyEnable;
    }

    @Override
    public String toString() {
        return "Mag: " + magFilter + ", Min: " + minFilter + ", U: " + addressModeU + ", V: " + addressModeV
                + ", maxAnisotropy: " + maxAnisotropy + ", enable: " + anisotropyEnable + " minLod: " + minLod
                + ", maxLod: " + maxLod;
    }

}

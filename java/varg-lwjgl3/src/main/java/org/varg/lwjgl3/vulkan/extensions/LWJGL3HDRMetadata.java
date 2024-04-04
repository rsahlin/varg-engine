package org.varg.lwjgl3.vulkan.extensions;

import org.eclipse.jdt.annotation.NonNull;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTHdrMetadata;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkHdrMetadataEXT;
import org.lwjgl.vulkan.VkXYColorEXT;
import org.varg.vulkan.extensions.EXTHDRMetadata;
import org.varg.vulkan.extensions.KHRSwapchain;
import org.varg.vulkan.structs.HDRMetadata;
import org.varg.vulkan.structs.XYColor;

public class LWJGL3HDRMetadata extends EXTHDRMetadata {

    private final VkDevice deviceInstance;

    public LWJGL3HDRMetadata(@NonNull VkDevice deviceInstance) {
        this.deviceInstance = deviceInstance;
    }

    @Override
    public void setHDRMetadataEXT(KHRSwapchain swapChain, HDRMetadata metadata) {

        VkXYColorEXT redPrimary = createColor(metadata.getPrimaryRed());
        VkXYColorEXT greenPrimary = createColor(metadata.getPrimaryGreen());
        VkXYColorEXT bluePrimary = createColor(metadata.getPrimaryBlue());
        VkXYColorEXT whitePoint = createColor(metadata.getWhitePoint());
        VkHdrMetadataEXT.Buffer vkMetadata = VkHdrMetadataEXT.calloc(1)
                .sType(EXTHdrMetadata.VK_STRUCTURE_TYPE_HDR_METADATA_EXT)
                .pNext(MemoryUtil.NULL)
                .displayPrimaryRed(redPrimary)
                .displayPrimaryGreen(greenPrimary)
                .displayPrimaryBlue(bluePrimary)
                .whitePoint(whitePoint)
                .maxContentLightLevel(metadata.maxContentLightLevel)
                .maxLuminance(metadata.maxLuminance)
                .minLuminance(metadata.minLuminance)
                .maxFrameAverageLightLevel(metadata.maxFrameAverageLightLevel);
        EXTHdrMetadata.vkSetHdrMetadataEXT(deviceInstance, swapChain.getSwapChainBuffer(), vkMetadata);
        MemoryUtil.memFree(vkMetadata);
        redPrimary.free();
        greenPrimary.free();
        bluePrimary.free();
        whitePoint.free();
    }

    private VkXYColorEXT createColor(XYColor color) {
        return createColor(color.x, color.y);
    }

    private VkXYColorEXT createColor(float... xy) {
        VkXYColorEXT color = VkXYColorEXT.calloc()
                .set(xy[0], xy[1]);
        return color;
    }

}

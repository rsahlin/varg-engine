
package org.varg.vulkan.renderpass;

import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.AttachmentDescriptionFlagBit;
import org.varg.vulkan.Vulkan10.AttachmentLoadOp;
import org.varg.vulkan.Vulkan10.AttachmentStoreOp;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;

/**
 * Wrapper for VkAttachmentDescription
 *
 */
public class AttachmentDescription {

    public final AttachmentDescriptionFlagBit[] flags;
    public final Vulkan10.Format format;
    public final SampleCountFlagBit samples;
    public final AttachmentLoadOp loadOp;
    public final AttachmentStoreOp storeOp;
    public final AttachmentLoadOp stencilLoadOp;
    public final AttachmentStoreOp stencilStoreOp;
    public final ImageLayout initialLayout;
    public final ImageLayout finalLayout;

    /**
     * Creates a new AttachmentDescription using the specified values.
     * VK_ATTACHMENT_LOAD_OP_DONT_CARE is used for stencilLoadOp
     * VK_ATTACHMENT_STORE_OP_DONT_CARE is used for stencilStoreOp
     * 
     * @param format
     * @param flags
     * @param loadOp
     * @param storeOp
     * @param initialLayout
     * @param finalLayout
     */
    public AttachmentDescription(Vulkan10.Format format, AttachmentDescriptionFlagBit[] flags, AttachmentLoadOp loadOp,
            AttachmentStoreOp storeOp, SampleCountFlagBit samples,
            ImageLayout initialLayout, ImageLayout finalLayout) {
        this.format = format;
        this.flags = flags;
        this.samples = samples;
        this.loadOp = loadOp;
        this.storeOp = storeOp;
        stencilLoadOp = AttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
        stencilStoreOp = AttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE;
        this.initialLayout = initialLayout;
        this.finalLayout = finalLayout;
    }

    public final int getFlagsValue() {
        return BitFlags.getFlagsValue(flags);
    }

}

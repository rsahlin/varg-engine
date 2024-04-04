
package org.varg.vulkan.renderpass;

import org.varg.vulkan.Vulkan10.ImageLayout;

/**
 * Wrapper for VkAttachmentReference
 *
 */
public class AttachmentReference {
    public final int attachment;
    public final ImageLayout layout;

    public AttachmentReference(int attachment, ImageLayout layout) {
        this.attachment = attachment;
        this.layout = layout;
    }
}

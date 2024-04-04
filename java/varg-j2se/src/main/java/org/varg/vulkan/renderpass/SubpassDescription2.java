
package org.varg.vulkan.renderpass;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.PipelineBindPoint;
import org.varg.vulkan.Vulkan10.SubpassDescriptionFlagBit;

/**
 * Wrapper for VkSubpassDescription
 *
 */
public class SubpassDescription2 {

    public static final int COLOR_ATTACHMENT_INDEX = 0;
    public static final int DEPTH_ATTACHMENT_INDEX = 1;
    public static final int RESOLVE_ATTACHMENT_INDEX = 2;

    public final SubpassDescriptionFlagBit[] flags;
    public final PipelineBindPoint pipelineBindPoint;
    public final AttachmentReference[] inputAttachments;
    final AttachmentReference[] attachments;

    public SubpassDescription2(AttachmentReference[] attachments) {
        if (attachments == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Attachment is null");
        }
        pipelineBindPoint = PipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS;
        flags = null;
        inputAttachments = null;
        this.attachments = attachments;
    }

    /**
     * Returns the color attachement reference
     * 
     * @return
     */
    public AttachmentReference getColorAttachmentReference() {
        return attachments[COLOR_ATTACHMENT_INDEX];
    }

    /**
     * Returns the depth attachement reference
     * 
     * @return
     */
    public AttachmentReference getDepthAttachmentReference() {
        return attachments[DEPTH_ATTACHMENT_INDEX];
    }

    /**
     * Returns the multisample resolve attachement, or null of sampling not used
     * 
     * @return
     */
    public AttachmentReference getResolveAttachment() {
        return attachments.length > RESOLVE_ATTACHMENT_INDEX ? attachments[RESOLVE_ATTACHMENT_INDEX] : null;
    }

}

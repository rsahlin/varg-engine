
package org.varg.vulkan.framebuffer;

import java.nio.LongBuffer;

import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.FramebufferCreateFlagBits;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.renderpass.RenderPass;

public class FramebufferCreateInfo {

    private final LongBuffer attachments;
    public final FramebufferCreateFlagBits[] flags;
    public final RenderPass renderPass;
    public final ImageView[] pAttachments;
    public final int width;
    public final int height;
    public final int layers;

    public FramebufferCreateInfo(FramebufferCreateFlagBits[] flags, RenderPass renderPass, ImageView[] attachments,
            int width, int height, int layers) {
        if (renderPass == null || attachments == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.flags = flags;
        this.renderPass = renderPass;
        pAttachments = new ImageView[attachments.length];
        System.arraycopy(attachments, 0, pAttachments, 0, attachments.length);
        this.width = width;
        this.height = height;
        this.layers = layers;
        this.attachments = Buffers.createLongBuffer(attachments.length);
        for (ImageView a : attachments) {
            long view = a.getImageView();
            if (view == 0) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "ImageView is 0");
            }
            this.attachments.put(view);
        }

    }

    public final int getFlagsValue() {
        return BitFlags.getFlagsValue(flags);
    }

    /**
     * Returns the buffer containing attachements pointer, buffer positioned at 0
     * 
     * @return
     */
    public LongBuffer getAttachments() {
        attachments.position(0);
        return attachments;
    }

}

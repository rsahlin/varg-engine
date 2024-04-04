
package org.varg.vulkan.renderpass;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.framebuffer.FrameBuffer;
import org.varg.vulkan.structs.Rect2D;

public class RenderPassBeginInfo {

    public final RenderPass renderPass;
    public final FrameBuffer framebuffer;
    public final Rect2D renderArea;
    public final ClearValue[] pClearValues;

    public RenderPassBeginInfo(RenderPass renderPass, FrameBuffer frameBuffer, Rect2D renderArea,
            ClearValue[] clearValues) {
        if (renderPass == null || frameBuffer == null || renderArea == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.renderPass = renderPass;
        this.framebuffer = frameBuffer;
        this.renderArea = renderArea;
        this.pClearValues = clearValues;
    }

}

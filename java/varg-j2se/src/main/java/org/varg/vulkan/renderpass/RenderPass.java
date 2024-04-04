
package org.varg.vulkan.renderpass;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;

public class RenderPass {

    public final long handle;
    private final RenderPassCreateInfo renderPassCreateInfo;

    /**
     * Internal constructor - do not use!
     * 
     * @param handle
     * @param renderPassCreateInfo
     */
    public RenderPass(long handle, RenderPassCreateInfo renderPassCreateInfo) {
        if (handle == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Handle is 0");
        }
        if (renderPassCreateInfo == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.handle = handle;
        this.renderPassCreateInfo = renderPassCreateInfo;
    }

    /**
     * Returns the renderpass createinfo
     * 
     * @return
     */
    public RenderPassCreateInfo getRenderPassCreateInfo() {
        return renderPassCreateInfo;
    }

    /**
     * Returns the colocbuffer samples
     * 
     * @return
     */
    public SampleCountFlagBit getColorBufferSamples() {
        return renderPassCreateInfo.attachments[SubpassDescription2.COLOR_ATTACHMENT_INDEX].samples;
    }

    public void getAttachement() {
        // renderPassCreateInfo.attachments[0].
    }

}

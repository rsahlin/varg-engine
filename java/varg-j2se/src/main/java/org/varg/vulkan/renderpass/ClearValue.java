
package org.varg.vulkan.renderpass;

import org.varg.vulkan.cmd.ClearColorValue;
import org.varg.vulkan.cmd.ClearDepthStencilValue;

public class ClearValue {

    private ClearColorValue color;
    public final ClearDepthStencilValue depthStencil;

    public ClearValue(ClearColorValue color, ClearDepthStencilValue depthScencil) {
        this.color = new ClearColorValue(color);
        this.depthStencil = new ClearDepthStencilValue(depthScencil);
    }

    /**
     * Sets the clearcolor value
     * 
     * @param clearColor
     */
    public void setClearColor(ClearColorValue clearColor) {
        this.color = clearColor;
    }

    /**
     * Returns the clearcolor value
     * 
     * @return
     */
    public ClearColorValue getClearColor() {
        return color;
    }

}

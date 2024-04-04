
package org.varg.vulkan.cmd;

public class ClearDepthStencilValue {

    public final float depth;
    public final int stencil;

    public ClearDepthStencilValue(ClearDepthStencilValue source) {
        this.depth = source.depth;
        this.stencil = source.stencil;
    }

    public ClearDepthStencilValue(float depth, int stencil) {
        this.depth = depth;
        this.stencil = stencil;
    }

}

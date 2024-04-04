
package org.varg.vulkan.structs;

/**
 * Wrapper for VkRect2D
 */
public class Rect2D {

    public final Offset2D offset;
    public final Extent2D extent;

    /**
     * Creates a new rect2d with the extent as widht/height - offset will be 0,0
     * 
     * @param extent
     */
    public Rect2D(Extent2D extent) {
        offset = new Offset2D(0, 0);
        this.extent = new Extent2D(extent);
    }

    public Rect2D(int x, int y, int width, int height) {
        offset = new Offset2D(x, y);
        extent = new Extent2D(width, height);
    }

}

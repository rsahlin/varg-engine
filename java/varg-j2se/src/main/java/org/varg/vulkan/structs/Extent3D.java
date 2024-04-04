
package org.varg.vulkan.structs;

public class Extent3D extends Extent2D {

    public final int depth;

    public Extent3D(Extent2D extent) {
        super(extent);
        this.depth = 1;
    }

    public Extent3D(int width, int height, int depth) {
        super(width, height);
        this.depth = depth;
    }

    @Override
    public String toString() {
        return width + ", " + height + ", " + depth;
    }

}

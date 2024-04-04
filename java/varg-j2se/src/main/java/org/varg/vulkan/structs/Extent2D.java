
package org.varg.vulkan.structs;

public class Extent2D {

    public final int width;
    public final int height;

    public Extent2D(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Extent2D(Extent2D source) {
        this.width = source.width;
        this.height = source.height;
    }

    @Override
    public String toString() {
        return "Size " + width + ", " + height;
    }

}

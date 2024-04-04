
package org.varg.vulkan.structs;

public class Offset3D extends Offset2D {

    public final int z;

    public Offset3D() {
        super(0, 0);
        this.z = 0;
    }

    public Offset3D(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

}

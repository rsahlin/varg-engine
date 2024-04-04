
package org.varg.assets;

import org.varg.vulkan.Vulkan10;

/**
 * Abstraction for platform texture image object, this is the object that holds the texture memory image.
 *
 */
public abstract class TextureMemory {

    public final long sizeInBytes;
    public final int width;
    public final int height;
    public final Vulkan10.Format format;

    protected TextureMemory(int w, int h, Vulkan10.Format format, long size) {
        width = w;
        height = h;
        this.format = format;
        sizeInBytes = size;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + format.hashCode();
        result = prime * result + height;
        result = prime * result + (int) (sizeInBytes ^ (sizeInBytes >>> 32));
        result = prime * result + width;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        TextureMemory other = (TextureMemory) obj;
        if (format != other.format) {
            return false;
        } else if (height != other.height) {
            return false;
        } else if (sizeInBytes != other.sizeInBytes) {
            return false;
        } else if (width != other.width) {
            return false;
        }
        return true;
    }

    public abstract long getMemoryPointer();

}

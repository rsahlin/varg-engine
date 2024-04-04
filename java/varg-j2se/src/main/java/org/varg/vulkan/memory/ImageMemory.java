
package org.varg.vulkan.memory;

import org.eclipse.jdt.annotation.NonNull;

import org.gltfio.lib.BitFlags;
import org.varg.assets.TextureMemory;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.image.Image;

public class ImageMemory extends TextureMemory {

    private boolean bound = false;
    private final Memory memory;
    private final Image image;
    public final long offset;

    public ImageMemory(int width, int height, Vulkan10.Format format, long offset, long size,
            @NonNull Image image, @NonNull Memory memory) {
        super(width, height, format, size);
        this.offset = offset;
        this.image = image;
        this.memory = memory;
    }

    /**
     * Returns the image
     * 
     * @return
     */
    public Image getImage() {
        return image;
    }

    /**
     * Returns true if this memory has been bound
     * 
     * @return
     */
    public boolean isBound() {
        return bound;
    }

    /**
     * Internal method - DO NOT USE
     * 
     * @param bound
     */
    public void setBound(boolean bound) {
        this.bound = bound;
    }

    /**
     * Returns the memory
     * 
     * @return
     */
    public Memory getMemory() {
        return memory;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((image == null) ? 0 : image.hashCode());
        result = prime * result + ((memory == null) ? 0 : memory.hashCode());
        result = prime * result + (int) (offset ^ (offset >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!super.equals(obj)) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        ImageMemory other = (ImageMemory) obj;
        if (image == null) {
            if (other.image != null) {
                return false;
            }
        } else if (!image.equals(other.image)) {
            return false;
        } else if (memory == null) {
            if (other.memory != null) {
                return false;
            }
        } else if (!memory.equals(other.memory)) {
            return false;
        } else if (offset != other.offset) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Memorysize " + memory.size + ", dimension " + image.getExtent() + ", Format " + Vulkan10.Format.get(
                image.getFormatValue()) + ", Createflags " + image.getCreateFlags() + ", Usageflags " + BitFlags
                        .toString(image.getUsageFlags()) + ", Miplevels " + image.getMipLevels() + ", Arraylayers "
                + image.getArrayLayers() + ", Samples " + image.getSamples();
    }

    @Override
    public long getMemoryPointer() {
        return memory.pointer;
    }

}

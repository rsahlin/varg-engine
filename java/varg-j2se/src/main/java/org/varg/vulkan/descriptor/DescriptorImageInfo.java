
package org.varg.vulkan.descriptor;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.assets.TextureDescriptor;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.image.ImageSubresourceRange;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.memory.ImageMemory;
import org.varg.vulkan.structs.Sampler;

/**
 * 
 * Wrapper for VkDescriptorImageInfo
 * Vulkan implementation of descriptorimageinfo
 *
 */
public class DescriptorImageInfo extends TextureDescriptor {

    public final Sampler sampler;
    public final ImageView imageView;
    public final ImageLayout imageLayout;

    public DescriptorImageInfo(@NonNull Sampler sampler, @NonNull ImageView imageView, @NonNull ImageMemory texture,
            @NonNull ImageSubresourceRange subresource) {
        super(texture);
        this.sampler = sampler;
        this.imageView = imageView;
        this.imageLayout = texture.getImage().getLayout(subresource);
    }

    @Override
    public String toString() {
        return "Dimension: " + imageView.image.getExtent().width + ", " + imageView.image.getExtent().height + ", "
                + Vulkan10.Format.get(imageView.image.getFormatValue());
    }
}

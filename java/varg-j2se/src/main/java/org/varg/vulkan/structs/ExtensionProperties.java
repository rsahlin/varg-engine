
package org.varg.vulkan.structs;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.Logger;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.VulkanExtension;

/**
 * Support for VkExtensionProperties
 *
 */
public final class ExtensionProperties {
    public ExtensionProperties(String name, int specVersion) {
        this.name = name;
        this.specVersion = specVersion;
    }

    String name;
    int specVersion;

    public String getName() {
        return name;
    }

    public int getSpecVersion() {
        return specVersion;
    }

    public static VulkanExtension[] getExtensions(ExtensionProperties... extensions) {
        ArrayList<VulkanExtension> result = new ArrayList<>();
        for (ExtensionProperties extension : extensions) {
            VulkanExtension e = VulkanExtension.getExtension(extension.getName());
            if (e != null) {
                result.add(e);
            } else {
                Logger.d(Vulkan10.class, "Deprecated extension " + extension.getName());
            }
        }
        return result.toArray(new VulkanExtension[0]);
    }

    /**
     * Returns the extension property for the extension if it is present in array
     * 
     * @param requested
     * @param extensions
     * @return Extension property or null
     */
    public static ExtensionProperties get(String requested, ExtensionProperties... extensions) {
        for (ExtensionProperties e : extensions) {
            if (e.getName().equalsIgnoreCase(requested)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Returns the name of the extension in a null terminated byte buffer.
     * 
     * @return
     */
    public ByteBuffer createByteBuffer() {
        return Buffers.createByteBuffer(name);
    }

}

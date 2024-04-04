
package org.varg.vulkan;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.vulkan.structs.ExtensionProperties;
import org.varg.vulkan.structs.PhysicalDeviceFeatureExtensions;
import org.varg.vulkan.structs.PhysicalDeviceFeatures;

/**
 * Collection of physical device features and extensions supported.
 *
 */
public class Features {

    private final PhysicalDeviceFeatures deviceFeatures;
    private final PhysicalDeviceFeatureExtensions deviceFeatureExtensions;
    private final ExtensionProperties[] extensions;

    public Features(@NonNull PhysicalDeviceFeatures deviceFeatures,
            @NonNull PhysicalDeviceFeatureExtensions deviceFeatureExtensions, ExtensionProperties[] extensions) {
        this.deviceFeatures = deviceFeatures;
        this.deviceFeatureExtensions = deviceFeatureExtensions;
        this.extensions = extensions;
    }

    /**
     * Returns the physical device features
     * 
     * @return
     */
    public PhysicalDeviceFeatures getPhysicalDeviceFeatures() {
        return deviceFeatures;
    }

    /**
     * Returns the physical device feature extensions
     * 
     * @return
     */
    public PhysicalDeviceFeatureExtensions getPhysicalDeviceFeatureExtensions() {
        return deviceFeatureExtensions;
    }

    /**
     * Returns the extension properties, this includes deprecated extensions
     * 
     * @return
     */
    public ExtensionProperties[] getExtensions() {
        return extensions;
    }

}

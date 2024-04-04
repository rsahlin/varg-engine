
package org.varg;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.SurfaceFormat;

/**
 * Singleton taking care of reading properties - this is ONLY read properties that needs to be accessible such
 * as the surfaceformat.
 *
 */
public class VargReadProperties {

    private static VargReadProperties environment;

    private VargReadProperties() {
    }

    public static VargReadProperties getInstance() {
        if (environment == null) {
            environment = new VargReadProperties();
        }
        return environment;
    }

    private SurfaceFormat surfaceFormat;

    /**
     * Internal method - sets the surface format - throws Exception if already set
     * 
     * @param setSurfaceFormat
     */
    public void setSurfaceFormat(SurfaceFormat setSurfaceFormat) {
        if (this.surfaceFormat != null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + ", surfaceformat already set : " + this.surfaceFormat);
        }
        surfaceFormat = setSurfaceFormat;
    }

    /**
     * Returns the display surfaceformat
     * 
     * @return
     */
    public SurfaceFormat getSurfaceFormat() {
        return surfaceFormat;
    }

}

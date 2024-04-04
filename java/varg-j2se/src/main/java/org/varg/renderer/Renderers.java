
package org.varg.renderer;

/**
 * The supported renderers
 * 
 */
public enum Renderers {

    VULKAN10(1, 0),
    VULKAN11(1, 1),
    VULKAN12(1, 2),
    VULKAN13(1, 3);

    public final int major;
    public final int minor;

    Renderers(int maj, int min) {
        major = maj;
        minor = min;
    };

    /**
     * Returns the enum from major.minor version
     * 
     * @param glVersion
     * @return
     */
    public static Renderers get(int[] glVersion) {
        for (Renderers r : Renderers.values()) {
            if (r.major == glVersion[0] && r.minor == glVersion[1]) {
                return r;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "vulkan" + major + "." + minor;
    }

}


package org.varg.renderer;

import java.util.List;
import java.util.StringTokenizer;

/**
 * Info about the renderer in the system.
 *
 */
public abstract class RenderInfo {

    /**
     * The version number uses one of these forms:
     * major_number.minor_number major_number.minor_number.release_number
     * Vendor-specific information may follow the version number.
     * Its format depends on the implementation, but a space always separates the version number and the vendor-specific
     * information.
     *
     */
    public static class Version {
        public final int major;
        public final int minor;

        public Version(String versionStr) {
            int offset = 0;
            int whitespace = 0;
            String result = null;
            while ((whitespace = versionStr.indexOf(" ", offset)) != -1) {
                result = versionStr.substring(offset, whitespace);
                if (result.contains(".")) {
                    break;
                }
                offset = whitespace + 1;
            }
            if (whitespace == -1) {
                result = versionStr.substring(offset);
            }
            int dotIndex = result.indexOf(".");
            major = Integer.parseInt(result.substring(0, dotIndex));
            // Check for release number
            StringTokenizer st = new StringTokenizer(result.substring(dotIndex + 1));
            minor = Integer.parseInt(st.nextToken());
        }
    }

    protected String vendor;
    protected String renderer;
    protected Version glVersion;
    protected Version shadingLanguageVersion;
    protected List<String> extensions;
    protected int maxTextureSize;
    protected Renderers renderVersion;

    /**
     * Internal constructor - do not use
     */
    protected RenderInfo(Renderers version) {
        renderVersion = version;
    }

    /**
     * Returns the company responsible for this implementation.
     * This name does not change from release to release.
     * 
     * @return
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Returns the name of the renderer. This name is typically specific to a particular configuration of a hardware
     * platform. It does not change from release to release.
     * 
     * @return
     */
    public String getRenderer() {
        return renderer;
    }

    /**
     * Returns the renderer version
     * 
     * @return
     */
    public Renderers getRenderVersion() {
        return renderVersion;
    }

    /**
     * Returns the highest shader language version supported
     * 
     * @return
     */
    public Version getShadingLanguageVersion() {
        return shadingLanguageVersion;
    }

}

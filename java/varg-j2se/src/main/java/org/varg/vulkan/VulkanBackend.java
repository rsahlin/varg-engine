
package org.varg.vulkan;

import java.util.HashSet;
import java.util.StringTokenizer;

import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Settings;
import org.gltfio.lib.Settings.BooleanProperty;
import org.gltfio.lib.Settings.FloatProperty;
import org.gltfio.lib.Settings.IntArrayProperty;
import org.gltfio.lib.Settings.IntProperty;
import org.gltfio.lib.Settings.StringProperty;
import org.ktximageio.ktx.ImageReader.TransferFunction;
import org.varg.renderer.Renderers;
import org.varg.uniform.GltfStorageBuffers;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;
import org.varg.vulkan.Vulkan10.ColorSpaceKHR;
import org.varg.vulkan.Vulkan10.Result;
import org.varg.vulkan.structs.QueueFamilyProperties;
import org.varg.window.J2SEWindow.WindowHandle;

/**
 * The Vulkan backend -all things related to Vulkan functionality that is shared
 * independently of version
 *
 */
public abstract class VulkanBackend {

    public static final int RESERVED_FOR_FUTURE_USE = 0;

    public final Renderers version;

    /**
     * Checks that the resultcode is VK_SUCCESS, if not RuntimeException is thrown.
     * 
     * @param value
     */
    public static void assertResult(int value) {
        Result r = Result.getResult(value);
        if (r == null || r != Result.VK_SUCCESS) {
            throw new RuntimeException("Failed with error: " + r);
        }
    }

    /**
     * Key / value properties for render settings
     *
     */
    public enum BackendProperties implements BooleanProperty {
        /**
         * Used for enabling the validation layers in the render backend.
         * If true validation shall be turned on for the graphics layer.
         */
        VALIDATE("varg.validate", true),
        /**
         * Used for runtime switch of debug checks - to enable debug features this must be 'true'
         */
        DEBUG("varg.debug", true),
        /**
         * Disable backface culling - pipeline property, only read when creating pipeline.
         * No dynamic toggle
         */
        NO_BACKFACE_CULLING("varg.nobackfaceculling", false),
        /**
         * Disable depth test - pipeline property, only read when creating pipeline.
         * No dynamic toggle
         */
        NO_DEPTHTEST("varg.nodepthtest", false),
        /**
         * Disable normal texture - compiler property, only read when compiling shader
         * No dynamic toggle
         */
        NO_NORMALTEXTURE("varg.nonormaltexture", false),
        /**
         * Disable mr texture - compiler property, only read when compiling shader
         * No dynamic toggle
         */
        NO_MRTEXTURE("varg.nomrtexture", false),
        /**
         * Disable occlusion texture - compiler property, only read when compiling shader
         * No dynamic toggle
         */
        NO_OCCLUSIONTEXTURE("varg.noocclusiontexture", false),
        /**
         * Disable emissive texture - compiler property, only read when compiling shader
         * No dynamic toggle
         */
        NO_EMISSIVETEXTURE("varg.noemissivetexture", false),
        RECOMPILE_SPIRV("varg.recompile-spirv", true),
        DISPLAYENCODE("varg.displayencode", true),
        DOUBLEBUFFER("varg.doublebuffer", true),
        FULLSCREEN("varg.fullscreen", false),
        /**
         * Set to true to keep gltf source images after textures are created - may be used for testing purposes
         */
        KEEP_SOURCE_IMAGES("varg.keepsourceimages", false),
        HEADLESS("varg.headless", false);

        private final String key;
        private final boolean defaultValue;

        BackendProperties(String key, boolean defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return Boolean.toString(defaultValue);
        }

    }

    public enum BackendStringProperties implements StringProperty {
        /**
         * lines or points
         * Can be switched in runtime
         */
        DEBUG_MODE("varg.debugmode", null, (String[]) null),
        COLORSPACE("varg.colorspace", null, ColorSpaceKHR.values()),
        SURFACE_FORMAT("varg.surfaceformat", TransferFunction.LINEAR.name(),
                new String[] { TransferFunction.LINEAR.name(), TransferFunction.SRGB.name() }),
        DEBUGCHANNEL("varg.debugchannel", null, new String[] { "BASECOLOR", "ORM", "OCCLUSION",
                "NORMAL", "FRESNEL", "REFLECTED", "TRANSMITTED", "CUBEMAP", "HSL", "CHROMA", "NDF", "GAF" }),
        BRDF("varg.brdf", "RSAHLIN", new String[] { "RSAHLIN" }),
        // Option to add image usage flags for the swapchain - for instance for debugging
        SWAPCHAIN_USAGE("varg.swapchain_usage", null, (String[]) null),
        // Option to add uniform buffer usage flags - for instance for debugging
        UNIFORM_USAGE("varg.uniform_usage", null, (String[]) null),
        // Option to add vertex buffer usage flags - for instance for debugging
        VERTEX_USAGE("varg.vertex_usage", null, (String[]) null);

        private final String key;
        public final String[] values;

        private final String defaultValue;

        BackendStringProperties(String key, String defaultValue, String... values) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.values = values;
        }

        BackendStringProperties(String key, String defaultValue, ColorSpaceKHR... colorSpaces) {
            this.key = key;
            this.defaultValue = defaultValue;
            values = new String[colorSpaces.length];
            for (int i = 0; i < colorSpaces.length; i++) {
                values[i] = colorSpaces[i].name();
            }
        }

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return defaultValue;
        }

        /**
         * Returns the allowed string values as one string.
         * 
         * @return
         */
        public String getValues() {
            StringBuffer sb = new StringBuffer();
            for (String s : values) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(s);
            }
            return sb.toString();
        }

    }

    /**
     * Integer properties, value is an int
     *
     */
    public enum BackendIntProperties implements IntProperty {

        /**
         * Number of samples to use when rendering, 1,2,4,8,16 or 32 for vulkan
         */
        SAMPLE_COUNT("varg.samplecount", 8),
        /**
         * If > 1 then fragmentshadingrate is enable for background render
         */
        BACKGROUND_FRAGMENTSIZE("varg.background.fragmentsize", 1),
        /**
         * If > 1 then fragmentshadingrate is enable for graphicspipeline
         */
        FRAGMENTSIZE("varg.fragmentsize", 1),
        SURFACE_WIDTH("varg.width", 1920),
        SURFACE_HEIGHT("varg.height", 1080),
        MAX_WHITE("varg.maxwhite", 10000),
        // The HDR allowed max lightlevel - displayencoding will use factor output pixels using:
        // 10000 / hdrmaxlight
        // Use this option where the framebuffer is not output directly, for instance Win 10/11 windowed mode where
        // the hdrmaxlight is 1000 (from HDR10)
        HDR_MAX_CONTENT_LIGHTLEVEL("varg.hdrmaxlight", 10000),
        MAX_CUBEMAPS("varg.maxcubemaps", GltfStorageBuffers.MAX_CUBEMAP_COUNT),
        MAX_DIRECTIONAL_LIGHTS("varg.maxdirectional", GltfStorageBuffers.MAX_DIRECTIONAL_LIGHTS),
        MAX_POINT_LIGHTS("varg.maxpoint", GltfStorageBuffers.MAX_POINT_LIGHTS);

        public final String key;
        public final int defaultValue;

        BackendIntProperties(String key, int defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return Integer.toString(defaultValue);
        }

    }

    public enum IntArrayProperties implements IntArrayProperty {

        /**
         * Set to null to disable clear
         */
        CLEAR_COLOR("gltf.clearcolor", new int[] { 50, 50, 50, 255 }),
        WINDOW_POSITION("varg.windowposition", null);

        private final String key;
        private final int[] defaultValue;

        IntArrayProperties(String key, int... defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            if (defaultValue != null) {
                // TODO - count number of values in enum
                return defaultValue[0] + "," + defaultValue[1] + "," + defaultValue[2] + "," + defaultValue[3];
            }
            return null;
        }
    }

    /**
     * Properties where value is a float
     *
     */
    public enum FloatProperties implements FloatProperty {

        MAX_ANISOTROPY("varg.maxanisotropy", null);

        private final String key;
        private final String defaultValue;

        FloatProperties(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return defaultValue;
        }

    }

    public interface VulkanDeviceSelector {
        /**
         * Vulkan implementation agnostic method to select the device to use
         * 
         * @param devices
         * @return The device to use or null if no device that can be used
         */
        PhysicalDevice selectDevice(PhysicalDevice[] devices, WindowHandle window);

        /**
         * Used to select the queue instance of the device
         * 
         * @param device
         */
        QueueFamilyProperties selectQueueInstance(PhysicalDevice device, WindowHandle window);

    }

    protected static VulkanDeviceSelector deviceSelector;

    /**
     * Sets the vulkan device selector - use this to override the default selector
     * behavior
     * 
     * @param setDeviceSelector The device selector to be called when Vulkan backend is
     * created, or null to remove.
     */
    public static void setVulkanDeviceSelector(VulkanDeviceSelector setDeviceSelector) {
        VulkanBackend.deviceSelector = setDeviceSelector;
    }

    /**
     * The Vulkan API version #define VK_VERSION_MAJOR(version) ((int)(version) >>
     * 22) #define VK_VERSION_MINOR(version) (((int)(version) >> 12) & 0x3ff)
     * #define VK_VERSION_PATCH(version) ((int)(version) & 0xfff)
     *
     */
    public class APIVersion {
        public final int major;
        public final int minor;
        public final int patch;

        public APIVersion(int api) {
            this.major = api >> 22;
            this.minor = (api >> 12) & 0x3ff;
            this.patch = (api & 0x0fff);
        }

        @Override
        public String toString() {
            return major + "." + minor + " patch " + patch;
        }

    }

    protected VulkanBackend(Renderers version) {
        this.version = version;
    }

    /**
     * Destroys the backend instance - call this when application exits to release render API instance.
     * If not initialized or already destroyed then this method does nothing.
     * - Do NOT make calls to backend after calling this method.
     */
    protected abstract void destroy();

    /**
     * Returns an array with the specified usage flags plus any additional buffer usage flags that may be set using
     * the string property.
     * 
     * @param addFlags
     * @param flags
     * @return
     */
    public static BufferUsageFlagBit[] getBufferUsage(StringProperty addFlags, BufferUsageFlagBit... flags) {
        HashSet<BufferUsageFlagBit> usage = new HashSet<BufferUsageFlagBit>();
        for (BufferUsageFlagBit flag : flags) {
            usage.add(flag);
        }
        String uniformUsage = Settings.getInstance().getProperty(addFlags);
        if (uniformUsage != null) {
            StringTokenizer st = new StringTokenizer(uniformUsage, ",");
            while (st.hasMoreTokens()) {
                String flagStr = st.nextToken();
                BufferUsageFlagBit flag = BufferUsageFlagBit.get(flagStr);
                if (flag == null) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid usage flag: "
                            + flagStr);
                }
                usage.add(flag);
            }
        }
        return usage.toArray(new BufferUsageFlagBit[0]);
    }

}

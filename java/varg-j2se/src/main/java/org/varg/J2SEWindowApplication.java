
package org.varg;

import java.util.StringTokenizer;

import org.gltfio.lib.ConsoleInputScanner;
import org.gltfio.lib.ConsoleInputScanner.ConsoleInputListener;
import org.gltfio.lib.Constants;
import org.gltfio.lib.DefaultPeriodicLogger;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.gltfio.lib.Settings.ModuleProperties;
import org.varg.gltf.VulkanMesh;
import org.varg.renderer.GltfRenderer;
import org.varg.renderer.RenderFactory;
import org.varg.renderer.Renderers;
import org.varg.renderer.SurfaceConfiguration;
import org.varg.vulkan.Vulkan10.Format;
import org.varg.vulkan.Vulkan10.FormatFeatureFlagBits;
import org.varg.vulkan.Vulkan10.ImageTiling;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.Vulkan10Backend.CreateDevice;
import org.varg.vulkan.VulkanBackend;
import org.varg.vulkan.VulkanBackend.BackendIntProperties;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.structs.DeviceLimits;
import org.varg.vulkan.structs.PhysicalDeviceFeatures;
import org.varg.window.J2SEWindow;

/**
 * Base class for J2SE Windowed application, use this for implementations that need to create a window
 *
 */
public abstract class J2SEWindowApplication implements ConsoleInputListener {

    /**
     * Console based commands that is parsed by the application
     *
     */
    public enum ApplicationCommand {
        /**
         * Set a system property as key=value
         */
        SETPROP(),
        /**
         * List the physical device features available
         */
        PHYSICALDEVICEFEATURES(),
        /**
         * List device limits
         */
        DEVICELIMITS(),
        /**
         * List the used texture channels, formats, sizes and samplers
         */
        TEXTURES(),
        /**
         * List the memory currently allocated
         */
        MEMORY(),
        /**
         * Info from the glTF asset
         */
        INFO(),
        /**
         * Set the logger periodicy
         */
        LOGGER(),
        /**
         * Run granska
         */
        GRANSKA();

        public static ApplicationCommand get(String command) {
            for (ApplicationCommand c : values()) {
                if (c.name().equalsIgnoreCase(command)) {
                    return c;
                }
            }
            return null;
        }

    }

    private void handleCommand(ApplicationCommand command, StringTokenizer tokenizer) {
        Logger.d(getClass(), "Application command: " + command);
        switch (command) {
            case SETPROP:
                String key = tokenizer.hasMoreTokens() ? tokenizer.nextToken("=") : null;
                String value = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
                if (key != null && value != null) {
                    key = key.trim();
                    value = value.trim();
                    Settings.getInstance().setProperty(key, value);
                } else {
                    Logger.d(getClass(), "Missing key/value : " + key + " = " + value);
                }
                break;
            case PHYSICALDEVICEFEATURES:
                Vulkan10Backend<?> backend = renderer.getBackend();
                PhysicalDeviceFeatures features = backend.getPhysicalDeviceFeatures();
                Logger.d(getClass(), "Selected physical device features:");
                Logger.d(getClass(), features.toString() + "\n");
                Logger.d(getClass(),
                        "Format support for VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT in optimal tiling features");
                Format[] formats = backend.getSupportedFormats(ImageTiling.VK_IMAGE_TILING_OPTIMAL,
                        FormatFeatureFlagBits.VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT);
                Logger.d(getClass(), Format.toString(formats));
                break;
            case DEVICELIMITS:
                DeviceLimits limits = renderer.getBackend().getSelectedDevice().getProperties().getLimits();
                Logger.d(getClass(), "Selected device limits:\n");
                Logger.d(getClass(), limits.toString());
                break;
            case MEMORY:
                renderer.getBufferFactory().logMemory();
                break;
            case LOGGER:
                value = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
                if (value != null) {
                    int threshold = Integer.parseInt(value);
                    DefaultPeriodicLogger.getInstance().setLogDelta(threshold);
                    Logger.d(getClass(), "Set periodic logger threshold to " + threshold + " seconds");
                }
                break;
            default:
                Logger.d(getClass(), ErrorMessage.NOT_IMPLEMENTED.message + command);
        }
    }

    public enum WindowType {
        GLFW(),
        HEADLESS();
    }

    /**
     * Property settings that can be changed by user
     *
     */
    public static class PropertySettings {
        /**
         * Settings that can be changed
         * TODO - move to a new settings class?
         */
        public final int alphaBits;
        public final int samples;
        public final int depthBits;
        public final int width;
        public final int height;
        public final boolean fullscreen;
        public final WindowType windowType;

        public final int swapInterval = 1;

        public PropertySettings(int depth, int alpha, int sampleCount, boolean fullScreen, WindowType windowType) {
            int value = Settings.getInstance().getInt(BackendIntProperties.SAMPLE_COUNT);
            samples = value == Constants.NO_VALUE ? sampleCount : value;
            value = Settings.getInstance().getInt(BackendIntProperties.SURFACE_WIDTH);
            width = value == Constants.NO_VALUE ? DEFAULT_WINDOW_WIDTH : value;
            value = Settings.getInstance().getInt(BackendIntProperties.SURFACE_HEIGHT);
            height = value == Constants.NO_VALUE ? DEFAULT_WINDOW_HEIGHT : value;
            depthBits = depth;
            alphaBits = alpha;
            fullscreen = fullScreen;
            this.windowType = windowType;
        }

        /**
         * Returns surface configuration
         * 
         * @return
         */
        public SurfaceConfiguration getConfiguration() {
            SurfaceConfiguration config = new SurfaceConfiguration();
            config.setDepthBits(depthBits);
            config.setAlphaBits(alphaBits);
            config.setSamples(samples);
            return config;
        }

    }

    public static class HeadlessPropertySettings extends PropertySettings {

        public HeadlessPropertySettings() {
            super(16, 8, 0, false, WindowType.HEADLESS);
        }

    }

    public static final int DEFAULT_SAMPLES = VulkanBackend.BackendIntProperties.SAMPLE_COUNT.defaultValue;
    public static final int DEFAULT_ALPHA_BITS = 8;
    public static final int DEFAULT_DEPTH_BITS = 32;
    public static final int DEFAULT_WINDOW_WIDTH = 1920;
    public static final int DEFAULT_WINDOW_HEIGHT = 1080;

    private J2SEWindow j2seWindow;
    public final RenderFactory factory;
    protected final String title;
    private GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer;
    protected ConsoleInputScanner scanner;
    protected Renderers version;
    protected PropertySettings appSettings;

    public J2SEWindowApplication(String[] args, RenderFactory fac, Renderers version, String titleStr) {
        factory = fac;
        title = titleStr;
        this.version = version;
        if (!Settings.getInstance().getBoolean(BackendProperties.HEADLESS)) {
            appSettings = new PropertySettings(DEFAULT_DEPTH_BITS, DEFAULT_ALPHA_BITS, DEFAULT_SAMPLES,
                    Settings.getInstance().getBoolean(BackendProperties.FULLSCREEN), WindowType.GLFW);
        } else {
            appSettings = new HeadlessPropertySettings();
        }

        if (Settings.getInstance().getProperty(ModuleProperties.NAME) == null) {
            Module module = getClass().getModule();
            String moduleName = module.getName();
            Settings.getInstance().setProperty(ModuleProperties.NAME, moduleName);
            Logger.d(getClass(), "Set module NAME to: " + moduleName);
        }

    }

    public final J2SEWindow getJ2SEWindow() {
        return j2seWindow;
    }

    public final GltfRenderer<VulkanRenderableScene, VulkanMesh> getRenderer() {
        return renderer;
    }

    public final void setWindow(J2SEWindow window) {
        this.j2seWindow = window;
    }

    /**
     * Creates the renderer according to settings and initializes it
     */
    protected void createRenderer(CreateDevice callback) {
        renderer = factory.createRenderer(version, j2seWindow.getWindowHandle(), callback);
        renderer.init(appSettings.getConfiguration(), appSettings.width, appSettings.height);
    }

    /**
     * Create and setup the window implementation based on the renderer version
     * The returned window shall be ready to be used.
     * 
     * @return
     */
    protected abstract J2SEWindow createWindow(Renderers version);

    /**
     * Quits the render loop
     */
    protected abstract void destroy();

    /**
     * Creates the input scanner using a new thread to read.
     * 
     * @throws IllegalArgumentException If console input already has been started
     */
    protected void startConsoleInput() {
        if (scanner != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Console input already started");
        }
        scanner = new ConsoleInputScanner(this);
    }

    @Override
    public void handleInput(String line) {
        if (!line.trim().isEmpty()) {
            StringTokenizer st = new StringTokenizer(line);
            String cString = st.nextToken();
            ApplicationCommand appCommand = ApplicationCommand.get(cString);
            if (appCommand != null) {
                handleCommand(appCommand, st);
            }
        }
    }

}

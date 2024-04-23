package org.varg.lwjgl3.apps;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;

import org.gltfio.deserialize.Ladda;
import org.gltfio.deserialize.LaddaProperties;
import org.gltfio.gltf2.AssetBaseObject;
import org.gltfio.gltf2.J2SEModelPreparation;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.BACKGROUND;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.KHREnvironmentMapReference;
import org.gltfio.gltf2.extensions.KHRdisplayencoding;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Key;
import org.gltfio.lib.KeyListener;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.gltfio.prepare.GltfSettings;
import org.gltfio.prepare.GltfSettings.Alignment;
import org.gltfio.prepare.ModelPreparation;
import org.ktximageio.itu.BT2100;
import org.varg.BackendException;
import org.varg.J2SEWindowApplication;
import org.varg.assets.TextureImages;
import org.varg.gltf.VulkanGltf;
import org.varg.gltf.VulkanMesh;
import org.varg.lwjgl3.vulkan.GLFWVulkanWindow;
import org.varg.lwjgl3.vulkan.LWJGL3RenderFactory;
import org.varg.lwjgl3.vulkan.LWJGLNativeBuffer;
import org.varg.renderer.DrawCallBundle;
import org.varg.renderer.GltfRenderer;
import org.varg.renderer.Renderers;
import org.varg.scene.GltfSceneControl;
import org.varg.shader.BackgroundMeshShader.BackgroundMeshShaderCreateInfo;
import org.varg.shader.Gltf2GraphicsShader.Gltf2GraphicsShaderCreateInfo;
import org.varg.shader.Gltf2GraphicsShader.GraphicsShaderType;
import org.varg.shader.MeshShader;
import org.varg.shader.MeshShader.MeshShaderType;
import org.varg.uniform.DescriptorBuffers;
import org.varg.uniform.GltfStorageBuffers;
import org.varg.vulkan.Features;
import org.varg.vulkan.IndirectDrawCalls;
import org.varg.vulkan.IndirectDrawing;
import org.varg.vulkan.NativeBuffer;
import org.varg.vulkan.Vulkan10.ColorSpaceKHR;
import org.varg.vulkan.Vulkan10.DescriptorPoolCreateFlagBits;
import org.varg.vulkan.Vulkan10.DescriptorType;
import org.varg.vulkan.Vulkan10.Extension;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.Vulkan10.Vulkan12Extension;
import org.varg.vulkan.VulkanBackend.BackendIntProperties;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.descriptor.DescriptorPoolCreateInfo;
import org.varg.vulkan.descriptor.DescriptorPoolSize;
import org.varg.vulkan.extensions.KHRFragmentShadingRate.PipelineFragmentShadingRateStateCreateInfoKHR;
import org.varg.vulkan.memory.VertexMemory;
import org.varg.vulkan.renderpass.RenderPassCreateInfo;
import org.varg.vulkan.structs.ExtensionProperties;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.PhysicalDeviceFeatureExtensions;
import org.varg.vulkan.structs.PhysicalDeviceFeatures.VulkanFeatures;
import org.varg.vulkan.structs.PhysicalDeviceFeatures.VulkanPhysicalDeviceFeatures;
import org.varg.vulkan.structs.PhysicalDeviceVulkan12Features.Vulkan12Features;
import org.varg.vulkan.structs.PhysicalDeviceVulkan13Features.Vulkan13Features;
import org.varg.vulkan.structs.RequestedFeatures;
import org.varg.window.J2SEWindow;

/**
 * Entry point for a Vulkan application using lwjgl3 library
 */
public abstract class LWJGL3Application extends J2SEWindowApplication implements KeyListener {

    /**
     * We should never need more than this samplers
     */
    protected static final int MAX_DESCRIPTORSET_SAMPLERS = 32;
    protected static final WindowType DEFAULT_WINDOW_TYPE = WindowType.GLFW;

    protected volatile GltfSceneControl sceneControl;
    protected AssetBaseObject<VulkanRenderableScene> loadedAsset;
    protected int sceneIndex = 0;
    private volatile boolean running = false;
    private volatile boolean initialized = false;
    private final HashMap<Integer, Boolean> keyMap = new HashMap<Integer, Boolean>();
    private GltfSettings settings;
    private J2SEModelPreparation modelPrep = new J2SEModelPreparation();
    protected Gltf2GraphicsShaderCreateInfo shaderInfo;
    protected MeshShader backgroundShader;
    /**
     * Keep track of assets that have been prepared for render
     */
    private HashSet<Integer> preparedAssets = new HashSet<Integer>();

    protected static final VulkanPhysicalDeviceFeatures[] DEFAULT_REQUESTEDFEATURES =
            new VulkanPhysicalDeviceFeatures[] {
                    VulkanFeatures.wideLines,
                    VulkanFeatures.fillModeNonSolid,
                    VulkanFeatures.geometryShader,
                    VulkanFeatures.sampleRateShading,
                    VulkanFeatures.samplerAnisotropy,
                    VulkanFeatures.imageCubeArray,
                    Vulkan12Features.drawIndirectCount,
                    VulkanFeatures.multiDrawIndirect,
                    Vulkan13Features.maintenance4,
                    Vulkan13Features.synchronization2,
                    Vulkan12Features.scalarBlockLayout
            };

    /**
     * Creates a new application starter with the specified renderer and client main class implementation.
     * 
     * @param args
     * @param version
     * @param title
     */
    public LWJGL3Application(String[] args, Renderers version, String title) {
        super(args, new LWJGL3RenderFactory(), version, title);
    }

    /**
     * Sets the value of the running flag, this controls if main run() loop is exited
     * 
     * @param running
     */
    protected void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Creates the application window, adds keylistener and sets window to visible
     * 
     */
    public void createApp() {
        initLWJGL();
        setWindow(createWindow(version));
        getJ2SEWindow().addKeyListener(this);
        getJ2SEWindow().setVisible(true);
    }

    private void initLWJGL() {
        NativeBuffer.setHandle(new LWJGLNativeBuffer());
    }

    /**
     * Call after init has been done
     */
    protected void setInitialized() {
        this.initialized = true;
    }

    @Override
    protected J2SEWindow createWindow(Renderers version) {
        switch (version) {
            case VULKAN10:
            case VULKAN11:
            case VULKAN12:
            case VULKAN13:
                return createVulkanWindow(version);
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + version);
        }
    }

    private J2SEWindow createVulkanWindow(Renderers version) {
        switch (appSettings.windowType) {
            case GLFW:
            case HEADLESS:
                J2SEWindow window = new GLFWVulkanWindow(title);
                window.createWindow(appSettings);
                return window;
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + appSettings.windowType);
        }
    }

    /**
     * Call this method after instantiation to drive rendering if it is not driven by paint() method from window.
     * Will automatically exit if window type is one that drives rendering via paint()
     */
    public void run() {

        try {
            Logger.d(getClass(), "run()");
            if (!initialized) {
                init();
                setInitialized();
                RenderPassCreateInfo info = getRenderer().getSwapBuffer().getRenderPassCreateInfo();
                SampleCountFlagBit samples = info.attachments[0].samples;
                org.varg.vulkan.extensions.KHRSwapchain<?> swapChain = getRenderer().getBackend().getKHRSwapchain();
                SurfaceFormat surfaceFormat = swapChain.getCreateInfo().surfaceFormat;
                String sampleStr =
                        samples != SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT ? " " + samples.value + " samples"
                                : " no MSAA";
                getJ2SEWindow().setWindowTitle(title + " using " + surfaceFormat + sampleStr + ", Depth buffer: "
                        + info.attachments[1].format);
            }
            switch (appSettings.windowType) {
                case GLFW:
                    running = true;
                    while (running) {
                        drawFrame();
                    }
                    break;
                default:
            }
        } catch (Throwable t) {
            catchException(t);
        }
        Logger.d(getClass(), "Exiting run.");
        System.exit(0);
    }

    /**
     * Catches runtime exception - override to do specific behavior
     * 
     * @param t
     */
    protected void catchException(Throwable t) {
        if (getJ2SEWindow() != null) {
            getJ2SEWindow().destroy();
        }
        t.printStackTrace();
    }

    /**
     * Initialize the application - this shall only be called once.
     */
    protected abstract void init();

    /**
     * Draws the current frame
     */
    protected abstract void drawFrame();

    /**
     * Convenience method to do the default drawframe behavior.
     */
    protected float internalDrawFrame(VulkanRenderableScene s, MeshShader... meshShaders) {
        GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer = getRenderer();
        float time = renderer.beginFrame();
        if (time > 0 && s != null && preparedAssets.contains(s.getId())) {
            GltfStorageBuffers buffers = (GltfStorageBuffers) renderer.getAssets().getStorageBuffers(
                    shaderInfo.shaderType);
            renderer.getBufferFactory().uploadBuffers(renderer.getQueue(), buffers,
                    shaderInfo.shaderType.getTargets());
            renderer.prepareFrameData(s, buffers);
            renderer.beginRenderPass();

            for (MeshShader ms : meshShaders) {
                DescriptorBuffers<?> buf = renderer.getAssets().getStorageBuffers(ms.getShaderInfo().shaderType);
                renderer.bindDescriptorSets(ms.getShaderInfo().shaderType, buf, renderer.getQueue(),
                        ms.getShaderInfo().shaderType.getTargets());
                renderer.drawMeshShader(ms, buf, renderer.getQueue());
            }

            renderer.bindDescriptorSets(GraphicsShaderType.GLTF2, buffers, renderer.getQueue(),
                    shaderInfo.shaderType.getTargets());
            // Render background if enabled
            // TODO - split render of opaque gltf and transparent/transmission on this level so that
            // background can be rendered after opaque gltf (but before transparent)
            renderer.drawMeshShader(backgroundShader, buffers, renderer.getQueue());
            renderer.render(s, renderer.getQueue(), buffers);
            renderer.endRenderPass();
        }
        renderer.endFrame();
        getJ2SEWindow().drawFrame();
        return time;
    }

    @Override
    protected void destroy() {
        Logger.d(getClass(), "destroy()");
        running = false;
    }

    private void initSettings(Features deviceFeatures) {
        if (settings == null) {
            PhysicalDeviceFeatureExtensions featureExtensions = deviceFeatures.getPhysicalDeviceFeatureExtensions();
            Alignment cameraAlignment = Alignment.get(Settings.getInstance().getProperty(LaddaProperties.CAMERA_ALIGNMENT));
            settings = new GltfSettings(cameraAlignment);
            if (!featureExtensions.hasIndexTypeUint8()) {
                settings.setIndexedToShort(modelPrep);
            }
            settings.setCreateTangents(modelPrep);
            settings.setCreateNormals(modelPrep);
        }
    }

    /**
     * Loads and prepares the glTF asset using default model preparation and gltf settings.
     * This will load any glTF model.
     * If device does not support indexed 8 bit mode, those buffers are converted to short.
     * 
     * @param path
     * @param asset
     */
    protected void loadGltfAsset(String path, String asset) {
        Logger.d(getClass(), "Load asset, path: " + path + " asset: " + asset);
        initSettings(getRenderer().getBackend().getLogicalDevice().getFeatures());
        loadGltfAsset(path, asset, modelPrep, settings);
    }

    /**
     * Loads and prepares the glTF asset - helper method to load and prepare a model to be rendered.
     * 
     * @param path
     * @param asset
     * @return
     */
    protected void loadGltfAsset(String path, String asset, ModelPreparation modelPreparation,
            GltfSettings gltfSettings) {
        try {
            long start = System.currentTimeMillis();
            loadedAsset = Ladda.getInstance(VulkanGltf.class).loadGltf(path, asset, modelPreparation, gltfSettings);
            long loaded = System.currentTimeMillis();
            VulkanRenderableScene currentScene = loadedAsset.getScene(sceneIndex);
            prepareAsset(currentScene);
            createSceneControl(currentScene);
            Logger.d(getClass(), "Load and prepare asset (" + asset + ") took " + (System.currentTimeMillis() - start)
                    + " millis, load and mesh processing took " + (loaded - start) + " millis");
        } catch (IOException | BackendException | ClassNotFoundException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates scenecontrol for the loaded asset, sets camera and adds key/pointer listeners to scenecontrol
     */
    protected void createSceneControl(VulkanRenderableScene currentScene) {
        if (sceneControl == null) {
            Extent2D extent = getRenderer().getBackend().getKHRSwapchain().getExtent();
            sceneControl = new GltfSceneControl(loadedAsset.getCameraInstance(), currentScene, extent);
            getRenderer().setCamera(sceneControl.getCamera());
            getJ2SEWindow().addPointerListener(sceneControl);
            getJ2SEWindow().addKeyListener(sceneControl);
        } else {
            sceneControl.setScene(currentScene, loadedAsset.getCameraInstance());
        }
    }

    /**
     * Creates the descriptorpool and descriptorsets
     * 
     */
    protected void createDescriptorPool(int maxSets, int uniformBuffers, int dynamicUniformBuffers,
            int storageBuffers) {
        // Create the descriptor pool
        // TODO - currently only handles one glTF model. Update to be able to handle more than one
        getRenderer().getBackend().createDescriptorPool(getDescriptorPoolCreateInfo(maxSets, uniformBuffers,
                dynamicUniformBuffers, storageBuffers));
    }

    final DescriptorPoolCreateInfo getDescriptorPoolCreateInfo(int maxSets, int uniformBuffers,
            int dynamicUniformBuffers, int storageBuffers) {
        DescriptorPoolCreateInfo poolInfo = new DescriptorPoolCreateInfo(
                new DescriptorPoolCreateFlagBits[] {
                        DescriptorPoolCreateFlagBits.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT },
                maxSets,
                new DescriptorPoolSize[] { new DescriptorPoolSize(
                        DescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, dynamicUniformBuffers),
                        new DescriptorPoolSize(DescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, uniformBuffers),
                        new DescriptorPoolSize(DescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                                MAX_DESCRIPTORSET_SAMPLERS),
                        new DescriptorPoolSize(DescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
                                storageBuffers) });
        return poolInfo;
    }

    /**
     * Loads and prepares texture images, creates vertex buffers uploads vertex buffer data
     * Use this as a helper method to setup model for rendering - if this method is called multiple times
     * it will do nothing.
     * 
     * @param asset
     * @throws IOException
     * @throws BackendException
     */
    protected boolean prepareAsset(VulkanRenderableScene asset) throws IOException, BackendException {
        if (!preparedAssets.contains(asset.getId())) {
            // First create the indirect drawcalls - this may set new instance id to primitive.
            asset.createIndirectDrawCalls(getRenderer().getBufferFactory());
            GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer = getRenderer();
            renderer.getAssets().loadSourceImages(asset);
            TextureImages textureImages = renderer.getAssets().createTextureImages(asset);
            shaderInfo = new Gltf2GraphicsShaderCreateInfo(asset, renderer.getVersion(), GraphicsShaderType.GLTF2,
                    null);
            int vsr = Settings.getInstance().getInt(BackendIntProperties.FRAGMENTSIZE);
            if (vsr > 1) {
                shaderInfo.setFragmentShadingRate(new PipelineFragmentShadingRateStateCreateInfoKHR(
                        vsr, vsr));
            }
            GltfStorageBuffers buffers = new GltfStorageBuffers(textureImages);
            renderer.getAssets().createStorageBuffers(shaderInfo, shaderInfo, buffers);
            buffers.setStaticStorage(asset, renderer);
            DrawCallBundle<IndirectDrawCalls> drawBundle = asset.getDrawCallBundle();
            IndirectDrawCalls[] dcs = drawBundle.getAllDrawCalls();
            renderer.getPipelines().createGraphicsPipelines(dcs, asset, shaderInfo);
            renderer.getAssets().updateVertexBuffers(asset.getVertexMemory().values().toArray(new VertexMemory[0]));
            // Upload drawcalls to device memory
            for (IndirectDrawing dc : dcs) {
                dc.copyToDevice(renderer.getBufferFactory(), renderer.getQueue());
            }
            KHREnvironmentMapReference envMap = asset.getEnvironmentExtension();
            if (envMap != null) {
                BACKGROUND bgHint = envMap.getBackgroundHint();
                if (bgHint == BACKGROUND.CUBEMAP || bgHint == BACKGROUND.SH) {
                    BackgroundMeshShaderCreateInfo backgroundShaderInfo = new BackgroundMeshShaderCreateInfo(
                            asset, version, MeshShaderType.GLTF_BACKGROUND);
                    int bvsr = Settings.getInstance().getInt(BackendIntProperties.BACKGROUND_FRAGMENTSIZE);
                    if (bvsr > 1) {
                        backgroundShaderInfo.setFragmentShadingRate(new PipelineFragmentShadingRateStateCreateInfoKHR(
                                bvsr, bvsr));
                    }
                    renderer.getAssets().createStorageBuffers(backgroundShaderInfo, backgroundShaderInfo, buffers);
                    backgroundShader = renderer.getPipelines().createMeshPipeline(backgroundShaderInfo);
                }
            }
            // This will delete the Java side of source images.
            renderer.getAssets().deleteSourceImages(asset);
            preparedAssets.add(asset.getId());
            return true;
        }
        return false;
    }

    private void checkPQColorspace(JSONGltf glTF, ColorSpaceKHR colorSpace) {
        // If colorspace is PQ then turn on displayencoding extension
        if (colorSpace.isPQColorSpace()) {
            KHRdisplayencoding dm = (KHRdisplayencoding) glTF.getExtension(ExtensionTypes.KHR_displayencoding);
            if (dm == null) {
                Logger.d(getClass(),
                        "Swapbuffer uses PQ colorspace (" + colorSpace + ") adding KHR_displayencoding to asset");
                dm = new KHRdisplayencoding();
                glTF.addExtension(dm);
            }
            dm.setColorPrimaries(BT2100.getColorPrimaries());
        }
    }

    /**
     * Deletes the buffers and textures for the assets
     * 
     * @param scenes
     */
    protected void deleteAssets(VulkanRenderableScene... scenes) {
        GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer = getRenderer();
        for (VulkanRenderableScene s : scenes) {
            renderer.getAssets().deleteTextureImages(s);
            renderer.getAssets().deleteStorageBuffers(shaderInfo);
            s.freeVertexMemory(renderer.getBufferFactory());
            s.getRoot().destroy();
            s.destroy();
        }
        renderer.getBufferFactory().logMemory();
    }

    @Override
    public boolean keyEvent(Key key) {
        Logger.d(getClass(), key.getAction().name() + ", " + key.getKeyValue());
        switch (key.getAction()) {
            case PRESSED:
                keyPressed(key.getKeyValue());
                break;
            case RELEASED:
                keyReleased(key.getKeyValue());
                break;
            default:
                // Do nothing
        }
        // Never consume key
        return false;
    }

    /**
     * If method overridden - call super to be able to use {@link #isKeyPressed()} or {@link #isKeyPressed(int)}
     * 
     * @param key
     */
    protected void keyPressed(int key) {
        keyMap.put(key, true);

    }

    /**
     * If method overridden - call super to be able to use {@link #isKeyPressed()} or {@link #isKeyPressed(int)}
     * 
     * @param key
     */
    protected void keyReleased(int key) {
        keyMap.remove(key);
    }

    /**
     * Checks if a key is pressed or not - using java.awt.KeyEvent.keyCode
     * 
     * @param key java.awt.KeyEvent keyCode for key to check
     * @return True if key is currently pressed
     */
    public boolean isKeyPressed(int key) {
        return (keyMap.get(key) != null);
    }

    /**
     * Returns true if one or more keys are pressed
     * 
     * @return
     */
    public boolean isKeyPressed() {
        return keyMap.size() > 0;
    }

    protected final RequestedFeatures getDefaultRequestedFeatures(Features availableFeatures) {
        RequestedFeatures requestedFeatures = new RequestedFeatures(availableFeatures);
        if (!Settings.getInstance().getBoolean(BackendProperties.HEADLESS)) {
            requestedFeatures.addKHRSwapChain();
        }
        if (ExtensionProperties.get(Extension.VK_EXT_hdr_metadata.getName(), availableFeatures.getExtensions())
                != null) {
            requestedFeatures.addEXTHdrMetadata();
        }
        if (ExtensionProperties.get(Vulkan12Extension.VK_KHR_shader_float_controls.getName(),
                availableFeatures.getExtensions()) != null) {
            requestedFeatures.addExtension(Vulkan12Extension.VK_KHR_shader_float_controls);
        }
        requestedFeatures.add8BitStorage(true, true, false);
        requestedFeatures.add16BitStorage(true, true, false, false);
        requestedFeatures.addShaderFloat16Int8(true, true, true);
        requestedFeatures.setFeatures(DEFAULT_REQUESTEDFEATURES);
        requestedFeatures.add16BitStorage(initialized, initialized, running, initialized);
        if (availableFeatures.getPhysicalDeviceFeatureExtensions()
                .getPhysicalDeviceRobustness2Features().nullDescriptor) {
            requestedFeatures.enableNullDescriptor();
        }
        return requestedFeatures;
    }

}

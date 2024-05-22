
package org.varg.lwjgl3.apps;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

import org.gltfio.VanillaCreatorCallback;
import org.gltfio.VanillaGltfCreator;
import org.gltfio.deserialize.LaddaProperties;
import org.gltfio.glb2.Glb2Reader.Glb2Streamer;
import org.gltfio.gltf2.AssetBaseObject.FileType;
import org.gltfio.gltf2.MinMax;
import org.gltfio.gltf2.StreamingGltf;
import org.gltfio.gltf2.stream.SubStream.Type;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.gltfio.lib.Settings.BooleanProperty;
import org.gltfio.lib.WindowListener;
import org.gltfio.prepare.GltfSettings.Alignment;
import org.gltfio.serialize.Writer;
import org.varg.BackendException;
import org.varg.gltf.VulkanStreamingScene;
import org.varg.pipeline.Pipelines.SetType;
import org.varg.renderer.Renderers;
import org.varg.shader.Gltf2GraphicsShader.GltfDescriptorSetTarget;
import org.varg.shader.Gltf2GraphicsShader.GraphicsShaderType;
import org.varg.uniform.DescriptorBuffers;
import org.varg.vulkan.Features;
import org.varg.vulkan.Vulkan10.Extension;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.Vulkan10Backend.CreateDevice;
import org.varg.vulkan.VulkanBackend.BackendIntProperties;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;
import org.varg.vulkan.VulkanBackend.IntArrayProperties;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.extensions.PhysicalDeviceAccelerationStructureFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceFragmentShadingRateFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderFeaturesEXT;
import org.varg.vulkan.structs.ExtensionProperties;
import org.varg.vulkan.structs.RequestedFeatures;

public class VARGViewer extends LWJGL3Application implements Glb2Streamer<VulkanStreamingScene>, WindowListener, CreateDevice {

    public enum VargViewerBooleanProperties implements BooleanProperty {
        LIST_GLTF_FILES("varg.viewer.listfiles", false);

        private final String key;
        private final boolean defaultValue;

        VargViewerBooleanProperties(String key, boolean defaultValue) {
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

    // ***********************************
    // Model loading fields
    // ***********************************
    /**
     * List of found .gltf filenames - including folder name
     */
    protected ArrayList<String> gltfFilenames;
    private ArrayList<String> folders;
    protected int modelIndex = 0;

    public VARGViewer(String[] args, Renderers version, String title) {
        super(args, version, title);
    }

    public static void main(String[] args) {
        Settings settings = Settings.getInstance();
        settings.setProperty(BackendProperties.RECOMPILE_SPIRV, false);
        settings.setProperty(VARGViewer.VargViewerBooleanProperties.LIST_GLTF_FILES, true);
        settings.setProperty(BackendProperties.VALIDATE, false);
        settings.setProperty(BackendProperties.DEBUG, false);
        settings.setProperty(BackendIntProperties.SURFACE_WIDTH, 1920);
        settings.setProperty(BackendIntProperties.SURFACE_HEIGHT, 1080);
        settings.setProperty(BackendIntProperties.BACKGROUND_FRAGMENTSIZE, 1);
        settings.setProperty(BackendIntProperties.FRAGMENTSIZE, 1);
        settings.setProperty(BackendIntProperties.SAMPLE_COUNT, 8);
        settings.setProperty(BackendStringProperties.COLORSPACE, "VK_COLOR_SPACE_SRGB_NONLINEAR_KHR");
        settings.setProperty(BackendIntProperties.MAX_WHITE, 1500);
        settings.setProperty(IntArrayProperties.CLEAR_COLOR, null);
        settings.setProperty(LaddaProperties.IRRADIANCEMAP, "intensity:200|irmap:STUDIO_5");
        settings.setProperty(LaddaProperties.ENVMAP_BACKGROUND, "SH");
        settings.setProperty(LaddaProperties.DIRECTIONAL_LIGHT, "intensity:5000|color:1,1,1|position:0,10000,1000");

        VARGViewer varg = new VARGViewer(args, Renderers.VULKAN13, "VARG Model Viewer");
        DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        float aspect = ((float) mode.getWidth()) / mode.getHeight();
        Settings.getInstance().setProperty(Settings.PlatformFloatProperties.DISPLAY_ASPECT, aspect);
        varg.createApp();
        varg.run();
    }

    /**
     * Returns the default modelname
     * 
     * @return
     */
    protected String getDefaultModelName() {
        return "FlightHelmet.gltf";
    }

    /**
     * Returns the (relative) path to where resources are located
     * 
     * @return
     */
    protected String getResourcePath() {
        return "C:/source/glTF-Sample-Assets/Models/";
    }

    public static final float schlickFrenesnel(float u, float n) {
        float m = 1 - u;
        float m2 = m * m;
        return n + (1 - n) * m2 * m2 * m; // pow(m,5)
    }

    @Override
    protected void init() {
        try {
            createRenderer(this);
            int uniformTargets = GltfDescriptorSetTarget.getTargets(SetType.UNIFORM_TYPE).length;
            createDescriptorPool(uniformTargets + MAX_DESCRIPTORSET_SAMPLERS, uniformTargets, 1, 0);
            loadDefaultModel();
            startConsoleInput();
            getJ2SEWindow().addKeyListener(this);
            getJ2SEWindow().addWindowListener(this);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void loadGltfAsset(int loadModelIndex) {
        if (gltfFilenames != null && gltfFilenames.size() > loadModelIndex) {
            String modelName = gltfFilenames.get(loadModelIndex);
            loadGltfAsset(getResourcePath(), modelName);
            setModelName(modelName);
        } else {
            Logger.e(getClass(), "Could not find model " + getDefaultModelName());
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "No model named " + getDefaultModelName());
        }
    }

    /**
     * Call this when scene has been updated.
     */
    protected void sceneUpdated() {
        try {
            VulkanStreamingScene scene = (VulkanStreamingScene) loadedAsset.getScene(0);
            getRenderer().getQueue().queueBegin();
            if (!prepareAsset(scene)) {
                getRenderer().getAssets().updateVertexBuffers(scene);
                DescriptorBuffers<?> buffers = getRenderer().getAssets().getStorageBuffers(GraphicsShaderType.GLTF2);
                buffers.setStaticStorage(scene, getRenderer());
            }
            scene.addIndirectDrawCalls();
            // scene.getIndirectDrawCall().copyToDevice(getRenderer().getBufferFactory(), getRenderer().getQueue());

        } catch (IOException | BackendException e) {
            throw new IllegalArgumentException(e);
        }

    }

    @Override
    public void glb2Update(StreamingGltf<VulkanStreamingScene> glTF, Type type) {
        switch (type) {
            case SCENE:
                loadedAsset = glTF;
                VulkanStreamingScene scene = glTF.getScene();
                MinMax bounds = scene.calculateBounds();
                if (bounds != null) {
                    float aspect = Settings.getInstance().getFloat(Settings.PlatformFloatProperties.DISPLAY_ASPECT);
                    glTF.addRuntimeCamera("Default camera", bounds, Alignment.CENTER, aspect, scene);
                }
                createSceneControl(scene);
                break;
            case NODE:
                // scene = glTF.getScene();
                // scene.setUpdated();
                // Logger.d(getClass(), "scene.setUpdated()");
                break;
            default:
                // Do nothing
        }
    }

    @Override
    public void glb2Loaded(StreamingGltf<VulkanStreamingScene> glTF) {
        Logger.d(getClass(), "Loaded");
        VulkanStreamingScene scene = glTF.getScene();
        scene.setUpdated();
    }

    /**
     * Loads the default model
     * 
     * @throws URISyntaxException
     * @throws IOException
     */
    protected void loadDefaultModel() throws URISyntaxException, IOException {
        String modelName = getDefaultModelName();
        String foldername = getResourcePath();
        if (modelName.startsWith("CREATOR:")) {
            modelName = modelName.substring("CREATOR:".length());
            VanillaCreatorCallback callback = new VanillaCreatorCallback();
            VanillaGltfCreator creator = new VanillaGltfCreator(callback.getCopyRight(), callback
                    .getInitialBuffer(), callback);
            Writer.writeGltf(creator.createAsset(), foldername, modelName);
            loadGltfAsset(foldername, modelName);
        } else {
            if (Settings.getInstance().getBoolean(VargViewerBooleanProperties.LIST_GLTF_FILES)) {
                fetchGltfFilenames();
                modelIndex = findModelIndex(modelName);
                loadGltfAsset(modelIndex);
            } else {
                loadGltfAsset(foldername, modelName);
            }
        }
    }

    private void setModelName(String name) {
        SurfaceFormat surfaceFormat = getRenderer().getBackend().getKHRSwapchain().getCreateInfo().surfaceFormat;
        getJ2SEWindow().setWindowTitle(title + " : " + name + " using " + surfaceFormat);
    }

    private int findModelIndex(String model) {
        for (int i = 0; i < gltfFilenames.size(); i++) {
            if (gltfFilenames.get(i).endsWith(model)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * List gltf filenames
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    protected void fetchGltfFilenames() throws IOException, URISyntaxException {
        long start = System.currentTimeMillis();
        if (folders == null) {
            folders = FileUtils.getInstance().listResourceFolders(getResourcePath());
        }
        if (folders.size() == 0) {
            folders.add("");
        }
        FileUtils f = FileUtils.getInstance();
        gltfFilenames = FileUtils.getInstance().listFiles(getResourcePath(), folders, new String[] { "." + FileType.GLTF.extension, "." + FileType.GLB.extension }, 3);
        // Remove all folders with Draco in the name and trim duplicates for gltf/glb
        ArrayList<String> glbList = new ArrayList<String>();
        ArrayList<String> gltfList = new ArrayList<String>();
        HashSet<String> modelnames = new HashSet<String>();
        for (String name : gltfFilenames) {
            if (!name.contains("Draco")) {
                if (name.endsWith(FileType.GLB.extension)) {
                    String modelname = name.substring(f.getFolder(name).length() + 1, name.length() - 4);
                    if (modelnames.contains(modelname)) {
                        remove(gltfList, modelname);
                    } else {
                        modelnames.add(modelname);
                    }
                    glbList.add(name);
                } else if (name.endsWith(FileType.GLTF.extension)) {
                    String modelname = name.substring(f.getFolder(name).length() + 1, name.length() - 5);
                    if (modelnames.contains(modelname)) {
                        // Do nothing - contains.glb
                    } else {
                        modelnames.add(modelname);
                        gltfList.add(name);
                    }
                }
            }
        }
        gltfFilenames.clear();
        gltfFilenames.addAll(glbList);
        gltfFilenames.addAll(gltfList);
        Logger.d(getClass(), "Found " + gltfFilenames.size() + " glTF/glb files - took " + (System.currentTimeMillis() - start) + ", millis");
    }

    private void remove(ArrayList<String> names, String modelname) {
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).contains(modelname)) {
                names.remove(i);
                break;
            }
        }
    }

    @Override
    protected void drawFrame() {
        if (sceneControl != null) {
            sceneControl.autoRotate();
            VulkanRenderableScene scene = sceneControl.getCurrentScene();
            if (scene.isUpdated()) {
                sceneUpdated();
            }
            internalDrawFrame(scene);
        }
    }

    @Override
    protected void keyPressed(int key) {
        super.keyPressed(key);
        switch (key) {
            default:
                // Do nothing
        }
    }

    @Override
    protected void keyReleased(int key) {
        super.keyReleased(key);
        switch (key) {
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_UP:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
                handleDirectionKeys(key);
                break;
            case KeyEvent.VK_C:
                sceneControl.setCamera(loadedAsset.getCamera(loadedAsset.selectNextCamera()));
                getRenderer().setCamera(sceneControl.getCamera());
            default:
                // Do nothing
        }
    }

    private void handleDirectionKeys(int key) {
        switch (key) {
            case KeyEvent.VK_LEFT:
                handleKeyLeft();
                break;
            case KeyEvent.VK_RIGHT:
                handleKeyRight();
                break;
            case KeyEvent.VK_UP:
                handleKeyUp();
                break;
            case KeyEvent.VK_DOWN:
                handleKeyDown();
                break;
            default:
                // Do nothing
        }
    }

    private void handleKeyLeft() {
        Logger.d(getClass(), "LEFT");
        deleteAssets(loadedAsset.getScene(sceneIndex));
        modelIndex--;
        if (modelIndex < 0) {
            modelIndex = gltfFilenames.size() - 1;
        }
        loadGltfAsset(modelIndex);
    }

    private void handleKeyRight() {
        Logger.d(getClass(), "RIGHT");
        deleteAssets(loadedAsset.getScene(sceneIndex));
        modelIndex++;
        if (modelIndex >= gltfFilenames.size()) {
            modelIndex = 0;
        }
        loadGltfAsset(modelIndex);
    }

    private void handleKeyUp() {
    }

    private void handleKeyDown() {
    }

    @Override
    public boolean windowEvent(WindowEvent event) {
        Logger.d(getClass(), "WindowEvent " + event.action);
        return false;
    }

    @Override
    public Features getRequestedDeviceFeatures(Features availableFeatures) {
        RequestedFeatures requestedFeatures = getDefaultRequestedFeatures(availableFeatures);
        if (ExtensionProperties.get(Extension.VK_EXT_mesh_shader.getName(), availableFeatures.getExtensions()) != null) {
            requestedFeatures.addEXTMeshShader(new PhysicalDeviceMeshShaderFeaturesEXT(true, true, false, false, false));
        }
        if (ExtensionProperties.get(Extension.VK_KHR_fragment_shading_rate.getName(), availableFeatures.getExtensions()) != null) {
            requestedFeatures.addKHRFragmentShadingRate(new PhysicalDeviceFragmentShadingRateFeaturesKHR(true, false, false));
        }
        PhysicalDeviceAccelerationStructureFeaturesKHR accelerationFeatures = new PhysicalDeviceAccelerationStructureFeaturesKHR();
        requestedFeatures.addRayTracing(accelerationFeatures);
        return requestedFeatures.getFeatures();
    }

}

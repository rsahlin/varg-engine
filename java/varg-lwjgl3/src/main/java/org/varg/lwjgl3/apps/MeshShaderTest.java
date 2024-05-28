package org.varg.lwjgl3.apps;

import java.io.IOException;
import java.net.URISyntaxException;

import org.gltfio.deserialize.Ladda.LaddaProperties;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.gltfio.lib.WindowListener;
import org.varg.BackendException;
import org.varg.gltf.VulkanMesh;
import org.varg.renderer.GltfRenderer;
import org.varg.renderer.Renderers;
import org.varg.shader.MeshShader.MeshDescriptorSetTarget;
import org.varg.shader.Shader;
import org.varg.shader.voxels.PixelsToVoxels;
import org.varg.shader.voxels.PixelsToVoxels.VoxelData;
import org.varg.shader.voxels.VoxelMeshShader;
import org.varg.shader.voxels.VoxelMeshShader.VoxelMeshShaderCreateInfo;
import org.varg.uniform.DescriptorBuffers;
import org.varg.uniform.StorageBuffers;
import org.varg.vulkan.Features;
import org.varg.vulkan.Vulkan10.Format;
import org.varg.vulkan.Vulkan10Backend.CreateDevice;
import org.varg.vulkan.VulkanBackend.BackendIntProperties;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;
import org.varg.vulkan.VulkanBackend.IntArrayProperties;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.extensions.EXTMeshShader;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderFeaturesEXT;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderPropertiesEXT;
import org.varg.vulkan.structs.RequestedFeatures;

public class MeshShaderTest extends VARGViewer implements WindowListener, CreateDevice {

    private VoxelMeshShader meshShader;
    private BouncySprites sprite;

    public MeshShaderTest(String[] args, Renderers version, String title) {
        super(args, version, title);
    }

    public static void main(String[] args) {
        Settings settings = Settings.getInstance();
        settings.setProperty(BackendIntProperties.SURFACE_WIDTH, 1080);
        settings.setProperty(BackendIntProperties.SURFACE_HEIGHT, 1080);
        settings.setProperty(BackendProperties.FULLSCREEN, false);
        settings.setProperty(BackendIntProperties.BACKGROUND_FRAGMENTSIZE, 1);
        settings.setProperty(BackendIntProperties.FRAGMENTSIZE, 1);
        settings.setProperty(BackendIntProperties.SAMPLE_COUNT, 8);
        settings.setProperty(BackendStringProperties.COLORSPACE, "VK_COLOR_SPACE_SRGB_NONLINEAR_KHR");
        settings.setProperty(BackendIntProperties.MAX_WHITE, 1000);
        settings.setProperty(IntArrayProperties.CLEAR_COLOR, null);

        settings.setProperty(LaddaProperties.IRRADIANCEMAP, "intensity:500|irmap:STUDIO_5");
        settings.setProperty(LaddaProperties.ENVMAP_BACKGROUND, "SH");
        settings.setProperty(LaddaProperties.DIRECTIONAL_LIGHT, "intensity:1000|color:1,1,1|position:0,10000,0000");
        settings.setProperty(LaddaProperties.CAMERA_ALIGNMENT, "BOTTOM");

        MeshShaderTest meshShader = new MeshShaderTest(args, Renderers.VULKAN13, "Mesh Shader test");
        meshShader.createApp();
        meshShader.run();
    }

    @Override
    protected String getDefaultModelName() {
        return "room1.glb";
    }

    @Override
    protected String getResourcePath() {
        return "assets/gltf/room1/";
    }

    @Override
    protected void init() {
        try {
            super.init();
            createMeshShader();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void createMeshShader() throws URISyntaxException, IOException, BackendException {
        GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer = getRenderer();
        EXTMeshShader<?> meshShaderExtension = renderer.getBackend().getEXTMeshShader();
        PhysicalDeviceMeshShaderPropertiesEXT meshShaderProperties = meshShaderExtension.getMeshShaderProperties();

        VoxelData voxels = createVoxelData("assets/voxels/bob-large.png");
        VoxelMeshShaderCreateInfo meshInfo =
                new VoxelMeshShaderCreateInfo(voxels.getVoxelCount(), renderer.getVersion(),
                        meshShaderProperties);

        StorageBuffers storage = new StorageBuffers();
        renderer.getAssets().createStorageBuffers(meshInfo, meshInfo, storage);
        meshShader = (VoxelMeshShader) renderer.getPipelines().createMeshPipeline(meshInfo);
        sprite = prepareSpriteData(meshShader, voxels);
        storage.storeByteData(MeshDescriptorSetTarget.DATA, meshShader.getShaderInfo().getCubeCount()
                * meshInfo.offsetFormat.getComponentByteSize() * 4, voxels.paletteIndexes);
        int offset = storage.storeShortData(MeshDescriptorSetTarget.GLOBAL, 0, sprite.getPalette());
        if (meshShader.hasStage(Shader.Stage.TASK)) {
            // Number of invocations in the EmitMeshTasksEXT operation
            int[] emitTasks = meshShader.getShaderInfo().getWorkGroupCounts(Shader.Stage.MESH);
            storage.storeShortData(MeshDescriptorSetTarget.GLOBAL, offset, new short[] { (short) emitTasks[0],
                    (short) emitTasks[1], (short) emitTasks[2] });
            long start = System.currentTimeMillis();
            if (meshInfo.offsetFormat == Format.VK_FORMAT_R16_SFLOAT) {
                storage.storeShortData(MeshDescriptorSetTarget.DATA, 0, sprite.getShortOffsets());
            } else {
                storage.storeFloatData(MeshDescriptorSetTarget.DATA, 0, sprite.getOffsets());
            }
            Logger.d(getClass(), "Convert and store positions took " + (System.currentTimeMillis() - start)
                    + " milliseconds");
        } else {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Missing task shader");
        }
        renderer.getBufferFactory().uploadBuffers(renderer.getQueue(), storage,
                meshShader.getShaderInfo().shaderType.getTargets());
    }

    private BouncySprites prepareSpriteData(VoxelMeshShader cubeShader, VoxelData voxels) {
        int maxSprites = cubeShader.getShaderInfo().getMaxSpriteCount();
        return new BouncySprites(maxSprites, getPalette(VoxelMeshShader.TIA_COLOR_PALETTE), voxels,
                sceneControl.getCurrentScene().calculateBounds());
    }

    private VoxelData createVoxelData(String imageName) throws URISyntaxException, IOException {
        // Load bitmap and convert to voxels
        PixelsToVoxels converter = new PixelsToVoxels();
        VoxelData result = converter.convert(imageName,
                // VoxelData result = converter.convert("assets/voxels/bob-small-transparency.png",
                new float[] { 0.015f, 0.015f, 0.015f }, VoxelMeshShader.TIA_COLOR_PALETTE);
        return result;
    }

    private float[] getPalette(int[] intPalette) {
        float[] result = new float[intPalette.length * 4];
        int offset = 0;
        for (int i = 0; i < intPalette.length; i++) {
            int color = intPalette[i];
            result[offset++] = (float) ((color >>> 16) & 0x0ff) / 255;
            result[offset++] = (float) ((color >>> 8) & 0x0ff) / 255;
            result[offset++] = (float) ((color) & 0x0ff) / 255;
            result[offset++] = 1f;
        }
        return result;
    }

    private float[] createCubeData(int[] cubeCount, float[] spacing) {
        float[] data = new float[cubeCount[0] * cubeCount[1] * cubeCount[2] * 4];

        int index = 0;
        float zpos = (cubeCount[2] >>> 1) * spacing[2];
        for (int z = 0; z < cubeCount[2]; z++) {
            float ypos = (cubeCount[1] >>> 1) * spacing[1];
            for (int y = 0; y < cubeCount[1]; y++) {
                float xpos = -(cubeCount[0] >>> 1) * spacing[0];
                for (int x = 0; x < cubeCount[0]; x++) {
                    data[index++] = xpos;
                    data[index++] = ypos;
                    data[index++] = zpos;
                    data[index++] = 1;
                    xpos += spacing[0];
                }
                ypos -= spacing[1];
            }
            zpos -= spacing[2];
        }
        return data;
    }

    @Override
    protected void drawFrame() {
        DescriptorBuffers<?> buf =
                getRenderer().prepareFrameData(meshShader, sceneControl.getCurrentScene().getSceneTransform());
        float time = internalDrawFrame(sceneControl.getCurrentScene(), meshShader);
        sprite.processAndStoreData(time, sceneControl.getCurrentScene().getSceneTransform(), buf);
    }

    @Override
    public boolean windowEvent(WindowEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Features getRequestedDeviceFeatures(Features availableFeatures) {
        RequestedFeatures requestedFeatures = getDefaultRequestedFeatures(availableFeatures);
        requestedFeatures.addEXTMeshShader(new PhysicalDeviceMeshShaderFeaturesEXT(true, true, false, false,
                false));
        return requestedFeatures.getFeatures();
    }

}

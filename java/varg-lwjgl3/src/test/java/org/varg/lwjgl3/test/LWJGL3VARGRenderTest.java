package org.varg.lwjgl3.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.gltfio.deserialize.Ladda.LaddaFloatProperties;
import org.gltfio.deserialize.Ladda.LaddaProperties;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Settings;
import org.varg.lwjgl3.apps.FrameGrabber;
import org.varg.renderer.BRDF.BRDFFloatProperties;
import org.varg.renderer.Renderers;
import org.varg.vulkan.VulkanBackend.BackendIntProperties;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;
import org.varg.vulkan.VulkanBackend.IntArrayProperties;

/**
 * Render out testmodels to png or jpeg
 */
public class LWJGL3VARGRenderTest extends FrameGrabber {

    private String absoluteOutputPath;
    private static String OUTPUT_DIRECTORY = "rendertest/output/";
    private static String[] TEST_MODELS =
            new String[] { "assets/gltf/Duck.glb", "assets/gltf/OrientationTest.glb", "assets/gltf/Fox.glb", "assets/gltf/ClearCoatTestIOR.glb", "assets/gltf/BoxTexturedNonPowerOfTwo.glb" };

    public LWJGL3VARGRenderTest(String[] args, Renderers version, String title) {
        super(args, version, title);
    }

    public static void main(String[] args) {
        Settings settings = Settings.getInstance();

        settings.setProperty(IntArrayProperties.CLEAR_COLOR, new int[] { 70, 70, 80, 255 });
        settings.setProperty(BackendStringProperties.COLORSPACE, "VK_COLOR_SPACE_SRGB_NONLINEAR_KHR");
        settings.setProperty(BackendStringProperties.SURFACE_FORMAT, "8888_UNORM");
        settings.setProperty(BackendStringProperties.SWAPCHAIN_USAGE, "VK_IMAGE_USAGE_TRANSFER_SRC_BIT");
        settings.setProperty(BackendIntProperties.SAMPLE_COUNT, 1);
        settings.setProperty(BackendIntProperties.MAX_WHITE, 1000);
        settings.setProperty(LaddaProperties.DIRECTIONAL_LIGHT, "intensity:3000|color:1,1,1|position:-10000,10000,10000");
        settings.setProperty(BackendIntProperties.SURFACE_WIDTH, 1920);
        settings.setProperty(BackendIntProperties.SURFACE_HEIGHT, 1080);
        settings.setProperty(LaddaFloatProperties.MATERIAL_ABSORPTION, 0.1f);
        settings.setProperty(BRDFFloatProperties.NDF_FACTOR, 3.0f);
        settings.setProperty(BRDFFloatProperties.SOLIDANGLE_FUDGE, 0.001f);
        settings.setProperty(LaddaFloatProperties.CAMERA_YFOV, 0.7f);

        LWJGL3VARGRenderTest renderTest = new LWJGL3VARGRenderTest(args, Renderers.VULKAN13, "VARG Render Test");
        ArrayList<String> names = new ArrayList<String>();
        try {
            renderTest.addNames(names, TEST_MODELS);
            renderTest.setupFiles("", OUTPUT_DIRECTORY, names);
            renderTest.createApp();
            renderTest.run();
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getOutputFilename(String name, CameraSetup camera) {
        String outputName = FileUtils.getInstance().getFilename(name);
        return absoluteOutputPath + outputFolder + outputName + "." + outputFormat.type;
    }

    @Override
    protected CameraSetup[] createCameras() {
        return new CameraSetup[] { new CameraSetup(VIEWPOINT.FRONT_FACING) };
    }

    private void addNames(ArrayList<String> list, String... names) throws URISyntaxException, IOException {
        absoluteOutputPath = FileUtils.getInstance().getResourcePath(OUTPUT_DIRECTORY, "target/test-classes", "src/test/resources", "varglwjgl3");
        for (String name : names) {
            list.add(name);
        }
    }

}

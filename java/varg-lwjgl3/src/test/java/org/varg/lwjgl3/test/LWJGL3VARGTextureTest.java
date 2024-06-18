
package org.varg.lwjgl3.test;

import org.gltfio.lib.FileUtils.FilesystemProperties;
import org.gltfio.lib.Settings;
import org.varg.renderer.Renderers;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;

public class LWJGL3VARGTextureTest extends LWJGLBaseTest {

    public LWJGL3VARGTextureTest(String[] args, Renderers version, String title) {
        super(args, version, title);
    }

    public static void main(String[] args) {
        System.setProperty(FilesystemProperties.JAVA_TARGET_DIRECTORY.getKey(), "target/classes");
        System.setProperty(FilesystemProperties.SOURCE_DIRECTORY.getKey(), "src/main");
        Settings.getInstance().setProperty(BackendProperties.KEEP_SOURCE_IMAGES, true);
        Settings.getInstance().setProperty(BackendStringProperties.COLORSPACE, "VK_COLOR_SPACE_SRGB_NONLINEAR_KHR");
        Settings.getInstance().setProperty(BackendStringProperties.SURFACE_FORMAT, "8888_UNORM");
        LWJGL3VARGTextureTest varg = new LWJGL3VARGTextureTest(args, Renderers.VULKAN13, "VARG Texture Test");
        varg.createApp();
        varg.run();
    }

    @Override
    protected String getDefaultModelName() {
        return "gltf/Duck.glb";
    }

    @Override
    protected void drawFrame() {
        internalDrawFrame(loadedAsset.getScene(0));
        validateTextureBuffers(loadedAsset.getScene(0));
        setRunning(false);
        destroy();
    }

}

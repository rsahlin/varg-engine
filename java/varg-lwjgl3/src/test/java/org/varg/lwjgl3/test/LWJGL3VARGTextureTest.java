
package org.varg.lwjgl3.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.gltfio.lib.FileUtils.FilesystemProperties;
import org.ktximageio.awt.AWTImageFactory;
import org.gltfio.lib.Settings;
import org.varg.renderer.Renderers;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;

public class LWJGL3VARGTextureTest extends LWJGLBaseTest {

    public LWJGL3VARGTextureTest(String[] args, Renderers version, String title) {
        super(args, version, title);
    }

    public static void main(String[] args) {
        System.setProperty(FilesystemProperties.JAVA_TARGET_DIRECTORY.getKey(), "target/test-classes");
        System.setProperty(FilesystemProperties.SOURCE_DIRECTORY.getKey(), "src/test");
        Settings.getInstance().setProperty(BackendProperties.KEEP_SOURCE_IMAGES, true);
        Settings.getInstance().setProperty(BackendStringProperties.COLORSPACE, "VK_COLOR_SPACE_SRGB_NONLINEAR_KHR");
        Settings.getInstance().setProperty(BackendStringProperties.SURFACE_FORMAT, "8888_UNORM");
        LWJGL3VARGTextureTest varg = new LWJGL3VARGTextureTest(args, Renderers.VULKAN13, "Rita Texture Test");
        varg.createApp();
        varg.run();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void init() {
        try {
            createDescriptorPool(10, 0, 0, 10);
            loadGltfAsset("assets/", "gltf/WaterBottle/WaterBottle.gltf");
            getRenderer().setCamera(loadedAsset.getCamera(0));
            startConsoleInput();
            setInitialized();
            getRenderer().getSwapBuffer().setClearValue(1, 0, 0, 1);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

    }

    @Override
    protected void drawFrame() {
        internalDrawFrame(loadedAsset.getScene(0));
        validateTextureBuffers(loadedAsset.getScene(0));
        destroy();
    }

    private void createPng() throws IOException {

        // retrieve image
        BufferedImage bi = AWTImageFactory.createBufferedImage(512, 512, 1, 7, 0f, 0f);
        // AwtImageFactory.logImage(bi);
        File outputfile = new File("textureLinearGradient.png");
        ImageIO.write(bi, "png", outputfile);
        // AwtImageFactory factory = new AwtImageFactory();
        // ImageBuffer buffer = factory.createImage("C:/source//textureMR.png",
        // ColorSpace.SRGB, ImageFormat.RGB);
        // Logger.d(getClass(), Buffers.toString(buffer.getBuffer(), 0, 300, 30));
    }

}

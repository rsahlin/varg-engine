
package org.varg.lwjgl3.test;

import org.gltfio.lib.FileUtils.FilesystemProperties;
import org.gltfio.lib.Settings;
import org.varg.renderer.Renderers;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;

/**
 * Assert that vertex and uniform buffers are correct
 * This is done by performing one renderloop, then fetch all Vulkan vertexbuffers and comparing the data in them
 * to the contents in the glTF BufferView (ie the loaded attribute data)
 * Uniform data is validated by asserting that not all values are zero - this could be improved in the future.
 *
 */
public class LWJGL3VARGBufferTest extends LWJGLBaseTest {

    public LWJGL3VARGBufferTest(String[] args, Renderers version, String title) {
        super(args, version, title);
    }

    public static void main(String[] args) {
        System.setProperty(FilesystemProperties.JAVA_TARGET_DIRECTORY.getKey(), "target/test-classes");
        System.setProperty(FilesystemProperties.SOURCE_DIRECTORY.getKey(), "src/test");
        Settings.getInstance().setProperty(BackendStringProperties.UNIFORM_USAGE, "VK_BUFFER_USAGE_TRANSFER_SRC_BIT");
        Settings.getInstance().setProperty(BackendStringProperties.VERTEX_USAGE, "VK_BUFFER_USAGE_TRANSFER_SRC_BIT");

        LWJGL3VARGBufferTest varg = new LWJGL3VARGBufferTest(args, Renderers.VULKAN13, "Varg Buffer Test");
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
        validateVertexBuffers(loadedAsset.getScene(0));
        validateUniformBuffers();
        setRunning(false);
        destroy();
    }

}

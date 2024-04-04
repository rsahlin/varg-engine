
package org.varg.lwjgl3.vulkan;

import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.varg.BackendException;
import org.varg.renderer.GltfRenderer;
import org.varg.renderer.RenderFactory;
import org.varg.renderer.Renderers;
import org.varg.renderer.VulkanGltfRenderer;
import org.varg.vulkan.Vulkan10Backend.CreateDevice;
import org.varg.window.J2SEWindow.WindowHandle;

public class LWJGL3RenderFactory extends RenderFactory {

    @Override
    public GltfRenderer createRenderer(Renderers version, WindowHandle window, CreateDevice callback) {
        if (version == null) {
            throw new IllegalArgumentException("Version is null");
        }
        try {
            long start = System.currentTimeMillis();
            GltfRenderer renderer = null;
            LWJGL3Vulkan12Backend backend = null;
            switch (version) {
                case VULKAN13:
                    backend = new LWJGL3Vulkan13Backend(version, window);
                    break;
                case VULKAN12:
                    backend = new LWJGL3Vulkan12Backend(version, window);
                    break;
                default:
                    throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + version);
            }
            backend.createBackend(callback);
            Logger.d(getClass(), "Creating Vulkan took " + (System.currentTimeMillis() - start) + " millis");
            renderer = new VulkanGltfRenderer(version, backend, new LWJGLVulkan12MemoryAllocator(backend));
            return renderer;
        } catch (BackendException e) {
            throw new RuntimeException(e);
        }
    }

}

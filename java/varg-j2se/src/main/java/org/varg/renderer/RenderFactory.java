
package org.varg.renderer;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.gltf.VulkanMesh;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.Vulkan10Backend.CreateDevice;
import org.varg.window.J2SEWindow.WindowHandle;

public abstract class RenderFactory {

    /**
     * Creates a new renderer for the backend
     * 
     * @param version Min version of renderer API - result may be higher
     * @param window Handle to native render window
     * @return New instance of GltfRenderer
     * @throws IllegalArgumentException If backend could not be created
     */
    public abstract GltfRenderer<VulkanRenderableScene, VulkanMesh> createRenderer(@NonNull Renderers version,
            WindowHandle window, @NonNull CreateDevice callback);
}

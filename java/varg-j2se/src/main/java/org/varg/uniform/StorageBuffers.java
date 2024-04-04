package org.varg.uniform;

import org.varg.gltf.VulkanMesh;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.renderer.GltfRenderer;
import org.varg.shader.Shader;
import org.varg.vulkan.VulkanRenderableScene;

/**
 * Handling of storage buffers
 *
 */
public class StorageBuffers extends DescriptorBuffers<Shader<?>> {

    @Override
    public DescriptorSetTarget[] setDynamicStorage(Shader source) {
        return null;
    }

    @Override
    public int getSetCount(DescriptorSetTarget target) {
        return 1;
    }

    @Override
    public void setStaticStorage(VulkanRenderableScene glTF, GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer) {
    }

}

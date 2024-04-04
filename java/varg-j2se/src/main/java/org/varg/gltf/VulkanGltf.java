
package org.varg.gltf;

import java.util.HashMap;

import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;

/**
 * This is the main class for Gltf assets rendered using Vulkan
 * Use it when loading assets with the Ladda module.
 * 
 */
public class VulkanGltf extends JSONGltf<VulkanPrimitive, VulkanMesh, VulkanScene> {

    @Override
    public VulkanMesh[] getMeshes() {
        if (meshArray == null) {
            meshArray = meshes.toArray(new VulkanMesh[0]);
        }
        return meshArray;
    }

    @Override
    public void destroy() {
        super.destroy();
        meshes = null;
        nodes = null;
    }

    @Override
    public int getMeshCount() {
        return meshes != null ? meshes.size() : 0;
    }

    @Override
    public VulkanPrimitive createPrimitive(DrawMode mode, int materialIndex, int indicesIndex,
            HashMap<Attributes, Integer> attributeMap) {
        return new VulkanPrimitive(this, mode, materialIndex, indicesIndex, attributeMap);
    }

    @Override
    public int createMesh(String name, VulkanPrimitive... primitives) {
        VulkanMesh mesh = new VulkanMesh(name, primitives);
        return addMesh(mesh);
    }

}

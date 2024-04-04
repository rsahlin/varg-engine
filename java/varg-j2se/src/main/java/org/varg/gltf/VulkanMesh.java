package org.varg.gltf;

import java.util.ArrayList;

import org.gltfio.gltf2.JSONMesh;

public class VulkanMesh extends JSONMesh<VulkanPrimitive> {

    /**
     * DO NOT USE THIS CONSTRUCTOR - USE #createMesh() in JSONGltf
     * 
     * @param name
     * @param primitives
     */
    protected VulkanMesh(String name, VulkanPrimitive... primitives) {
        super(name);
        if (primitives != null) {
            for (VulkanPrimitive primitive : primitives) {
                this.primitives.add(primitive);
            }
        }
    }

    protected transient VulkanPrimitive[] primitiveArray;

    @Override
    public VulkanPrimitive[] getPrimitives() {
        if (primitiveArray == null) {
            primitiveArray = primitives.toArray(new VulkanPrimitive[0]);
        }
        return primitiveArray;
    }

    @Override
    public void addPrimitives(ArrayList<VulkanPrimitive> primitives) {
        primitiveArray = null;
        for (VulkanPrimitive p : primitives) {
            this.primitives.add(p);
        }
    }

}

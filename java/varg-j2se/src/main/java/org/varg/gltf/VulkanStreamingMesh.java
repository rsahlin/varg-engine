package org.varg.gltf;

import org.gltfio.gltf2.stream.MeshStream;
import org.gltfio.gltf2.stream.PrimitiveStream;

public class VulkanStreamingMesh extends VulkanMesh {

    private final int[] primitiveIndexes;

    public VulkanStreamingMesh(MeshStream stream, int primitiveIndex) {
        super(stream.getName());
        int primitiveCount = stream.getPrimitiveCount();
        if (primitiveCount > 0) {
            primitiveArray = new VulkanPrimitive[primitiveCount];
            primitiveIndexes = new int[primitiveCount];
            PrimitiveStream[] primitiveStreams = stream.getPrimitives();
            for (int i = 0; i < primitiveCount; i++) {
                primitiveArray[i] = new VulkanStreamingPrimitive(primitiveStreams[i], primitiveIndex);
                primitiveIndexes[i] = primitiveIndex++;
            }
        } else {
            primitiveIndexes = null;
        }
    }

    @Override
    public VulkanPrimitive[] getPrimitives() {
        return primitiveArray;
    }

}

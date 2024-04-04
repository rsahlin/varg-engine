package org.varg.gltf;

import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.stream.NodeStream;
import org.gltfio.lib.Transform;

public class VulkanStreamingNode extends JSONNode<VulkanMesh> {

    public VulkanStreamingNode(NodeStream stream, VulkanStreamingScene scene, JSONMesh[] meshes) {
        this.name = stream.getName();
        this.setRoot(scene);
        setMesh(stream.getMeshIndex());
        this.nodeMesh = getMesh();
        this.transform = new Transform();
        transform.set(stream.getTRS());
        children = new int[stream.getChildCount()];
    }

}

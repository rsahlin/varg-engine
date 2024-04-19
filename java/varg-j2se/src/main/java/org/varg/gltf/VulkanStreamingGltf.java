package org.varg.gltf;

import org.gltfio.gltf2.StreamingGltf;
import org.gltfio.gltf2.stream.SceneStream;
import org.gltfio.lib.Logger;

public class VulkanStreamingGltf extends StreamingGltf<VulkanStreamingScene> {

    @Override
    protected VulkanStreamingScene createScene(SceneStream stream) {
        scene = new VulkanStreamingScene(this, stream);
        return scene;
    }

    @Override
    public void finishedLoading() {
        Logger.d(getClass(), "Finished loading streaming gltf");
    }

    @Override
    public VulkanStreamingScene getScene() {
        return scene;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}

package org.varg.vulkan;

import java.util.HashMap;

import org.gltfio.gltf2.RenderableScene;
import org.varg.renderer.DrawCallBundle;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.VertexMemory;

/**
 * Extension of interface for rendering a scene - this adds functionality needed by vulkan renderer
 */
public interface VulkanRenderableScene extends RenderableScene {

    /**
     * Returns an array of primmitive vertex inputstates needed to render the scene.
     * This is used when creating pipelines.
     * 
     * @return
     */
    PrimitiveVertexInputState[] getVertexInputStates();

    /**
     * Creates the indirect drawcalls needed to render the scene, this will allocate device buffers for attribute
     * storage and indirect drawcalls.
     * Vertex attributes will not be uploaded to device
     * 
     * @param deviceMemory Memory manager used to create vertex buffer memory and memory for indirect drawcalls.
     * @return
     */
    void createIndirectDrawCalls(DeviceMemory deviceMemory);

    DrawCallBundle<IndirectDrawCalls> getDrawCallBundle();

    HashMap<Integer, VertexMemory> getVertexMemory();

    void freeVertexMemory(DeviceMemory deviceMemory);

}

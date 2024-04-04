package org.varg.vulkan;

import org.gltfio.gltf2.JSONMaterial.AlphaMode;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.vertex.BindVertexBuffers;

/**
 * Used to issue an indirect drawing command.
 */
public interface IndirectDrawing {

    /**
     * Copies the indirect drawing data to device, if drawing uses indexed mode then index buffers are uploaded as well
     * Not including vertex attributes
     * 
     * @param deviceMemory
     * @param queue
     */
    int copyToDevice(DeviceMemory deviceMemory, Queue queue);

    /**
     * Before issuing indirect drawcalls to the graphics API the vertexbuffers shall be bound.
     * If issuing an indexed call, then the index buffer shall be bound
     * 
     * @param queue
     */
    void drawIndirect(Queue queue);

    /**
     * Returns the hash for the pipeline for the indirect draw
     * 
     * @return
     */
    int getPipelineHash();

    /**
     * Returns the number of ints needed for the indirect buffer, this is the same as
     * drawcalls * sizeofindirectcommands
     * 
     * @return
     */
    int getCommandBufferSize();

    /**
     * Returns the primitive vertex inputstate
     * 
     * @return
     */
    PrimitiveVertexInputState getInputState();

    /**
     * Returns the vertexbuffers to be bound before issuing drawcalls
     * 
     * @return
     */
    BindVertexBuffers getVertexBuffers();

    Attributes[] getAttributes();

    Channel[] getTextureChannels();

    DrawMode getDrawMode();

    AlphaMode getAlphaMode();

}

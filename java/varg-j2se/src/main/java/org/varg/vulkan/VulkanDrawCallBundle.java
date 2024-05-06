package org.varg.vulkan;

import java.util.HashMap;

import org.gltfio.data.FlattenedScene.PrimitiveSorter;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.ErrorMessage;
import org.varg.renderer.AbstractDrawCalls;
import org.varg.renderer.DrawCallBundle;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;
import org.varg.vulkan.Vulkan10.MemoryPropertyFlagBit;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.Memory;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.memory.VertexMemory;
import org.varg.vulkan.vertex.BindVertexBuffers;

public class VulkanDrawCallBundle extends DrawCallBundle<IndirectDrawCalls> {

    private DeviceMemory deviceMemory;
    private HashMap<Integer, VertexMemory> vertexMap;
    private int commandSize = 0;
    private MemoryBuffer vkIndirectBuffer;

    public VulkanDrawCallBundle(HashMap<Integer, VertexMemory> vertexMemory, DeviceMemory deviceMemory,
            int instanceCount, int instanceDataSize) {
        super(instanceCount, instanceDataSize);
        this.deviceMemory = deviceMemory;
        this.vertexMap = vertexMemory;
    }

    @Override
    protected IndirectDrawCalls createDrawCalls(PrimitiveSorter primitives) {
        VertexMemory mem = vertexMap.get(primitives.getAttributeHash());
        if (mem == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "No vertex memory for key " + primitives.getAttributeHash());
        }
        MemoryBuffer[] buffers = mem.getMemoryBuffers();
        MemoryBuffer[] indexBuffers = mem.getIndexMemoryBuffers();
        BindVertexBuffers bindBuffers = new BindVertexBuffers(0, buffers, new long[buffers.length]);
        BindVertexBuffers bindIndexBuffers = null;
        if (indexBuffers != null && indexBuffers.length > 0) {
            bindIndexBuffers = new BindVertexBuffers(0, indexBuffers, new long[indexBuffers.length]);
        }
        IndirectDrawCalls idc = new IndirectDrawCalls(primitives, bindIndexBuffers, bindBuffers, firstInstance);
        commandSize += idc.getCommandBufferSize();
        return idc;
    }

    @Override
    protected void createMemory() {
        vkIndirectBuffer = deviceMemory.createBuffer(commandSize * Integer.BYTES, BufferUsageFlagBit.VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT.value | BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT.value);
        Memory indirectMemory = deviceMemory.allocateMemory(vkIndirectBuffer.allocationSize, BitFlags.getFlagsValue(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));
        // Bind indirect buffers to memory
        deviceMemory.bindBufferMemory(indirectMemory, 0, vkIndirectBuffer);
        int offset = 0;
        for (AbstractDrawCalls dc : drawCalls) {
            IndirectDrawCalls idc = (IndirectDrawCalls) dc;
            idc.setCommandMemoryBuffer(vkIndirectBuffer, offset);
            offset += idc.getCommandBufferSize() * Integer.BYTES;
        }
        if (offset != vkIndirectBuffer.size) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Drawcall size does not match buffer size");
        }
    }

    @Override
    public void freeMemory() {
        if (vkIndirectBuffer != null) {
            deviceMemory.freeBuffer(vkIndirectBuffer);
            vkIndirectBuffer = null;
        }
    }

    @Override
    public IndirectDrawCalls[] getAllDrawCalls() {
        return drawCalls.toArray(new IndirectDrawCalls[0]);
    }

}

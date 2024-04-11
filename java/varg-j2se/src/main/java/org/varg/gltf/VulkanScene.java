package org.varg.gltf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.gltfio.DepthFirstNodeIterator;
import org.gltfio.data.FlattenedScene;
import org.gltfio.data.FlattenedScene.PrimitiveSorterMap;
import org.gltfio.data.VertexBuffer.VertexBufferBundle;
import org.gltfio.gltf2.AttributeSorter;
import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONScene;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.varg.renderer.DrawCallBundle;
import org.varg.uniform.GltfStorageBuffers;
import org.varg.vulkan.IndirectDrawCalls;
import org.varg.vulkan.PrimitiveVertexInputState;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;
import org.varg.vulkan.VulkanBackend;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;
import org.varg.vulkan.VulkanDrawCallBundle;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.VulkanUtils;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.VertexMemory;
import org.varg.vulkan.pipeline.PipelineVertexInputState;
import org.varg.vulkan.vertex.VertexInputAttributeDescription;
import org.varg.vulkan.vertex.VertexInputBindingDescription;
import org.varg.vulkan.vertex.VertexInputBindingDescription.VertexInputRate;

public class VulkanScene extends JSONScene implements VulkanRenderableScene {

    private static transient VertexInputAttributeDescription[] inputs;
    private transient PrimitiveVertexInputState[] inputStates = null;
    /**
     * Vertex memory where key is attribute hash
     */
    private transient HashMap<Integer, VertexMemory> memoryBuffers;
    private transient VulkanDrawCallBundle drawCallBundle;
    private transient int primitiveInstanceCount;
    public static final AttributeSorter SORTER = AttributeSorter.getInstance();

    @Override
    public PrimitiveVertexInputState[] getVertexInputStates() {
        if (inputStates == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message
                    + "Must create indirect drawcommands before calling this method");
        }
        return inputStates;
    }

    public static PrimitiveVertexInputState[] createVertexInputStates(RenderableScene scene) {
        ArrayList<PrimitiveVertexInputState> inputStates = new ArrayList<PrimitiveVertexInputState>();
        DepthFirstNodeIterator iterator = new DepthFirstNodeIterator(scene);
        JSONNode<VulkanMesh> node = null;
        HashSet<Integer> createdStates = new HashSet<Integer>();
        while (iterator.hasNext()) {
            node = iterator.next();
            VulkanMesh mesh = node.getMesh();
            if (mesh != null) {
                VulkanPrimitive[] primitives = mesh.getPrimitives();
                if (primitives != null) {
                    for (VulkanPrimitive p : primitives) {
                        PrimitiveVertexInputState inputState = new PrimitiveVertexInputState(VulkanScene
                                .createBindings(p.getAccessors(), p.getMaterial().getTextureChannels()),
                                p
                                        .getMode());
                        if (!createdStates.contains(inputState.getHash())) {
                            inputStates.add(inputState);
                            createdStates.add(inputState.getHash());
                        }
                    }
                }
            }
        }
        return inputStates.toArray(new PrimitiveVertexInputState[0]);
    }

    private static PipelineVertexInputState createBindings(JSONAccessor[] sortedAccessors, Channel[] textureChannels) {
        VertexInputAttributeDescription[] vertexInputs =
                new VertexInputAttributeDescription[AttributeSorter.GLTF_SORT_ORDER.length];
        VertexInputBindingDescription[] vertexBindings =
                new VertexInputBindingDescription[vertexInputs.length];
        for (int i = 0; i < vertexInputs.length; i++) {
            Attributes attribute = AttributeSorter.GLTF_SORT_ORDER[i];
            JSONAccessor accessor = sortedAccessors[i];
            Vulkan10.Format format = accessor != null ? VulkanUtils.getFormat(accessor) : getDefaultFormat(
                    attribute);
            // if (accessor != null && usesAttribute(attribute)) {
            if (accessor != null) {
                vertexInputs[i] = getInput(i);
                int stride = accessor.getBufferView().getByteStride();
                if (stride != format.sizeInBytes) {
                    vertexBindings[i] = new VertexInputBindingDescription(i, stride,
                            VertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX);
                } else {
                    vertexBindings[i] = VulkanPrimitive.getInputBinding(i);
                }
            } else {
                switch (attribute) {
                    // Shader code controlled by texture channels, so include binding but null input.
                    case BITANGENT:
                    case TANGENT:
                    case NORMAL:
                    case TEXCOORD_0:
                    case TEXCOORD_1:
                    case POSITION:
                        vertexInputs[i] = getInput(i);
                        vertexBindings[i] = VulkanPrimitive.getInputBinding(i);
                        break;
                    case COLOR_0:
                        vertexInputs[i] = null;
                        vertexBindings[i] = null;
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + attribute);
                }
            }
        }
        return new PipelineVertexInputState(AttributeSorter.GLTF_SORT_ORDER,
                vertexBindings, vertexInputs, textureChannels);
    }

    public static VertexInputAttributeDescription getInput(int binding) {
        if (inputs == null) {
            inputs = new VertexInputAttributeDescription[AttributeSorter.GLTF_SORT_ORDER.length];
            for (int i = 0; i < inputs.length; i++) {
                Vulkan10.Format format = getDefaultFormat(AttributeSorter.GLTF_SORT_ORDER[i]);
                inputs[i] = new VertexInputAttributeDescription(i, i, format, 0);
            }
        }
        return inputs[binding];
    }

    public static Vulkan10.Format getDefaultFormat(Attributes attribute) {
        switch (attribute) {
            case TANGENT:
                return Vulkan10.Format.VK_FORMAT_R32G32B32A32_SFLOAT;
            case BITANGENT:
            case NORMAL:
            case POSITION:
            case COLOR_0:
                return Vulkan10.Format.VK_FORMAT_R32G32B32_SFLOAT;
            case TEXCOORD_0:
            case TEXCOORD_1:
                return Vulkan10.Format.VK_FORMAT_R32G32_SFLOAT;
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + attribute);
        }
    }

    private HashMap<Integer, VertexMemory> createAttributeMemory(DeviceMemory deviceMemory,
            VertexBufferBundle vertexBufferMap) {
        BufferUsageFlagBit[] vertexFlags = VulkanBackend.getBufferUsage(BackendStringProperties.VERTEX_USAGE,
                BufferUsageFlagBit.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
        int vertexUsage = BitFlags.getFlagsValue(vertexFlags);

        BufferUsageFlagBit[] indexFlags = new BufferUsageFlagBit[] {
                BufferUsageFlagBit.VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT };
        int indexUsage = BitFlags.getFlagsValue(indexFlags);

        HashMap<Integer, VertexMemory> result = new HashMap<Integer, VertexMemory>();
        for (Integer key : vertexBufferMap.getVertexBufferKeys()) {
            VertexMemory mem = deviceMemory.allocateVertexMemory(indexUsage, vertexBufferMap.getIndexBuffers(key),
                    vertexUsage, vertexBufferMap.getVertexBuffers(key));
            result.put(key, mem);
        }
        return result;
    }

    @Override
    public void createIndirectDrawCalls(DeviceMemory deviceMemory) {
        if (nodeIndexes != null && nodeIndexes.size() > 0) {
            // Temp vertex buffer usage -
            VertexBufferBundle vertexBufferMap = streamifyVertexData();
            Logger.d(getClass(), vertexBufferMap.toString());
            memoryBuffers = createAttributeMemory(deviceMemory, vertexBufferMap);
            FlattenedScene fs = new FlattenedScene(vertexBufferMap);
            PrimitiveSorterMap primitivesByPipeline = fs.sortByPipelines(getNodes());
            primitiveInstanceCount = primitivesByPipeline.getTotalPrimitiveCount();
            VulkanDrawCallBundle calls = new VulkanDrawCallBundle(memoryBuffers, deviceMemory, primitiveInstanceCount, GltfStorageBuffers.getUniformPrimitiveDataSize());
            calls.createDrawCalls(primitivesByPipeline, vertexBufferMap);
            IndirectDrawCalls[] dcs = calls.getAllDrawCalls();
            Logger.d(getClass(), "Created " + dcs.length + " IndirectDrawCalls, for a total of "
                    + getPrimitiveInstanceCount() + " primitives.");
            for (IndirectDrawCalls dc : dcs) {
                Logger.d(getClass(), Integer.toString(dc.arrayInstanceCount) + " array instances, " + dc
                        .getIndexedInstanceCount(IndexType.BYTE) + " byte indexinstances, "
                        + dc.getIndexedInstanceCount(IndexType.SHORT) + " short indexinstances, "
                        + dc.getIndexedInstanceCount(IndexType.INT) + " int indexinstances");
            }
            this.drawCallBundle = calls;
        } else {
            Logger.e(getClass(), "No nodes in scene");
        }
    }

    @Override
    public DrawCallBundle<IndirectDrawCalls> getDrawCallBundle() {
        return drawCallBundle;
    }

    @Override
    public HashMap<Integer, VertexMemory> getVertexMemory() {
        return memoryBuffers;
    }

    @Override
    public int getPrimitiveInstanceCount() {
        return primitiveInstanceCount;
    }

    @Override
    public void freeVertexMemory(DeviceMemory deviceMemory) {
        if (memoryBuffers != null) {
            for (VertexMemory mem : memoryBuffers.values()) {
                mem.freeDeviceMemory(deviceMemory);
            }
            memoryBuffers.clear();
        }
        if (drawCallBundle != null) {
            drawCallBundle.freeMemory();
        }
    }

}

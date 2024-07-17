package org.varg.gltf;

import java.util.ArrayList;
import java.util.HashMap;

import org.gltfio.gltf2.AttributeSorter;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.gltf2.JSONTexture;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.StreamingScene;
import org.gltfio.gltf2.stream.MeshStream;
import org.gltfio.gltf2.stream.NodeStream;
import org.gltfio.gltf2.stream.SceneStream;
import org.gltfio.gltf2.stream.SubStream.DataType;
import org.gltfio.gltf2.stream.VertexAttributeStream;
import org.gltfio.lib.ErrorMessage;
import org.varg.renderer.DrawCallBundle;
import org.varg.vulkan.IndirectDrawCalls;
import org.varg.vulkan.IndirectDrawing;
import org.varg.vulkan.PrimitiveVertexInputState;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.VertexMemory;
import org.varg.vulkan.pipeline.PipelineVertexInputState;
import org.varg.vulkan.vertex.VertexInputAttributeDescription;
import org.varg.vulkan.vertex.VertexInputBindingDescription;

/**
 * Scene serialized from binary glb
 */
public class VulkanStreamingScene extends StreamingScene implements VulkanRenderableScene {

    private VulkanStreamingNode[] nodes;
    private transient PrimitiveVertexInputState[] inputStates;
    private transient IndirectDrawCalls[] indirectDrawCalls;

    public VulkanStreamingScene(VulkanStreamingGltf root, SceneStream stream) {
        super(root, stream);
    }

    private PrimitiveVertexInputState[] createVertexInputStates(Attributes[] attributes, DataType[] dataTypes,
            Channel[] textureChannels) {
        VertexInputAttributeDescription[] vertexInputs =
                new VertexInputAttributeDescription[AttributeSorter.GLTF_SORT_ORDER.length];
        VertexInputBindingDescription[] vertexBindings =
                new VertexInputBindingDescription[vertexInputs.length];
        Attributes[] sorted = VulkanScene.SORTER.sortAttributes(attributes);
        for (int i = 0; i < sorted.length; i++) {
            Attributes attribute = sorted[i];
            Vulkan10.Format format = VulkanScene.getDefaultFormat(AttributeSorter.GLTF_SORT_ORDER[i]);
            vertexBindings[i] = VulkanPrimitive.getInputBinding(i);
            vertexInputs[i] = new VertexInputAttributeDescription(i, i, format, 0);
        }
        PipelineVertexInputState pipelineInput = new PipelineVertexInputState(AttributeSorter.GLTF_SORT_ORDER,
                vertexBindings, vertexInputs, textureChannels);
        return new PrimitiveVertexInputState[] { new PrimitiveVertexInputState(pipelineInput, DrawMode.TRIANGLES) };
    }

    @Override
    protected void createArrays(SceneStream stream) {
        super.createArrays(stream);
        nodes = new VulkanStreamingNode[stream.getNodeCount()];
        Attributes[] attributes = stream.getAttributeTypes();
        DataType[] dataTypes = new DataType[attributes.length];
        for (int i = 0; i < dataTypes.length; i++) {
            dataTypes[i] = VertexAttributeStream.getAttributeSizeInBytes(attributes[i]);
        }
        inputStates = createVertexInputStates(stream.getAttributeTypes(), dataTypes,
                new JSONTexture.Channel[0]);
        primitiveAttributeIndexes = new ArrayList[AttributeSorter.getInstance().getSortOrder().length];
    }

    @Override
    public int getMeshCount() {
        return currentMeshIndex;
    }

    @Override
    protected JSONMesh createMesh(MeshStream stream) {
        return new VulkanStreamingMesh(stream, currentPrimitiveIndex);
    }

    private void assertArray(Object[] objects, String objName) {
        for (Object o : objects) {
            if (o == null) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + objName + " is null");
            }
        }
    }

    @Override
    public JSONNode<VulkanMesh>[] getNodes() {
        return nodes;
    }

    @Override
    public JSONNode addNode(String name, JSONNode parent) {
        throw new IllegalArgumentException();
    }

    @Override
    public void addNode(NodeStream stream) {
        VulkanStreamingNode vn = new VulkanStreamingNode(stream, this, meshes);
        nodes[currentNodeIndex++] = vn;
    }

    @Override
    public PrimitiveVertexInputState[] getVertexInputStates() {
        return inputStates;
    }

    /**
     * Returns the indirect drawcalls
     * 
     * @return
     */
    public IndirectDrawing[] getIndirectDrawCall() {
        return indirectDrawCalls;

    }

    @Override
    public void createIndirectDrawCalls(DeviceMemory deviceMemory) {
        // TODO Auto-generated method stub
    }

    @Override
    public HashMap<Integer, VertexMemory> getVertexMemory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DrawCallBundle getDrawCallBundle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void freeVertexMemory(DeviceMemory deviceMemory) {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}

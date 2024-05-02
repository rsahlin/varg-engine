
package org.varg.pipeline;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONMaterial.AlphaMode;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.Light;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.varg.BackendException;
import org.varg.assets.TextureDescriptor;
import org.varg.assets.TextureImages;
import org.varg.assets.TextureImages.SamplerType;
import org.varg.gltf.VulkanMesh;
import org.varg.pipeline.Pipelines.InternalPipelines;
import org.varg.renderer.GltfRenderer;
import org.varg.renderer.GltfRenderer.RenderPasses;
import org.varg.renderer.GltfUtils;
import org.varg.shader.BaseShaderImplementation;
import org.varg.shader.ComputeShader;
import org.varg.shader.ComputeShader.ComputeShaderCreateInfo;
import org.varg.shader.Gltf2GraphicsShader.GltfDescriptorSetTarget;
import org.varg.shader.GraphicsShader;
import org.varg.shader.GraphicsShader.GraphicsShaderCreateInfo;
import org.varg.shader.MeshShader;
import org.varg.shader.MeshShader.MeshShaderCreateInfo;
import org.varg.shader.RayTracingShader;
import org.varg.shader.RayTracingShader.RayTracingCreateInfo;
import org.varg.shader.Shader;
import org.varg.shader.Shader.GraphicsSubtype;
import org.varg.shader.Shader.Stage;
import org.varg.shader.Shader.Subtype;
import org.varg.shader.ShaderCreateInfo;
import org.varg.uniform.BindBuffer;
import org.varg.uniform.DescriptorBuffers;
import org.varg.uniform.PushConstants;
import org.varg.uniform.StorageBuffers;
import org.varg.vulkan.GLSLCompiler;
import org.varg.vulkan.IndirectDrawing;
import org.varg.vulkan.PrimitiveVertexInputState;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10.CompareOp;
import org.varg.vulkan.Vulkan10.CullModeFlagBit;
import org.varg.vulkan.Vulkan10.DescriptorType;
import org.varg.vulkan.Vulkan10.FrontFace;
import org.varg.vulkan.Vulkan10.PipelineBindPoint;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;
import org.varg.vulkan.Vulkan10.ShaderStageFlagBit;
import org.varg.vulkan.Vulkan12Backend;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.descriptor.DescriptorBufferInfo;
import org.varg.vulkan.descriptor.DescriptorSet;
import org.varg.vulkan.descriptor.DescriptorSetLayout;
import org.varg.vulkan.descriptor.DescriptorSetLayoutBinding;
import org.varg.vulkan.descriptor.DescriptorSetLayoutCreateInfo;
import org.varg.vulkan.descriptor.Descriptors;
import org.varg.vulkan.extensions.KHRFragmentShadingRate.PipelineFragmentShadingRateStateCreateInfoKHR;
import org.varg.vulkan.extensions.KHRSwapchain;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.pipeline.ComputePipeline;
import org.varg.vulkan.pipeline.ComputePipelineCreateInfo;
import org.varg.vulkan.pipeline.GraphicsPipeline;
import org.varg.vulkan.pipeline.GraphicsPipelineCreateInfo;
import org.varg.vulkan.pipeline.PipelineColorBlendStateCreateInfo;
import org.varg.vulkan.pipeline.PipelineDepthStencilStateCreateInfo;
import org.varg.vulkan.pipeline.PipelineInputAssemblyStateCreateInfo;
import org.varg.vulkan.pipeline.PipelineLayout;
import org.varg.vulkan.pipeline.PipelineLayoutCreateInfo;
import org.varg.vulkan.pipeline.PipelineMultisampleStateCreateInfo;
import org.varg.vulkan.pipeline.PipelineRasterizationStateCreateInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.GLTF2SpecializationConstant;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationConstant;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationMapEntry;
import org.varg.vulkan.pipeline.PipelineVertexInputStateCreateInfo;
import org.varg.vulkan.pipeline.PipelineViewportStateCreateInfo;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.PushConstantRange;
import org.varg.vulkan.structs.Viewport;

/**
 * Handles different pipelins for rendering
 * Note that only ONE PipelineLayout (VkPipelineLayout) shall be used per glTF asset, this is in order for descriptors
 * to be bound once
 *
 */
public class VulkanPipelines implements Pipelines<VulkanRenderableScene>, InternalPipelines {

    public static final float MIN_DEPTH = 0.0f;
    public static final float MAX_DEPTH = 1.0f;

    final HashMap<String, GraphicsPipeline> graphicsPipelineMap = new HashMap<>();
    final HashMap<Integer, GraphicsPipeline> pipelineByHash = new HashMap<Integer, GraphicsPipeline>();
    final HashMap<String, ComputePipeline> computePipelines = new HashMap<>();
    private HashMap<Subtype, PipelineShaderStageCreateInfo[]> shaderStageInfo =
            new HashMap<Subtype, PipelineShaderStageCreateInfo[]>();
    private final Vulkan12Backend<?> backend;
    private final GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer;

    HashMap<Integer, MemoryBuffer[]> vertexBuffersMap = new HashMap<Integer, MemoryBuffer[]>();
    private ArrayList<PrimitiveVertexInputState> inputStates = new ArrayList<PrimitiveVertexInputState>();

    private Descriptors descriptors = new Descriptors();
    // DO NOT ACCESS DIRECTLY, use get and put methods
    private HashMap<Integer, PipelineLayout> shaderLayouts = new HashMap<>();

    public VulkanPipelines(Vulkan12Backend<?> vulkan, GltfRenderer<VulkanRenderableScene, VulkanMesh> render) {
        if (vulkan == null || render == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        backend = vulkan;
        renderer = render;
    }

    private void putPipelineLayout(ShaderCreateInfo info, PipelineLayout layout) {
        Subtype shaderType = info.shaderType;
        if (shaderLayouts.containsKey(shaderType.getDescriptorSetLayoutHash())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already contains layout for "
                    + shaderType.getName());
        }
        shaderLayouts.put(shaderType.getDescriptorSetLayoutHash(), layout);
    }

    private PipelineLayout getPipelineLayout(Subtype shaderType) {
        return shaderLayouts.get(shaderType.getDescriptorSetLayoutHash());
    }

    /**
     * Creates a new instance of GraphicsPipeline to be used with the graphics shader.
     * Vertex bindings and inputs should generally be aggregated from the glTF model in order to enable
     * reuse of pipelines.
     * 
     * @return The created graphics pipeline.
     * @throws BackendException If there is an error creating the pipeline.
     */
    private GraphicsPipeline createGraphicsPipeline(VulkanRenderableScene glTF,
            IndirectDrawing dc, GraphicsShaderCreateInfo info, SpecializationInfo specializationInfo)
            throws BackendException {
        long start = System.currentTimeMillis();
        PrimitiveVertexInputState inputState = dc.getInputState();
        GraphicsShader graphicsShader = info.getInstance(inputState.getInputState());
        PipelineFragmentShadingRateStateCreateInfoKHR vsr = info.getFragmentShadingRate();
        try {
            KHRSwapchain swapChain = renderer.getBackend().getKHRSwapchain();
            graphicsShader.loadModules(renderer.getBackend(), dc.getPipelineHash());
            PipelineShaderStageCreateInfo[] stageInfo = graphicsShader.createShaderStageInfos(specializationInfo);
            this.shaderStageInfo.put(info.graphicsShaderType, stageInfo);
            if (descriptors.getDescriptorSet(GltfDescriptorSetTarget.MATERIAL_TEXTURE) == null) {
                allocateDescriptorSets(GltfDescriptorSetTarget.MATERIAL_TEXTURE);
            }
            if (descriptors.getDescriptorSet(GltfDescriptorSetTarget.CUBEMAP_TEXTURE) == null) {
                allocateDescriptorSets(GltfDescriptorSetTarget.CUBEMAP_TEXTURE);
            }
            PipelineLayout layout = getPipelineLayout(info.graphicsShaderType);
            if (layout == null) {
                PipelineLayoutCreateInfo layoutInfo = new PipelineLayoutCreateInfo(descriptors.getDescriptorLayouts(
                        GltfDescriptorSetTarget.values()), null);
                layout = renderer.getBackend().createPipelineLayout(layoutInfo);
                putPipelineLayout(graphicsShader.getShaderInfo(), layout);
            }
            GraphicsPipelineCreateInfo pipelineCreateInfo = createGraphicsPipelineInfo(layout,
                    graphicsShader.getPipelineVertexInputState().getPipelineVertexInputStateCreateInfo(),
                    info.graphicsShaderType, inputState.getDrawMode(), dc.getAlphaMode(),
                    swapChain.getExtent(), stageInfo);
            pipelineCreateInfo.setFragmentShadingRateCreateInfo(vsr);
            GraphicsPipeline graphicsPipeline = renderer.getBackend().createGraphicsPipeline(pipelineCreateInfo,
                    renderer.getRenderPass(), graphicsShader);

            Logger.d(getClass(),
                    "Created GraphicsPipeline for " + info.graphicsShaderType + ", DrawMode "
                            + inputState.getDrawMode() + " : " + graphicsShader.toString() + " in "
                            + (System.currentTimeMillis() - start) + " millis");

            return graphicsPipeline;
        } catch (IOException e) {
            throw new BackendException(e);
        }
    }

    private GraphicsPipelineCreateInfo createGraphicsPipelineInfo(PipelineLayout layout,
            PipelineVertexInputStateCreateInfo pipelineInputs, GraphicsSubtype shaderType, DrawMode drawMode,
            AlphaMode alphaMode, Extent2D swapChainDimension, PipelineShaderStageCreateInfo[] stageInfo) {

        // Mesh shader does not use vertex input state - VkPipelineVertexInputStateCreateInfo and
        // VkPipelineInputAssemblyStateCreateInfo
        PipelineInputAssemblyStateCreateInfo assemblyInfo = null;
        if (pipelineInputs == null) {
            // Make sure mesh shader
            if (!shaderType.hasStage(Stage.MESH)) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                        + "Vertex input state is null but no Mesh shader stage");
            }
        } else {
            assemblyInfo = new PipelineInputAssemblyStateCreateInfo(GltfUtils.getFromGltfMode(drawMode), false);
        }
        Viewport[] viewports = new Viewport[] {
                new Viewport(0, 0, swapChainDimension.width, swapChainDimension.height, 0, 1) };
        PipelineViewportStateCreateInfo viewportInfo = new PipelineViewportStateCreateInfo(viewports, null);
        CullModeFlagBit cullMode = Settings.getInstance().getBoolean(BackendProperties.NO_BACKFACE_CULLING)
                ? CullModeFlagBit.VK_CULL_MODE_NONE
                : CullModeFlagBit.VK_CULL_MODE_BACK_BIT;
        CompareOp compareOp = Settings.getInstance().getBoolean(BackendProperties.NO_DEPTHTEST)
                ? CompareOp.VK_COMPARE_OP_ALWAYS
                : CompareOp.VK_COMPARE_OP_LESS_OR_EQUAL;
        PipelineRasterizationStateCreateInfo rasterizationInfo = new PipelineRasterizationStateCreateInfo(
                shaderType.getPolygonMode(), cullMode, FrontFace.VK_FRONT_FACE_COUNTER_CLOCKWISE, 1.0f);
        PipelineMultisampleStateCreateInfo multisampleInfo = createMultisampleState();
        PipelineDepthStencilStateCreateInfo depthStencilInfo = new PipelineDepthStencilStateCreateInfo(compareOp);
        PipelineColorBlendStateCreateInfo colorBlendInfo =
                new PipelineColorBlendStateCreateInfo(alphaMode != AlphaMode.OPAQUE);
        return new GraphicsPipelineCreateInfo(stageInfo,
                pipelineInputs, assemblyInfo, viewportInfo, rasterizationInfo, multisampleInfo, depthStencilInfo,
                colorBlendInfo, layout);
    }

    private PipelineMultisampleStateCreateInfo createMultisampleState() {
        SampleCountFlagBit samples = renderer.getRenderPass().getColorBufferSamples();
        float minSamples = 1f;
        return new PipelineMultisampleStateCreateInfo(samples,
                samples != SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT, minSamples);
    }

    @Override
    public ComputeShader createComputePipelines(ComputeShaderCreateInfo shaderInfo, StorageBuffers buffers)
            throws BackendException {
        long start = System.currentTimeMillis();
        Subtype shaderType = shaderInfo.shaderType;

        if (buffers != null) {
            createDescriptorSetLayouts(buffers, shaderType.getTargets());
            createDescriptorSets(shaderType.getTargets());
            updateDescriptorSets(buffers, shaderType.getTargets());
        }
        ComputeShader computeShader = shaderInfo.getInstance();
        GLSLCompiler compiler = GLSLCompiler.getInstance(shaderInfo.version);
        compiler.clearMacros();
        shaderInfo.setMacros(compiler);

        ComputePipeline pipeline = createComputePipeline(computeShader, null);
        if (computePipelines.containsKey(shaderType.getName())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message
                    + "Already added graphics pipeline for " + shaderType.getName());
        }
        computePipelines.put(shaderType.getName(), pipeline);
        return computeShader;
    }

    private ComputePipeline createComputePipeline(ComputeShader computeShader, SpecializationInfo specializationInfo)
            throws BackendException {
        long start = System.currentTimeMillis();
        try {
            computeShader.loadModules(renderer.getBackend(), computeShader.getShaderInfo().shaderType
                    .getDescriptorSetLayoutHash());
            PipelineShaderStageCreateInfo[] stageInfo = computeShader.createShaderStageInfos(specializationInfo);
            shaderStageInfo.put(computeShader.getShaderInfo().shaderType, stageInfo);
            PipelineLayout layout = getPipelineLayout(computeShader.getShaderInfo().shaderType);
            Subtype shaderType = computeShader.getShaderInfo().shaderType;
            if (layout == null) {
                PipelineLayoutCreateInfo layoutInfo = new PipelineLayoutCreateInfo(descriptors.getDescriptorLayouts(
                        shaderType.getTargets()), null);
                layout = renderer.getBackend().createPipelineLayout(layoutInfo);
                putPipelineLayout(computeShader.getShaderInfo(), layout);
            } else {
                // Todo - how do we now that this is a compatible layout?
                Logger.d(getClass(), "Using existing layout for hash: " + shaderType.getDescriptorSetLayoutHash());
            }
            ComputePipelineCreateInfo createInfo = new ComputePipelineCreateInfo(null, stageInfo, layout);
            ComputePipeline cp = renderer.getBackend().createComputePipeline(createInfo, computeShader);
            return cp;
        } catch (IOException e) {
            throw new BackendException(e);
        }

    }

    @Override
    public void createGraphicsPipelines(IndirectDrawing[] drawCalls, VulkanRenderableScene glTF, GraphicsShaderCreateInfo info) throws BackendException {
        TextureImages textureImages = renderer.getAssets().getTextureImages(glTF);
        Subtype shaderType = info.shaderType;
        DescriptorBuffers<?> buffers = renderer.getAssets().getStorageBuffers(shaderType);
        createDescriptorSetLayouts(buffers, shaderType.getTargets());
        createDescriptorSets(shaderType.getTargets());
        updateDescriptorSets(buffers, shaderType.getTargets());
        SpecializationInfo specializationInfo = createSpecializationInfo(glTF, buffers);
        if (drawCalls != null && drawCalls.length > 0) {
            for (IndirectDrawing dc : drawCalls) {
                if (!pipelineByHash.containsKey(dc.getPipelineHash())) {
                    GLSLCompiler compiler = GLSLCompiler.getInstance(info.version);
                    // TODO - make compiler setting go through shader create info .setMacros() method
                    // Removes any macros from previously compiled pipelines
                    compiler.clearMacros();
                    // Set the compiler macros
                    compiler.setMacros(glTF, backend.getSurfaceFormat());
                    compiler.setMacros(dc.getAttributes(), dc.getAlphaMode(), dc.getTextureChannels());
                    compiler.setMacros(BaseShaderImplementation.ShaderProperties.values());
                    pipelineByHash.put(dc.getPipelineHash(), createGraphicsPipeline(glTF, dc, info, specializationInfo));
                }
            }
            updateTextureDescriptorSets(textureImages, SamplerType.sampler2DArray);
            updateTextureDescriptorSets(textureImages, SamplerType.samplerCubeArray);
        }
    }

    @Override
    public RayTracingShader createRayTracingPipeline(RayTracingCreateInfo shaderInfo) throws BackendException {
        SpecializationInfo specializationInfo = null;
        try {
            Subtype shaderType = shaderInfo.shaderType;
            DescriptorBuffers<?> buffers = renderer.getAssets().getStorageBuffers(shaderType);

            if (buffers != null) {
                createDescriptorSetLayouts(buffers, shaderType.getTargets());
                createDescriptorSets(shaderType.getTargets());
                updateDescriptorSets(buffers, shaderType.getTargets());
            }

            RayTracingShader rayTracingShader = shaderInfo.getInstance();
            GLSLCompiler compiler = GLSLCompiler.getInstance(shaderInfo.version);
            compiler.clearMacros();
            shaderInfo.setMacros(compiler);
            rayTracingShader.loadModules(renderer.getBackend(), rayTracingShader.getShaderInfo().shaderType
                    .getDescriptorSetLayoutHash());
            PipelineShaderStageCreateInfo[] stageInfo = rayTracingShader.createShaderStageInfos(specializationInfo);
            this.shaderStageInfo.put(rayTracingShader.getShaderInfo().shaderType, stageInfo);
            PipelineLayout layout = getPipelineLayout(rayTracingShader.getShaderInfo().shaderType);
            if (layout == null) {
                PipelineLayoutCreateInfo layoutInfo = new PipelineLayoutCreateInfo(descriptors.getDescriptorLayouts(
                        shaderType.getTargets()), null);
                layout = renderer.getBackend().createPipelineLayout(layoutInfo);
                putPipelineLayout(rayTracingShader.getShaderInfo(), layout);
            } else {
                // Todo - how do we now that this is a compatible layout?
                Logger.d(getClass(), "Using existing layout for hash: " + shaderType.getDescriptorSetLayoutHash());
            }
            // return renderer.getBackend().createGraphicsPipeline(pipelineCreateInfo, renderer.getRenderPass(),
            // meshShader);
            return null;
        } catch (IOException e) {
            throw new BackendException(e);
        }
    }

    @Override
    public MeshShader<?> createMeshPipeline(MeshShaderCreateInfo shaderInfo) throws BackendException {
        // When using a mesh shader the vertex input state is not used in the pipeline:
        long start = System.currentTimeMillis();
        Subtype shaderType = shaderInfo.shaderType;
        DescriptorBuffers<?> buffers = renderer.getAssets().getStorageBuffers(shaderType);

        if (buffers != null) {
            createDescriptorSetLayouts(buffers, shaderType.getTargets());
            createDescriptorSets(shaderType.getTargets());
            updateDescriptorSets(buffers, shaderType.getTargets());
        }
        MeshShader meshShader = shaderInfo.getInstance();
        GLSLCompiler compiler = GLSLCompiler.getInstance(shaderInfo.version);
        compiler.clearMacros();
        shaderInfo.setMacros(compiler);

        GraphicsPipeline pipeline = createMeshPipeline(meshShader, null, shaderInfo.getFragmentShadingRate());
        if (graphicsPipelineMap.containsKey(shaderType.getName())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message
                    + "Already added graphics pipeline for id " + shaderType.getName());
        }
        graphicsPipelineMap.put(shaderType.getName(), pipeline);
        return meshShader;
    }

    private GraphicsPipeline createMeshPipeline(MeshShader<?> meshShader,
            SpecializationInfo specializationInfo,
            PipelineFragmentShadingRateStateCreateInfoKHR fragmentShadingRate)
            throws BackendException {
        try {
            meshShader.loadModules(renderer.getBackend(), meshShader.getShaderInfo().shaderType
                    .getDescriptorSetLayoutHash());
            PipelineShaderStageCreateInfo[] stageInfo = meshShader.createShaderStageInfos(specializationInfo);
            this.shaderStageInfo.put(meshShader.getShaderInfo().shaderType, stageInfo);
            PipelineLayout layout = getPipelineLayout(meshShader.getShaderInfo().shaderType);
            Subtype shaderType = meshShader.getShaderInfo().shaderType;
            if (layout == null) {
                PipelineLayoutCreateInfo layoutInfo = new PipelineLayoutCreateInfo(descriptors.getDescriptorLayouts(
                        shaderType.getTargets()), null);
                layout = renderer.getBackend().createPipelineLayout(layoutInfo);
                putPipelineLayout(meshShader.getShaderInfo(), layout);
            } else {
                // Todo - how do we now that this is a compatible layout?
                Logger.d(getClass(), "Using existing layout for hash: " + shaderType.getDescriptorSetLayoutHash());
            }
            KHRSwapchain swapChain = renderer.getBackend().getKHRSwapchain();
            GraphicsPipelineCreateInfo pipelineCreateInfo = createGraphicsPipelineInfo(layout, null,
                    meshShader.getShaderInfo().meshShaderType, null, AlphaMode.OPAQUE, swapChain.getExtent(),
                    stageInfo);
            pipelineCreateInfo.setFragmentShadingRateCreateInfo(fragmentShadingRate);
            return renderer.getBackend().createGraphicsPipeline(
                    pipelineCreateInfo, renderer.getRenderPass(), meshShader);
        } catch (IOException e) {
            throw new BackendException(e);
        }
    }

    @Override
    public void deleteGraphicsPipelines(JSONGltf glTF) {
        freeDescriptorSets(GltfDescriptorSetTarget.MATERIAL_TEXTURE);
        deleteDescriptorLayout(GltfDescriptorSetTarget.MATERIAL_TEXTURE);
        inputStates.clear();
    }

    private void createDescriptorSets(DescriptorSetTarget... targets) {
        for (DescriptorSetTarget target : targets) {
            if (descriptors.getDescriptorSet(target) == null) {
                allocateDescriptorSets(target);
            }
        }
    }

    @Override
    public void updateDescriptorSets(DescriptorBuffers buffers, DescriptorSetTarget... targets) {
        ArrayList<DescriptorSet> descriptorSets = new ArrayList<>();
        ArrayList<DescriptorBufferInfo> bufferInfos = new ArrayList<>();
        for (DescriptorSetTarget target : targets) {
            BindBuffer buffer = buffers.getBuffer(target);
            if (buffer != null && !buffer.isDescriptorSetUpdated()) {
                long range = buffer.getDynamicSize() > 0 ? buffer.getDynamicSize() : buffer.getBuffer().size;
                bufferInfos.add(new DescriptorBufferInfo(buffer.getBuffer(), 0, range));
                descriptorSets.add(descriptors.getDescriptorSet(target));
                buffer.setDescriptorsetUpdated(true);
            }
        }
        backend.updateDescriptorSets(bufferInfos.toArray(new DescriptorBufferInfo[0]), descriptorSets.toArray(new DescriptorSet[0]));
    }

    @Override
    public GraphicsPipeline getPipeline(int pipelineHash) {
        GraphicsPipeline pipeline = pipelineByHash.get(pipelineHash);
        if (pipeline == null) {
            Logger.d(getClass(),
                    "Could not find pipeline for inputstate: " + pipelineHash);
            throw new IllegalArgumentException("Not implemented");
        }
        return pipeline;
    }

    @Override
    public GraphicsPipeline getPipeline(GraphicsShader.GraphicsSubtype shaderType) {
        GraphicsPipeline p = graphicsPipelineMap.get(shaderType.getName());
        if (p == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "No graphics pipeline for "
                    + shaderType.getName());
        }
        return p;
    }

    @Override
    public void cmdBindDescriptorSets(Subtype type, Queue queue, IntBuffer dynamicOffsets, DescriptorSetTarget target) {
        PipelineLayout layout = getPipelineLayout(type);
        DescriptorSet descriptorSet = descriptors.getDescriptorSet(target);
        PipelineBindPoint bindPoint = ShaderStageFlagBit.getPipelineBindPoint(target.getStageBits());
        switch (target.getSetType()) {
            case UNIFORM_TYPE:
            case STORAGE_BUFFER_TYPE:
                queue.cmdBindDescriptorSets(layout, descriptorSet, bindPoint, target.getSet(), dynamicOffsets);
                break;
            case TEXTURE_TYPE:
                queue.cmdBindDescriptorSets(layout, descriptorSet, bindPoint, target.getSet(), null);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", " + target.getSetType());
        }

    }

    @Override
    public void cmdPushConstants(PushConstants pushConstants, Queue queue, GraphicsPipeline pipeline) {
        PushConstantRange[] pushRanges = pushConstants.getPushConstantRange();
        if (pushRanges != null) {
            for (PushConstantRange pr : pushRanges) {
                queue.cmdPushConstants(pipeline.getLayout(), pr, pushConstants.getByteBuffer());
            }
        }
    }

    @Override
    public SpecializationInfo createSpecializationInfo(RenderableScene glTF,
            DescriptorBuffers<?> storageBuffers) {
        ByteBuffer bb = Buffers
                .createByteBuffer(SpecializationConstant.getTotalByteSize(GLTF2SpecializationConstant.values()));
        ArrayList<SpecializationMapEntry> entries =
                new ArrayList<PipelineShaderStageCreateInfo.SpecializationMapEntry>();
        int[] lights = glTF.getMaxPunctualLights();
        for (GLTF2SpecializationConstant constant : GLTF2SpecializationConstant.values()) {
            switch (constant) {
                case TEXTURE_TRANSFORM_COUNT:
                    BindBuffer buffer = storageBuffers.getBuffer(GltfDescriptorSetTarget.TEXTURE_TRANSFORM);
                    if (buffer != null) {
                        int count = buffer.getDynamicCount();
                        if (count > 0) {
                            entries.add(new SpecializationMapEntry(constant, bb, count));
                        }
                    }
                    break;
                case MATERIAL_COUNT:
                    int materialCount = glTF.getMaterialCount();
                    if (materialCount > 0) {
                        entries.add(new SpecializationMapEntry(constant, bb, materialCount));
                    }
                    break;
                case MATERIAL_SAMPLER_COUNT:
                    int materialSamplers = storageBuffers.getSetCount(GltfDescriptorSetTarget.MATERIAL_TEXTURE);
                    if (materialSamplers > 0) {
                        entries.add(new SpecializationMapEntry(constant, bb, materialSamplers));
                    }
                    break;
                case DIRECTIONAL_LIGHT_COUNT:
                    int directionalLights = Math.max(1, lights[Light.Type.directional.index]);
                    entries.add(new SpecializationMapEntry(constant, bb, directionalLights));
                    break;
                case POINT_LIGHT_COUNT:
                    int pointLights = Math.max(1, lights[Light.Type.point.index]);
                    entries.add(new SpecializationMapEntry(constant, bb, pointLights));
                    break;
                case MATRIX_COUNT:
                    int matrixCount = glTF.getMeshCount();
                    entries.add(new SpecializationMapEntry(constant, bb, matrixCount));
                    break;
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + constant);
            }
        }
        return new SpecializationInfo(entries.toArray(new SpecializationMapEntry[0]), bb);
    }

    @Override
    public void allocateDescriptorSets(DescriptorSetTarget target) {
        if (target == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        DescriptorSetLayout setLayout = descriptors.getDescriptorLayout(target);
        if (setLayout == null) {
            Logger.d(getClass(), "Warning - no descriptorlayout for target " + target);
        } else {
            descriptors.addDescriptorSet(target, renderer.getBackend().allocateDescriptorSet(setLayout));
        }
    }

    @Override
    public void freeDescriptorSets(DescriptorSetTarget target) {
        DescriptorSet set = descriptors.removeDescriptorSet(target);
        if (set != null) {
            renderer.getBackend().freeDescriptorSet(set);
        }
    }

    @Override
    public void deleteDescriptorLayout(DescriptorSetTarget target) {
        DescriptorSetLayout l = descriptors.removeDescriptorLayout(target);
        if (l == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", No layout for target " + target);
        }
        renderer.getBackend().destroyDescriptorSetLayout(l);
    }

    @Override
    public void createPipelineTextureLayout(TextureImages textureImages, int firstBinding) {
        if (descriptors.getDescriptorLayout(GltfDescriptorSetTarget.MATERIAL_TEXTURE) != null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + ", contains layout for target "
                            + GltfDescriptorSetTarget.MATERIAL_TEXTURE);
        }
        // Must create layout with at least 1 descriptor.
        // TODO - remove texture usage if layout is untextured.
        int textureDescriptors = textureImages.getDescriptorCount(SamplerType.sampler2DArray);
        descriptors.addDescriptorLayout(GltfDescriptorSetTarget.MATERIAL_TEXTURE,
                createTextureLayout(textureDescriptors > 0 ? textureDescriptors : 1, firstBinding));
        int cubemapDescriptors = textureImages.getDescriptorCount(SamplerType.samplerCubeArray);
        if (cubemapDescriptors > 0) {
            descriptors.addDescriptorLayout(GltfDescriptorSetTarget.CUBEMAP_TEXTURE,
                    createTextureLayout(cubemapDescriptors, firstBinding));
        }
    }

    @Override
    public void createDescriptorSetLayouts(DescriptorBuffers<?> buffers, DescriptorSetTarget... targets) {
        for (DescriptorSetTarget target : targets) {
            if ((descriptors.getDescriptorLayout(target) == null)) {
                ShaderStageFlagBit[] stageBits = target.getStageBits();
                DescriptorSetLayoutBinding binding = new DescriptorSetLayoutBinding(target.getBinding(), target.getDescriptorType(), buffers.getSetCount(target), stageBits, null);
                descriptors.addDescriptorLayout(target, renderer.getBackend().createDescriptorSetLayout(new DescriptorSetLayoutCreateInfo(null, binding)));
            } else {
                Logger.i(getClass(), "Reusing DescriptorSetLayoutBinding for target " + target);
            }
        }
    }

    @Override
    public DescriptorSetLayout createTextureLayout(int count, int firstBinding) {
        DescriptorSetLayoutBinding binding = new DescriptorSetLayoutBinding(firstBinding,
                DescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, count,
                ShaderStageFlagBit.getBitFlags(ShaderStageFlagBit.VK_SHADER_STAGE_FRAGMENT_BIT), null);
        return renderer.getBackend().createDescriptorSetLayout(
                new DescriptorSetLayoutCreateInfo(null, binding));
    }

    @Override
    public void createPushConstants(DescriptorBuffers<?> buffers, RenderPasses pass, int offset, int size,
            ShaderStageFlagBit... stageBits) {
        if (buffers.getPushConstants() != null) {
            throw new IllegalArgumentException();
        }
        PushConstantRange[] pushConstantRange = new PushConstantRange[] {
                new PushConstantRange(stageBits, offset, size) };
        buffers.setPushConstants(new PushConstants(pushConstantRange));
    }

    private void updateTextureDescriptorSets(TextureImages textureImages, SamplerType type) {
        DescriptorSet textureSet = descriptors.getDescriptorSet(type.target);
        TextureDescriptor[] textureDescriptorList = textureImages.getTextureDescriptors(type);
        if (textureSet != null) {
            if (textureDescriptorList.length > 0) {
                backend.updateDescriptorSets(textureDescriptorList, textureSet);
            }
        }
    }

    @Override
    public ComputePipeline getPipeline(Shader.Subtype shaderType) {
        return computePipelines.get(shaderType.getName());
    }

}

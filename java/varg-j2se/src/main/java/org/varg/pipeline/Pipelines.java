
package org.varg.pipeline;

import java.nio.IntBuffer;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.lib.ErrorMessage;
import org.varg.BackendException;
import org.varg.assets.TextureImages;
import org.varg.renderer.GltfRenderer.RenderPasses;
import org.varg.shader.ComputeShader;
import org.varg.shader.ComputeShader.ComputeShaderCreateInfo;
import org.varg.shader.GraphicsShader;
import org.varg.shader.GraphicsShader.GraphicsShaderCreateInfo;
import org.varg.shader.MeshShader;
import org.varg.shader.MeshShader.MeshShaderCreateInfo;
import org.varg.shader.RayTracingShader;
import org.varg.shader.RayTracingShader.RayTracingCreateInfo;
import org.varg.shader.Shader;
import org.varg.shader.Shader.Subtype;
import org.varg.uniform.DescriptorBuffers;
import org.varg.uniform.PushConstants;
import org.varg.uniform.StorageBuffers;
import org.varg.vulkan.IndirectDrawing;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;
import org.varg.vulkan.Vulkan10.DescriptorType;
import org.varg.vulkan.Vulkan10.ShaderStageFlagBit;
import org.varg.vulkan.descriptor.DescriptorSetLayout;
import org.varg.vulkan.pipeline.ComputePipeline;
import org.varg.vulkan.pipeline.GraphicsPipeline;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationInfo;

/**
 * Handles fetching, loading and destruction of pipelines that can be used to render glTF models.
 * Implementations of this interface will take care of creating and caching created pipelines.
 * The pipelines managed by this interface shall share the same texture and uniform descriptorsets, ie they
 * shall use the same PipelineLayout - this is to enable sharing of descriptorsets (texture and uniform buffers)
 *
 * Pipelines shall manage and allocate the memory needed for uniforms and vertex data - this is done using the
 * {@link InternalPipelines} interface.
 *
 */
public interface Pipelines<A extends RenderableScene> {

    interface DescriptorSetTarget {

        String getName();

        SetType getSetType();

        int getSet();

        int getBinding();

        DescriptorType getDescriptorType();

        DescriptorSetTarget[] getValues();

        BufferUsageFlagBit[] getBufferUsage();

        /**
         * Returns the shader stage flagbits
         * 
         * @return
         */
        ShaderStageFlagBit[] getStageBits();

        static DescriptorSetTarget[] sortBySet(DescriptorSetTarget... targets) {
            DescriptorSetTarget[] result = null;
            if (targets != null) {
                result = new DescriptorSetTarget[targets.length];
                for (int i = 0; i < targets.length; i++) {
                    DescriptorSetTarget target = targets[i];
                    if (result[target.getSet()] != null) {
                        throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Conflicting sets");
                    }
                    result[target.getSet()] = target;
                }
            }
            return result;
        }

        static int getHash(DescriptorSetTarget... targets) {
            final int prime = 31;
            int result = 1;
            for (DescriptorSetTarget t : targets) {
                if (t != null) {
                    result += prime * t.getName().hashCode();
                }
            }
            return result;
        }

    }

    enum SetType {
        UNIFORM_TYPE(),
        TEXTURE_TYPE(),
        STORAGE_BUFFER_TYPE();
    }

    interface InternalPipelines {

        /**
         * Creates the specialization info for the uniform buffers and textureimages.
         * 
         * @param glTF
         * @param storageBuffers
         * @return
         */
        SpecializationInfo createSpecializationInfo(RenderableScene glTF,
                DescriptorBuffers<?> storageBuffers);

        /**
         * Creates the pushconstants and adds to buffers
         * 
         * @param buffers Pushconstants are stored here
         * @param pass
         * @param size
         * 
         * @return
         * @throws IllegalArgumentException If pushconstants already have been created.
         */
        void createPushConstants(DescriptorBuffers<?> buffers, RenderPasses pass, int offset, int size,
                ShaderStageFlagBit... stageBits);

        /**
         * Allocate the descriptorsets needed for a specific target (frequency) - this is normally on a global,
         * matrix, material or texture level
         * 
         * @param target
         */
        void allocateDescriptorSets(@NonNull DescriptorSetTarget target);

        /**
         * Frees the descriptorsets for the specific target, if no descriptors exist for target then nothing is done.
         * 
         * @param target
         */
        void freeDescriptorSets(@NonNull DescriptorSetTarget target);

        /**
         * Creates the descriptorsetlayout for the targets
         * 
         * @param targets
         */
        void createDescriptorSetLayouts(@NonNull DescriptorBuffers<?> buffers, DescriptorSetTarget... targets);

        /**
         * Creates the pipeline texture layout - this layout will be used by all pipelines created
         * 
         * @param textureImages Texture images to create layouts for
         * @throws IllegalArgumentException If pipelinelayouts already have been created
         */
        void createPipelineTextureLayout(@NonNull TextureImages textureImages, int firstBinding);

        /**
         * Creates the descriptorsetlayout for the textures
         * 
         * @param number of descriptorsets to create
         * @param first binding
         * @return
         */
        DescriptorSetLayout createTextureLayout(int count, int firstBinding);

        /**
         * Deletes the descriptorsetlayout for the specified target.
         * 
         * @param target
         * @throws IllegalArgumentException If descriptorlayout for the target does not exist.
         */
        void deleteDescriptorLayout(@NonNull DescriptorSetTarget target);

    }

    /**
     * Creates the compute pipelines for the shader
     * 
     * @return The created compute shader
     * @throws BackendException
     */
    ComputeShader createComputePipelines(ComputeShaderCreateInfo shaderInfo, StorageBuffers buffers)
            throws BackendException;

    /**
     * Creates the graphics pipelines for the scene - this will load and compile the pipelines needed
     * for rendering all primitives in the model.
     * This will create bindings, layout and pipelines for the specified shader type.
     * Uniform buffers will be created and the data from backing buffers is updated.
     * Vertex buffer will be created and the data from glTF bufferViews will be updated.
     * Pipelines for other shader types shall be created on demand during runtime.
     * This is to avoid any uncecessary delay before render can start.
     * 
     * @param glTF
     * @param info
     * @throws IllegalStateException If {@link #init(JSONGltf, TextureImages)} has not been called prior to calling this
     * method.
     * @throws BackendException If pipeline could not be loaded and linked
     * 
     */
    void createGraphicsPipelines(IndirectDrawing[] drawCalls, @NonNull A glTF, @NonNull GraphicsShaderCreateInfo info)
            throws BackendException;

    /**
     * Creates the mesh pipeline for the shader, this will load and compile pipelines as needed for
     * rendering the shader.
     * call {@link #getPipeline(Subtype)} to get the pipeline
     * 
     * @param shaderInfo
     * @return The created mesh shader
     * @throws BackendException
     */
    MeshShader createMeshPipeline(MeshShaderCreateInfo shaderInfo) throws BackendException;

    /**
     * Creates a raytracing pipeline
     * 
     * @param shaderInfo
     * @return
     * @throws BackendException
     */
    RayTracingShader createRayTracingPipeline(RayTracingCreateInfo shaderInfo)
            throws BackendException;

    /**
     * Deletes all created graphicspipelines
     */
    void deleteGraphicsPipelines(@NonNull JSONGltf glTF);

    /**
     * Binds the descriptorsets for the target
     * 
     * @param queue
     * @param dynamicOffsets
     * @param target
     */
    void cmdBindDescriptorSets(@NonNull Subtype type, @NonNull Queue queue, IntBuffer dynamicOffsets,
            @NonNull DescriptorSetTarget target);

    /**
     * Records a command to push constants
     * 
     * @param pushConstants
     * @param queue
     * @param pipeline
     */
    void cmdPushConstants(@NonNull PushConstants pushConstants, @NonNull Queue queue,
            @NonNull GraphicsPipeline pipeline);

    /**
     * Fetches the graphics pipeline for the specified hash
     * 
     * @param pipelineHash
     * @return
     */
    GraphicsPipeline getPipeline(int pipelineHash);

    /**
     * Returns the graphics pipeline for the shader type
     * 
     * @param shaderType
     * @return
     */
    GraphicsPipeline getPipeline(GraphicsShader.GraphicsSubtype shaderType);

    /**
     * Returns the compute pipeline for the shader type
     * 
     * @param shaderType
     * @return
     */
    ComputePipeline getPipeline(Shader.Subtype shaderType);

    /**
     * Update the descriptorsets
     * 
     * @param buffers
     * @param targets
     */
    void updateDescriptorSets(DescriptorBuffers buffers, DescriptorSetTarget... targets);

}

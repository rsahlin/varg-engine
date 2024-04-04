
package org.varg.shader;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.renderer.Renderers;
import org.varg.shader.Gltf2GraphicsShader.GltfDescriptorSetTarget;
import org.varg.shader.Gltf2GraphicsShader.GraphicsShaderType;
import org.varg.shader.GraphicsShader.GraphicsShaderCreateInfo;
import org.varg.vulkan.extensions.KHRFragmentShadingRate.PipelineFragmentShadingRateStateCreateInfoKHR;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationInfo;
import org.varg.vulkan.pipeline.PipelineVertexInputState;

/**
 * Holds the buffers and methods needed for a specific graphics shader program.
 * This class is used for the traditional rendering pipeline, where vertex data is input into the pipeline and
 * processed in geometry, vertex and fragment stages.
 * 
 * The GraphicsShader does not know anything about renderpasses - that shall be handled by the renderer.
 * This is an internal interface and should normally not be used by clients - only by the implementation
 */
public interface GraphicsShader extends Shader<GraphicsShaderCreateInfo> {

    abstract class GraphicsShaderCreateInfo extends ShaderCreateInfo implements
            Shader.StorageBufferConsumer<GltfDescriptorSetTarget> {

        public final GraphicsShaderType graphicsShaderType;
        private PipelineFragmentShadingRateStateCreateInfoKHR fragmentShadingRate;

        public GraphicsShaderCreateInfo(@NonNull Renderers version, @NonNull GraphicsShaderType graphicsShaderType,
                @NonNull SpecializationInfo specializationInfo) {
            super(version, graphicsShaderType);
            this.graphicsShaderType = graphicsShaderType;
        }

        /**
         * Returns an empty instance of the graphics shader
         * 
         * @return
         */
        public abstract GraphicsShader getInstance(PipelineVertexInputState inputState);

        /**
         * Sets the fragmentshadingrate - to use the VK_KHR_fragment_shading_rate extension must be enabled
         * 
         * @return
         */
        public void setFragmentShadingRate(PipelineFragmentShadingRateStateCreateInfoKHR fragmentShadingRate) {
            this.fragmentShadingRate = fragmentShadingRate;
        }

        /**
         * Returns the fragmentshadingrate if specified, or null
         * 
         * @return
         */
        public PipelineFragmentShadingRateStateCreateInfoKHR getFragmentShadingRate() {
            return fragmentShadingRate;
        }

    }

    /**
     * Returns the pipeline vertex input for this shader.
     * 
     * @return
     */
    PipelineVertexInputState getPipelineVertexInputState();

}

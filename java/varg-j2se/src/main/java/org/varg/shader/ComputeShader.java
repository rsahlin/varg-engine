
package org.varg.shader;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.renderer.Renderers;
import org.varg.shader.ComputeShader.ComputeShaderCreateInfo;
import org.varg.vulkan.GLSLCompiler;
import org.varg.vulkan.GLSLCompiler.MacroIntProperties;

/**
 * Holds the state and methods needed for a specific compute shader program.
 * The {@link #ComputeShader()} does not know anything about renderpasses - that shall be handled by the renderer.
 * This is an internal interface and should normally not be used by clients - only by the implementation
 *
 */
public interface ComputeShader extends Shader<ComputeShaderCreateInfo> {

    int COMPUTE_BINDING = 0;

    /**
     * Returns the workgroupcounts for the shader
     * 
     * @return
     */
    int[] getWorkGroupCounts();

    abstract class ComputeShaderCreateInfo extends ShaderCreateInfo implements
            Shader.StorageBufferConsumer<DescriptorSetTarget> {

        protected ComputeShaderCreateInfo(@NonNull Renderers version, @NonNull Subtype shaderType) {
            super(version, shaderType);
        }

        /**
         * Returns an empty compute shader instance
         * 
         * @return
         */
        public abstract ComputeShader getInstance();

        /**
         * Returns the global workgroup count for x, y and z.
         * 
         * @return
         */
        public abstract int[] getWorkGroupCounts(Shader.Stage stage);

        /**
         * Returns the local workgroup invocations in up to 3 dimensions for the stage, or null if not used
         * 
         * @return Array with 1 to 3 dimensions, or null if not used.
         */
        public abstract int[] getLocalSizes(Shader.Stage stage);

        /**
         * Internal method
         * Sets local workgroup sizes
         * 
         * @param compiler
         * @param stages
         */
        protected void setMacros(GLSLCompiler compiler, Stage... stages) {
            for (Stage stage : stages) {
                int[] localSizes = getLocalSizes(stage);
                compiler.addMacro(new MacroIntProperties[] { MacroIntProperties.SIZE_X,
                        MacroIntProperties.SIZE_Y, MacroIntProperties.SIZE_Z }, localSizes,
                        stage);

            }
        }

    }

}

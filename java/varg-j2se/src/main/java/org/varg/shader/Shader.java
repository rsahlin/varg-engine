
package org.varg.shader;

import java.io.IOException;

import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.vulkan.Vulkan10.PolygonMode;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationInfo;

/**
 * The methods needed for a programmable stage of the pipeline - this is for a
 * generic shader, compute/graphics etc. Pipeline implementations shall take
 * care of loading, compiling and linking of shaders
 *
 */
public interface Shader<T extends ShaderCreateInfo> {

    interface Subtype {

        /**
         * The unique name
         */
        String getName();

        String getSourceName();

        String getFolder();

        Stage[] getStages();

        boolean hasStage(Stage stage);

        DescriptorSetTarget[] getTargets();

        /**
         * Returns the hash for the descriptorsetlayout
         * TODO - this could probably be removed since we always use the same descriptorlayout
         * 
         * @return
         */
        int getDescriptorSetLayoutHash();

    }

    interface GraphicsSubtype extends Subtype {
        /**
         * Returns the drawmode
         * 
         * @return
         */
        PolygonMode getPolygonMode();

    }

    interface StorageBufferConsumer<T extends DescriptorSetTarget> {

        /**
         * Returns the minimum buffersize, in bytes, for the target
         * 
         * @param target
         * @return
         */
        int getBufferSize(DescriptorSetTarget target);

        /**
         * Returns the minimum buffersize, in bytes, for the targets, this will iterate the descriptorSetTargets array
         * and call getBufferSize() on each
         * 
         * @param descriptorSetTargets
         * @return
         */
        int[] getBufferSizes(DescriptorSetTarget... descriptorSetTargets);

        /**
         * Returns the dynamic offsets used by targets
         * 
         * @param descriptorSetTargets
         * @return
         */
        int[] getDynamicOffsets(DescriptorSetTarget... descriptorSetTargets);
    }

    enum Stage {
        VERTEX("vert", "_vert"),
        GEOMETRY("geom", "_geom"),
        FRAGMENT("frag", "_frag"),
        COMPUTE("comp", "_comp"),
        MESH("mesh", "_mesh"),
        TASK("task", "_task"),
        RAYGEN("raygen", "_raygen"),
        ANYHIT("anyhit", "_anyhit"),
        CLOSESTHIT("closesthit", "_closesthit"),
        MISS("miss", "_miss"),
        INTERSECTION("intersection", "_intersection"),
        CALLABLE("callable", "_callable");

        public final String stage;
        public final String name;

        Stage(String stage, String name) {
            this.stage = stage;
            this.name = name;
        }
    }

    /**
     * Returns the empty and uncompiled shader binary that shall be used with this shader for the specified stage
     * 
     * @param stage shader stage
     * @return
     */
    ShaderBinary getShaderSource(Stage stage);

    /**
     * Returns the shaderinfo
     * 
     * @return
     */
    T getShaderInfo();

    /**
     * Loads the binaries to be used with this shader .
     * Binaries may only be loaded once.
     * Creates the shader modules
     * 
     * @param backend
     * @param pipelineHash
     * @throws IllegalStateException If binaries already have been loaded.
     * @throws IOException If there is an error loading binaries
     */
    void loadModules(Vulkan10Backend<?> backend, int pipelineHash) throws IOException;

    /**
     * Creates the shaderstageinfo - shader module must have been loaded/created
     * 
     * @param specializationInfo
     * @return
     * @throws IllegalArgumentException If shader modules have not been created for shaderType
     */
    PipelineShaderStageCreateInfo[] createShaderStageInfos(SpecializationInfo specializationInfo);

    /**
     * Returns true if this shader contains the specified stage.
     * 
     * @param stage
     * @return
     */
    boolean hasStage(Stage stage);

}

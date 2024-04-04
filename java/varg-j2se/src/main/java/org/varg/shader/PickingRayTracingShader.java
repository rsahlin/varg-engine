package org.varg.shader;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.lib.ErrorMessage;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.renderer.Renderers;
import org.varg.shader.RayTracingShader.RayTracingCreateInfo;
import org.varg.vulkan.GLSLCompiler;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.extensions.KHRRayTracingPipeline.RayTracingShaderGroupCreateInfoKHR;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationInfo;

public class PickingRayTracingShader extends BaseShaderImplementation<RayTracingCreateInfo>
        implements RayTracingShader {

    public static class PickingRayTracingShaderCreateInfo extends RayTracingCreateInfo {

        public PickingRayTracingShaderCreateInfo(@NonNull Renderers version, @NonNull Subtype shaderType) {
            super(version, shaderType);
        }

        @Override
        public int getBufferSize(DescriptorSetTarget target) {
            RayTracingDescriptorSetTarget rayTarget = RayTracingDescriptorSetTarget.get(target.getName());
            switch (rayTarget) {
                case DATA:
                    return 10000;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public int[] getDynamicOffsets(DescriptorSetTarget... descriptorSetTargets) {
            return null;
        }

        @Override
        public RayTracingShader getInstance() {
            return new PickingRayTracingShader(this);
        }

        @Override
        public void setMacros(GLSLCompiler compiler) {
            setMacros(compiler, Stage.RAYGEN);
        }

    }

    PickingRayTracingShader(PickingRayTracingShaderCreateInfo shaderInfo) {
        super(shaderInfo);
    }

    @Override
    public ShaderBinary getShaderSource(Stage stage) {
        switch (stage) {
            case FRAGMENT:
                VulkanMeshShaderBinary spirv = new VulkanMeshShaderBinary();
                return internalGetShaderSource(stage, getShaderInfo(), spirv);
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + stage);
        }
    }

    @Override
    public void loadModules(Vulkan10Backend<?> backend, int pipelineHash) throws IOException {
        internalLoadModules(backend, this, shaderInfo, pipelineHash);
    }

    @Override
    public PipelineShaderStageCreateInfo[] createShaderStageInfos(SpecializationInfo specializationInfo) {
        return createShaderStageInfos(shaderInfo, specializationInfo);
    }

    @Override
    public boolean hasStage(Stage stage) {
        return internalHasStage(stage);
    }

    @Override
    public RayTracingCreateInfo getShaderInfo() {
        return shaderInfo;
    }

    @Override
    public RayTracingShaderGroupCreateInfoKHR[] createRayTracingShaderGroupCreateInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}

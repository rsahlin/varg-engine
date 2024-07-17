
package org.varg.shader;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.BACKGROUND;
import org.gltfio.lib.ErrorMessage;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.renderer.Renderers;
import org.varg.shader.MeshShader.MeshShaderCreateInfo;
import org.varg.vulkan.GLSLCompiler;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationInfo;

public class BackgroundMeshShader extends BaseShaderImplementation<MeshShaderCreateInfo>
        implements MeshShader<MeshShaderCreateInfo> {

    public enum ShadeMode {
        BACK_CUBEMAP("BACK_CUBEMAP", "CUBEMAP"),
        BACK_SH("BACK_SH", "SH");

        public final String[] macros;

        ShadeMode(String... macros) {
            this.macros = macros;
        }

        public static ShadeMode get(String settingsStr) {
            if (settingsStr != null) {
                for (ShadeMode mode : values()) {
                    if (mode.name().equalsIgnoreCase(settingsStr)) {
                        return mode;
                    }
                }
            }
            return null;
        }

        protected static String[] getMacros(BACKGROUND bgHint) {
            switch (bgHint) {
                case CUBEMAP:
                    return ShadeMode.BACK_CUBEMAP.macros;
                case SH:
                    return ShadeMode.BACK_SH.macros;
                case CLEAR:
                    return null;
                default:
                    throw new IllegalArgumentException(bgHint.name());
            }
        }

    }

    public static class BackgroundMeshShaderCreateInfo extends MeshShaderCreateInfo {

        BACKGROUND backgroundHint;

        /**
         * @param version
         * @param shaderType
         */
        public BackgroundMeshShaderCreateInfo(@NonNull RenderableScene asset,
                @NonNull Renderers version, @NonNull MeshShaderType shaderType) {
            super(asset, version, shaderType);
            backgroundHint = asset.getEnvironmentExtension().getBackgroundHint();
        }

        @Override
        public int getBufferSize(DescriptorSetTarget target) {
            return 0;
        }

        @Override
        public int[] getBufferSizes(DescriptorSetTarget... descriptorSetTargets) {
            return null;
        }

        @Override
        public int[] getDynamicOffsets(DescriptorSetTarget... descriptorSetTargets) {
            return null;
        }

        @Override
        public MeshShader getInstance() {
            return new BackgroundMeshShader(this);
        }

        @Override
        public int[] getWorkGroupCounts(Stage stage) {
            switch (stage) {
                case MESH:
                    return new int[] { 1, 1, 1 };
                default:
                    throw new IllegalArgumentException(stage.name);
            }
        }

        @Override
        public int[] getLocalSizes(Stage stage) {
            switch (stage) {
                case MESH:
                    return new int[] { 1, 1, 1 };
                default:
                    throw new IllegalArgumentException(stage.name);
            }
        }

        @Override
        public void setMacros(GLSLCompiler compiler) {
            setMacros(compiler, Stage.MESH);
            compiler.setMacros(BaseShaderImplementation.ShaderProperties.values());
            // TODO This is a simplified way of setting the macros for background render
            // Perhaps they should be set same way as for graphics shader, reading environment from glTF?
            String[] bgMacros = ShadeMode.getMacros(backgroundHint);
            if (bgMacros != null) {
                for (String bgMacro : bgMacros) {
                    compiler.addMacro(bgMacro, "1", Stage.FRAGMENT, Stage.MESH);
                }
            }
        }
    }

    BackgroundMeshShader(BackgroundMeshShaderCreateInfo info) {
        super(info);
    }

    @Override
    public ShaderBinary getShaderSource(Stage stage) {
        switch (stage) {
            case MESH:
            case FRAGMENT:
                VulkanShaderBinary spirv = new VulkanShaderBinary();
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
    public MeshShaderCreateInfo getShaderInfo() {
        return shaderInfo;
    }

}

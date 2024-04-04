package org.varg.shader;

import java.io.IOException;

import org.gltfio.lib.ErrorMessage;
import org.varg.spirv.SpirvBinary;
import org.varg.vulkan.GLSLCompiler;

public class VulkanComputeShaderBinary extends ShaderBinary {

    public VulkanComputeShaderBinary() {
    }

    @Override
    public void compileShader(ShaderCreateInfo shaderInfo, String outputHash) throws IOException {
        if (data != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Shader binary already present");
        }
        SpirvBinary binary = GLSLCompiler.getInstance(shaderInfo.version).compileStage(getSourcePath(
                shaderInfo.shaderType), getSourceName(shaderInfo.shaderType), getStage(),
                getSourcePath(shaderInfo.shaderType) + COMPILED_DIRECTORY, outputHash);
        data = binary.spirv;
    }

}

package org.varg.shader;

import java.io.IOException;

import org.gltfio.lib.ErrorMessage;
import org.varg.spirv.SpirvBinary;
import org.varg.vulkan.GLSLCompiler;

/**
 * Implementation of vulkan shader binary - this will compile shader binary using data from shaderInfo and save in default directory.
 */
public class VulkanShaderBinary extends ShaderBinary {

    public VulkanShaderBinary() {
    }

    @Override
    public void compileShader(ShaderCreateInfo shaderInfo, String outputHash) throws IOException {
        if (data != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Shader binary already present");
        }
        SpirvBinary binary =
                GLSLCompiler.getInstance(shaderInfo.version).compileStage(getSourcePath(shaderInfo.shaderType), getSourceName(shaderInfo.shaderType), getStage(), getSourcePath(shaderInfo.shaderType) + COMPILED_DIRECTORY, outputHash);
        if (binary != null) {
            data = binary.spirv;
        } else {
            loadShader(shaderInfo, outputHash);
        }
    }

}

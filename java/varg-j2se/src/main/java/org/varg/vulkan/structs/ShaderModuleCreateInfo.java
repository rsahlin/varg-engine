
package org.varg.vulkan.structs;

import org.varg.shader.ShaderBinary;

/**
 * Wrapper for VkShaderModuleCreateInfo
 *
 */
public class ShaderModuleCreateInfo {

    private ShaderBinary binary;

    public ShaderModuleCreateInfo(ShaderBinary code) {
        binary = code;
    }

}

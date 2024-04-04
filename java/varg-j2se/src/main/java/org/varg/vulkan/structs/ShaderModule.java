
package org.varg.vulkan.structs;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Wrapper for VkShaderModule
 *
 */
public class ShaderModule extends Handle<ShaderModuleCreateInfo> {

    public ShaderModule(long shaderModule, @NonNull ShaderModuleCreateInfo info) {
        super(shaderModule, info);
    }

}

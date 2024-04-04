package org.varg.shader;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.renderer.Renderers;
import org.varg.shader.Shader.Subtype;
import org.varg.vulkan.GLSLCompiler;

public abstract class ShaderCreateInfo {

    public final Subtype shaderType;
    public final Renderers version;

    ShaderCreateInfo(@NonNull Renderers version, @NonNull Subtype shaderType) {
        this.shaderType = shaderType;
        this.version = version;
    }

    /**
     * Sets specific compiler macros
     * 
     * @param compiler
     */
    public abstract void setMacros(GLSLCompiler compiler);

}

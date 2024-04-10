
package org.varg.renderer;

import org.gltfio.gltf2.JSONPrimitive.DrawMode;

/**
 * Renderer configuration, this is the runtime configuration parameters.
 * 
 * @author Richard Sahlin
 *
 */
public class Configuration {

    private static final Configuration CONFIGURATION = new Configuration();

    private boolean useVBO = true;
    private boolean generateMipMaps = true;
    /**
     * Read by gltfnode renderer to allow force setting of a specific drawmode
     */
    private DrawMode forceGLTFMode = null;

    /**
     * Returns the instance of the Configuration, this will always be the same.
     * 
     * @return The configuration
     */
    public static Configuration getInstance() {
        return CONFIGURATION;
    }

    /**
     * Sets the VBO configuration, if true then buffer objects are used instead of passing
     * Buffer to glVertexAttribPointer()
     * Note that passing Buffer instead of allocated buffer objects, using glGenBuffers(), is considered
     * to be the _old_way.
     * Future versions of GL will remove this and rely on using VBO (or similar) - this has already
     * been done in JOGL.
     * 
     * @param vbo True to use VBO
     */
    public void setUseVBO(boolean vbo) {
        useVBO = vbo;
    }

    /**
     * Returns the state of the VBO flag, if true then VBO should be used.
     * 
     * @return True to use VBO, false otherwise.
     */
    public boolean isUseVBO() {
        return useVBO;
    }

    /**
     * Returns true if generation of mipmaps should be forced.
     * 
     * @return
     */
    public boolean isGenerateMipMaps() {
        return generateMipMaps;
    }

    /**
     * If a specific gl drawmode shall be used when rendering GLTF it is set here.
     * This is for debugging / visualization
     * 
     * @return The gl mode to use when rendering gltf
     */
    public DrawMode getGLTFMode() {
        return forceGLTFMode;
    }

    /**
     * Sets the gl mode to use when rendering gltf content, use for debugging / visualization
     * 
     * @param forceMode or null to remove any forced gl mode
     */
    public void setGLTFMode(DrawMode forceMode) {
        forceGLTFMode = forceMode;
    }

}

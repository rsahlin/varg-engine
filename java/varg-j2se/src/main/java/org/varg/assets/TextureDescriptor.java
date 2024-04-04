
package org.varg.assets;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Texture descriptor abstraction
 * Encapsulates platform sampler, texture image and layout
 * This is the information needed to update texturedescriptors
 */
public abstract class TextureDescriptor {

    protected final TextureMemory texture;

    protected TextureDescriptor(@NonNull TextureMemory textureMem) {
        texture = textureMem;
    }

    /**
     * Returns the texture memory
     * 
     * @return
     */
    public TextureMemory getTexture() {
        return texture;
    }

}


package org.varg.assets;

import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.ErrorMessage;

/**
 * Collection of TextureDescriptors that are used for a material
 * This class can help keep track of materials that use the same texture descriptors.
 *
 */
public class MaterialTextures<T extends TextureDescriptor> {

    private final T[] textureDescriptors;
    private final Channel[] textureChannels;

    public MaterialTextures(T[] descriptors, Channel[] channels) {
        if (descriptors == null || channels == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        textureDescriptors = descriptors;
        textureChannels = channels;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // Use flags values instead of array check
        result = prime * result + BitFlags.getFlagsValue(textureChannels);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        MaterialTextures other = (MaterialTextures) obj;
        // Use flags values instead of array check
        if (BitFlags.getFlagsValue(textureChannels) != BitFlags.getFlagsValue(other.textureChannels)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the array of texture descriptors - do NOT modify
     * 
     * @return Array of texturedescriptors
     */
    public T[] getTextureDescriptors() {
        return textureDescriptors;
    }

}


package org.varg.assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONTexture;
import org.gltfio.gltf2.JSONSampler;
import org.gltfio.lib.ErrorMessage;
import org.varg.shader.Gltf2GraphicsShader.GltfDescriptorSetTarget;
import org.varg.vulkan.memory.ImageMemory;

/**
 * The texture images and samplers used for a glTF model.
 * Texture samplers are accessed using TextureDescriptor, texture descriptors are referenced on a per material basis.
 * 
 *
 */
public class TextureImages {

    public enum SamplerType {
        sampler2DArray(GltfDescriptorSetTarget.MATERIAL_TEXTURE),
        samplerCubeArray(GltfDescriptorSetTarget.CUBEMAP_TEXTURE);

        public final GltfDescriptorSetTarget target;

        SamplerType(GltfDescriptorSetTarget target) {
            this.target = target;
        }

    }

    /**
     * Containing data for texture memory and target layer, this is used when multiple textures with same
     * size use the same texture memory with different layer pointers.
     *
     */
    public static class TextureImageInfo {
        public final ImageMemory texture;
        public final byte layer;

        private TextureImageInfo(ImageMemory texture, byte layer) {
            this.texture = texture;
            this.layer = layer;
        }

        @Override
        public String toString() {
            return "Layer " + layer + ", texture dimension " + texture.width + ", "
                    + texture.height;
        }
    }

    /**
     * glTF Sampler mapping to a Vulkan sampler (descriptor)
     * Samplers are accessed using index into descriptor array
     *
     */
    public static class TextureSamplerInfo extends TextureImageInfo {

        final JSONSampler sampler;
        final SamplerType type;
        private byte descriptorIndex = -1;

        public TextureSamplerInfo(@NonNull JSONSampler samp, @NonNull SamplerType sampType,
                @NonNull TextureImageInfo texInfo) {
            super(texInfo.texture, texInfo.layer);
            sampler = samp;
            type = sampType;
        }

        /**
         * Returns the index to the descriptor
         * 
         * @return
         */
        public byte getDescriptorIndex() {
            return descriptorIndex;
        }

        /**
         * Returns the hashcode that is unique for size, format and sampler wrapmode
         * 
         * @return
         */
        protected int getTextureHash() {
            final int prime = 31;
            int result = 1;
            result = prime * result + texture.hashCode();
            result = prime * result + sampler.getWrapHashCode();
            result = prime * result + type.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Descriptorindex " + descriptorIndex + ", " + super.toString() + ", Sampler: " + sampler.getWrapS()
                    + ", " + sampler.getWrapT();
        }

    }

    /**
     * Lookup for textureimageinfo from source image id
     */
    private HashMap<String, TextureImageInfo> textureImages = new HashMap<String, TextureImages.TextureImageInfo>();
    private HashMap<SamplerType, List<TextureDescriptor>> textureDescriptorMap =
            new HashMap<SamplerType, List<TextureDescriptor>>();
    private HashMap<Long, TextureMemory> textureMemoryMap = new HashMap<Long, TextureMemory>();
    private HashMap<Integer, TextureSamplerInfo> samplerMap = new HashMap<Integer, TextureImages.TextureSamplerInfo>();
    private HashMap<Integer, TextureSamplerInfo> samplerHashMap =
            new HashMap<Integer, TextureImages.TextureSamplerInfo>();

    /**
     * Registers the texture memory
     * 
     * @param memory
     * @throws IllegalArgumentException if memory already has been added
     */
    protected void addTextureMemory(TextureMemory memory) {
        if (textureMemoryMap.containsKey(memory.getMemoryPointer())) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + "Already added memory " + memory.getMemoryPointer());
        }
        textureMemoryMap.put(memory.getMemoryPointer(), memory);
    }

    /**
     * Returns the number of texturememory, added by calling {@link #addTextureMemory(TextureMemory)}
     * 
     * @return
     */
    protected int getTextureMemoryCount() {
        return textureMemoryMap.size();
    }

    /**
     * Registers a texture image to be used with texturememory, storing TextureImageInfo with sourceId
     * 
     * @param sourceId
     * @param texture
     * @param layer
     * @throws IllegalArgumentException If an image already has been registered with sourceId
     * @throws IllegalArgumentException If texture has not been registered by calling
     * {@link #addTextureMemory(TextureMemory)}
     */
    protected void addTextureImage(String sourceId, ImageMemory texture, byte layer) {
        if (textureImages.containsKey(sourceId)) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + " Already contains texture for source image id " + sourceId);
        } else if (!textureMemoryMap.containsKey(texture.getMemoryPointer())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", TextureMemory not registered");
        }
        textureImages.put(sourceId, new TextureImageInfo(texture, layer));
    }

    /**
     * Returns the number of texture images registered by calling {@link #addTextureImage(String, TextureMemory, int)}
     * 
     * @return
     */
    protected int getTextureImageCount() {
        return textureImages.size();
    }

    /**
     * Returns the texture image info for the texture created for source glTF image id
     * 
     * @param sourceId
     * @return
     */
    public TextureImageInfo getTextureImage(String sourceId) {
        return textureImages.get(sourceId);
    }

    /**
     * Adds a texturedescriptor and connects it to the samplerInfo.
     * 
     * @throws IllegalArgumentException If texturedescriptor already exists for samplerInfo using hash.
     */
    protected void addTextureDescriptor(TextureSamplerInfo samplerInfo, TextureDescriptor descriptor) {
        int hash = samplerInfo.getTextureHash();
        if (samplerHashMap.containsKey(hash)) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message
                    + ", already contains TextureDescriptor for sampler hash " + hash);
        }
        List<TextureDescriptor> textureDescriptors = textureDescriptorMap.get(samplerInfo.type);
        if (textureDescriptors == null) {
            textureDescriptors = new ArrayList<TextureDescriptor>();
            textureDescriptorMap.put(samplerInfo.type, textureDescriptors);
        }
        samplerInfo.descriptorIndex = (byte) textureDescriptors.size();
        textureDescriptors.add(descriptor);
        samplerHashMap.put(hash, samplerInfo);
    }

    /**
     * Returns the sampler info, register by calling {@link #addTextureSamplerInfo(int, TextureSamplerInfo)}
     * 
     * @param samplerInfo
     * @return sampler info or null
     */
    protected TextureSamplerInfo getSamplerInfo(TextureSamplerInfo samplerInfo) {
        return samplerHashMap.get(samplerInfo.getTextureHash());
    }

    /**
     * Registers the texture sampler info.
     * 
     * @param id
     * @param samplerInfo
     * @throws IllegalArgumentException If id is already registered
     */
    protected void addTextureSamplerInfo(int id, TextureSamplerInfo samplerInfo) {
        if (samplerMap.containsKey(id)) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + ", contains texturesamplerinfo for id " + id);
        }
        TextureSamplerInfo current = getSamplerInfo(samplerInfo);
        if (current == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message);
        }
        byte descriptorIndex = current.descriptorIndex;
        if (samplerInfo.descriptorIndex < 0) {
            samplerInfo.descriptorIndex = descriptorIndex;
        }
        if (samplerInfo.descriptorIndex != descriptorIndex || descriptorIndex < 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", descriptorIndexes do not match "
                    + samplerInfo.descriptorIndex + " vs " + descriptorIndex);
        }
        samplerMap.put(id, samplerInfo);
    }

    /**
     * Returns texture sampler info for the (material) textures
     * 
     * @param texture
     * @return
     */
    public TextureSamplerInfo getSamplerInfo(JSONTexture texture) {
        return samplerMap.get(texture.getId());
    }

    /**
     * Returns the array with Texturedescriptors to be used when updating the texture descriptorsets
     * 
     * @return
     */
    public TextureDescriptor[] getTextureDescriptors(SamplerType type) {
        List<TextureDescriptor> descriptors = new ArrayList<TextureDescriptor>();
        List<TextureDescriptor> textureDescriptors = textureDescriptorMap.get(type);
        if (textureDescriptors != null) {
            descriptors.addAll(textureDescriptors);
        }
        return descriptors.toArray(new TextureDescriptor[0]);
    }

    /**
     * Returns the array of texture memory
     * 
     * @return
     */
    public TextureMemory[] getTextureMemory() {
        return textureMemoryMap.values().toArray(new TextureMemory[0]);
    }

    /**
     * Returns the number of texture descriptors
     * 
     * @return
     */
    public int getDescriptorCount(SamplerType... type) {
        int count = 0;
        for (SamplerType t : type) {
            List<TextureDescriptor> textureDescriptors = textureDescriptorMap.get(t);
            count += textureDescriptors != null ? textureDescriptors.size() : 0;
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("TextureDescriptors and TextureImages:\n");
        for (SamplerType type : SamplerType.values()) {
            TextureDescriptor[] textureDescriptors = getTextureDescriptors(type);
            for (TextureDescriptor textureDescriptor : textureDescriptors) {
                result.append(textureDescriptor.toString() + "\n");
            }
        }
        return result.toString();
    }

    /**
     * Cleanup
     */
    public void destroy() {
        textureImages.clear();
        textureDescriptorMap.clear();
        textureMemoryMap.clear();
        textureImages = null;
        textureDescriptorMap = null;
        textureMemoryMap = null;
    }

}


package org.varg.assets;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Settings;
import org.ktximageio.ktx.ImageBuffer;
import org.ktximageio.ktx.ImageBuffer.ImageBufferInfo;
import org.ktximageio.ktx.KTX.TextureType;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.VulkanBackend.BackendProperties;

/**
 * Holds the asset source images
 * Use this when glTF textures are to be created - discard when textures have been created.
 *
 */
public class SourceImages {

    /**
     * Holds an array of source image buffers per glTF, key is the glTF asset id.
     */
    private final HashMap<@NonNull Integer, @NonNull VulkanImageBuffer[]> sourceImages =
            new HashMap<Integer, VulkanImageBuffer[]>();

    public static class SourceImageBufferInfo extends ImageBufferInfo {

        public final int mipLevels;

        private SourceImageBufferInfo(int width, int height, int arrayLayers, Vulkan10.Format format,
                int mips) {
            super(width, height, arrayLayers, format.value);
            mipLevels = mips;
        }

        /**
         * Returns the size of one layer of the image, in bytes
         * THis will return width * height * format.sizeInBytes
         * 
         * @return
         */
        protected int getLayerSizeInBytes() {
            return width * height * format.sizeInBytes;
        }

    }

    /**
     * Container for imagebuffer and the destination texture format
     * This is used as a bridge between the loaded image (buffer) and the target texture.
     * Component layout and format may change from the loaded image.
     *
     */
    public static class VulkanImageBuffer {
        public final ImageBuffer sourceImageBuffer;
        public final Vulkan10.Format textureFormat;
        public final String sourceId;
        private int mipLevels = -1;

        VulkanImageBuffer(@NonNull ImageBuffer img, Vulkan10.Format format, String id) {
            sourceImageBuffer = img;
            textureFormat = format;
            sourceId = id;
        }

        /**
         * Returns the imagebuffer info - using the widht, height and target texture format.
         * 
         * @return
         */
        public SourceImageBufferInfo getInfo() {
            return new SourceImageBufferInfo(sourceImageBuffer.width, sourceImageBuffer.height,
                    sourceImageBuffer.faceCount, textureFormat, mipLevels);
        }

        /**
         * Internal method
         * 
         * @param levels
         */
        public void setMipLevels(int levels) {
            this.mipLevels = levels;
        }

        /**
         * Returns the number of miplevels
         * 
         * @return
         */
        public int getMipLevels() {
            return mipLevels;
        }

    }

    /**
     * Returns the imagebuffers for the asset or null
     * 
     * @param asset
     * @return
     */
    public VulkanImageBuffer[] get(RenderableScene asset, TextureType... types) {
        if (types == null || types.length == 0) {
            return sourceImages.get(asset.getId());
        }
        if (sourceImages.size() > 0) {
            ArrayList<VulkanImageBuffer> result = new ArrayList<>();
            VulkanImageBuffer[] images = sourceImages.get(asset.getId());
            for (VulkanImageBuffer vib : images) {
                if (vib.getInfo().getTextureType().isType(types)) {
                    result.add(vib);
                }
            }
            return result.toArray(new VulkanImageBuffer[0]);
        }
        return null;
    }

    /**
     * Returns the number of images for the asset
     * 
     * @param asset
     * @return
     */
    public int getImageCount(RenderableScene asset) {
        VulkanImageBuffer[] images = sourceImages.get(asset.getId());
        return images != null ? images.length : 0;
    }

    /**
     * Adds the image buffer array, storing with glTF as key - fetch by calling {@link #get(RenderableScene)}
     * 
     * @param asset
     * @param imageBuffers
     * @throws IllegalArgumentException If already contains imagebuffers for the glTF
     */
    public void add(RenderableScene asset, VulkanImageBuffer[] imageBuffers) {
        if (imageBuffers != null && imageBuffers.length > 0) {
            if (sourceImages.containsKey(asset.getId())) {
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_STATE.message + ", already contains imagebuffers for asset " + asset
                                .getId());
            }
            sourceImages.put(asset.getId(), imageBuffers);
        }
    }

    /**
     * Removes the imagebuffers for the asset
     * 
     * @param asset
     */
    public void remove(RenderableScene asset) {
        VulkanImageBuffer[] images = sourceImages.get(asset.getId());
        if (images != null) {
            if (!Settings.getInstance().getBoolean(BackendProperties.KEEP_SOURCE_IMAGES)) {
                // Todo - cleanup be deleting bytebuffers?
                for (VulkanImageBuffer vib : images) {
                    vib.sourceImageBuffer.destroy();
                }
                sourceImages.remove(asset.getId());
            }
        }
    }

}

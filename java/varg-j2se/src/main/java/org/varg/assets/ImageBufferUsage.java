
package org.varg.assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import org.gltfio.lib.ErrorMessage;
import org.ktximageio.ktx.ImageBuffer;
import org.varg.assets.SourceImages.SourceImageBufferInfo;
import org.varg.assets.SourceImages.VulkanImageBuffer;

/**
 * The images needed for textures in a glTF asset - this shall only be used as a first step when
 * figuring out how many different texture sizes and formats are needed.
 *
 */
class ImageBufferUsage {

    /**
     * A number of image buffers with same dimension and format
     *
     */
    class ImageBufferList {

        private final List<DynamicImageBuffer> dynamicImageBuffers = new ArrayList<DynamicImageBuffer>();

        /**
         * Adds an imagebuffer - if imagebuffer already present the new buffer must match the existing
         * 
         * @param buffer
         * @throws IllegalArgumentException If buffer does not match already added imagebuffer
         */
        protected void addImageBuffer(DynamicImageBuffer buffer) {
            if (dynamicImageBuffers.size() > 0) {
                if (!dynamicImageBuffers.get(0).buffer.getInfo().equals(buffer.buffer.getInfo())) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", "
                            + dynamicImageBuffers.get(0).buffer.getInfo().toString() + ", does not match "
                            + buffer.buffer.getInfo().toString());
                }
            }
            dynamicImageBuffers.add(buffer);
        }

        protected ImageBuffer[] getImageBuffers() {
            ImageBuffer[] result = new ImageBuffer[dynamicImageBuffers.size()];
            int index = 0;
            for (DynamicImageBuffer buffer : dynamicImageBuffers) {
                result[index++] = buffer.buffer.sourceImageBuffer;
            }
            return result;
        }

        protected int getDynamicImageBufferCount() {
            return dynamicImageBuffers.size();
        }

        protected DynamicImageBuffer getDynamicImageBuffer(int index) {
            return dynamicImageBuffers.get(index);
        }

        protected void destory() {
            dynamicImageBuffers.clear();
        }
    }

    /**
     * An imagebuffer and the glTF image index of the source image.
     *
     */
    class DynamicImageBuffer {

        /**
         * The id of the source (glTF) image
         */
        public final String sourceId;
        public final VulkanImageBuffer buffer;

        /**
         * 
         * @param buff The image buffer
         * @param id The id of the glTF source image
         */
        protected DynamicImageBuffer(@NonNull VulkanImageBuffer buff, String id) {
            buffer = buff;
            sourceId = id;
        }
    }

    private HashMap<@NonNull SourceImageBufferInfo, @NonNull ImageBufferList> imageByInfo =
            new HashMap<SourceImageBufferInfo, ImageBufferList>();
    private Set<String> imageById = new HashSet<String>();

    protected void addTextureImage(VulkanImageBuffer image) {
        // Store by size and format
        SourceImageBufferInfo info = image.getInfo();
        ImageBufferList images = imageByInfo.get(info);
        if (images == null) {
            images = new ImageBufferList();
            imageByInfo.put(info, images);
        }
        DynamicImageBuffer dynamicImage = new DynamicImageBuffer(image, image.sourceId);
        images.addImageBuffer(dynamicImage);
        imageById.add(image.sourceId);
    }

    protected boolean containsImage(String sourceId) {
        return imageById.contains(sourceId);
    }

    /**
     * Returns the number of image arrays needed to store the different image sources, this will be one
     * array per size and format
     * 
     * @return
     */
    protected int getImageArrayCount() {
        return imageByInfo.size();
    }

    protected SourceImageBufferInfo[] getImageBufferInfos() {
        return imageByInfo.keySet().toArray(new SourceImageBufferInfo[0]);
    }

    protected ImageBufferList removeDynamicImageBuffers(SourceImageBufferInfo info) {
        return imageByInfo.remove(info);
    }

    protected ImageBufferList getDynamicImageBuffers(SourceImageBufferInfo info) {
        return imageByInfo.get(info);
    }

    protected void destroy() {
        imageByInfo.clear();
        imageByInfo = null;
        imageById.clear();
        imageById = null;
    }

    /**
     * 
     * @return
     */
    protected int getStagingBufferRequirement() {
        int size = 0;
        for (SourceImageBufferInfo info : imageByInfo.keySet()) {
            size = Math.max(size, info.getLayerSizeInBytes());
        }
        return size;
    }

}

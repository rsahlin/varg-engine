
package org.varg.assets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONImage;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONTexture;
import org.gltfio.gltf2.JSONTexture.ComponentMapping;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.extensions.EnvironmentMap;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.KHREnvironmentMap;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.gltfio.lib.ThreadService;
import org.ktximageio.ktx.ByteArrayImageBuffer;
import org.ktximageio.ktx.ImageBuffer;
import org.ktximageio.ktx.ImageHeader;
import org.ktximageio.ktx.ImageReader.ColorSpace;
import org.ktximageio.ktx.ImageReader.MimeFormat;
import org.ktximageio.ktx.ImageUtils.ArrayToLinearRunnable;
import org.ktximageio.ktx.ImageUtils.ArrayToLinearRunnableInt;
import org.ktximageio.ktx.ImageUtils.ArrayToLinearRunnableShort;
import org.ktximageio.ktx.IntArrayImageBuffer;
import org.ktximageio.ktx.KTX.TextureType;
import org.ktximageio.ktx.ShortArrayImageBuffer;
import org.varg.assets.ImageBufferUsage.DynamicImageBuffer;
import org.varg.assets.ImageBufferUsage.ImageBufferList;
import org.varg.assets.SourceImages.SourceImageBufferInfo;
import org.varg.assets.SourceImages.VulkanImageBuffer;
import org.varg.assets.TextureImages.SamplerType;
import org.varg.assets.TextureImages.TextureImageInfo;
import org.varg.assets.TextureImages.TextureSamplerInfo;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.shader.Shader.StorageBufferConsumer;
import org.varg.shader.Shader.Subtype;
import org.varg.shader.ShaderCreateInfo;
import org.varg.uniform.BindBuffer;
import org.varg.uniform.DescriptorBuffers;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;
import org.varg.vulkan.Vulkan10.Filter;
import org.varg.vulkan.Vulkan10.FormatFeatureFlagBits;
import org.varg.vulkan.Vulkan10.GLFilter;
import org.varg.vulkan.Vulkan10.GLWrapMode;
import org.varg.vulkan.Vulkan10.ImageAspectFlagBit;
import org.varg.vulkan.Vulkan10.ImageCreateFlagBits;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.ImageTiling;
import org.varg.vulkan.Vulkan10.ImageType;
import org.varg.vulkan.Vulkan10.ImageUsageFlagBits;
import org.varg.vulkan.Vulkan10.ImageViewType;
import org.varg.vulkan.Vulkan10.MemoryPropertyFlagBit;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.Vulkan12.MemoryAllocateFlagBits;
import org.varg.vulkan.VulkanBackend;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;
import org.varg.vulkan.VulkanBackend.FloatProperties;
import org.varg.vulkan.descriptor.DescriptorImageInfo;
import org.varg.vulkan.image.ImageCreateInfo;
import org.varg.vulkan.image.ImageSubresourceLayers;
import org.varg.vulkan.image.ImageSubresourceRange;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.image.ImageViewCreateInfo;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.ImageMemory;
import org.varg.vulkan.memory.Memory;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.memory.VertexMemory;
import org.varg.vulkan.structs.Extent3D;
import org.varg.vulkan.structs.FormatProperties;
import org.varg.vulkan.structs.Sampler;
import org.varg.vulkan.structs.SamplerCreateInfo;

/**
 * Implementation of Assets interface for Vulkan
 *
 */
public class VulkanAssets implements Assets {

    public static final float MIPMAP_PERCENT = 0.34f;
    private final Vulkan10Backend<?> backend;
    private final DeviceMemory deviceMemory;
    private final Queue queue;
    private HashMap<@NonNull Integer, @NonNull Sampler> samplers = new HashMap<Integer, Sampler>();
    private HashMap<Integer, TextureImages> textureImagesMap = new HashMap<Integer, TextureImages>();
    @Deprecated
    private HashMap<Integer, VertexMemory> vertexBuffersMap = new HashMap<Integer, VertexMemory>();
    private HashMap<String, DescriptorBuffers<?>> storageBuffersMap = new HashMap<>();

    private final SourceImages sourceImages = new SourceImages();

    public VulkanAssets(Vulkan10Backend<?> vulkanBack, DeviceMemory memoryHandler, Queue q) {
        if (vulkanBack == null || memoryHandler == null || q == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        backend = vulkanBack;
        deviceMemory = memoryHandler;
        queue = q;
    }

    private static class ImageLoadRunnable implements Runnable {

        private final Assets assets;
        private final RenderableScene asset;
        private final JSONImage image;
        private volatile ImageBuffer[] imageBuffers;
        private volatile ColorSpace[] sourceColorSpace;
        private final int index;
        private final Semaphore semaphore;

        private ImageLoadRunnable(Assets assetHandler, ImageBuffer[] imgBuffers, ColorSpace[] sCSpace, int i, Semaphore sem, RenderableScene a, JSONImage img) {
            assets = assetHandler;
            asset = a;
            image = img;
            imageBuffers = imgBuffers;
            sourceColorSpace = sCSpace;
            index = i;
            semaphore = sem;
        }

        @Override
        public void run() {
            try {
                sourceColorSpace[index] = image.isSRGB() ? ColorSpace.SRGB : ColorSpace.LINEAR;
                imageBuffers[index] = assets.createImage(asset, image);
                semaphore.release();
            } catch (Throwable t) {
                semaphore.release();
                Logger.d(getClass(), "Exception reading image " + image.getUri() + " : " + t.toString());
                t.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() {
        throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
    }

    private void calculateTextureUsage(RenderableScene asset, ImageBufferUsage imageBufferUsage) {
        JSONTexture[] textures = asset.getTextures();
        VulkanImageBuffer[] imageBuffers = sourceImages.get(asset, TextureType.TYPE_2D, TextureType.TYPE_2D_ARRAY);
        if (textures != null) {
            for (JSONTexture texture : textures) {
                if (!imageBufferUsage.containsImage(texture.getSource().getSourceId())) {
                    VulkanImageBuffer img = imageBuffers[texture.getSourceIndex()];
                    imageBufferUsage.addTextureImage(img);
                }
            }
        }
        Logger.d(getClass(), "Created textures use " + imageBufferUsage.getImageArrayCount() + " image arrays");
    }

    private void calculateImageUsage(RenderableScene asset, VulkanImageBuffer[] images,
            ImageBufferUsage imageBufferUsage) {
        if (images != null && images.length > 0) {
            for (VulkanImageBuffer vib : images) {
                if (!imageBufferUsage.containsImage(vib.sourceId)) {
                    imageBufferUsage.addTextureImage(vib);
                }
            }
        }
    }

    @Override
    public void loadSourceImages(RenderableScene asset) throws IOException {
        if (sourceImages.get(asset, TextureType.TYPE_2D) != null) {
            throw new IllegalStateException(ErrorMessage.INVALID_STATE.message + ", already loaded images for glTF " + asset.getId());
        }
        sourceImages.add(asset, loadImages(asset, (KHREnvironmentMap) asset.getExtension(ExtensionTypes.KHR_environment_map)));
    }

    private VulkanImageBuffer[] loadImages(RenderableScene asset, KHREnvironmentMap extension) {
        VulkanImageBuffer[] loadedImages = loadImages(asset, asset.getImages());
        if (loadedImages != null && extension != null) {
            EnvironmentMap[] environmentMaps = extension.getEnvironmentMaps();
            for (int i = 0; i < loadedImages.length; i++) {
                if (extension.referencesImage(i)) {
                    if (loadedImages[i].sourceImageBuffer.faceCount != 6) {
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Source image is not cubemap. Facecount = " + loadedImages[i].sourceImageBuffer.faceCount);
                    }
                    // Set miplevels in environmentmap so that it can be uploaded to uniform storage
                    int mipLevels = environmentMaps[0].calculateMipLevels(loadedImages[i].sourceImageBuffer.width, loadedImages[i].sourceImageBuffer.height);
                    // Set miplevels in vulkanimagebuffer so texture is created with that number of miplevels
                    loadedImages[i].setMipLevels(mipLevels);
                    extension.getCubemap(0).setSize(loadedImages[i].getInfo().width);
                }
            }
        }
        return loadedImages;
    }

    /**
     * Loads a number of images and returns an array of ImageBuffers - will use ThreadService for multithreading.
     * 
     * @param asset
     * @param images
     * @return The array of loaded images, or null if images is null.
     * @throws RuntimeException If loading of an image failed.
     */
    private VulkanImageBuffer[] loadImages(RenderableScene asset, JSONImage[] images) {
        long start = System.currentTimeMillis();
        if (images != null) {
            ImageBuffer[] imageBuffers = new ImageBuffer[images.length];
            ColorSpace[] colorSpace = new ColorSpace[images.length];
            java.util.concurrent.Semaphore lock = new java.util.concurrent.Semaphore(0);
            int index = 0;
            for (JSONImage image : images) {
                ImageLoadRunnable loader = new ImageLoadRunnable(this, imageBuffers, colorSpace, index++, lock, asset, image);
                ThreadService.getInstance().execute(loader);
            }
            try {
                lock.acquire(imageBuffers.length);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Check if loading of source image failed.
            for (ImageBuffer ib : imageBuffers) {
                if (ib == null) {
                    throw new RuntimeException(ErrorMessage.FAILED_WITH_ERROR.message + " Could not load image, check log for details.");
                }
            }
            Logger.d(getClass(), "Loaded " + images.length + " images in " + (System.currentTimeMillis() - start) + " millis");
            return toVulkanBuffers(asset, imageBuffers, colorSpace, images);
        }
        return null;
    }

    private VulkanImageBuffer[] toVulkanBuffers(RenderableScene asset, ImageBuffer[] imageBuffers, ColorSpace[] colorSpace, JSONImage[] srcImages) {
        // Check colorspace of the source and make sure source is SRGB where relevant, target texture format
        // shall always be linear.
        long start = System.currentTimeMillis();
        VulkanImageBuffer[] vulkanImageBuffers = new VulkanImageBuffer[imageBuffers.length];
        SurfaceFormat surfaceFormat = backend.getSurfaceFormat();
        for (int i = 0; i < vulkanImageBuffers.length; i++) {
            // Check if pq is used - then use gamma 2.8 instead of 2.4
            Vulkan10.Format textureFormat = deviceMemory.getMemoryFormat(imageBuffers[i]);
            if (colorSpace[i] == ColorSpace.SRGB) {
                Logger.d(getClass(), "Image source colorspace is sRGB - converting to linear " + surfaceFormat.getColorSpace());
                toLinearJava(imageBuffers[i], surfaceFormat);
            }
            vulkanImageBuffers[i] = new VulkanImageBuffer(imageBuffers[i], textureFormat, srcImages[i].getSourceId());
        }
        Logger.d(getClass(), "Converting images took " + (System.currentTimeMillis() - start) + " millis");
        return vulkanImageBuffers;

    }

    @Override
    public void deleteSourceImages(RenderableScene asset) {
        sourceImages.remove(asset);
    }

    @Override
    public VulkanImageBuffer getSource(RenderableScene asset, String sourceId) {
        VulkanImageBuffer[] imageBuffers = this.sourceImages.get(asset, null);
        if (imageBuffers != null) {
            for (VulkanImageBuffer vib : imageBuffers) {
                if (vib.sourceId.contentEquals(sourceId)) {
                    return vib;
                }
            }
        }
        return null;
    }

    @Override
    public TextureImages createTextureImages(RenderableScene asset) {
        TextureImages textureImages = textureImagesMap.get(asset.getId());
        if (textureImages != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already created textureimages for glTF with ID " + asset.getId());
        }
        ImageBufferUsage imageBufferUsage = new ImageBufferUsage();
        if (asset.getTextureCount() > 0) {
            if (sourceImages.getImageCount(asset) == 0) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", must load texture images before calling this method");
            }
            calculateTextureUsage(asset, imageBufferUsage);
        }
        textureImages = new TextureImages();
        VulkanImageBuffer[] cubemapImages = calculateCubemapUsage(asset, imageBufferUsage, textureImages);
        createTextureImages(imageBufferUsage, textureImages);
        // Texture descriptors for samplers - may end up with more than image array count descriptors.
        Logger.d(getClass(), "Using " + textureImages.getTextureMemoryCount() + " texture memory allocations, for " + textureImages.getTextureImageCount() + " texture image uses.");
        createDescriptors(asset, cubemapImages, textureImages);
        textureImagesMap.put(asset.getId(), textureImages);
        return textureImages;
    }

    private VulkanImageBuffer[] calculateCubemapUsage(RenderableScene asset, ImageBufferUsage imageBufferUsage,
            TextureImages textureImages) {
        VulkanImageBuffer[] cubemap = sourceImages.get(asset, TextureType.CUBEMAP);
        if (cubemap != null && cubemap.length > 0) {
            calculateImageUsage(asset, cubemap, imageBufferUsage);
        }
        return cubemap;
    }

    private void createDescriptors(RenderableScene asset, VulkanImageBuffer[] cubemap, TextureImages textureImages) {
        createCubemapDescriptors(cubemap, textureImages);
        createMaterialTextureDescriptors(asset, asset.getTextures(), textureImages);
        Logger.d(getClass(), "Created " + textureImages.getDescriptorCount(org.varg.assets.TextureImages.SamplerType.sampler2DArray) + " material texture descriptors (samplers)");
        Logger.d(getClass(), "Created " + textureImages.getDescriptorCount(SamplerType.samplerCubeArray) + " cubemap texture descriptors (samplers)");
        int maxSamplers = backend.getSelectedDevice().getProperties().getLimits().getMaxPerStageDescriptorSamplers();
        if (textureImages.getDescriptorCount() > maxSamplers) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Need more samplers: " + textureImages.getDescriptorCount() + " device max is: " + maxSamplers);
        }
    }

    private void createTextureImages(ImageBufferUsage imageBufferUsage, TextureImages textureImages) {
        SourceImageBufferInfo[] infos = imageBufferUsage.getImageBufferInfos();
        if (infos != null && infos.length > 0) {
            int stagingSize = imageBufferUsage.getStagingBufferRequirement();
            if (stagingSize > 0) {
                deviceMemory.allocateStagingBuffer(stagingSize);
                for (SourceImageBufferInfo info : infos) {
                    ImageBufferList buffers = imageBufferUsage.getDynamicImageBuffers(info);
                    ImageMemory imageMemory = (ImageMemory) createTextureMemory(deviceMemory, queue, ImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, info, buffers.getImageBuffers());
                    textureImages.addTextureMemory(imageMemory);
                    for (byte layer = 0; layer < buffers.getDynamicImageBufferCount(); layer++) {
                        DynamicImageBuffer buffer = buffers.getDynamicImageBuffer(layer);
                        textureImages.addTextureImage(buffer.sourceId, imageMemory, layer);
                    }
                }
                deviceMemory.freeStagingBuffer();
            }
        }
    }

    private void createCubemapDescriptors(VulkanImageBuffer[] cubemap, TextureImages textureImages) {
        if (cubemap != null) {
            // Create cubemap descriptors and flag materials to use cubemap channel
            for (VulkanImageBuffer vib : cubemap) {
                org.gltfio.gltf2.JSONSampler cubemapSampler =
                        new org.gltfio.gltf2.JSONSampler();
                TextureImageInfo cubemapInfo = textureImages.getTextureImage(vib.sourceId);
                TextureSamplerInfo samplerInfo = new TextureSamplerInfo(cubemapSampler, SamplerType.samplerCubeArray,
                        cubemapInfo);
                TextureSamplerInfo currentSampler = textureImages.getSamplerInfo(samplerInfo);
                if (currentSampler == null) {
                    float max = backend.getSelectedDevice().getProperties().getLimits().getMaxSamplerAnisotropy();
                    float anisotropy = Settings.getInstance().getFloat(FloatProperties.MAX_ANISOTROPY, max);
                    ImageMemory mem = cubemapInfo.texture;
                    Sampler sampler = createSampler(new org.gltfio.gltf2.JSONSampler(), 0f,
                            mem.getImage().getMipLevels(), 0f, anisotropy);
                    ImageViewType viewType = ImageViewType.get(vib.sourceImageBuffer.getTextureType().vkValue);
                    createDescriptorImageInfo(mem, viewType, new ComponentMapping(),
                            sampler);
                    DescriptorImageInfo descriptor = createDescriptorImageInfo(mem,
                            ImageViewType.VK_IMAGE_VIEW_TYPE_CUBE_ARRAY, new ComponentMapping(), sampler);
                    textureImages.addTextureDescriptor(samplerInfo, descriptor);
                    textureImages.addTextureSamplerInfo(new JSONTexture().getId(), samplerInfo);
                    Logger.d(getClass(), "Creating new texturedescriptor for: " + samplerInfo);
                } else {
                    textureImages.addTextureSamplerInfo(new JSONTexture().getId(), samplerInfo);
                    Logger.d(getClass(), "Reusing texturedescriptor for " + samplerInfo);
                }
            }
        }
    }

    private void createMaterialTextureDescriptors(RenderableScene asset, JSONTexture[] textures, TextureImages textureImages) {
        if (textures != null) {
            for (JSONTexture texture : textures) {
                TextureImageInfo info = textureImages.getTextureImage(texture.getSource().getSourceId());
                Logger.d(getClass(), info.toString());
                TextureSamplerInfo samplerInfo = new TextureSamplerInfo(asset.getSampler(texture), SamplerType.sampler2DArray, info);
                TextureSamplerInfo currentSampler = textureImages.getSamplerInfo(samplerInfo);
                if (currentSampler == null) {
                    DescriptorImageInfo descriptor = (DescriptorImageInfo) createTextureDescriptor(asset, texture, info.texture, ImageViewType.VK_IMAGE_VIEW_TYPE_2D_ARRAY, new ComponentMapping());
                    textureImages.addTextureDescriptor(samplerInfo, descriptor);
                    textureImages.addTextureSamplerInfo(texture.getId(), samplerInfo);
                    Logger.d(getClass(), "Creating new texturedescriptor for: " + samplerInfo);
                } else {
                    textureImages.addTextureSamplerInfo(texture.getId(), samplerInfo);
                    Logger.d(getClass(), "Reusing texturedescriptor for " + samplerInfo);
                }
            }
        }
    }

    @Override
    public void deleteTextureImages(RenderableScene asset) {
        TextureImages textureImages = textureImagesMap.get(asset.getId());
        if (textureImages == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "No textureimages for glTF with id: " + asset.getId());
        }
        TextureMemory[] textureMemory = textureImages.getTextureMemory();
        for (TextureMemory memory : textureMemory) {
            deleteTextureMemory(deviceMemory, memory);
        }
        textureImagesMap.remove(asset.getId());
        textureImages.destroy();
    }

    @Override
    public TextureMemory createTextureMemory(DeviceMemory memoryAllocator, ImageCreateInfo imageCreateInfo) {
        if (memoryAllocator == null || imageCreateInfo == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        ImageMemory imageMemory = memoryAllocator.allocateImageMemory(imageCreateInfo,
                MemoryPropertyFlagBit.getBitFlags(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));
        if (imageCreateInfo.initialLayout != null
                && imageCreateInfo.initialLayout != ImageLayout.VK_IMAGE_LAYOUT_UNDEFINED) {
            // TODO - should not need to wait idle
            // queue.queueBegin();
            ImageSubresourceRange subresource = new ImageSubresourceRange(ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT,
                    0, imageCreateInfo.mipLevels, 0, imageCreateInfo.arrayLayers);
            queue.cmdPipelineBarrier(imageMemory.getImage(), ImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
                    imageCreateInfo.initialLayout,
                    subresource);
            // queue.queueWaitIdle();
        }
        return imageMemory;

    }

    private ImageMemory internalCreateTextureMemory(@NonNull DeviceMemory memoryAllocator,
            int mipLevels, int formatValue, ImageTiling tiling, @NonNull ImageBuffer... imageBuffer) {
        FormatFeatureFlagBits flags = FormatFeatureFlagBits.VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT;
        FormatProperties properties = backend.getFormatProperties(backend.getSelectedDevice(), formatValue);
        if (!properties.supportsFeature(tiling, flags)) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + "Can not use format " + Vulkan10.Format.get(formatValue)
                            + " with tiling " + ImageTiling.VK_IMAGE_TILING_OPTIMAL);
        }
        int faceCount = 0;
        for (ImageBuffer ib : imageBuffer) {
            faceCount += Math.max(1, ib.faceCount);
        }
        ImageBuffer buffer = imageBuffer[0];
        Extent3D extent = new Extent3D(buffer.width, buffer.height, 1);
        mipLevels = mipLevels < 0 ? JSONTexture.getMipLevels(extent.width, extent.height) : mipLevels;
        ImageCreateFlagBits[] createFlags = buffer.getTextureType() == TextureType.CUBEMAP
                ? new ImageCreateFlagBits[] { ImageCreateFlagBits.VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT }
                : null;
        ImageCreateInfo imageCreateInfo = new ImageCreateInfo(createFlags, ImageType.VK_IMAGE_TYPE_2D, formatValue,
                extent,
                mipLevels, faceCount, SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT, tiling,
                ImageUsageFlagBits.getBitFlags(ImageUsageFlagBits.VK_IMAGE_USAGE_TRANSFER_DST_BIT,
                        ImageUsageFlagBits.VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
                        ImageUsageFlagBits.VK_IMAGE_USAGE_SAMPLED_BIT),
                ImageLayout.VK_IMAGE_LAYOUT_UNDEFINED);
        ImageMemory imageMemory = (ImageMemory) createTextureMemory(memoryAllocator, imageCreateInfo);
        return imageMemory;
    }

    @Override
    public TextureMemory createTextureMemory(DeviceMemory memoryAllocator, Queue q,
            ImageLayout destinationLayout, SourceImageBufferInfo info, ImageBuffer... imageBuffer) {
        ImageMemory imageMemory = internalCreateTextureMemory(memoryAllocator, info.mipLevels, info.format.value,
                ImageTiling.VK_IMAGE_TILING_OPTIMAL, imageBuffer);
        int mipLevels = imageMemory.getImage().getMipLevels();
        int arrayLayers = imageMemory.getImage().getArrayLayers();
        internalCopyImage(imageMemory, memoryAllocator, 0, mipLevels, 0, imageBuffer);
        // Transition layout to destination layout if not null
        if (destinationLayout != null) {
            for (int level = 0; level < mipLevels; level++) {
                q.transitionToLayout(imageMemory.getImage(), destinationLayout,
                        new ImageSubresourceLayers(level, arrayLayers));
            }
        }
        return imageMemory;
    }

    @Override
    public void deleteTextureMemory(DeviceMemory memoryAllocator, TextureMemory textureMemory) {
        if (!(textureMemory instanceof ImageMemory)) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "texturememory not instance of "
                    + ImageMemory.class.getSimpleName());
        }
        memoryAllocator.freeMemory(((ImageMemory) textureMemory).getMemory());
    }

    /**
     * Copies from imageBuffers to device memory
     * 
     * @param imageMemory
     * @param memoryAllocator
     * @param mipLevels
     * @param arrayLayers
     * @param imageBuffers
     */
    private void internalCopyImage(ImageMemory imageMemory, DeviceMemory memoryAllocator, int mipLevel, int mipCount,
            int baseLayer, ImageBuffer... imageBuffers) {
        int layer = baseLayer;
        int layerCount = 0;
        for (ImageBuffer imageBuffer : imageBuffers) {
            memoryAllocator.copyToDeviceMemory(imageBuffer,
                    backend.getFormatProperties(backend.getSelectedDevice(), imageBuffer.getFormat().value),
                    imageMemory,
                    backend.getFormatProperties(backend.getSelectedDevice(), imageMemory.getImage().getFormatValue()),
                    new ImageSubresourceLayers(ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT, mipLevel, layer,
                            Math.max(1, imageBuffer.faceCount)),
                    queue);
            layer++;
            layerCount += Math.max(1, imageBuffer.faceCount);
        }
        queue.queueBegin();
        if (mipCount > 1) {
            boolean cubic = checkFilterBlit(imageMemory.getImage(),
                    FormatFeatureFlagBits.VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_CUBIC_BIT_EXT,
                    FormatFeatureFlagBits.VK_FORMAT_FEATURE_BLIT_SRC_BIT,
                    FormatFeatureFlagBits.VK_FORMAT_FEATURE_BLIT_DST_BIT);
            boolean linear = checkFilterBlit(imageMemory.getImage(),
                    FormatFeatureFlagBits.VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT,
                    FormatFeatureFlagBits.VK_FORMAT_FEATURE_BLIT_SRC_BIT,
                    FormatFeatureFlagBits.VK_FORMAT_FEATURE_BLIT_DST_BIT);
            // queue.queueBegin();
            Filter filter = cubic ? Filter.VK_FILTER_CUBIC_EXT : linear ? Filter.VK_FILTER_LINEAR
                    : Filter.VK_FILTER_NEAREST;
            for (int level = mipLevel; level < mipCount - 1; level++) {
                ImageSubresourceLayers source = new ImageSubresourceLayers(
                        ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT,
                        level, baseLayer, layerCount);
                ImageSubresourceLayers destination = new ImageSubresourceLayers(
                        ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT, level + 1, baseLayer, layerCount);
                queue.cmdBlitImage(imageMemory.getImage(), imageMemory.getImage(), source, destination, filter);
            }
            // TODO - replace with queueEnd()?
            // queue.queueWaitIdle();
        }
    }

    private boolean checkFilterBlit(org.varg.vulkan.image.Image image,
            FormatFeatureFlagBits... features) {
        FormatProperties formatProperties = backend.getFormatProperties(backend.getSelectedDevice(),
                image.getFormatValue());
        ImageTiling tiling = image.getTiling();
        return formatProperties.supportsFeature(tiling, features);
    }

    @Override
    public ImageBuffer createImage(RenderableScene asset, JSONImage image) throws IOException {
        if (asset == null || image == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        try {
            String mime = image.getMimeType() != null ? image.getMimeType() : image.getUri();
            org.ktximageio.ktx.ImageReader.MimeFormat format = org.ktximageio.ktx.ImageReader.MimeFormat.get(mime);
            if (format == null) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", no FileFormat for " + mime);
            }
            org.ktximageio.ktx.ImageReader reader = org.ktximageio.ktx.ImageReader.getImageReader(format);
            if (reader == null) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", no ImageReader for " + format);
            }
            ImageBuffer buffer = loadImage(asset, image, reader);
            return buffer;
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private void toLinearJava(Object sourceImage, SurfaceFormat surfaceFormat) {
        if (sourceImage instanceof ByteArrayImageBuffer) {
            toLinearJavaByte((ByteArrayImageBuffer) sourceImage, surfaceFormat);
        } else if (sourceImage instanceof ShortArrayImageBuffer) {
            toLinearJavaShort((ShortArrayImageBuffer) sourceImage, surfaceFormat);
        } else {
            toLinearJavaInt((IntArrayImageBuffer) sourceImage, surfaceFormat);
        }
    }

    private void toLinearJavaByte(ByteArrayImageBuffer sourceImage, SurfaceFormat surfaceFormat) {
        byte[] pixels = sourceImage.getAsByteArray();
        toLinearFromSRGB(pixels);
    }

    private void toLinearJavaShort(ShortArrayImageBuffer sourceImage, SurfaceFormat surfaceFormat) {
        short[] pixels = sourceImage.getAsShortArray();
        toLinearFromSRGB(pixels);
    }

    private void toLinearJavaInt(IntArrayImageBuffer sourceImage, SurfaceFormat surfaceFormat) {
        int[] pixels = sourceImage.getAsIntArray();
        toLinearFromSRGB(pixels);
    }

    /**
     * TODO - consider moving this to an image package
     */
    private static void toLinearFromSRGB(byte[] resultData) {
        ThreadService ts = ThreadService.getInstance();
        int offset = 0;
        int length = resultData.length / ts.threadCount;
        Semaphore lock = new Semaphore(0);
        ArrayToLinearRunnable command = null;
        for (int i = 0; i < ts.threadCount; i++) {
            command = i < ts.threadCount - 1 ? new ArrayToLinearRunnable(length, offset, resultData, lock)
                    : new ArrayToLinearRunnable(resultData.length - i * length, offset, resultData, lock);
            offset += length;
            ts.execute(command);
        }
        try {
            lock.acquire(ts.threadCount);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO - consider moving this to an image package
     */
    private static void toLinearFromSRGB(short[] resultData) {
        ThreadService ts = ThreadService.getInstance();
        int offset = 0;
        int length = resultData.length / ts.threadCount;
        Semaphore lock = new Semaphore(0);
        ArrayToLinearRunnable command = null;
        for (int i = 0; i < ts.threadCount; i++) {
            command = i < ts.threadCount - 1 ? new ArrayToLinearRunnableShort(length, offset, resultData, lock) : new ArrayToLinearRunnableShort(resultData.length - i * length, offset, resultData, lock);
            offset += length;
            ts.execute(command);
        }
        try {
            lock.acquire(ts.threadCount);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO - consider moving this to an image package
     */
    private static void toLinearFromSRGB(int[] resultData) {
        ThreadService ts = ThreadService.getInstance();
        int offset = 0;
        int length = resultData.length / ts.threadCount;
        Semaphore lock = new Semaphore(0);
        ArrayToLinearRunnable command = null;
        for (int i = 0; i < ts.threadCount; i++) {
            command = i < ts.threadCount - 1 ? new ArrayToLinearRunnableInt(length, offset, resultData, lock) : new ArrayToLinearRunnableInt(resultData.length - i * length, offset, resultData, lock);
            offset += length;
            ts.execute(command);
        }
        try {
            lock.acquire(ts.threadCount);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ImageBuffer loadImage(@NonNull RenderableScene asset, @NonNull JSONImage image,
            org.ktximageio.ktx.ImageReader reader) throws IOException, URISyntaxException {
        long start = System.currentTimeMillis();
        ImageHeader header = null;
        if (image.getBufferView() != Constants.NO_VALUE) {
            header = reader.read(asset.getBufferView(image.getBufferView()).getReadByteBuffer(0));
            Logger.d(getClass(), "Loading image from bufferview " + image.getBufferView() + " with buffer format " + header.getFormat() + ", using reader " + reader.getReaderName() + " for formats " + MimeFormat.toString(reader.getMime())
                    + " took " + (System.currentTimeMillis() - start) + " millis");
        } else {
            URI uri = URI.create(image.getUri());
            Path path = null;
            if (uri.isAbsolute()) {
                path = FileUtils.getInstance().getPath(image.getUri(), "");
            } else if (image.getUri().startsWith(FileUtils.DIRECTORY_SEPARATOR_STRING)) {
                path = FileUtils.getInstance().getPath(image.getUri(), "");
            } else {
                path = FileUtils.getInstance().getPath(asset.getRoot().getPath(), image.getUri());
            }
            header = reader.read(path);
            Logger.d(getClass(),
                    "Loading image from path " + path + " with buffer format " + header.getFormat() + ", using reader "
                            + reader.getReaderName() + " for formats " + MimeFormat.toString(reader.getMime())
                            + " took " + (System.currentTimeMillis() - start) + " millis");
        }
        return header.getData();
    }

    @Override
    public TextureImages getTextureImages(RenderableScene asset) {
        TextureImages textureImages = textureImagesMap.get(asset.getId());
        if (textureImages == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Must create texture images first.");
        }
        return textureImages;
    }

    @Override
    public TextureDescriptor createTextureDescriptor(RenderableScene asset, JSONTexture texture, ImageMemory imageMemory, ImageViewType type, ComponentMapping mapping) {
        Sampler sampler = samplers.get(texture.getSamplerIndex());
        if (sampler == null) {
            float max = backend.getSelectedDevice().getProperties().getLimits().getMaxSamplerAnisotropy();
            float anisotropy = Settings.getInstance().getFloat(FloatProperties.MAX_ANISOTROPY, max);
            sampler = createSampler(asset.getSampler(texture), 0f, imageMemory.getImage().getMipLevels(), 0f, anisotropy);
            samplers.put(texture.getSamplerIndex(), sampler);
        }
        TextureDescriptor descriptor = createDescriptorImageInfo(imageMemory, type, mapping, sampler);
        return descriptor;
    }

    private DescriptorImageInfo createDescriptorImageInfo(ImageMemory imageMemory, ImageViewType type, ComponentMapping mapping, Sampler sampler) {
        int layerCount = (type == ImageViewType.VK_IMAGE_VIEW_TYPE_CUBE_ARRAY || type == ImageViewType.VK_IMAGE_VIEW_TYPE_CUBE) ? Vulkan10.VK_REMAINING_ARRAY_LAYERS : imageMemory.getImage().getArrayLayers();
        ImageSubresourceRange subresource = new ImageSubresourceRange(ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT, 0, imageMemory.getImage().getMipLevels(), 0, layerCount);
        ImageViewCreateInfo createInfo = new ImageViewCreateInfo(imageMemory.getImage(), type, imageMemory.getImage().getFormatValue(), mapping, subresource);
        ImageView imageView = backend.createImageView(createInfo);
        return new DescriptorImageInfo(sampler, imageView, imageMemory, subresource);
    }

    /**
     * Creates the vulkan sampler for a texture
     * 
     * @param glTFSampler
     * @param minLid
     * @param maxLod
     * @param lodBias
     * @return
     */
    private Sampler createSampler(org.gltfio.gltf2.JSONSampler glTFSampler, float minLod, float maxLod, float lodBias, float anisotropy) {
        Sampler sampler = null;
        SamplerCreateInfo create = null;
        GLFilter mag = GLFilter.get(glTFSampler.getMagFilter());
        GLFilter min = GLFilter.get(glTFSampler.getMinFilter());
        create = new SamplerCreateInfo(mag.filter, min.filter, min.mipmapMode, GLWrapMode.get(glTFSampler.getWrapS()).mode, GLWrapMode.get(glTFSampler.getWrapT()).mode, minLod, maxLod, lodBias);
        if (backend.getLogicalDevice().getFeatures().getPhysicalDeviceFeatures().samplerAnisotropy && (mag != GLFilter.GL_NEAREST || min != GLFilter.GL_NEAREST)) {
            create.setMaxAnisotropy(anisotropy);
            Logger.d(getClass(), "SamplerAnisotropy set to " + anisotropy);
        } else {
            create.setMaxAnisotropy(0f);
        }
        sampler = backend.createSampler(create);
        Logger.d(getClass(), "Created new Sampler: " + create);
        return sampler;
    }

    @Override
    public void updateVertexBuffers(VertexMemory... vertexBuffers) {
        queue.queueBegin();
        for (VertexMemory vm : vertexBuffers) {
            deviceMemory.updateBuffers(vm.getBuffers(), vm.getMemoryBuffers(), queue);
            deviceMemory.updateBuffers(vm.getIndexBuffers(), vm.getIndexMemoryBuffers(), queue);
        }
    }

    @Override
    public VertexMemory getVertexBuffers(JSONNode node) {
        return vertexBuffersMap.get(node.getRoot().getId());
    }

    @Override
    public DescriptorBuffers<?> getStorageBuffers(Subtype type) {
        return storageBuffersMap.get(type.getName());
    }

    @Override
    public void createStorageBuffers(ShaderCreateInfo info, StorageBufferConsumer<?> consumer, DescriptorBuffers<?> buffers) {
        DescriptorSetTarget[] targets = info.shaderType.getTargets();
        int[] bufferSizes = consumer.getBufferSizes(targets);
        int[] dynamicOffsets = consumer.getDynamicOffsets(targets);
        if (bufferSizes != null) {
            createStorageBuffers(targets, bufferSizes, dynamicOffsets, buffers);
        }
        addStorageBuffers(info.shaderType, buffers);
    }

    private void createStorageBuffers(DescriptorSetTarget[] targets, int[] bufferSizes, int[] dynamicOffsets, DescriptorBuffers<?> buffers) {
        targets = DescriptorSetTarget.sortBySet(targets);
        MemoryBuffer[] bufferList = new MemoryBuffer[targets.length];
        int[] offsets = new int[targets.length];
        int[] sizes = new int[targets.length];
        int totalSize = 0;
        int allocateFlags = 0;
        for (int i = 0; i < targets.length; i++) {
            if (targets[i].getSet() != i) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Conficting sets");
            }
            DescriptorSetTarget target = targets[i];
            int set = target.getSet();
            int align = 0;
            int size = bufferSizes[set];
            if (size > 0) {
                Vulkan10.BufferUsageFlagBit[] usageBits = VulkanBackend.getBufferUsage(BackendStringProperties.UNIFORM_USAGE, target.getBufferUsage());
                int usage = BitFlags.getFlagsValue(usageBits);
                if ((usage & BufferUsageFlagBit.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT.value) != 0) {
                    int maxUniform = backend.getLogicalDevice().getPhysicalDeviceProperties().getLimits().getMaxUniformBufferRange();
                    if (size > maxUniform) {
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + target + " uniform buffer too large " + size + ", max=" + maxUniform);
                    }
                }
                if ((usage & BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT.value) != 0) {
                    int maxStorage = backend.getLogicalDevice().getPhysicalDeviceProperties().getLimits().getMaxStorageBufferRange();
                    if (size > maxStorage) {
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + target + " storage buffer too large " + size + ", max=" + maxStorage);
                    }
                }
                if ((usage & BufferUsageFlagBit.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT.value) != 0) {
                    allocateFlags = allocateFlags | MemoryAllocateFlagBits.VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT.value;
                }
                MemoryBuffer b = deviceMemory.createBuffer(bufferSizes[set], usage);
                align = (totalSize % (int) b.alignment) == 0 ? 0 : (int) b.alignment - (totalSize % (int) b.alignment);
                sizes[set] = (int) b.allocationSize;
                bufferList[set] = b;
            }
            offsets[set] = totalSize + align;
            totalSize += sizes[set] + align;
        }

        Memory memory = deviceMemory.allocateMemory(totalSize, BitFlags.getFlagsValue(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT), allocateFlags, 0);
        ByteBuffer backingBuffer = Buffers.createByteBuffer((int) memory.size);
        buffers.setAllocatedMemory(memory, backingBuffer);

        for (DescriptorSetTarget target : targets) {
            int set = target.getSet();
            if (bufferList[set] != null) {
                deviceMemory.bindBufferMemory(bufferList[set], memory, offsets[set]);
                backingBuffer.limit(offsets[set] + bufferSizes[set]);
                backingBuffer.position(offsets[set]);
                int dynamicCount = dynamicOffsets != null ? dynamicOffsets[set] : 0;
                BindBuffer bind = new BindBuffer(bufferList[set], backingBuffer, dynamicCount);
                buffers.addBuffer(target, dynamicCount, bind, null);
            }
        }
    }

    private DescriptorBuffers<?> addStorageBuffers(Subtype type, DescriptorBuffers<?> buffers) {
        if (storageBuffersMap.containsKey(type.getName())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already added storagebuffers for " + type.getName() + "(" + type + ")");
        }
        storageBuffersMap.put(type.getName(), buffers);
        return buffers;
    }

    @Override
    public void deleteStorageBuffers(ShaderCreateInfo info) {
        Subtype type = info.shaderType;
        DescriptorBuffers<?> buffers = getStorageBuffers(type);
        if (buffers == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", no storagebuffers for type: "
                    + type);
        }
        deviceMemory.freeBuffer(buffers.getBuffers());
        if (buffers.getPushConstants() != null) {
            throw new IllegalArgumentException("Not implemented");
        }
        buffers.destroy();
        storageBuffersMap.remove(type.getName());
    }

}

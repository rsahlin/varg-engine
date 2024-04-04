package org.varg.assets;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONBuffer;
import org.gltfio.gltf2.JSONImage;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONTexture;
import org.gltfio.gltf2.JSONTexture.ComponentMapping;
import org.ktximageio.ktx.ImageBuffer;
import org.gltfio.gltf2.RenderableScene;
import org.varg.BackendException;
import org.varg.assets.SourceImages.SourceImageBufferInfo;
import org.varg.assets.SourceImages.VulkanImageBuffer;
import org.varg.shader.Shader.StorageBufferConsumer;
import org.varg.shader.Shader.Subtype;
import org.varg.shader.ShaderCreateInfo;
import org.varg.uniform.DescriptorBuffers;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.ImageViewType;
import org.varg.vulkan.image.ImageCreateInfo;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.ImageMemory;
import org.varg.vulkan.memory.VertexMemory;
import org.varg.vulkan.memory.VertexMemory.Mode;

/**
 * Handles loading and tracking of glTF assets such as textures and images,
 * Loading and unloading assets, mainly textures - this is the main entrypoint for loading of textures.
 * Assets in this context shall be unique (global) and not tied to a specific model/file/scene.
 * This means that if two models use the same texture it shall not be duplicated.
 * Handles texture descriptors and samplers.
 * 
 * Shall not have dependency on GltfRenderer or render API
 * 
 * This is an internal API
 * TODO - How to hide from client implementations?
 * Clients shall only use this interface to handle assets - do not create textures or assets by using
 * implementation classes directly.
 *
 */
public interface Assets {

    /**
     * Preloads all referenced source images, this method may only be called once per asset unless
     * #deleteSourceImages() is called.
     * Implementations may use multiple threads to load images.
     * Call this method before calling {@link #createTextureImages(RenderableScene)}
     * 
     * @param asset
     * @throws IOException If there is an exception loading a source image.
     * @throws IllegalStateException If source images are already loaded for the glTF
     */
    void loadSourceImages(RenderableScene asset) throws IOException;

    /**
     * Deletes (frees) all loaded source images for the asset.
     * This means that the source image (buffer) is not accessible, call this method after textureimages has been
     * created.
     * 
     * @param asset
     */
    void deleteSourceImages(RenderableScene asset);

    /**
     * Returns the source image buffer for the asset and uri, or null if not loaded or deleted.
     * ONLY USE FOR TEST/DEBUGGING
     * 
     * @param asset
     * @param sourceId
     * @return
     */
    VulkanImageBuffer getSource(RenderableScene asset, String sourceId);

    /**
     * Creates texture images and creates platform texture images.
     * This must be called when the asset file has been loaded and before any textures/images are accessed.
     * Call {@link #loadSourceImages(RenderableScene)} before calling this method.
     * The Queue must be in the recording state when calling this method.
     * 
     * Call {@link #deleteTextureImages(RenderableScene)} to delete the textures when not needed
     * Call {@link #deleteSourceImages(RenderableScene)} to delete source images after calling this method.
     * 
     * @param asset
     * @throws IllegalStateException if #loadSourceImages() has not been
     * called.
     * 
     */
    TextureImages createTextureImages(RenderableScene asset);

    /**
     * Deletes loaded texture assets that have been created by calling {@link #createTextureImages(RenderableScene)}
     * This will delete texture images
     * Do not call this wile asset is in use - must call outside from render.
     * After this call the asset must be loaded in order to be used again.
     * 
     * @param asset
     * @throws IllegalArgumentException If textures have not been created by calling #createTextureImages()
     */
    void deleteTextureImages(RenderableScene asset);

    /**
     * Creates the imagebuffer
     * This is used to get the backing image that can be used for instance to create a texture
     * 
     * @param asset
     * @param image
     * @return
     * @throws IOException
     */
    ImageBuffer createImage(RenderableScene asset, JSONImage image) throws IOException;

    /**
     * Creates a texturedescriptor
     * 
     * @param asset
     * @param texture
     * @param imageMemory
     * @param mapping
     * @return
     */
    TextureDescriptor createTextureDescriptor(RenderableScene asset,
            JSONTexture texture, ImageMemory imageMemory, ImageViewType type, ComponentMapping mapping);

    /**
     * Teardown and cleanup all assets, removes all references and resources, call when the program is exiting.
     * Do not call any of the methods after calling this method
     * 
     */
    void destroy();

    /**
     * Returns the textureimages used for the asset
     * 
     * @param asset The asset to get the texture images for
     * @throws IllegalArgumentException If textureImages is null
     */
    TextureImages getTextureImages(RenderableScene asset);

    /**
     * Creates the texture memory with the specified properties - the created texture memory will be empty
     * 
     * @return The allocated texture memory - it will be uninitialized, if destLayout is specified the memory will be
     * transitioned to this layout. Otherwise it will be undefined.
     * @throws IllegalArgumentException If memoryAllocator, extent or format is null
     */
    TextureMemory createTextureMemory(DeviceMemory memoryAllocator, ImageCreateInfo imageCreateInfo);

    /**
     * Allocates the texture image memory and copies the image to device memory, generating mip-levels on the fly.
     * The returned texture memory will have mip-levels up to 1 x 1
     * 
     * @param memoryAllocator
     * @param subresource
     * @param destinationLayout The layout to transition the created texture image to, or null
     * @param info The format and info of the image to create texture for
     * @param imageBuffer One or more imagebuffers to create texture memory for - must be same dimension
     * @return
     */
    TextureMemory createTextureMemory(DeviceMemory memoryAllocator, Queue queue,
            ImageLayout destinationLayoutr, SourceImageBufferInfo info, ImageBuffer... imageBuffe);

    /**
     * Deletes the texture created by calling createTextureMemory()
     * After this call it is an error to use the TextureMemory
     * 
     * @param memoryAllocator
     * @param textureMemory
     * @throws IllegalArgumentException If texture memory has already been deleted or it has not been allocted
     * with memoryAllocator.
     */
    void deleteTextureMemory(DeviceMemory memoryAllocator, TextureMemory textureMemory);

    /**
     * Creates the vertex buffers as needed for rendering the model
     * This will allocate buffer memory - this shall not upload data or create any layout specific objects.
     * 
     * @param mode
     * @param glTF
     * @throws BackendException
     * @throws IllegalArgumentException If vertexbuffers already have been created
     */
    @Deprecated
    VertexMemory createVertexBuffers(Mode mode, RenderableScene glTF) throws BackendException;

    /**
     * Creates the vertex buffer memory for the buffers and attributes, array sizes must match and may have null
     * elements
     * 
     * @param indicesBuffers Array of buffers with indices - byte, short and int
     * @param vertexBuffers Array of vertex attributes - must match VertexAttribute order
     * @param attributes
     * @return
     */
    @Deprecated
    VertexMemory createVertexBuffers(RenderableScene glTF, JSONBuffer[] indicesBuffers,
            JSONBuffer[] vertexBuffers);

    /**
     * Deletes the vertex buffers for the glTF asset
     * 
     * @param glTF
     * @throws IllegalArgumentException If vertexbuffers have not bee created or already deleted
     */
    @Deprecated
    void deleteVertexBuffers(RenderableScene glTF);

    /**
     * Updates the vertex buffers for the glTF model to allocated memory by copying the data in all BufferViews
     * to allocated memory
     * 
     * @param glTF
     * @throws IllegalArgumentException If vertexbuffers has not been created by calling #createVertexBuffers()
     */
    @Deprecated
    void updateVertexBuffers(RenderableScene glTF);

    /**
     * Updates the vertexbuffers to device, including indexbuffers if specified.
     * 
     * @param vertexBuffers
     */
    void updateVertexBuffers(VertexMemory... vertexBuffers);

    /**
     * Returns the vertex buffers for the asset the node belongs to.
     * 
     * @param node
     * @return The vertex buffers or null
     */
    VertexMemory getVertexBuffers(@NonNull JSONNode node);

    /**
     * Returns the vertex buffers for the glTF asset
     * 
     * @param glTF
     * @return The vertex buffers or null
     */
    VertexMemory getVertexBuffers(@NonNull RenderableScene glTF);

    /**
     * Creates the storagebuffers for the consumer and adds using the createinfo subtype.
     * Storagebuffers can be fetched by calling {@link #getStorageBuffers(Subtype)}
     * 
     * @param info
     * @param consumer
     * @param buffers
     */
    void createStorageBuffers(ShaderCreateInfo info, StorageBufferConsumer<?> consumer, DescriptorBuffers<?> buffers);

    /**
     * Deletes the storage buffers, freeing all memory
     * 
     * @param info The info used when calling
     * {@link #createStorageBuffers(ShaderCreateInfo, StorageBufferConsumer, DescriptorBuffers)}
     */
    void deleteStorageBuffers(ShaderCreateInfo info);

    /**
     * Returns the storagebuffer for the type
     * 
     * @param type
     * @return
     */
    DescriptorBuffers<?> getStorageBuffers(Subtype type);

}

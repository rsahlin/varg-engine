
package org.varg.renderer;

import org.gltfio.gltf2.JSONCamera;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.lib.Transform;
import org.varg.assets.Assets;
import org.varg.pipeline.Pipelines;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.shader.ComputeShader;
import org.varg.shader.MeshShader;
import org.varg.shader.MeshShader.MeshShaderCreateInfo;
import org.varg.shader.Shader.Subtype;
import org.varg.uniform.BindBuffer;
import org.varg.uniform.DescriptorBuffers;
import org.varg.uniform.GltfStorageBuffers;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan12Backend;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.renderpass.RenderPass;

/**
 * An interface for rendering scenes. This is done by supporting a Node based hierarchy.
 * Parts, or all, of the Nodetree can be rendered by calling {@link #render(JSONNode)} or {@link #render(RootNode)
 * The goal for this API is to provide an abstraction that can render a node based structure.
 *
 */
public interface GltfRenderer<R extends RenderableScene, M extends JSONMesh<?>> {

    enum RenderPasses {
        DRAW();
    }

    /**
     * Call this first time when the context is created, before calling GLContextCreated()
     * Initialize parameters that do not need to be updated when context is re-created.
     * Will set the window size to 0,0,width,height
     * If this method is called more than once nothing is done.
     * 
     * @param surfaceConfig The configuration of the surface in use (from EGL)
     * @param width Width of window surface
     * @param height Height of window surface
     */
    void init(SurfaceConfiguration surfaceConfig, int width, int height);

    /**
     * Returns the surface configuration
     * 
     * @return The renderers surface configuration
     */
    SurfaceConfiguration getSurfaceConfiguration();

    /**
     * Set the camera that is used for rendering.
     * 
     * @param camera
     */
    void setCamera(JSONCamera camera);

    /**
     * Signals the start of a frame - Acquires the next frame to render into, begins queue operations by calling
     * queue.queueBegin()
     * The main purpose of the method is to synk to next frame to render into and begin queue operations.
     * 
     * This must be called by the thread driving rendering.
     * Do not perform rendering or time consuming tasks in this method.
     * 
     * @return Number of seconds since last call to beginFrame
     */
    float beginFrame();

    /**
     * Prepares the storage buffer and uniform data for the asset root,
     * call this each frame before the root is rendered.
     * 
     * @param scene The data used to prepare frame.
     * @param buffers
     */
    void prepareFrameData(R scene, GltfStorageBuffers buffers);

    /**
     * Set and upload storage and uniform buffer that changes on a per/frame basis.
     * NOTE - this may only update buffers that are < 65536 bytes
     * Larger updates MUST be done outside the frame scope
     * 
     * @param shader
     * @return
     */
    DescriptorBuffers<MeshShader<? extends MeshShaderCreateInfo>> prepareFrameData(MeshShader shader, Transform sceneTransform);

    /**
     * Binds the descriptorsets for buffers, must be called before renderpass is started
     * If any of the targets uses dynamic offsets it is ignored. DescriptorSets with dynamic offsets must
     * be set separately.
     * 
     * @param buffers
     * @param queue
     * @param targets
     */
    void bindDescriptorSets(Subtype type, DescriptorBuffers<?> buffers, Queue queue, DescriptorSetTarget... targets);

    /**
     * Draws the mesh shader
     * 
     * @param meshShader
     * @param buffers
     * @param queue
     */
    void drawMeshShader(MeshShader<?> meshShader, DescriptorBuffers<?> buffers, Queue queue);

    /**
     * Invokes the compute shader
     * 
     * @param computeShader
     * @param buffers
     * @param queue
     */
    void invokeComputeShader(ComputeShader computeShader, DescriptorBuffers<?> buffers, Queue queue);

    /**
     * Starts the renderpass to render to the current frame in the swapchain
     */
    void beginRenderPass();

    /**
     * Issues an end renderpass command on the queue.
     * 
     * @param queue
     */
    void endRenderPass();

    /**
     * Copies the buffer to device memory - this must be done outside of renderpass and is normally only
     * needed for uniform buffers where the content changes.
     * 
     * @param buffer
     */
    void copyBuffer(BindBuffer buffer);

    /**
     * Submits a scene to be rendered on the specified queue, the queue must be setup to access commands.
     * This is usually done by calling {@link #beginRenderPass()}
     * DescriptorSets must be bound before calling this method.
     * 
     * Uses the current mvp matrix, will call nodes in scene and children recursively.
     * This must be called by the thread driving rendering.
     * 
     * @param root The scene to render
     * @param queue The queue to record render commands on
     * @param descriptorBuffers The bound buffers
     */
    void render(R root, Queue queue, DescriptorBuffers<?> descriptorBuffers);

    /**
     * Submits a node to be rendered using the current mvp matrix, will call children recursively.
     * 
     * @param node The node to be rendered
     * @param descriptorBuffers The bound buffers
     * @param queue The queue to record render commands on
     */
    @Deprecated
    void render(JSONNode<M> node, DescriptorBuffers<?> descriptorBuffers, Queue queue);

    /**
     * Signals the end of a frame - rendering is considered to be finished, the current image shall be displayed
     * and rendering synchronized.
     * 
     * Submits the queue and waits for idle
     * 
     * This must be called by the thread driving rendering.
     */
    void endFrame();

    /**
     * Returns true if this renderer has been initialized by calling init() when
     * the context is created.
     * 
     * @return
     */
    boolean isInitialized();

    /**
     * Sets the projection matrix, this will copy the values from the source matrix.
     * Please not that this will be overwritten when rendering nodes that have a projections.
     * This method can be used when a Node shall be rendered that does not have a projection property.
     * 
     * @param matrix The projection matrix
     * @param index Index into array where matrix is
     * @throws NullPointerException If matrix is null
     * @throws IndexOutOfBoundsException If there is not enough storage in the source matrix at index
     */
    void setProjection(float[] matrix, int index);

    /**
     * Returns the factory that shall be used to create (java application) buffers used by the backend.
     * 
     * @return
     */
    DeviceMemory getBufferFactory();

    /**
     * Returns the assets manager for the renderer, this shall be used to load/fetch resource such as textures and
     * shaders
     * 
     * @return The assets manager for the renderer
     */
    Assets getAssets();

    /**
     * Returns the pipeline manager - this shall be used to fetch pipelines
     * 
     * @return
     */
    Pipelines<VulkanRenderableScene> getPipelines();

    /**
     * Returns the version of backing graphics api
     * 
     * @return
     */
    Renderers getVersion();

    /**
     * Returns wrapper for graphics API that is used by renderer
     */
    Vulkan12Backend<?> getBackend();

    /**
     * Returns the swapbuffer (swapchain) - this can be used to determine size of output or change clear parameters.
     * 
     * @return
     */
    SwapBuffer getSwapBuffer();

    /**
     * Returns the main renderpass
     * 
     * @return
     */
    RenderPass getRenderPass();

    /**
     * Returns the queue used by the renderer - this is used to handle swapchain and drawrelated commands.
     * 
     * @return
     */
    Queue getQueue();

}

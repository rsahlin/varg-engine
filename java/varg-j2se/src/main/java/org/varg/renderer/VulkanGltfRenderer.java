
package org.varg.renderer;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.gltfio.gltf2.JSONCamera;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONTexture.ComponentMapping;
import org.gltfio.gltf2.JSONTexture.ComponentSwizzle;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Constants;
import org.gltfio.lib.DefaultPeriodicLogger;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Matrix;
import org.gltfio.lib.MatrixUtils;
import org.gltfio.lib.RenderLogger;
import org.gltfio.lib.Settings;
import org.gltfio.lib.TimeLogger;
import org.gltfio.lib.Transform;
import org.ktximageio.ktx.HalfFloatImageBuffer.FP16Convert;
import org.ktximageio.ktx.ImageReader.ImageFormat;
import org.varg.assets.Assets;
import org.varg.assets.VulkanAssets;
import org.varg.gltf.VulkanMesh;
import org.varg.gltf.VulkanPrimitive;
import org.varg.pipeline.Pipelines;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.pipeline.Pipelines.SetType;
import org.varg.pipeline.VulkanPipelines;
import org.varg.renderer.MVPMatrices.Matrices;
import org.varg.shader.ComputeShader;
import org.varg.shader.Gltf2GraphicsShader;
import org.varg.shader.Gltf2GraphicsShader.GltfDescriptorSetTarget;
import org.varg.shader.MeshShader;
import org.varg.shader.MeshShader.MeshDescriptorSetTarget;
import org.varg.shader.Shader.Stage;
import org.varg.shader.Shader.Subtype;
import org.varg.uniform.BindBuffer;
import org.varg.uniform.BindBuffer.BufferState;
import org.varg.uniform.DescriptorBuffers;
import org.varg.uniform.GltfStorageBuffers;
import org.varg.vulkan.IndirectDrawCalls;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.AccessFlagBit;
import org.varg.vulkan.Vulkan10.ColorSpaceKHR;
import org.varg.vulkan.Vulkan10.ImageAspectFlagBit;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.ImageUsageFlagBits;
import org.varg.vulkan.Vulkan10.ImageViewType;
import org.varg.vulkan.Vulkan10.IndexType;
import org.varg.vulkan.Vulkan10.MemoryPropertyFlagBit;
import org.varg.vulkan.Vulkan10.PipelineBindPoint;
import org.varg.vulkan.Vulkan10.PipelineStageFlagBit;
import org.varg.vulkan.Vulkan10.Result;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.Vulkan12Backend;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.cmd.ClearColorValue;
import org.varg.vulkan.cmd.ClearDepthStencilValue;
import org.varg.vulkan.extensions.EXTMeshShader;
import org.varg.vulkan.extensions.KHRSwapchain;
import org.varg.vulkan.framebuffer.FrameBuffer;
import org.varg.vulkan.framebuffer.FramebufferCreateInfo;
import org.varg.vulkan.image.ImageCreateInfo;
import org.varg.vulkan.image.ImageMemoryBarrier;
import org.varg.vulkan.image.ImageSubresourceRange;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.image.ImageViewCreateInfo;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.ImageMemory;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.memory.VertexMemory;
import org.varg.vulkan.pipeline.ComputePipeline;
import org.varg.vulkan.pipeline.GraphicsPipeline;
import org.varg.vulkan.renderpass.AttachmentDescription;
import org.varg.vulkan.renderpass.ClearValue;
import org.varg.vulkan.renderpass.RenderPass;
import org.varg.vulkan.renderpass.RenderPassCreateInfo;
import org.varg.vulkan.renderpass.SubpassDescription2;
import org.varg.vulkan.structs.DeviceLimits;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.HDRMetadata;
import org.varg.vulkan.structs.Rect2D;
import org.varg.vulkan.structs.Semaphore;
import org.varg.vulkan.structs.SemaphoreCreateInfo;
import org.varg.vulkan.vertex.BindVertexBuffers;

/**
 * Platform and render backend agnostic renderer - use this to implement renderer based on specific API such as Vulkan.
 * 
 */
public class VulkanGltfRenderer implements GltfRenderer<VulkanRenderableScene, VulkanMesh> {

    public static final String NOT_INITIALIZED_ERROR = "Not initialized, must call init()";
    protected static final String BASE_RENDERER_TAG = "BaseRenderer";
    protected static final String NULL_VERSION_ERROR = "Renderer version is null";

    protected static final int FPS_SAMPLER_DELAY = 5;

    protected SurfaceConfiguration surfaceConfig;

    /**
     * Temp matrix - not threadsafe
     */
    protected float[] tempMatrix = MatrixUtils.createMatrix();
    // Viewport is setup using 0, 0 as upper left corner.:
    // x+ -> right
    // y+ -> down
    // Flip y axis to align to glTF
    protected float[] vulkanDepthMatrix = new float[] { 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

    protected MVPMatrices mvpMatrices = new MVPMatrices();
    private JSONCamera camera;
    protected float[] globalUniformData;

    protected Vulkan12Backend<?> vulkan;
    protected DeviceMemory bufferFactory;
    protected Assets assetManager;
    protected VulkanPipelines pipelineManager;
    protected int boundPipelineHash = Constants.NO_VALUE;
    protected DeviceMemory memoryAllocator;
    protected GraphicsPipeline currentPipeline = null;
    protected Renderers version;

    protected SwapBuffer swapBuffer;
    protected RenderPass swapBufferRenderPass;
    protected RenderPass toTextureRenderPass;
    protected Queue queue;
    protected Semaphore presentComplete;
    protected Semaphore drawComplete;

    private long prevFrameTime;
    protected float deltaTime = (float) 1 / 60;

    protected Window window = Window.getInstance();
    private Rect2D viewport;

    private TimeLogger fpsLogger = new TimeLogger(getClass(), "Drawframe took ");
    private RenderLogger renderLogger = new RenderLogger();

    /**
     * Set to true when init is called
     */
    protected boolean initialized = false;
    /**
     * Set to true when context is created, if set again it means context was
     * lost and re-created.
     */
    protected boolean contextCreated = false;

    public VulkanGltfRenderer(Renderers renderVersion, Vulkan12Backend<?> backend, DeviceMemory memAllocator) {
        if (backend == null || memAllocator == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        vulkan = backend;
        version = renderVersion;
        memoryAllocator = memAllocator;
        initLoggers();
    }

    private void initLoggers() {
        DefaultPeriodicLogger.getInstance().addLogMessager(fpsLogger);
        DefaultPeriodicLogger.getInstance().addLogMessager(renderLogger);
    }

    @Override
    public SurfaceConfiguration getSurfaceConfiguration() {
        return surfaceConfig;
    }

    @Override
    public void init(SurfaceConfiguration configuration, int width, int height) {
        if (initialized) {
            return;
        }
        long start = System.currentTimeMillis();
        initCommandQueue();
        assetManager = new VulkanAssets(vulkan, memoryAllocator, queue);
        pipelineManager = new VulkanPipelines(vulkan, this);
        presentComplete = vulkan.createSemaphore(new SemaphoreCreateInfo());
        drawComplete = vulkan.createSemaphore(new SemaphoreCreateInfo());
        swapBufferRenderPass = createSwapBuffer(configuration);
        // toTextureRenderPass = createTextureRenderPass();
        initialized = true;
        surfaceConfig = configuration;
        Logger.d(getClass(), "Init took " + Integer.toString((int) (System.currentTimeMillis() - start)) + " millis");
    }

    private void initCommandQueue() {
        queue = getBackend().createQueue(getBackend().getQueueFamilyProperties(),
                getBackend().createCommandPool(getBackend().getQueueFamilyProperties(), 20));
    }

    private RenderPass createSwapBuffer(SurfaceConfiguration configuration) {
        Vulkan10Backend<?> backend = getBackend();
        Extent2D swapExtent = new Extent2D(100, 100);
        ArrayList<ImageUsageFlagBits> usageFlags = getSwapChainUsage();
        if (backend.getSurfaceCapabilities() != null) {
            swapExtent = backend.selectSwapExtent(backend.getSurfaceCapabilities());
            Logger.d(getClass(), "Swapchain extent selected: " + swapExtent);
            backend.getKHRSwapchain().createSwapchainKHR(backend, swapExtent, usageFlags.toArray(
                    new ImageUsageFlagBits[0]));
            Logger.d(getClass(), "Created swapchain with " + backend.getKHRSwapchain().getImageCount() + " images");
            SampleCountFlagBit samples = checkSampleCount(configuration);
            RenderPassCreateInfo renderPassInfo = RenderPassCreateInfo.createRenderPass(samples, backend
                    .getSurfaceFormat(), ImageFormat.VK_FORMAT_D16_UNORM);

            ImageView multisampleView =
                    createMultisampleBuffer(renderPassInfo.attachments[SubpassDescription2.COLOR_ATTACHMENT_INDEX],
                            ImageUsageFlagBits.VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT,
                            ImageUsageFlagBits.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
            viewport = new Rect2D(swapExtent);
            ImageView depthView = createDepthBuffer(
                    renderPassInfo.attachments[SubpassDescription2.DEPTH_ATTACHMENT_INDEX], swapExtent);
            RenderPass renderPass = backend.createRenderPass(renderPassInfo);
            KHRSwapchain swapChain = getBackend().getKHRSwapchain();
            swapBuffer = createFrameBuffers(swapChain, multisampleView, depthView, renderPass);
            return renderPass;
        } else {
            Vulkan10.SurfaceFormat format = new SurfaceFormat(Vulkan10.Format.VK_FORMAT_R8G8B8A8_UNORM,
                    ColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
            RenderPassCreateInfo renderPassInfo = RenderPassCreateInfo.createRenderPass(
                    SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT, format,
                    ImageFormat.VK_FORMAT_D16_UNORM);
            ImageView multisampleView =
                    createMultisampleBuffer(renderPassInfo.attachments[SubpassDescription2.COLOR_ATTACHMENT_INDEX],
                            ImageUsageFlagBits.VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT,
                            ImageUsageFlagBits.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
            viewport = new Rect2D(swapExtent);
            ImageView depthView = createDepthBuffer(
                    renderPassInfo.attachments[SubpassDescription2.DEPTH_ATTACHMENT_INDEX], swapExtent);
            RenderPass renderPass = backend.createRenderPass(renderPassInfo);
            // swapBuffer = createHeadlessFrameBuffers(format, null, depthView, renderPass, swapExtent);
            return renderPass;
        }

    }

    private ArrayList<ImageUsageFlagBits> getSwapChainUsage() {
        ArrayList<ImageUsageFlagBits> usage = new ArrayList<Vulkan10.ImageUsageFlagBits>();
        String swapChainUsage = Settings.getInstance().getProperty(BackendStringProperties.SWAPCHAIN_USAGE);
        if (swapChainUsage != null) {
            StringTokenizer st = new StringTokenizer(swapChainUsage, ",");
            while (st.hasMoreTokens()) {
                String flagStr = st.nextToken();
                ImageUsageFlagBits flag = ImageUsageFlagBits.get(flagStr);
                if (flag == null) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid usage flag: "
                            + flagStr);
                }
                usage.add(flag);
            }
        }
        usage.add(ImageUsageFlagBits.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
        return usage;
    }

    private SampleCountFlagBit checkSampleCount(SurfaceConfiguration configuration) {
        SampleCountFlagBit samples = SampleCountFlagBit.get(configuration.samples);
        while (samples.value > 1) {
            // Make sure both color and depth buffer supports requested samples.
            DeviceLimits limits = getBackend().getSelectedDevice().getProperties().getLimits();
            if (BitFlags.contains(limits.getFramebufferColorSampleCounts(), samples.value)
                    && BitFlags.contains(limits.getFramebufferDepthSampleCounts(), samples.value)) {
                return samples;
            }
            Vulkan10.SampleCountFlagBit[] framebufferSamples = limits.getFramebufferColorSampleCounts();
            // Requested samples that are not supported.
            samples = framebufferSamples[framebufferSamples.length - 1];
        }
        return samples;
    }

    private ImageView createMultisampleBuffer(AttachmentDescription attachment, ImageUsageFlagBits... usage) {
        if (attachment.samples == SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT) {
            return null;
        }
        KHRSwapchain swapChain = getBackend().getKHRSwapchain();
        int formatValue = swapChain.getCurrentImage().getFormatValue();
        ImageCreateInfo info = new ImageCreateInfo(formatValue, swapChain.getExtent(), usage,
                attachment.samples);
        ImageMemory color =
                memoryAllocator.allocateImageMemory(info, MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        ImageSubresourceRange subresource = new ImageSubresourceRange(ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT, 0,
                1, 0, 1);
        ImageView colorView = getBackend().createImageView(new ImageViewCreateInfo(color.getImage(),
                ImageViewType.VK_IMAGE_VIEW_TYPE_2D, formatValue,
                new ComponentMapping(ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY,
                        ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY, ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY,
                        ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY),
                subresource));
        queue.queueBegin();
        queue.cmdPipelineBarrier(color.getImage(), colorView.image.getLayout(subresource),
                attachment.finalLayout, subresource);
        queue.queueWaitIdle();
        return colorView;
    }

    private ImageView createDepthBuffer(AttachmentDescription attachment, Extent2D extent) {
        ImageCreateInfo info = new ImageCreateInfo(attachment.format.value, extent,
                ImageUsageFlagBits.getBitFlags(ImageUsageFlagBits.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                        ImageUsageFlagBits.VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT),
                attachment.samples);
        ImageMemory depth =
                memoryAllocator.allocateImageMemory(info, MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        ImageSubresourceRange subresource = new ImageSubresourceRange(ImageAspectFlagBit.VK_IMAGE_ASPECT_DEPTH_BIT, 0,
                1, 0, 1);
        ImageView depthView = getBackend().createImageView(new ImageViewCreateInfo(depth.getImage(),
                ImageViewType.VK_IMAGE_VIEW_TYPE_2D, attachment.format.value,
                new ComponentMapping(ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY,
                        ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY, ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY,
                        ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY),
                subresource));
        queue.queueBegin();
        queue.cmdPipelineBarrier(depth.getImage(), depthView.image.getLayout(subresource),
                attachment.finalLayout, subresource);
        queue.queueWaitIdle();
        return depthView;
    }

    private ImageView createResolveBuffer(AttachmentDescription attachment) {
        KHRSwapchain swapChain = getBackend().getKHRSwapchain();
        int formatValue = swapChain.getCurrentImage().getFormatValue();
        ImageCreateInfo info = new ImageCreateInfo(formatValue, swapChain.getExtent(),
                ImageUsageFlagBits.getBitFlags(ImageUsageFlagBits.VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT,
                        ImageUsageFlagBits.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT),
                attachment.samples);
        ImageMemory resolve = memoryAllocator.allocateImageMemory(info,
                MemoryPropertyFlagBit.getBitFlags(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT));
        ImageSubresourceRange subresource = new ImageSubresourceRange(ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT, 0,
                1, 0, 1);
        ImageView resolveView = getBackend().createImageView(new ImageViewCreateInfo(resolve.getImage(),
                ImageViewType.VK_IMAGE_VIEW_TYPE_2D, formatValue,
                new ComponentMapping(ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY,
                        ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY, ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY,
                        ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY),
                subresource));
        queue.queueBegin();
        queue.cmdPipelineBarrier(resolve.getImage(), resolveView.image.getLayout(subresource),
                ImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, subresource);
        queue.queueWaitIdle();
        return resolveView;
    }

    private SwapBuffer createFrameBuffers(KHRSwapchain<Queue> swapChain, ImageView multisampleView, ImageView depthView,
            RenderPass renderPass) {
        Extent2D extent = swapChain.getExtent();
        if (extent.width != depthView.image.getExtent().width || extent.height != depthView.image.getExtent().height) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + "Sizes of depth and swapchain must match");
        }
        FramebufferCreateInfo[] createInfo = new FramebufferCreateInfo[swapChain.getImageCount()];
        AttachmentDescription[] attachmentDescription = renderPass.getRenderPassCreateInfo().attachments;
        ImageView[] attachments = new ImageView[attachmentDescription.length];
        ImageView colorView;
        attachments[SubpassDescription2.DEPTH_ATTACHMENT_INDEX] = depthView;
        for (int i = 0; i < swapChain.getImageCount(); i++) {
            colorView = multisampleView != null ? multisampleView : swapChain.getImageView(i);
            attachments[SubpassDescription2.COLOR_ATTACHMENT_INDEX] = colorView;
            if (attachments.length > SubpassDescription2.RESOLVE_ATTACHMENT_INDEX) {
                attachments[SubpassDescription2.RESOLVE_ATTACHMENT_INDEX] = swapChain.getImageView(i);
            }
            createInfo[i] = new FramebufferCreateInfo(null, renderPass, attachments, extent.width,
                    extent.height, 1);
        }
        FrameBuffer[] frameBuffers = getBackend().createFrameBuffers(createInfo);
        ClearValue[] clearValues = new ClearValue[2];
        // This will get clear color values from Settings
        ClearColorValue clearColor = new ClearColorValue((int[]) null);
        ClearDepthStencilValue clearDepth = new ClearDepthStencilValue(VulkanPipelines.MAX_DEPTH, 0);
        clearValues[0] = new ClearValue(clearColor, clearDepth);
        clearValues[1] = new ClearValue(clearColor, clearDepth);
        SwapBuffer swap = new SwapBuffer(swapChain.getCreateInfo().surfaceFormat, swapChain.getExtent(), frameBuffers,
                clearValues);
        return swap;
    }

    private SwapBuffer createHeadlessFrameBuffers(SurfaceFormat format, ImageView colorView, ImageView depthView,
            RenderPass renderPass, Extent2D extent) {
        if (extent.width != depthView.image.getExtent().width || extent.height != depthView.image.getExtent().height) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + "Sizes of depth and swapchain must match");
        }
        AttachmentDescription[] attachmentDescription = renderPass.getRenderPassCreateInfo().attachments;
        ImageView[] attachments = new ImageView[attachmentDescription.length];
        attachments[SubpassDescription2.DEPTH_ATTACHMENT_INDEX] = depthView;
        attachments[SubpassDescription2.COLOR_ATTACHMENT_INDEX] = colorView;
        FramebufferCreateInfo createInfo = new FramebufferCreateInfo(null, renderPass, attachments, extent.width,
                extent.height, 1);
        FrameBuffer[] frameBuffers = getBackend().createFrameBuffers(createInfo);
        ClearValue[] clearValues = new ClearValue[2];
        // This will get clear color values from Settings
        ClearColorValue clearColor = new ClearColorValue((int[]) null);
        ClearDepthStencilValue clearDepth = new ClearDepthStencilValue(VulkanPipelines.MAX_DEPTH, 0);
        clearValues[0] = new ClearValue(clearColor, clearDepth);
        clearValues[1] = new ClearValue(clearColor, clearDepth);
        SwapBuffer swap = new SwapBuffer(format, extent, frameBuffers, clearValues);
        return swap;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void setProjection(float[] matrix, int index) {
    }

    @Override
    public float beginFrame() {
        long current = System.currentTimeMillis();
        if (prevFrameTime != 0) {
            deltaTime = (float) (current - prevFrameTime) / 1000;
        }
        prevFrameTime = current;
        queue.queueBegin();

        int result =
                getBackend().getKHRSwapchain().acquireNextImageKHR(presentComplete, swapBuffer.getCurrentIndexBuffer());
        if (result != Result.VK_SUCCESS.value) {
            Logger.d(getClass(), "Result is NO SUCCESS");
            return 0;
        }
        ImageView imageView = getBackend().getKHRSwapchain().getImageView(swapBuffer.getCurrentImage());
        ImageMemoryBarrier beginBarrier = new ImageMemoryBarrier(
                AccessFlagBit.getBitFlags(AccessFlagBit.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT),
                AccessFlagBit.getBitFlags(AccessFlagBit.VK_ACCESS_MEMORY_READ_BIT),
                imageView.image.getLayout(imageView.subresourceRange),
                ImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                0, 0, imageView.image, imageView.subresourceRange);
        queue.cmdPipelineBarrier(beginBarrier,
                PipelineStageFlagBit.getBitFlags(PipelineStageFlagBit.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT),
                PipelineStageFlagBit.getBitFlags(PipelineStageFlagBit.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT));
        beginBarrier.updateLayout();
        return deltaTime;
    }

    @Override
    public void endFrame() {
        ImageView imageView = getBackend().getKHRSwapchain().getImageView(swapBuffer.getCurrentImage());
        ImageMemoryBarrier swapBarrier = new ImageMemoryBarrier(
                AccessFlagBit.getBitFlags(AccessFlagBit.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT),
                AccessFlagBit.getBitFlags(AccessFlagBit.VK_ACCESS_MEMORY_READ_BIT),
                ImageLayout.VK_IMAGE_LAYOUT_UNDEFINED, ImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
                0, 0, imageView.image, imageView.subresourceRange);
        queue.cmdPipelineBarrier(swapBarrier,
                PipelineStageFlagBit.getBitFlags(PipelineStageFlagBit.VK_PIPELINE_STAGE_ALL_GRAPHICS_BIT),
                PipelineStageFlagBit.getBitFlags(PipelineStageFlagBit.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT));
        queue.queueEnd();
        queue.queueSubmit(presentComplete, drawComplete,
                PipelineStageFlagBit.getBitFlags(PipelineStageFlagBit.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
        KHRSwapchain swapChain = getBackend().getKHRSwapchain();
        if (getBackend().getSurfaceFormat().getColorSpace().isPQColorSpace()) {
            // Set hdr metadata
            HDRMetadata meta = new HDRMetadata();
            getBackend().getEXTHDRMetadata().setHDRMetadataEXT(swapChain, meta);
        }
        int result = swapChain.queuePresentKHR(queue, drawComplete, swapBuffer.getCurrentIndexBuffer());
        swapBarrier.updateLayout();
        queue.queueWaitIdle();
        DefaultPeriodicLogger.getInstance().update();
    }

    @Override
    public Renderers getVersion() {
        return version;
    }

    @Override
    public SwapBuffer getSwapBuffer() {
        return swapBuffer;
    }

    @Override
    public Queue getQueue() {
        return queue;
    }

    @Override
    public DeviceMemory getBufferFactory() {
        return memoryAllocator;
    }

    @Override
    public Assets getAssets() {
        return assetManager;
    }

    @Override
    public void bindDescriptorSets(Subtype type, DescriptorBuffers buffers, Queue renderQueue,
            DescriptorSetTarget... targets) {
        if (targets == null || targets.length < 1) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Targets array is null or empty");
        }
        for (DescriptorSetTarget target : targets) {
            // Do not bind dynamic buffer here
            BindBuffer b = buffers.getBuffer(target);
            if (b == null || (b != null && b.getDynamicCount() == 0)) {
                getPipelines().cmdBindDescriptorSets(type, renderQueue, null, target);
            }
        }
    }

    @Override
    public void beginRenderPass() {
        // Render the default scene.
        queue.queueBegin();
        queue.cmdBeginRenderPass(swapBuffer.getRenderPassBeginInfo());
        queue.cmdSetViewport(viewport, 0, 1);
        queue.cmdSetScissor(viewport);
    }

    @Override
    public void endRenderPass() {
        queue.cmdEndRenderPass();
    }

    @Override
    public void copyBuffer(BindBuffer buffer) {
        if (buffer.getBuffer().size <= Queue.UPDATE_BUFFER_MAX_BYTES) {
            getBufferFactory().updateBuffer(buffer.getBackingBuffer(), buffer.getBuffer(), queue);
        } else {
            getBufferFactory().copyToDeviceMemory(buffer.getBackingBuffer(),
                    buffer.getBuffer(), queue);
        }
    }

    @Override
    public void render(VulkanRenderableScene scene, Queue renderQueue, DescriptorBuffers<?> descriptorBuffers) {
        boundPipelineHash = Constants.NO_VALUE;
        DrawCallBundle<IndirectDrawCalls> drawBundle = scene.getDrawCallBundle();
        BindBuffer buffer = descriptorBuffers.getBuffer(GltfDescriptorSetTarget.MATRIX);
        getPipelines().cmdBindDescriptorSets(Gltf2GraphicsShader.GraphicsShaderType.GLTF2,
                renderQueue, buffer.getDynamicOffsets(), GltfDescriptorSetTarget.MATRIX);
        for (IndirectDrawCalls dc : drawBundle.getAllDrawCalls()) {
            int pipelineHash = dc.getPipelineHash();
            GraphicsPipeline pipeline = pipelineManager.getPipeline(pipelineHash);
            renderQueue.cmdBindPipeline(pipeline, PipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS);
            dc.drawIndirect(renderQueue);
        }
    }

    @Override
    public void prepareFrameData(VulkanRenderableScene scene, GltfStorageBuffers buffers) {
        buffers.setDynamicStorage(scene, camera);
        memoryAllocator.uploadBuffers(queue, buffers, GltfDescriptorSetTarget.getTargets(SetType.UNIFORM_TYPE));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DescriptorBuffers<MeshShader> prepareFrameData(MeshShader shader, Transform sceneTransform) {
        if (shader != null) {
            // Todo - remove and use global uniform data shared with gltf
            mvpMatrices.setViewProjectionMatrices(camera, vulkanDepthMatrix);
            mvpMatrices.setMatrix(Matrices.MODEL, sceneTransform.getMatrix());
            FP16Convert matrixConverter = new FP16Convert(new short[Matrix.MATRIX_ELEMENTS * MVPMatrices.MATRIX_COUNT]);
            matrixConverter.convert(mvpMatrices.getMatrices());
            DescriptorBuffers<MeshShader> descriptorBuffers = (DescriptorBuffers<MeshShader>) getAssets()
                    .getStorageBuffers(shader.getShaderInfo().shaderType);
            descriptorBuffers.storeShortData(MeshDescriptorSetTarget.MATRIX, 0, matrixConverter.result);
            for (BindBuffer upload : descriptorBuffers.getBuffers()) {
                if (upload.getState() == BufferState.updated) {
                    if (!upload.isDescriptorSetUpdated()) {
                        throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message
                                + "DescriptorSet not upated");
                    }
                    if (upload.getBuffer().size > Queue.UPDATE_BUFFER_MAX_BYTES) {
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                                + "Buffer too large for in-frame update " + upload.getBuffer().size);
                    }
                    memoryAllocator.uploadBuffer(queue, upload);
                }
            }
            return descriptorBuffers;
        }
        return null;
    }

    @Override
    public void setCamera(JSONCamera camera) {
        if (camera != null) {
            if (!camera.hasProjectionMatrix()) {
                // Make sure aspect is set
                if (camera.getPerspective().getAspectRatio() <= 0) {
                    // For now just fetch the renderpassbegin info and set aspect based on framebuffer.
                    // Needs to be adopted when other render targets are suported
                    Extent2D size = swapBuffer.getRenderArea().extent;
                    camera.setupProjection(size.width, size.height);
                } else {
                    Extent2D size = swapBuffer.getRenderArea().extent;
                    camera.setupProjection(size.width, size.height);
                }
            }
        }
        this.camera = camera;
    }

    @Override
    public void render(JSONNode<VulkanMesh> node, DescriptorBuffers<?> uniforms, Queue renderQueue) {
        if (node.getMeshIndex() >= 0) {
            renderMesh(node, uniforms, renderQueue);
        }
        // Render children.
        renderNodes(node.getChildren(), uniforms, renderQueue);
        // mvpMatrices.pop(Matrices.MODEL);
    }

    private void renderNodes(JSONNode<VulkanMesh>[] children, DescriptorBuffers<?> uniforms, Queue renderQueue) {
        if (children != null && children.length > 0) {
            for (JSONNode<VulkanMesh> n : children) {
                render(n, uniforms, renderQueue);
            }
        }
    }

    private void renderMesh(JSONNode<VulkanMesh> node, DescriptorBuffers<?> descriptorBuffers, Queue renderQueue) {
        VulkanMesh mesh = node.getMesh();
        if (mesh != null) {
            VertexMemory vertexMemory = getAssets().getVertexBuffers(node);
            VulkanPrimitive[] primitives = mesh.getPrimitives();
            if (primitives != null) {
                for (VulkanPrimitive p : primitives) {
                    renderPrimitive(p, vertexMemory, descriptorBuffers, renderQueue);
                }
            }
        }

    }

    private void renderPrimitive(VulkanPrimitive primitive, VertexMemory vertexMemory,
            DescriptorBuffers<?> descriptorBuffers, Queue renderQueue) {
        // TODO - optimize for small number of pipelines, one pipeline per model would be best.
        int hash = primitive.getPipelineHash();
        if (boundPipelineHash != hash) {
            GraphicsPipeline pipeline = pipelineManager.getPipeline(primitive.getPipelineHash());
            renderQueue.cmdBindPipeline(pipeline, PipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS);
            boundPipelineHash = hash;
        }
        BindVertexBuffers bindBuffers = primitive.getBindBuffers();
        renderQueue.cmdBindVertexBuffers(bindBuffers.firstBinding, bindBuffers.getBuffers(), bindBuffers.getOffsets());
        MemoryBuffer indices = primitive.getIndicesBuffer();
        int count = primitive.getDrawCount();
        if (indices != null) {
            long buffer = indices.getPointer();
            IndexType type = GltfUtils.getFromGltfType(primitive.getIndicesType());
            renderQueue.cmdBindIndexBuffer(buffer, primitive.getIndicesOffset(), type);
            renderQueue.cmdDrawIndexed(count, 1, 0, 0, primitive.streamVertexIndex);
            renderLogger.logIndexedDraw(count);
        } else {
            count = primitive.getAccessor(Attributes.POSITION).getCount();
            renderQueue.cmdDraw(count, 1, 0, primitive.streamVertexIndex);
            renderLogger.logArrayDraw(count);
        }
    }

    @Override
    public Pipelines getPipelines() {
        return pipelineManager;
    }

    @Override
    public Vulkan12Backend<?> getBackend() {
        return vulkan;
    }

    @Override
    public RenderPass getRenderPass() {
        return swapBufferRenderPass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void drawMeshShader(MeshShader<?> meshShader, DescriptorBuffers<?> buffers, Queue q) {
        if (meshShader != null) {
            GraphicsPipeline pipeline = pipelineManager.getPipeline(meshShader.getShaderInfo().meshShaderType);
            q.cmdBindPipeline(pipeline, PipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS);
            EXTMeshShader<Queue> extension = (EXTMeshShader<Queue>) getBackend().getEXTMeshShader();
            int[] groupCounts = null;
            if (meshShader.hasStage(Stage.TASK)) {
                groupCounts = meshShader.getShaderInfo().getWorkGroupCounts(Stage.TASK);
            } else {
                groupCounts = meshShader.getShaderInfo().getWorkGroupCounts(Stage.MESH);
            }
            extension.drawMeshTasksEXT(q, groupCounts[0], groupCounts[1], groupCounts[2]);
        }
    }

    @Override
    public void invokeComputeShader(ComputeShader computeShader, DescriptorBuffers<?> buffers, Queue q) {
        if (computeShader != null) {
            ComputePipeline p = pipelineManager.getPipeline(computeShader.getShaderInfo().shaderType);
            q.cmdBindPipeline(p, PipelineBindPoint.VK_PIPELINE_BIND_POINT_COMPUTE);
            int[] workSize = computeShader.getWorkGroupCounts();
            q.cmdDispatch(workSize[0], workSize[1], workSize[2]);
        }
    }

}

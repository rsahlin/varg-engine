
package org.varg.vulkan;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONTexture.ComponentMapping;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.varg.BackendException;
import org.varg.assets.TextureDescriptor;
import org.varg.renderer.Renderers;
import org.varg.shader.ComputeShader;
import org.varg.shader.Shader;
import org.varg.shader.ShaderBinary;
import org.varg.vulkan.Vulkan10.Format;
import org.varg.vulkan.Vulkan10.FormatFeatureFlagBits;
import org.varg.vulkan.Vulkan10.ImageTiling;
import org.varg.vulkan.Vulkan10.PresentModeKHR;
import org.varg.vulkan.Vulkan10.QueryResultFlagBits;
import org.varg.vulkan.Vulkan10.QueueFlagBit;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.VulkanBackend.VulkanDeviceSelector;
import org.varg.vulkan.descriptor.DescriptorBufferInfo;
import org.varg.vulkan.descriptor.DescriptorPoolCreateInfo;
import org.varg.vulkan.descriptor.DescriptorSet;
import org.varg.vulkan.descriptor.DescriptorSetLayout;
import org.varg.vulkan.descriptor.DescriptorSetLayoutCreateInfo;
import org.varg.vulkan.extensions.EXTHDRMetadata;
import org.varg.vulkan.extensions.EXTMeshShader;
import org.varg.vulkan.extensions.KHRAccelerationStructure;
import org.varg.vulkan.extensions.KHRFragmentShadingRate;
import org.varg.vulkan.extensions.KHRRayTracingPipeline;
import org.varg.vulkan.extensions.KHRSwapchain;
import org.varg.vulkan.framebuffer.FrameBuffer;
import org.varg.vulkan.framebuffer.FramebufferCreateInfo;
import org.varg.vulkan.image.Image;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.image.ImageViewCreateInfo;
import org.varg.vulkan.pipeline.ComputePipeline;
import org.varg.vulkan.pipeline.ComputePipelineCreateInfo;
import org.varg.vulkan.pipeline.GraphicsPipeline;
import org.varg.vulkan.pipeline.GraphicsPipelineCreateInfo;
import org.varg.vulkan.pipeline.PipelineLayout;
import org.varg.vulkan.pipeline.PipelineLayoutCreateInfo;
import org.varg.vulkan.renderpass.RenderPass;
import org.varg.vulkan.renderpass.RenderPassCreateInfo;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.FormatProperties;
import org.varg.vulkan.structs.PhysicalDeviceFeatures;
import org.varg.vulkan.structs.PhysicalDeviceMemoryProperties;
import org.varg.vulkan.structs.PhysicalDeviceProperties.PhysicalDeviceType;
import org.varg.vulkan.structs.QueryPool;
import org.varg.vulkan.structs.QueryPoolCreateInfo;
import org.varg.vulkan.structs.QueueFamilyProperties;
import org.varg.vulkan.structs.Sampler;
import org.varg.vulkan.structs.SamplerCreateInfo;
import org.varg.vulkan.structs.Semaphore;
import org.varg.vulkan.structs.SemaphoreCreateInfo;
import org.varg.vulkan.structs.ShaderModule;
import org.varg.vulkan.structs.SubresourceLayout;
import org.varg.vulkan.structs.SurfaceCapabilitiesKHR;
import org.varg.window.J2SEWindow.WindowHandle;

/**
 * Backend for Vulkan version 1.0 funtionality
 * 
 * @param T The platform device instance object
 *
 */
public abstract class Vulkan10Backend<T> extends VulkanBackend implements VulkanDeviceSelector {

    /**
     * Internal interface for device selection
     *
     */
    public interface CreateDevice {
        Features getRequestedDeviceFeatures(Features availableFeatures);
    }

    protected LogicalDevice<T> logicalDevice;
    protected SurfaceCapabilitiesKHR surfaceCapabilities;
    protected FormatProperties formatProperties;
    protected SurfaceFormat surfaceFormat;
    protected PresentModeKHR presentMode;
    protected PhysicalDevice selectedDevice;
    protected PhysicalDeviceMemoryProperties memoryProperties;
    protected QueueFamilyProperties queueFamily;
    protected SurfaceFormatChooser surfaceFormatChooser = new SurfaceFormatChooserImplementation();

    protected KHRSwapchain<?> khrSwapchainExtension;
    protected EXTHDRMetadata extHdrMetadata;
    protected EXTMeshShader<?> extMeshShader;
    protected KHRFragmentShadingRate khrFragmentShadingRate;
    protected KHRAccelerationStructure<?> khrAccelerationStructure;
    protected KHRRayTracingPipeline<?> khrRayTracingPipeline;

    /**
     * 
     * @param version
     */
    protected Vulkan10Backend(Renderers version) {
        super(version);
    }

    /**
     * This method MUST be called by subclasses - after they have done
     * initialization. This method will trigger the device fetch and selection.
     */
    protected void createLogicalDevice(CreateDevice callback, WindowHandle window) throws BackendException {
        PhysicalDevice[] devices = fetchDevices();
        if (devices == null || devices.length == 0) {
            throw new IllegalArgumentException("Failed to fetch devices");
        }
        Logger.d(getClass(), "Found " + devices.length + " devices.");
        for (int i = 0; i < devices.length; i++) {
            Logger.d(getClass(), "Found device: " + devices[i]);
        }

        selectedDevice = selectDevice(devices, window);
        if (selectedDevice == null) {
            throw new IllegalArgumentException("No suitable Vulkan physical device");
        }
        Logger.d(getClass(),
                "Selected device: " + selectedDevice.getProperties().getDeviceName());
        queueFamily = selectQueueInstance(selectedDevice, window);
        memoryProperties = createMemoryProperties(selectedDevice);
        Features requestedFeatures = callback.getRequestedDeviceFeatures(selectedDevice.getFeatures());
        logicalDevice =
                createLogicalDevice(selectedDevice, queueFamily, requestedFeatures);
        if (window.handle != WindowHandle.HEADLESS) {
            surfaceFormat = surfaceFormatChooser.selectSurfaceFormat(getSurfaceFormats(selectedDevice));
            Logger.d(getClass(), "Selected surface format:" + surfaceFormat);
            surfaceCapabilities = getSurfaceCapabilities(selectedDevice);
            presentMode = selectPresentMode(surfaceFormat, getPresentModes(selectedDevice));
            Logger.d(getClass(), "Selected present mode :" + presentMode);
        }
    }

    /**
     * Creates the backend - must only be called once
     */
    protected abstract void createBackend(CreateDevice callback) throws BackendException;

    /**
     * Called by the constructor to fetch physical devices in system.
     * 
     * @return
     */
    protected abstract PhysicalDevice[] fetchDevices();

    /**
     * Creates the requested physical device features
     * 
     * @return
     */
    protected abstract Features createRequestedPhysicalDeviceFeatures(PhysicalDevice device);

    /**
     * Creates the logical device using the selected queue, this method is with the
     * result of {@link #selectDevice(PhysicalDevice[])}
     * {@link #selectQueueInstance(PhysicalDevice)} has b
     * 
     * @param device The device returned by
     * {@link #selectDevice(PhysicalDevice[])}
     * @param selectedQueue The queue family returned by
     * {@link #selectQueueInstance(PhysicalDevice)}
     * @param requestedFeatures Physical device features to enable when creating the logical device.
     * @throws IllegalArgumentException If device or selectedQueue is null
     */
    protected abstract LogicalDevice<T> createLogicalDevice(@NonNull PhysicalDevice device,
            @NonNull QueueFamilyProperties selectedQueue, @NonNull Features requestedFeatures);

    /**
     * Create the queue instance for the selected queue family
     * 
     * @param selectedQueue
     * @param commandBuffers
     */
    public abstract Queue createQueue(@NonNull QueueFamilyProperties selectedQueue,
            @NonNull CommandBuffers<?> commandBuffers);

    /**
     * Returns an array of available surface formats
     * 
     * @param device
     * @return
     */
    protected abstract ArrayList<SurfaceFormat> getSurfaceFormats(@NonNull PhysicalDevice device);

    /**
     * Returns an array with the available present modes
     * 
     * @param device
     * @return
     */
    protected abstract ArrayList<PresentModeKHR> getPresentModes(@NonNull PhysicalDevice device);

    /**
     * Select the swapchain extent
     * 
     * @param surfaceCaps
     * @return
     */
    public abstract Extent2D selectSwapExtent(@NonNull SurfaceCapabilitiesKHR surfaceCaps);

    /**
     * Fetches the surface capabilities from the selected (physical) device
     * 
     * @param device
     * @return
     */
    protected abstract SurfaceCapabilitiesKHR getSurfaceCapabilities(@NonNull PhysicalDevice device);

    /**
     * Fetches the format properties for the physical device
     * 
     * @param device
     * @param format
     * @return
     */
    public abstract FormatProperties getFormatProperties(@NonNull PhysicalDevice device, int formatValue);

    /**
     * Returns an array with the supported formats for the specified tiling
     * 
     * @param features
     * @param tiling
     * @return
     */
    public abstract Format[] getSupportedFormats(ImageTiling tiling, FormatFeatureFlagBits... features);

    /**
     * Returns the physical device features.
     * 
     * @return
     */
    public abstract PhysicalDeviceFeatures getPhysicalDeviceFeatures();

    /**
     * Fetches the subresource layout for the image
     * 
     * @param vkImage
     * @return
     */
    public abstract SubresourceLayout getSubresourceLayout(long vkImage);

    /**
     * Creates imageviews for the specified images
     * 
     */
    public abstract ImageView[] createImageViews(Image[] images, ComponentMapping components, int mipLevels,
            int arrayLayers);

    public abstract FrameBuffer[] createFrameBuffers(FramebufferCreateInfo... createInfo);

    public abstract RenderPass createRenderPass(RenderPassCreateInfo createInfo);

    /**
     * Creates an ImageView based on the createinfo
     * 
     * @param createInfo
     * @return
     */
    public abstract ImageView createImageView(ImageViewCreateInfo createInfo);

    /**
     * Creates the memory properties
     * 
     * @param device
     * @return
     */
    protected abstract PhysicalDeviceMemoryProperties createMemoryProperties(PhysicalDevice device);

    /**
     * Creates the shadermodule
     * 
     * @param binary
     * @return
     */
    public abstract ShaderModule createShaderModule(ShaderBinary binary);

    /**
     * Creates the graphics shader pipeline
     * 
     * @param createInfo
     * @param renderPass
     * @return
     */
    public abstract GraphicsPipeline createGraphicsPipeline(GraphicsPipelineCreateInfo info, RenderPass renderPass,
            Shader graphicsShader);

    /**
     * Creates the compute shader pipeline
     * 
     * @param info
     * @param computeShader
     * @return
     */
    public abstract ComputePipeline createComputePipeline(ComputePipelineCreateInfo info, ComputeShader computeShader);

    /**
     * Creates the pipeline layout
     * 
     * @param layoutInfo
     * @return
     */
    public abstract PipelineLayout createPipelineLayout(PipelineLayoutCreateInfo layoutInfo);

    /**
     * Destroys the pipeline layout
     * 
     * @param layout
     * @throws IllegalArgumentException If layout is already destroyed.
     */
    public abstract void destroyPipelineLayout(PipelineLayout layout);

    /**
     * Creates a commandbuffer pool for the queuefamily properties
     * 
     * @param queueFamily
     * @param commandBufferCount
     * @throws IllegalArgumentException If any of the parameters is null
     */
    public abstract CommandBuffers<?> createCommandPool(QueueFamilyProperties queueFamily, int commandBufferCount);

    /**
     * Creates the descriptor pool
     * 
     * @param poolInfo
     * @return
     */
    public abstract void createDescriptorPool(@NonNull DescriptorPoolCreateInfo poolInfo);

    /**
     * Creates and returns the descriptorset layout
     * 
     * @param layoutInfo
     * @return
     */
    public abstract DescriptorSetLayout createDescriptorSetLayout(@NonNull DescriptorSetLayoutCreateInfo layoutInfo);

    /**
     * Destroys the descriptorsetLayout
     * 
     * @param layout
     * @throws IllegalArgumentException If descriptorsetlayout already has been
     * destroyed
     */
    public abstract void destroyDescriptorSetLayout(@NonNull DescriptorSetLayout layout);

    /**
     * Returns the logical device - ie the device instance that has been created.
     * This is the device that MUST be used to check runtime support for features.
     * 
     * @return
     */
    public abstract LogicalDevice<?> getLogicalDevice();

    protected abstract PhysicalDeviceMemoryProperties getMemoryProperties();

    /**
     * Updates the descriptorset using descriptorimageinfo - use this for instance
     * for textures
     * 
     * @param descriptorImageInfos The image(s) to update
     * @param descriptorSet The descriptorset
     */
    public abstract void updateDescriptorSets(TextureDescriptor[] descriptorImageInfos, DescriptorSet descriptorSet);

    /**
     * Updates the descriptorset using descriptor buffer info - use this for
     * instance for uniform buffers
     * 
     * @param descriptorBufferInfos
     * @param descriptorSets
     */
    public abstract void updateDescriptorSets(DescriptorBufferInfo[] descriptorBufferInfos,
            DescriptorSet[] descriptorSets);

    /**
     * Allocates one or more descriptor sets using the flags and layoutBindings
     * 
     * @descriptorLayout
     * @return
     */
    public abstract DescriptorSet allocateDescriptorSet(DescriptorSetLayout descriptorLayout);

    /**
     * Returns the VK_KHR_swapchain extension object if enabled
     * 
     * @return
     */
    public abstract KHRSwapchain<?> getKHRSwapchain();

    /**
     * Returns the VK_EXT_hdr_metadata extension if enabled
     * 
     * @return
     */
    public abstract EXTHDRMetadata getEXTHDRMetadata();

    /**
     * Returns the VK_EXT_mesh_shader extension if enabled
     * 
     * @return
     */
    public abstract EXTMeshShader<?> getEXTMeshShader();

    /**
     * Returns the VK_KHR_fragment_shading_rate extension if enabled
     * 
     * @return
     */
    public abstract KHRFragmentShadingRate getKHRFragmentShadingRate();

    /**
     * Free the descriptorset
     * 
     * @param descriptorSet
     */
    public abstract void freeDescriptorSet(DescriptorSet descriptorSet);

    @Override
    public PhysicalDevice selectDevice(PhysicalDevice[] devices, WindowHandle window) {
        HashSet<PhysicalDevice> validDevices = new HashSet<>();
        for (PhysicalDevice d : devices) {
            for (QueueFamilyProperties qp : d.getQueueFamilyProperties()) {
                if (window.handle == WindowHandle.HEADLESS) {
                    validDevices.add(d);
                    break;
                } else if (qp.isSurfaceSupportsPresent()) {
                    validDevices.add(d);
                    break;
                }
            }
        }
        if (validDevices.size() == 0) {
            return null;
        }
        // Try to find discreet device.
        for (PhysicalDevice d : validDevices) {
            if (d.getProperties().getDeviceType()
                    == PhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                return d;
            }
        }
        // Try to find integrated device.
        for (PhysicalDevice d : validDevices) {
            if (d.getProperties().getDeviceType()
                    == PhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU) {
                return d;
            }
        }
        return validDevices.iterator().next();
    }

    @Override
    public QueueFamilyProperties selectQueueInstance(PhysicalDevice device, WindowHandle window) {
        for (QueueFamilyProperties qp : device.getQueueFamilyProperties()) {
            if (window.handle == WindowHandle.HEADLESS) {
                if (qp.getFlagsValue(QueueFlagBit.VK_QUEUE_GRAPHICS_BIT) && qp.getFlagsValue(
                        QueueFlagBit.VK_QUEUE_COMPUTE_BIT)) {
                    return qp;
                }
            } else if ((qp.isSurfaceSupportsPresent()) && qp.getFlagsValue(QueueFlagBit.VK_QUEUE_GRAPHICS_BIT) && qp
                    .getFlagsValue(QueueFlagBit.VK_QUEUE_COMPUTE_BIT)) {
                return qp;
            }
        }
        return null;
    }

    /**
     * Returns true if backend has support for a presentmode.
     * 
     * @param availableModes
     * @param mode
     * @return
     */
    protected boolean hasPresentMode(ArrayList<PresentModeKHR> availableModes, PresentModeKHR mode) {
        for (PresentModeKHR m : availableModes) {
            if (m == mode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the desired present mode
     * 
     * @param setSurfaceFormat
     * @param presentModes
     * @return
     */
    protected PresentModeKHR selectPresentMode(SurfaceFormat setSurfaceFormat, ArrayList<PresentModeKHR> presentModes) {
        if (hasPresentMode(presentModes, PresentModeKHR.VK_PRESENT_MODE_FIFO_RELAXED_KHR)) {
            return PresentModeKHR.VK_PRESENT_MODE_FIFO_RELAXED_KHR;
        }
        // Fallback to required presentmode
        return PresentModeKHR.VK_PRESENT_MODE_FIFO_KHR;
    }

    /**
     * Returns the selected device or throws exception if no device is selected
     * 
     * @return
     */
    public PhysicalDevice getSelectedDevice() {
        if (selectedDevice != null) {
            return selectedDevice;
        }
        throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "No selected Vulkan device");
    }

    /**
     * Creates a sampler object
     * 
     * @param samplerInfo
     * @return
     */
    public abstract Sampler createSampler(SamplerCreateInfo samplerInfo);

    public abstract Semaphore createSemaphore(SemaphoreCreateInfo createInfo);

    public abstract void destroySemaphore(Semaphore semaphore);

    /**
     * Creates a query pool
     * 
     * @param createInfo
     */
    public abstract int getQueryPoolResults(QueryPool queryPool, int firstQuery, int queryCount, ByteBuffer data, int stride, QueryResultFlagBits... flags);

    /**
     * Creates a query pool
     * 
     * @param createInfo
     */
    public abstract QueryPool createQueryPool(QueryPoolCreateInfo createInfo);

    /**
     * Returns the surface capabilities
     * 
     * @return
     */
    public SurfaceCapabilitiesKHR getSurfaceCapabilities() {
        return surfaceCapabilities;
    }

    /**
     * Returns the surface format
     * 
     * @return
     * @throws IllegalArgumentException If this method is called before
     * {@link #createLogicalDevice()} has bee called
     */
    public SurfaceFormat getSurfaceFormat() {
        if (surfaceFormat == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Null");
        }
        return surfaceFormat;
    }

    /**
     * Returns the presentmode
     * 
     * @return
     * @throws IllegalArgumentException If this method is called before
     * {@link #createLogicalDevice()} has bee called
     */
    public PresentModeKHR getPresentMode() {
        if (presentMode == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Null");
        }
        return presentMode;

    }

    /**
     * Returns the queuefamily properties
     * 
     * @return
     */
    public QueueFamilyProperties getQueueFamilyProperties() {
        if (queueFamily == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Null");
        }
        return queueFamily;

    }

}

package org.varg.lwjgl3.vulkan;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Hashtable;

import org.gltfio.gltf2.JSONTexture.ComponentMapping;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTConditionalRendering;
import org.lwjgl.vulkan.EXTDebugReport;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.EXTIndexTypeUint8;
import org.lwjgl.vulkan.EXTMeshShader;
import org.lwjgl.vulkan.EXTRobustness2;
import org.lwjgl.vulkan.KHRAccelerationStructure;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkAttachmentDescription2;
import org.lwjgl.vulkan.VkAttachmentReference2;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDebugReportCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkImageSubresource;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceAccelerationStructureFeaturesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceConditionalRenderingFeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceFragmentShadingRateFeaturesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceFragmentShadingRatePropertiesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceIndexTypeUint8FeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceMeshShaderFeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceMeshShaderPropertiesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;
import org.lwjgl.vulkan.VkPhysicalDeviceRayTracingPipelineFeaturesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceRayTracingPipelinePropertiesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceRobustness2FeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features;
import org.lwjgl.vulkan.VkPipelineCacheCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineFragmentShadingRateStateCreateInfoKHR;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkQueryPoolCreateInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkRenderPassCreateInfo2;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreTypeCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreWaitInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSpecializationInfo;
import org.lwjgl.vulkan.VkSpecializationMapEntry;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency2;
import org.lwjgl.vulkan.VkSubpassDescription2;
import org.lwjgl.vulkan.VkSubresourceLayout;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkTimelineSemaphoreSubmitInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import org.lwjgl.vulkan.VkWriteDescriptorSetAccelerationStructureKHR;
import org.varg.BackendException;
import org.varg.assets.TextureDescriptor;
import org.varg.lwjgl3.vulkan.LWJGLCommandBuffers.LWJGLCommandBuffer;
import org.varg.lwjgl3.vulkan.extensions.LWJGL3EXTMeshShader;
import org.varg.lwjgl3.vulkan.extensions.LWJGL3HDRMetadata;
import org.varg.lwjgl3.vulkan.extensions.LWJGL3KHRAccelerationStructure;
import org.varg.lwjgl3.vulkan.extensions.LWJGL3KHRFragmentShadingRate;
import org.varg.lwjgl3.vulkan.extensions.LWJGL3KHRRayTracingPipeline;
import org.varg.lwjgl3.vulkan.extensions.LWJGL3KHRSwapchain;
import org.varg.renderer.Renderers;
import org.varg.shader.ComputeShader;
import org.varg.shader.Shader;
import org.varg.shader.Shader.Stage;
import org.varg.shader.ShaderBinary;
import org.varg.vulkan.CommandBuffers;
import org.varg.vulkan.Features;
import org.varg.vulkan.LogicalDevice;
import org.varg.vulkan.PhysicalDevice;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.ColorSpaceKHR;
import org.varg.vulkan.Vulkan10.CompositeAlphaFlagBitsKHR;
import org.varg.vulkan.Vulkan10.DescriptorType;
import org.varg.vulkan.Vulkan10.Format;
import org.varg.vulkan.Vulkan10.FormatFeatureFlagBits;
import org.varg.vulkan.Vulkan10.ImageAspectFlagBit;
import org.varg.vulkan.Vulkan10.ImageTiling;
import org.varg.vulkan.Vulkan10.ImageUsageFlagBits;
import org.varg.vulkan.Vulkan10.ImageViewType;
import org.varg.vulkan.Vulkan10.PresentModeKHR;
import org.varg.vulkan.Vulkan10.QueryResultFlagBits;
import org.varg.vulkan.Vulkan10.QueueFlagBit;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.Vulkan10.SurfaceTransformFlagBitsKHR;
import org.varg.vulkan.Vulkan10.VulkanExtension;
import org.varg.vulkan.Vulkan12Backend;
import org.varg.vulkan.descriptor.AccelerationStructureDescriptorInfo;
import org.varg.vulkan.descriptor.DescriptorBufferInfo;
import org.varg.vulkan.descriptor.DescriptorImageInfo;
import org.varg.vulkan.descriptor.DescriptorPool;
import org.varg.vulkan.descriptor.DescriptorPoolCreateInfo;
import org.varg.vulkan.descriptor.DescriptorPoolSize;
import org.varg.vulkan.descriptor.DescriptorSet;
import org.varg.vulkan.descriptor.DescriptorSetLayout;
import org.varg.vulkan.descriptor.DescriptorSetLayoutBinding;
import org.varg.vulkan.descriptor.DescriptorSetLayoutCreateInfo;
import org.varg.vulkan.extensions.EXTHDRMetadata;
import org.varg.vulkan.extensions.EXTRobustness2.PhysicalDeviceRobustness2FeaturesEXT;
import org.varg.vulkan.extensions.KHRFragmentShadingRate;
import org.varg.vulkan.extensions.KHRFragmentShadingRate.PipelineFragmentShadingRateStateCreateInfoKHR;
import org.varg.vulkan.extensions.KHRRayTracingPipeline;
import org.varg.vulkan.extensions.KHRSwapchain;
import org.varg.vulkan.extensions.PhysicalDeviceAccelerationStructureFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceFragmentShadingRateFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceFragmentShadingRatePropertiesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderFeaturesEXT;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderPropertiesEXT;
import org.varg.vulkan.extensions.PhysicalDeviceRayTracingPipelineFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceRayTracingPipelinePropertiesKHR;
import org.varg.vulkan.framebuffer.FrameBuffer;
import org.varg.vulkan.framebuffer.FramebufferCreateInfo;
import org.varg.vulkan.image.Image;
import org.varg.vulkan.image.ImageSubresourceRange;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.image.ImageViewCreateInfo;
import org.varg.vulkan.pipeline.ComputePipeline;
import org.varg.vulkan.pipeline.ComputePipelineCreateInfo;
import org.varg.vulkan.pipeline.GraphicsPipeline;
import org.varg.vulkan.pipeline.GraphicsPipelineCreateInfo;
import org.varg.vulkan.pipeline.PipelineColorBlendAttachmentState;
import org.varg.vulkan.pipeline.PipelineLayout;
import org.varg.vulkan.pipeline.PipelineLayoutCreateInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationMapEntry;
import org.varg.vulkan.pipeline.PipelineVertexInputStateCreateInfo;
import org.varg.vulkan.renderpass.AttachmentDescription;
import org.varg.vulkan.renderpass.AttachmentReference;
import org.varg.vulkan.renderpass.RenderPass;
import org.varg.vulkan.renderpass.RenderPassCreateInfo;
import org.varg.vulkan.renderpass.SubpassDescription2;
import org.varg.vulkan.structs.DeviceLimits;
import org.varg.vulkan.structs.ExtensionProperties;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.Extent3D;
import org.varg.vulkan.structs.FormatProperties;
import org.varg.vulkan.structs.PhysicalDeviceFeatureExtensions;
import org.varg.vulkan.structs.PhysicalDeviceFeatures;
import org.varg.vulkan.structs.PhysicalDeviceMemoryProperties;
import org.varg.vulkan.structs.PhysicalDeviceProperties;
import org.varg.vulkan.structs.PhysicalDeviceVulkan11Features;
import org.varg.vulkan.structs.PhysicalDeviceVulkan12Features;
import org.varg.vulkan.structs.PhysicalDeviceVulkan13Features;
import org.varg.vulkan.structs.PlatformStruct;
import org.varg.vulkan.structs.PushConstantRange;
import org.varg.vulkan.structs.QueryPool;
import org.varg.vulkan.structs.QueryPoolCreateInfo;
import org.varg.vulkan.structs.QueueFamilyProperties;
import org.varg.vulkan.structs.Sampler;
import org.varg.vulkan.structs.SamplerCreateInfo;
import org.varg.vulkan.structs.Semaphore;
import org.varg.vulkan.structs.SemaphoreCreateInfo;
import org.varg.vulkan.structs.ShaderModule;
import org.varg.vulkan.structs.ShaderModuleCreateInfo;
import org.varg.vulkan.structs.SubmitInfo;
import org.varg.vulkan.structs.SubresourceLayout;
import org.varg.vulkan.structs.SurfaceCapabilitiesKHR;
import org.varg.vulkan.structs.TimelineSemaphore;
import org.varg.vulkan.structs.TimelineSemaphore.TimelineSemaphoreSubmitInfo;
import org.varg.vulkan.vertex.VertexInputAttributeDescription;
import org.varg.vulkan.vertex.VertexInputBindingDescription;
import org.varg.window.J2SEWindow.WindowHandle;

public class LWJGL3Vulkan12Backend extends Vulkan12Backend<VkDevice> {

    static final String VKLAYERKRHONOS_VALIDATION = "VK_LAYER_KHRONOS_validation";
    static final ByteBuffer KHRONOSVALIDATION = Buffers.createByteBuffer(VKLAYERKRHONOS_VALIDATION);

    PointerBuffer logicalDeviceExtensions;
    // Must be preserved otherwise pointerbuffer may point to collected memory.
    ByteBuffer[] logicalDeviceExtensionNames;
    final PointerBuffer instanceExtensions = MemoryUtil.memAllocPointer(16);
    final PointerBuffer validationLayers = MemoryUtil.memAllocPointer(16);

    VkInstance instance;
    long surface;
    final WindowHandle window;
    VkDevice deviceInstance;
    MemoryStack stack;

    /**
     * If created the descriptor pool is heres
     */
    DescriptorPool descriptorPool;

    public class Device implements PhysicalDevice {

        protected ExtensionProperties[] extensionProperties;
        private final VkPhysicalDevice device;
        private final DeviceProperties deviceProperties;
        private final Features deviceFeatures;
        private QueueFamilyProperties[] queueFamilyProperties;

        private Device(long deviceAdress, long surface) {
            if (deviceAdress == 0 || surface == 0 || instance == null) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
            }
            device = new VkPhysicalDevice(deviceAdress, instance);
            readExtensions(device);
            deviceProperties = new DeviceProperties(device, extensionProperties);
            PhysicalDeviceFeatures physicalFeatures = new LWJGLPhysicalDeviceVulkan13Features(device);
            PhysicalDeviceFeatureExtensions physicalFeatureExtensions = new LWJGLPhysicalDeviceFeatureExtensions(device);
            deviceFeatures = new Features(physicalFeatures, physicalFeatureExtensions, extensionProperties);
            readQueueProperties(device, surface);
        }

        protected void readQueueProperties(VkPhysicalDevice physicalDevice, long surfacePtr) {
            int[] queueCount = new int[1];
            VK12.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueCount, null);
            VkQueueFamilyProperties.Buffer queueProperties = VkQueueFamilyProperties.calloc(queueCount[0]);
            queueFamilyProperties = new QueueFamilyProperties[queueCount[0]];
            VK12.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueCount, queueProperties);
            queueProperties.rewind();
            for (int i = 0; i < queueFamilyProperties.length; i++) {
                VkExtent3D minImage = queueProperties.minImageTransferGranularity();
                QueueFlagBit[] flags = BitFlags.getBitFlags(queueProperties.queueFlags(), QueueFlagBit.values())
                        .toArray(new QueueFlagBit[0]);
                Extent3D extent = new Extent3D(minImage.width(), minImage.height(), minImage.depth());
                int[] supported = new int[1];
                if (surface != WindowHandle.HEADLESS) {
                    if (KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surfacePtr, supported)
                            != VK12.VK_SUCCESS) {
                        throw new IllegalArgumentException("Failed to get device surface support");
                    }
                }
                queueFamilyProperties[i] = new QueueFamilyProperties(i, flags, queueProperties.queueCount(),
                        queueProperties.timestampValidBits(), extent, (supported[0] == VK12.VK_TRUE));
            }
        }

        protected void readExtensions(VkPhysicalDevice physicalDevice) {
            IntBuffer ib = MemoryUtil.memAllocInt(1);
            VK12.vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, ib, null);
            if (ib.get(0) > 0) {
                int count = ib.get(0);
                extensionProperties = new ExtensionProperties[count];
                VkExtensionProperties.Buffer extensionsBuffer = VkExtensionProperties.calloc(count);
                VK12.vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, ib, extensionsBuffer);
                for (int i = 0; i < count; i++) {
                    extensionsBuffer.position(i);
                    extensionProperties[i] = new ExtensionProperties(extensionsBuffer.extensionNameString(),
                            extensionsBuffer.specVersion());
                }
            }
        }

        protected VkPhysicalDevice getVkPhysicalDevice() {
            return device;
        }

        @Override
        public String toString() {
            String result = deviceProperties.toString() + "\n";
            result += "Api version: " + device.getCapabilities().apiVersion + "\n";
            result += deviceFeatures.toString();
            result += "Queue support: \n";
            for (int i = 0; i < queueFamilyProperties.length; i++) {
                result += queueFamilyProperties[i].toString();
            }
            ExtensionProperties[] extensionProps = getExtensionProperties();
            if (extensionProps != null) {
                result += "Extension support: \n";
                for (ExtensionProperties ep : extensionProps) {
                    result += ep.getName() + " version " + ep.getSpecVersion() + "\n";
                }
            }
            return result;
        }

        @Override
        public PhysicalDeviceProperties getProperties() {
            return deviceProperties;
        }

        @Override
        public Features getFeatures() {
            return deviceFeatures;
        }

        @Override
        public QueueFamilyProperties[] getQueueFamilyProperties() {
            return queueFamilyProperties;
        }

        @Override
        public ExtensionProperties[] getExtensionProperties() {
            return extensionProperties;
        }

        @Override
        public ExtensionProperties getExtension(String extensionName) {
            if (extensionProperties != null) {
                for (ExtensionProperties ep : extensionProperties) {
                    if (extensionName.equalsIgnoreCase(ep.getName())) {
                        return ep;
                    }
                }
            }
            return null;
        }
    }

    public class DeviceProperties implements PhysicalDeviceProperties {

        private APIVersion apiVersion;
        private LWJGLVulkanLimits limits;
        private String deviceName;
        private PhysicalDeviceType deviceType;
        /**
         * Properties for extensions
         */
        private final Hashtable<VulkanExtension, PlatformStruct> propertiesTable = new Hashtable<>();

        @SuppressWarnings("checkstyle:linelength")
        public DeviceProperties(VkPhysicalDevice device, ExtensionProperties... extensions) {
            VkPhysicalDeviceProperties2 properties2 = VkPhysicalDeviceProperties2.calloc()
                    .sType(VK12.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROPERTIES_2);
            VkPhysicalDeviceMeshShaderPropertiesEXT vkMeshProperties = null;
            VkPhysicalDeviceFragmentShadingRatePropertiesKHR vkFragmentShadingRateProperties = null;
            VkPhysicalDeviceRayTracingPipelinePropertiesKHR vkRayTracingProperties = null;
            ExtensionProperties meshExtension = ExtensionProperties.get(org.varg.vulkan.Vulkan10.Extension.VK_EXT_mesh_shader.name(), extensions);
            ExtensionProperties fragmentShadingRateExtension = ExtensionProperties.get(Vulkan10.Extension.VK_KHR_fragment_shading_rate.name(), extensions);
            ExtensionProperties rayTracingExtension = ExtensionProperties.get(Vulkan10.Extension.VK_KHR_ray_tracing_pipeline.name(), extensions);
            long next = MemoryUtil.NULL;
            if (meshExtension != null) {
                vkMeshProperties = VkPhysicalDeviceMeshShaderPropertiesEXT.calloc()
                        .sType(EXTMeshShader.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MESH_SHADER_PROPERTIES_EXT)
                        .pNext(MemoryUtil.NULL);
                next = vkMeshProperties.address();
            }
            if (fragmentShadingRateExtension != null) {
                vkFragmentShadingRateProperties = VkPhysicalDeviceFragmentShadingRatePropertiesKHR.calloc()
                        .sType(org.lwjgl.vulkan.KHRFragmentShadingRate.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FRAGMENT_SHADING_RATE_PROPERTIES_KHR)
                        .pNext(next);
                next = vkFragmentShadingRateProperties.address();
            }
            if (rayTracingExtension != null) {
                vkRayTracingProperties = VkPhysicalDeviceRayTracingPipelinePropertiesKHR.calloc()
                        .sType(org.lwjgl.vulkan.KHRRayTracingPipeline.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_RAY_TRACING_PIPELINE_PROPERTIES_KHR)
                        .pNext(next);
                next = vkRayTracingProperties.address();
            }
            properties2.pNext(next);
            VK12.vkGetPhysicalDeviceProperties2(device, properties2);
            VkPhysicalDeviceProperties deviceProperties = properties2.properties();
            apiVersion = new APIVersion(deviceProperties.apiVersion());
            limits = new LWJGLVulkanLimits();
            limits.copy(deviceProperties.limits());
            deviceName = deviceProperties.deviceNameString();
            deviceType = PhysicalDeviceType.get(deviceProperties.deviceType());
            if (vkMeshProperties != null) {
                PhysicalDeviceMeshShaderPropertiesEXT meshProperties = new PhysicalDeviceMeshShaderPropertiesEXT(vkMeshProperties);
                propertiesTable.put(org.varg.vulkan.Vulkan10.Extension.VK_EXT_mesh_shader, meshProperties);
                vkMeshProperties.free();
            }
            if (vkFragmentShadingRateProperties != null) {
                PhysicalDeviceFragmentShadingRatePropertiesKHR fragmentShadingRateProperties = new PhysicalDeviceFragmentShadingRatePropertiesKHR(vkFragmentShadingRateProperties);
                propertiesTable.put(Vulkan10.Extension.VK_KHR_fragment_shading_rate, fragmentShadingRateProperties);
                vkFragmentShadingRateProperties.free();
            }
            if (vkRayTracingProperties != null) {
                PhysicalDeviceRayTracingPipelinePropertiesKHR rayTracingProperties = new PhysicalDeviceRayTracingPipelinePropertiesKHR(vkRayTracingProperties);
                propertiesTable.put(Vulkan10.Extension.VK_KHR_ray_tracing_pipeline, rayTracingProperties);
                vkRayTracingProperties.free();
            }
            properties2.free();
        }

        @Override
        public PhysicalDeviceType getDeviceType() {
            return deviceType;
        }

        @Override
        public String getDeviceName() {
            return deviceName;
        }

        @Override
        public APIVersion getAPIVersion() {
            return apiVersion;
        }

        @Override
        public DeviceLimits getLimits() {
            return limits;
        }

        @Override
        public String toString() {
            String result = getDeviceName() + ", " + getAPIVersion() + ", "
                    + getDeviceType() + "\n" + "Device limits:\n" +
                    getLimits().toString();
            return result;

        }

        @Override
        public PlatformStruct getProperties(VulkanExtension extension) {
            return propertiesTable.get(extension);
        }

    }

    /**
     * Internal constructor - DO NOT USE
     * 
     * @param version
     * @param window
     * @throws BackendException
     */
    public LWJGL3Vulkan12Backend(Renderers version, WindowHandle window) throws BackendException {
        super(version);
        this.window = window;
    }

    private void createDeviceInstance() {
        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new AssertionError("GLFW failed to find the Vulkan loader");
        }

        /* Look for instance extensions */
        PointerBuffer requiredExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        if (requiredExtensions == null) {
            throw new AssertionError("Failed to find list of required Vulkan extensions");
        }
        ArrayList<ByteBuffer> extensions = new ArrayList<ByteBuffer>();
        for (int i = 0; i < requiredExtensions.capacity(); i++) {
            extensions.add(Buffers.createByteBuffer(requiredExtensions.getStringASCII(i)));
        }

        addExtensions(requiredExtensions);
        addValidation(Settings.getInstance().getBoolean(BackendProperties.VALIDATE));
        boolean debug = addDebugExtension(Settings.getInstance().getBoolean(BackendProperties.DEBUG));

        instanceExtensions.flip();
        validationLayers.flip();
        instance = createInstance(instanceExtensions, validationLayers, debug);

        if (window.handle == WindowHandle.HEADLESS) {
            surface = window.handle;
        } else {
            long[] s = new long[1];
            if (GLFWVulkan.glfwCreateWindowSurface(instance, window.handle, null, s) != VK12.VK_SUCCESS) {
                throw new IllegalArgumentException("Could not create GLFW window surface");
            }
            surface = s[0];
        }
    }

    private void addExtensions(PointerBuffer requiredExtensions) {
        for (int i = 0; i < requiredExtensions.limit(); i++) {
            Logger.d(getClass(), "Adding extension: " + requiredExtensions.getStringASCII(i));
            instanceExtensions.put(requiredExtensions.get(i));
        }
    }

    @Override
    protected PhysicalDevice[] fetchDevices() {
        int[] deviceCount = new int[1];
        if (VK12.vkEnumeratePhysicalDevices(instance, deviceCount, null) == VK12.VK_SUCCESS) {
            PhysicalDevice[] devices = new PhysicalDevice[deviceCount[0]];
            PointerBuffer pb = null;
            try {
                pb = MemoryUtil.memAllocPointer(deviceCount[0]);
                if (VK12.vkEnumeratePhysicalDevices(instance, deviceCount, pb) == VK12.VK_SUCCESS) {
                    fetchDevices(pb, devices);
                } else {
                    throw new IllegalArgumentException("Failed to enumerate physical devices");
                }
                return devices;
            } finally {
                MemoryUtil.memFree(pb);
            }
        } else {
            throw new IllegalArgumentException("Failed to enumerate number of physical devices in system.");
        }
    }

    private void fetchDevices(PointerBuffer pb, PhysicalDevice[] devices) {
        pb.rewind();
        int index = 0;
        while (pb.remaining() > 0) {
            devices[index] = new Device(pb.get(), surface);
            index++;
        }
    }

    private VkInstance createInstance(PointerBuffer extensionsPtrs, PointerBuffer validationLayerPtrs, boolean debugMessenger) {

        VkApplicationInfo appInfo = VkApplicationInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pNext(MemoryUtil.NULL)
                .pApplicationName(MemoryUtil.memUTF8("LWJGL Vulkan Demo"))
                .pEngineName(MemoryUtil.memUTF8("graphics-by-vulkan"))
                .engineVersion(1)
                .apiVersion(VK12.VK_MAKE_VERSION(version.major, version.minor, 0));

        VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(RESERVED_FOR_FUTURE_USE)
                .pApplicationInfo(appInfo)
                .ppEnabledExtensionNames(extensionsPtrs);
        if (validationLayerPtrs != null) {
            pCreateInfo.ppEnabledLayerNames(validationLayerPtrs);
        }
        // created VkInstance
        PointerBuffer pointer = MemoryUtil.memAllocPointer(1);
        int result = VK12.vkCreateInstance(pCreateInfo, null, pointer);
        long instancePointer = pointer.get(0); // <- get the VkInstance handle
        // Check whether we succeeded in creating the VkInstance
        assertResult(result);
        // Create an object-oriented wrapper around the simple VkInstance long handle
        // This is needed by LWJGL to later "dispatch" (i.e. direct calls to) the right Vulkan functions.
        VkInstance vkInstance = new VkInstance(instancePointer, pCreateInfo);
        initDebug(vkInstance, debugMessenger);
        MemoryUtil.memFree(pointer);
        return vkInstance;
    }

    private void initDebug(VkInstance instancePointer, boolean debugMessenger) {
        if (debugMessenger) {
            VkDebugUtilsMessengerCreateInfoEXT messengerCallbackInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc()
                    .sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
                    .pNext(MemoryUtil.NULL)
                    .flags(0)
                    .messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT
                            | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT
                            | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT
                            | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
                    .messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
                            | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
                            | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
                    .pfnUserCallback(new VkDebugUtilsMessengerCallbackEXT() {
                        @Override
                        public int invoke(int messageSeverity, int messageTypes, long pCallbackData, long pUserData) {
                            VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT
                                    .create(pCallbackData);
                            Logger.d("DebugUtils:",
                                    callbackData.pMessageString() + "   messageIdName:"
                                            + callbackData.pMessageIdNameString()
                                            + (callbackData.messageIdNumber() != 0
                                                    ? "  messageIdNumber:" + callbackData.messageIdNumber()
                                                    : ""));
                            if (callbackData.queueLabelCount() > 0) {
                                Logger.d("DebugUtils:", "Queuelabelcount = " + callbackData.queueLabelCount());
                            }
                            if (callbackData.cmdBufLabelCount() > 0) {
                                Logger.d("DebugUtils:", "CmdBufLabelCount = " + callbackData.cmdBufLabelCount());
                            }
                            return VK12.VK_FALSE;
                        }
                    });

            // Now setup the debug utils messenger for persistent logging.
            LongBuffer pMessenger = MemoryUtil.memAllocLong(1);
            assertResult(
                    EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instancePointer, messengerCallbackInfo, null,
                            pMessenger));
        }
    }

    @Override
    protected void destroy() {
        if (instance != null) {
            VK12.vkDestroyInstance(instance, null);
            instance = null;
        }
    }

    @Override
    protected Features createRequestedPhysicalDeviceFeatures(PhysicalDevice selectedDevice) {
        return null;
    }

    /**
     * Sets the features to request into the list
     * 
     * @param featureExtensions
     */
    @SuppressWarnings("checkstyle:linelength")
    ArrayList<Long> setFeatureExtensions(PhysicalDeviceFeatureExtensions featureExtensions) {
        ArrayList<Long> result = new ArrayList<>();
        if (featureExtensions.hasConditionalRendering()) {
            VkPhysicalDeviceConditionalRenderingFeaturesEXT rendercon = VkPhysicalDeviceConditionalRenderingFeaturesEXT
                    .calloc()
                    .sType(EXTConditionalRendering.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_CONDITIONAL_RENDERING_FEATURES_EXT)
                    .pNext(getLastAdress(result))
                    .conditionalRendering(true);
            result.add(rendercon.address());
            Logger.d(getClass(), "Added VkPhysicalDeviceConditionalRenderingFeaturesEXT");
        }
        if (featureExtensions.hasIndexTypeUint8()) {
            VkPhysicalDeviceIndexTypeUint8FeaturesEXT indexedUint8 = VkPhysicalDeviceIndexTypeUint8FeaturesEXT.calloc()
                    .sType(EXTIndexTypeUint8.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_INDEX_TYPE_UINT8_FEATURES_EXT)
                    .indexTypeUint8(true)
                    .pNext(getLastAdress(result));
            result.add(indexedUint8.address());
        }
        PhysicalDeviceRayTracingPipelineFeaturesKHR rayTracing = featureExtensions.getPhysicalDeviceRayTracingPipelineFeaturesKHR();
        if (rayTracing != null) {
            VkPhysicalDeviceRayTracingPipelineFeaturesKHR vkRayFeatures = VkPhysicalDeviceRayTracingPipelineFeaturesKHR.calloc()
                    .sType(org.lwjgl.vulkan.KHRRayTracingPipeline.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_RAY_TRACING_PIPELINE_FEATURES_KHR)
                    .rayTracingPipeline(rayTracing.rayTracingPipeline)
                    .rayTracingPipelineShaderGroupHandleCaptureReplay(rayTracing.rayTracingPipelineShaderGroupHandleCaptureReplay)
                    .rayTracingPipelineShaderGroupHandleCaptureReplayMixed(rayTracing.rayTracingPipelineShaderGroupHandleCaptureReplayMixed)
                    .rayTracingPipelineTraceRaysIndirect(rayTracing.rayTracingPipelineTraceRaysIndirect)
                    .rayTraversalPrimitiveCulling(rayTracing.rayTraversalPrimitiveCulling)
                    .pNext(getLastAdress(result));
            result.add(vkRayFeatures.address());
            Logger.d(getClass(), "Added PhysicalDeviceRayTracingPipelineFeaturesKHR");
        }
        PhysicalDeviceMeshShaderFeaturesEXT requestedMesh = featureExtensions.getPhysicalDeviceMeshShaderFeaturesEXT();
        if (requestedMesh != null) {
            VkPhysicalDeviceMeshShaderFeaturesEXT meshFeatures = VkPhysicalDeviceMeshShaderFeaturesEXT.calloc()
                    .sType(EXTMeshShader.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MESH_SHADER_FEATURES_EXT)
                    .meshShader(requestedMesh.meshShader)
                    .meshShaderQueries(requestedMesh.meshShaderQueries)
                    .multiviewMeshShader(requestedMesh.multiviewMeshShader)
                    .primitiveFragmentShadingRateMeshShader(requestedMesh.primitiveFragmentShadingRateMeshShader)
                    .taskShader(requestedMesh.taskShader)
                    .pNext(getLastAdress(result));
            result.add(meshFeatures.address());
            Logger.d(getClass(), "Added VkPhysicalDeviceMeshShaderFeaturesEXT");
        }
        PhysicalDeviceFragmentShadingRateFeaturesKHR requestedShadingRate = featureExtensions
                .getPhysicalDeviceFragmentShadingRateFeaturesKHR();
        if (requestedShadingRate != null) {
            VkPhysicalDeviceFragmentShadingRateFeaturesKHR vkFragment = VkPhysicalDeviceFragmentShadingRateFeaturesKHR
                    .calloc()
                    .sType(org.lwjgl.vulkan.KHRFragmentShadingRate.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FRAGMENT_SHADING_RATE_FEATURES_KHR)
                    .attachmentFragmentShadingRate(requestedShadingRate.attachmentFragmentShadingRate)
                    .pipelineFragmentShadingRate(requestedShadingRate.pipelineFragmentShadingRate)
                    .primitiveFragmentShadingRate(requestedShadingRate.primitiveFragmentShadingRate)
                    .pNext(getLastAdress(result));
            result.add(vkFragment.address());
            Logger.d(getClass(), "Added VkPhysicalDeviceFragmentShadingRateFeaturesKHR");
        }
        // Only support for robustness2 nullDescriptor - robustBufferAccess and robustImageAccess features not
        // implemented.
        PhysicalDeviceRobustness2FeaturesEXT robustness2 = featureExtensions.getPhysicalDeviceRobustness2Features();
        if (robustness2 != null && robustness2.nullDescriptor) {
            // Enable nullDescriptor in robustness2 extension
            VkPhysicalDeviceRobustness2FeaturesEXT robustnessFeatures = VkPhysicalDeviceRobustness2FeaturesEXT.calloc()
                    .sType(EXTRobustness2.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_ROBUSTNESS_2_FEATURES_EXT)
                    .nullDescriptor(true)
                    .pNext(getLastAdress(result));
            result.add(robustnessFeatures.address());
            Logger.d(getClass(), "Added VkPhysicalDeviceRobustness2FeaturesEXT");
        }
        PhysicalDeviceAccelerationStructureFeaturesKHR acceleration =
                featureExtensions.getPhysicalDeviceAccelerationStructureFeatures();
        if (acceleration != null) {
            VkPhysicalDeviceAccelerationStructureFeaturesKHR vkAcceleration =
                    VkPhysicalDeviceAccelerationStructureFeaturesKHR.calloc()
                            .sType(KHRAccelerationStructure.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_ACCELERATION_STRUCTURE_FEATURES_KHR)
                            .accelerationStructure(acceleration.accelerationStructure)
                            .accelerationStructureCaptureReplay(acceleration.accelerationStructureCaptureReplay)
                            .accelerationStructureHostCommands(acceleration.accelerationStructureHostCommands)
                            .accelerationStructureIndirectBuild(acceleration.accelerationStructureIndirectBuild)
                            .descriptorBindingAccelerationStructureUpdateAfterBind(
                                    acceleration.descriptorBindingAccelerationStructureUpdateAfterBind)
                            .pNext(getLastAdress(result));
            result.add(vkAcceleration.address());
            Logger.d(getClass(), "Added VkPhysicalDeviceAccelerationStructureFeaturesKHR");
        }

        return result;
    }

    private long getLastAdress(ArrayList<Long> features) {
        if (features.size() > 0) {
            return features.get(features.size() - 1);
        }
        return MemoryUtil.NULL;
    }

    /**
     * Base method to create logical device
     * 
     * @param device
     * @param selectedQueue
     * @param requestedFeatures
     * @return
     */
    protected LogicalDevice<VkDevice> internalCreateLogicalDevice(PhysicalDevice device,
            QueueFamilyProperties selectedQueue, Features requestedFeatures, Object... platformObjects) {
        if (device == null || selectedQueue == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        PhysicalDeviceFeatures requestedDeviceFeatures = requestedFeatures.getPhysicalDeviceFeatures();
        PhysicalDeviceFeatureExtensions requestedFeatureExtensions =
                requestedFeatures.getPhysicalDeviceFeatureExtensions();
        // Store the features that are requested before creating logical device
        VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc();
        requestedDeviceFeatures.setVulkanFeatures(features);
        ((VkPhysicalDeviceFeatures2) platformObjects[0]).features(features);

        if (requestedDeviceFeatures instanceof PhysicalDeviceVulkan11Features) {
            ((PhysicalDeviceVulkan11Features) requestedDeviceFeatures).setVulkan11Features(platformObjects[1]);
        }
        if (requestedDeviceFeatures instanceof PhysicalDeviceVulkan12Features) {
            ((PhysicalDeviceVulkan12Features) requestedDeviceFeatures).setVulkan12Features(platformObjects[2]);
        }
        if (requestedDeviceFeatures instanceof PhysicalDeviceVulkan13Features) {
            ((PhysicalDeviceVulkan13Features) requestedDeviceFeatures).setVulkan13Features(platformObjects[3]);
        }
        FloatBuffer prios = Buffers.createFloatBuffer(selectedQueue.getQueueCount());
        prios.rewind();
        VkDeviceQueueCreateInfo.Buffer queue = VkDeviceQueueCreateInfo.calloc(1)
                .sType(VK13.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(0)
                .queueFamilyIndex(selectedQueue.getQueueIndex())
                .pQueuePriorities(prios);

        ExtensionProperties[] requestedExtensions = requestedFeatures.getExtensions();
        logicalDeviceExtensionNames = new ByteBuffer[requestedExtensions.length];
        logicalDeviceExtensions =
                MemoryUtil.memAllocPointer(requestedExtensions != null ? requestedExtensions.length : 0);
        if (requestedExtensions != null) {
            for (int i = 0; i < requestedExtensions.length; i++) {
                logicalDeviceExtensionNames[i] = requestedExtensions[i].createByteBuffer();
                logicalDeviceExtensions.put(logicalDeviceExtensionNames[i]);
                Logger.d(getClass(), "Enabling extension: " + requestedExtensions[i].getName());
            }
        }
        logicalDeviceExtensions.flip();
        VkDeviceCreateInfo deviceInfo = VkDeviceCreateInfo.calloc();
        deviceInfo.sType(VK13.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .pNext(((VkPhysicalDeviceFeatures2) platformObjects[0]).address())
                .flags(0)
                .pQueueCreateInfos(queue)
                .ppEnabledLayerNames(validationLayers)
                .ppEnabledExtensionNames(logicalDeviceExtensions)
                .pEnabledFeatures(null);

        VkPhysicalDevice physicalDevice = ((Device) device).getVkPhysicalDevice();
        PointerBuffer pointer = MemoryUtil.memAllocPointer(1);
        Logger.d(getClass(),
                Long.toString(physicalDevice.address(), 16) + ", " + Long.toString(deviceInfo.address(), 16) + ", "
                        + Long.toString(physicalDevice.getCapabilities().vkCreateDevice, 16));
        assertResult(VK13.vkCreateDevice(physicalDevice, deviceInfo, null, pointer));

        deviceInstance = new VkDevice(pointer.get(0), physicalDevice, deviceInfo);
        Logger.d(getClass(), "Created Vulkan instance with api version " + device.getProperties().getAPIVersion());
        return new LWJGLVulkanLogicalDevice(deviceInstance, selectedDevice.getProperties(),
                new Features(requestedDeviceFeatures, requestedFeatureExtensions, selectedDevice
                        .getExtensionProperties()),
                getExtensions(logicalDeviceExtensions.position(0)));
    }

    @Override
    protected LogicalDevice<VkDevice> createLogicalDevice(PhysicalDevice device, QueueFamilyProperties selectedQueue,
            Features requestedFeatures) {
        ArrayList<Long> extensionList = setFeatureExtensions(requestedFeatures.getPhysicalDeviceFeatureExtensions());
        long firstAdress = extensionList.size() > 0 ? extensionList.get(0) : MemoryUtil.NULL;
        VkPhysicalDeviceVulkan13Features vulkan13Features = VkPhysicalDeviceVulkan13Features.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_3_FEATURES)
                .pNext(firstAdress);
        VkPhysicalDeviceVulkan12Features vulkan12Features = VkPhysicalDeviceVulkan12Features.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_2_FEATURES)
                .pNext(vulkan13Features.address());
        VkPhysicalDeviceVulkan11Features vulkan11Features = VkPhysicalDeviceVulkan11Features.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_1_FEATURES)
                .pNext(vulkan12Features.address());
        VkPhysicalDeviceFeatures2 features2 = VkPhysicalDeviceFeatures2.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2)
                .pNext(vulkan11Features.address());
        return internalCreateLogicalDevice(device, selectedQueue, requestedFeatures, extensionList, features2,
                vulkan11Features, vulkan12Features, vulkan13Features);
    }

    /**
     * Returns the logical device extensions as an array of Vulkan extensions
     * 
     * @param buffer
     * @return
     */
    VulkanExtension[] getExtensions(PointerBuffer buffer) {
        VulkanExtension[] extensions = new VulkanExtension[buffer.remaining()];
        for (int i = 0; i < extensions.length; i++) {
            extensions[i] = VulkanExtension.getExtension(logicalDeviceExtensions.getStringASCII());
        }
        return extensions;
    }

    @Override
    protected ArrayList<SurfaceFormat> getSurfaceFormats(PhysicalDevice device) {
        IntBuffer ib = MemoryUtil.memAllocInt(1);
        VkPhysicalDevice vkDevice = ((Device) device).getVkPhysicalDevice();
        assertResult(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(vkDevice, surface, ib, null));
        VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(ib.get(0));
        assertResult(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(vkDevice, surface, ib, surfaceFormats));
        ArrayList<SurfaceFormat> result = new ArrayList<SurfaceFormat>();
        surfaceFormats.rewind();
        Logger.d(getClass(), "Found " + ib.get(0) + " surface formats.");
        while (surfaceFormats.hasRemaining()) {
            VkSurfaceFormatKHR vkSF = surfaceFormats.get();
            Vulkan10.Format format = Vulkan10.Format.get(vkSF.format());
            ColorSpaceKHR space = ColorSpaceKHR.get(vkSF.colorSpace());
            if (format != null && space != null) {
                SurfaceFormat sf = new SurfaceFormat(format, space);
                result.add(sf);
                Logger.d(getClass(), "Added surfaceformat: " + sf);
            } else {
                Logger.d(getClass(), "Error - could not find surfaceformat or colorspace");
            }
        }
        MemoryUtil.memFree(ib);
        return result;
    }

    @Override
    protected ArrayList<PresentModeKHR> getPresentModes(PhysicalDevice device) {
        IntBuffer ib = MemoryUtil.memAllocInt(1);
        ArrayList<PresentModeKHR> result = new ArrayList<>();
        VkPhysicalDevice vkDevice = ((Device) device).getVkPhysicalDevice();
        assertResult(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(vkDevice, surface, ib, null));
        IntBuffer presentModes = Buffers.createIntBuffer(ib.get(0));
        assertResult(KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(vkDevice, surface, ib, presentModes));

        presentModes.rewind();
        while (presentModes.hasRemaining()) {
            int v = presentModes.get();
            PresentModeKHR mode = PresentModeKHR.get(v);
            if (mode != null) {
                Logger.d(getClass(), "Added presentmode " + mode);
                result.add(mode);
            } else {
                Logger.d(getClass(), "Error - no presentmode for: " + v);
            }
        }
        MemoryUtil.memFree(ib);
        return result;
    }

    @Override
    public Extent2D selectSwapExtent(SurfaceCapabilitiesKHR surfaceCaps) {

        int w = surfaceCaps.getCurrentExtent().width;
        int h = surfaceCaps.getCurrentExtent().height;
        if (surfaceCaps.getCurrentExtent().width == 0xFFFFFFFF) {
            // If the surface size is undefined, the size is set to the size
            // of the images requested, which must fit within the minimum and
            // maximum values.
            if (w < surfaceCaps.getMaxImageExtent().width) {
                w = surfaceCaps.getMinImageExtent().width;
            } else if (w > surfaceCaps.getMaxImageExtent().width) {
                w = surfaceCaps.getMaxImageExtent().width;
            }

            if (h < surfaceCaps.getMinImageExtent().height) {
                h = surfaceCaps.getMinImageExtent().height;
            } else if (h > surfaceCaps.getMaxImageExtent().height) {
                h = surfaceCaps.getMaxImageExtent().height;
            }
        }
        return new Extent2D(w, h);
    }

    @Override
    public ImageView[] createImageViews(Image[] images, ComponentMapping components, int mipLevels, int arrayLayers) {
        int bufferCount = images.length;
        ImageView[] result = new ImageView[bufferCount];

        ImageViewType type = ImageViewType.VK_IMAGE_VIEW_TYPE_2D;
        ImageSubresourceRange range = new ImageSubresourceRange(ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT, 0, mipLevels, 0, arrayLayers);
        for (int i = 0; i < bufferCount; i++) {
            ImageViewCreateInfo createInfo = new ImageViewCreateInfo(images[i], type, images[i].getFormatValue(), components, range);
            result[i] = createImageView(createInfo);
        }
        return result;
    }

    @Override
    public FrameBuffer[] createFrameBuffers(FramebufferCreateInfo... createInfo) {

        FrameBuffer[] frameBuffers = new FrameBuffer[createInfo.length];
        VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc();

        int index = 0;
        LongBuffer lb = MemoryUtil.memAllocLong(1);
        for (FramebufferCreateInfo info : createInfo) {

            framebufferInfo.sType(VK12.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                    .pNext(MemoryUtil.NULL)
                    .flags(info.getFlagsValue())
                    .renderPass(info.renderPass.handle)
                    .pAttachments(info.getAttachments())
                    .width(info.width)
                    .height(info.height)
                    .layers(info.layers).address();
            assertResult(VK12.vkCreateFramebuffer(deviceInstance, framebufferInfo, null, lb));
            frameBuffers[index++] = new FrameBuffer(lb.get(0), info);
        }
        MemoryUtil.memFree(lb);
        return frameBuffers;
    }

    @Override
    public RenderPass createRenderPass(RenderPassCreateInfo createInfo) {
        VkAttachmentDescription2.Buffer info = VkAttachmentDescription2.calloc(createInfo.attachments.length);
        int index = 0;
        for (AttachmentDescription a : createInfo.attachments) {
            info.get(index++)
                    .sType(VK12.VK_STRUCTURE_TYPE_ATTACHMENT_DESCRIPTION_2)
                    .pNext(MemoryUtil.NULL)
                    .flags(a.getFlagsValue())
                    .format(a.format.value)
                    .samples(a.samples.value)
                    .loadOp(a.loadOp.value)
                    .storeOp(a.storeOp.value)
                    .stencilLoadOp(a.stencilLoadOp.value)
                    .stencilStoreOp(a.stencilStoreOp.value)
                    .initialLayout(a.initialLayout.value)
                    .finalLayout(a.finalLayout.value);
        }
        info.position(0);
        VkSubpassDescription2.Buffer subpass = VkSubpassDescription2.calloc(createInfo.subpasses.length)
                .sType(VK12.VK_STRUCTURE_TYPE_SUBPASS_DESCRIPTION_2)
                .pNext(MemoryUtil.NULL);
        VkSubpassDependency2.Buffer dependency = VkSubpassDependency2.calloc(createInfo.subpasses.length)
                .sType(VK12.VK_STRUCTURE_TYPE_SUBPASS_DEPENDENCY_2)
                .pNext(MemoryUtil.NULL);
        index = 0;
        for (SubpassDescription2 s : createInfo.subpasses) {
            VkAttachmentReference2.Buffer colorAttachments = VkAttachmentReference2.calloc(1);
            colorAttachments.get(0)
                    .pNext(MemoryUtil.NULL)
                    .sType(VK12.VK_STRUCTURE_TYPE_ATTACHMENT_REFERENCE_2)
                    .attachment(s.getColorAttachmentReference().attachment)
                    .layout(s.getColorAttachmentReference().layout.value);
            VkSubpassDescription2 description = subpass.get(index);
            VkSubpassDependency2 dep = dependency.get(index);
            index++;
            VkAttachmentReference2 depthAttachments = VkAttachmentReference2.calloc();
            depthAttachments.attachment(s.getDepthAttachmentReference().attachment)
                    .sType(VK12.VK_STRUCTURE_TYPE_ATTACHMENT_REFERENCE_2)
                    .pNext(MemoryUtil.NULL)
                    .layout(s.getDepthAttachmentReference().layout.value);
            description.pipelineBindPoint(s.pipelineBindPoint.value)
                    .sType(VK12.VK_STRUCTURE_TYPE_SUBPASS_DESCRIPTION_2)
                    .pNext(MemoryUtil.NULL)
                    .colorAttachmentCount(1)
                    .pColorAttachments(colorAttachments)
                    .pDepthStencilAttachment(depthAttachments);
            AttachmentReference resolve = s.getResolveAttachment();
            if (resolve != null) {
                VkAttachmentReference2.Buffer resolveAtt = VkAttachmentReference2.calloc(1);
                resolveAtt.attachment(resolve.attachment)
                        .sType(VK12.VK_STRUCTURE_TYPE_ATTACHMENT_REFERENCE_2)
                        .pNext(MemoryUtil.NULL)
                        .layout(resolve.layout.value);
                description.pResolveAttachments(resolveAtt);
            }
            dep.srcSubpass(VK12.VK_SUBPASS_EXTERNAL);
            dep.dstSubpass(0);
            dep.srcStageMask(VK12.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dep.srcAccessMask(0);
            dep.dstStageMask(VK12.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dep.dstAccessMask(VK12.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK12.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        }
        VkRenderPassCreateInfo2 renderPassInfo = VkRenderPassCreateInfo2.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO_2)
                .pNext(MemoryUtil.NULL)
                .pAttachments(info)
                .pSubpasses(subpass)
                .pDependencies(dependency);

        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkCreateRenderPass2(deviceInstance, renderPassInfo, null, lb));
        RenderPass renderPass = new RenderPass(lb.get(0), createInfo);
        MemoryUtil.memFree(lb);
        Logger.d(getClass(), "Created renderpass for " + createInfo.attachments.length + " attachments:");
        for (AttachmentDescription a : createInfo.attachments) {
            Logger.d(getClass(), a.format + ", " + a.samples + ", " + a.loadOp + ", " + a.storeOp);
        }
        return renderPass;
    }

    @Override
    protected PhysicalDeviceMemoryProperties createMemoryProperties(PhysicalDevice device) {
        VkPhysicalDevice vkDevice = ((Device) device).getVkPhysicalDevice();

        VkPhysicalDeviceMemoryProperties p = VkPhysicalDeviceMemoryProperties.calloc();
        VK12.vkGetPhysicalDeviceMemoryProperties(vkDevice, p);
        PhysicalDeviceMemoryProperties memoryProperties = new LWJGLPhysicalDeviceMemoryProperties(p);
        return memoryProperties;
    }

    @Override
    public ImageView createImageView(ImageViewCreateInfo createInfo) {
        VkImageViewCreateInfo attachment = VkImageViewCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(0)
                .image(createInfo.image.pointer)
                .viewType(createInfo.type.value)
                .format(createInfo.formatValue)
                .components(it -> it
                        .r(createInfo.components.red.value)
                        .g(createInfo.components.green.value)
                        .b(createInfo.components.blue.value)
                        .a(createInfo.components.alpha.value))
                .subresourceRange(it -> it
                        .aspectMask(BitFlags.getFlagsValue(createInfo.subresourceRange.aspectMask))
                        .baseMipLevel(createInfo.subresourceRange.baseMipLevel)
                        .levelCount(createInfo.subresourceRange.levelCount)
                        .baseArrayLayer(createInfo.subresourceRange.baseArrayLayer)
                        .layerCount(createInfo.subresourceRange.layerCount));

        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkCreateImageView(deviceInstance, attachment, null, lb));
        long view = lb.get(0);
        MemoryUtil.memFree(lb);
        return new ImageView(createInfo, view);
    }

    @Override
    public ShaderModule createShaderModule(ShaderBinary binary) {
        VkShaderModuleCreateInfo info = VkShaderModuleCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(0)
                .pCode(binary.getBuffer());
        long codesize = info.codeSize();
        Logger.d(getClass(), "Codesize=" + codesize);
        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkCreateShaderModule(deviceInstance, info, null, lb));
        ShaderModule shaderModule = new ShaderModule(lb.get(0), new ShaderModuleCreateInfo(binary));
        MemoryUtil.memFree(lb);
        return shaderModule;
    }

    private static VkPipelineShaderStageCreateInfo
            createShaderStageInfo(PipelineShaderStageCreateInfo shaderStageInfo) {
        VkSpecializationInfo vkSpecialization = null;
        if (shaderStageInfo.getSpecializationInfo() != null) {
            SpecializationMapEntry[] entries = shaderStageInfo.getSpecializationInfo().getEntries();
            VkSpecializationMapEntry.Buffer mapEntry = VkSpecializationMapEntry.calloc(entries.length);
            IntBuffer buffer = shaderStageInfo.getSpecializationInfo().getBuffer().asIntBuffer();
            for (int i = 0; i < entries.length; i++) {
                mapEntry.get(i).constantID(entries[i].getConstantID())
                        .offset(entries[i].getOffset())
                        .size(entries[i].getSize());
                buffer.position(entries[i].getOffset() / 4);
                Logger.d(LWJGL3Vulkan12Backend.class,
                        "SpecializationInfo: " + entries[i].getConstantID() + ", offset " + entries[i].getOffset()
                                + ", size " + entries[i].getSize() + ", value "
                                + buffer.get());
            }
            vkSpecialization = VkSpecializationInfo.calloc();
            vkSpecialization.set(mapEntry, shaderStageInfo.getSpecializationInfo().getBuffer());
        }
        VkPipelineShaderStageCreateInfo info = VkPipelineShaderStageCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(RESERVED_FOR_FUTURE_USE)
                .stage(shaderStageInfo.getStage().value)
                .module(shaderStageInfo.getModule().getHandle())
                .pName(shaderStageInfo.getName());
        if (vkSpecialization != null) {
            info.pSpecializationInfo(vkSpecialization);
        }
        return info;
    }

    /**
     * Creates the pipeline shaderstage create infos, returns buffer at position 0
     * 
     * @param stages
     * @return
     */
    public static VkPipelineShaderStageCreateInfo.Buffer
            createVkShaderStageCreateInfo(PipelineShaderStageCreateInfo[] stages) {
        VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo
                .calloc(stages.length);
        int index = 0;
        for (PipelineShaderStageCreateInfo stageInfo : stages) {
            shaderStages.get(index++).set(createShaderStageInfo(stageInfo));
        }
        shaderStages.position(0);
        return shaderStages;
    }

    @Override
    public ComputePipeline createComputePipeline(ComputePipelineCreateInfo info, ComputeShader computeShader) {
        VkPipelineShaderStageCreateInfo.Buffer shaderStages = createVkShaderStageCreateInfo(info.getStages());
        VkComputePipelineCreateInfo.Buffer pipelineCreateInfo = VkComputePipelineCreateInfo.calloc(1)
                .sType(VK12.VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .stage(shaderStages.get())
                .layout(info.getPipelineLayout().getPipelineLayout());
        VkPipelineCacheCreateInfo pipelineCacheCI = VkPipelineCacheCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO)
                .pNext(MemoryUtil.NULL);
        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkCreatePipelineCache(deviceInstance, pipelineCacheCI, null, lb));
        long pipelineCache = lb.get(0);
        LongBuffer pPipelines = MemoryUtil.memAllocLong(1);
        int result = VK12.vkCreateComputePipelines(deviceInstance, pipelineCache, pipelineCreateInfo, null, pPipelines);
        assertResult(result);
        ComputePipeline pipeline = new ComputePipeline(info, pPipelines.get(0), computeShader);
        MemoryUtil.memFree(lb);
        MemoryUtil.memFree(pPipelines);
        pipelineCreateInfo.free();
        pipelineCacheCI.free();
        return pipeline;
    }

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public GraphicsPipeline createGraphicsPipeline(GraphicsPipelineCreateInfo info, RenderPass renderPass,
            Shader graphicsShader) {

        VkPipelineRasterizationStateCreateInfo rasterizationState = VkPipelineRasterizationStateCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .polygonMode(info.getRasterizationState().polygonMode.value)
                .cullMode(info.getRasterizationState().cullMode.value)
                .frontFace(info.getRasterizationState().frontFace.value)
                .lineWidth(info.getRasterizationState().lineWidth);

        PipelineColorBlendAttachmentState[] colorBlendStates = info.getColorBlendState().pAttachments;
        VkPipelineColorBlendAttachmentState.Buffer colorWriteMask = VkPipelineColorBlendAttachmentState
                .calloc(colorBlendStates.length);
        int index = 0;
        for (PipelineColorBlendAttachmentState state : colorBlendStates) {
            colorWriteMask.get(index++).colorWriteMask(state.colorWriteMask)
                    .blendEnable(state.blendEnable)
                    .srcColorBlendFactor(state.srcColorBlendFactor.value)
                    .dstColorBlendFactor(state.dstColorBlendFactor.value)
                    .srcAlphaBlendFactor(state.srcAlphaBlendFactor.value)
                    .dstAlphaBlendFactor(state.dstAlphaBlendFactor.value)
                    .colorBlendOp(state.colorBlendOp.value)
                    .alphaBlendOp(state.alphaBlendOp.value);
        }
        VkPipelineColorBlendStateCreateInfo colorBlendState = VkPipelineColorBlendStateCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .pAttachments(colorWriteMask);

        VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .viewportCount(1) // <- one viewport
                .scissorCount(1); // <- one scissor rectangle

        IntBuffer pDynamicStates = MemoryUtil.memAllocInt(2);
        pDynamicStates.put(VK12.VK_DYNAMIC_STATE_VIEWPORT).put(VK12.VK_DYNAMIC_STATE_SCISSOR).flip();
        VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.calloc()
                // The dynamic state properties themselves are stored in the command buffer
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .pDynamicStates(pDynamicStates);

        VkPipelineDepthStencilStateCreateInfo depthStencilState = VkPipelineDepthStencilStateCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .depthCompareOp(info.getDepthStencilState().depthCompareOp.value)
                .depthTestEnable(info.getDepthStencilState().depthTestEnable)
                .depthWriteEnable(info.getDepthStencilState().depthWriteEnable)
                .minDepthBounds(info.getDepthStencilState().minDepthBounds)
                .maxDepthBounds(info.getDepthStencilState().maxDepthBounds)
                .depthBoundsTestEnable(info.getDepthStencilState().depthBoundsTestEnable)
                .stencilTestEnable(info.getDepthStencilState().stencilTestEnable)
                .back(it -> it
                        .failOp(VK12.VK_STENCIL_OP_KEEP)
                        .passOp(VK12.VK_STENCIL_OP_KEEP)
                        .compareOp(VK12.VK_COMPARE_OP_ALWAYS));
        depthStencilState.front(depthStencilState.back());

        VkPipelineMultisampleStateCreateInfo multisampleState = VkPipelineMultisampleStateCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .rasterizationSamples(info.getMultisampleState().rasterizationSamples.value)
                .minSampleShading(info.getMultisampleState().minSampleShading)
                .sampleShadingEnable(info.getMultisampleState().sampleShadingEnable);

        VkPipelineShaderStageCreateInfo.Buffer shaderStages = createVkShaderStageCreateInfo(info.getStages());
        VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1)
                .sType(VK12.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .pStages(shaderStages)
                .pViewportState(viewportState)
                .pRasterizationState(rasterizationState)
                .pMultisampleState(multisampleState)
                .pDepthStencilState(depthStencilState)
                .pColorBlendState(colorBlendState)
                .pDynamicState(dynamicState)
                .layout(info.getPipelineLayout().getPipelineLayout())
                .renderPass(renderPass.handle);

        PipelineFragmentShadingRateStateCreateInfoKHR vsrInfo = info.getFragmentShadingRateCreateInfo();
        VkExtent2D vkSize = null;
        VkPipelineFragmentShadingRateStateCreateInfoKHR vkInfo = null;
        if (vsrInfo != null) {
            vkSize = VkExtent2D.calloc()
                    .width(vsrInfo.fragmentSize.width)
                    .height(vsrInfo.fragmentSize.height);
            vkInfo = VkPipelineFragmentShadingRateStateCreateInfoKHR
                    .calloc()
                    .sType(org.lwjgl.vulkan.KHRFragmentShadingRate.VK_STRUCTURE_TYPE_PIPELINE_FRAGMENT_SHADING_RATE_STATE_CREATE_INFO_KHR)
                    .pNext(MemoryUtil.NULL)
                    .fragmentSize(vkSize)
                    .combinerOps(0, vsrInfo.combinerOps[0].value)
                    .combinerOps(1, vsrInfo.combinerOps[1].value);
            pipelineCreateInfo.pNext(vkInfo);
        }

        // If using a Mesh shader the vertex input state is omitted - VkPipelineVertexInputStateCreateInfo and
        // VkPipelineInputAssemblyStateCreateInfo
        if (!graphicsShader.hasStage(Stage.MESH)) {
            VkPipelineInputAssemblyStateCreateInfo inputAssemblyState = VkPipelineInputAssemblyStateCreateInfo.calloc()
                    .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                    .pNext(MemoryUtil.NULL)
                    .topology(info.getInputAssemblyState().topology.value);
            VkPipelineVertexInputStateCreateInfo vertexInputState = createPipelineVertexInputStateInfo(
                    info.getVertexInputState());
            pipelineCreateInfo.pVertexInputState(vertexInputState)
                    .pInputAssemblyState(inputAssemblyState);
        } else {
            pipelineCreateInfo.pVertexInputState(null)
                    .pInputAssemblyState(null);
        }
        // Create rendering pipeline
        VkPipelineCacheCreateInfo pipelineCacheCI = VkPipelineCacheCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO)
                .pNext(MemoryUtil.NULL);
        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkCreatePipelineCache(deviceInstance, pipelineCacheCI, null, lb));
        long pipelineCache = lb.get(0);
        LongBuffer pPipelines = MemoryUtil.memAllocLong(1);
        int result = VK12.vkCreateGraphicsPipelines(deviceInstance, pipelineCache, pipelineCreateInfo, null,
                pPipelines);
        MemoryUtil.memFree(pDynamicStates);
        MemoryUtil.memFree(lb);
        assertResult(result);
        Logger.d(getClass(), "Created pipeline for " + info.getStages().length + " stages, " + info.toString());
        return new GraphicsPipeline(info, pPipelines.get(0));
    }

    @Override
    public PipelineLayout createPipelineLayout(PipelineLayoutCreateInfo layoutInfo) {
        PushConstantRange[] pushConstantRanges = layoutInfo.getPushConstantRanges();
        VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .setLayoutCount(layoutInfo.getSetLayoutCount())
                .pSetLayouts(layoutInfo.getSetLayoutPointers());
        if (pushConstantRanges != null) {
            VkPushConstantRange.Buffer pushRange = VkPushConstantRange.calloc(pushConstantRanges.length);
            int index = 0;
            for (PushConstantRange pr : pushConstantRanges) {
                pushRange.get(index++).stageFlags(pr.stageFlagsValue)
                        .offset(pr.offset)
                        .size(pr.size);
            }
            pipelineLayoutCreateInfo.pPushConstantRanges(pushRange);
        }
        LongBuffer lb = MemoryUtil.memAllocLong(1);
        int result = VK12.vkCreatePipelineLayout(deviceInstance, pipelineLayoutCreateInfo, null, lb);
        PipelineLayout layout = new PipelineLayout(lb.get(0), layoutInfo);
        MemoryUtil.memFree(lb);
        assertResult(result);
        return layout;
    }

    @Override
    public void destroyPipelineLayout(PipelineLayout layout) {
        long layoutPtr = layout.getPipelineLayout();
        if (layoutPtr == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", layoutPtr is zero");
        }
        VK12.vkDestroyPipelineLayout(deviceInstance, layoutPtr, null);
        layout.getPipelineLayoutBuffer().put(0);
    }

    private VkPipelineVertexInputStateCreateInfo createPipelineVertexInputStateInfo(
            PipelineVertexInputStateCreateInfo vertexInputState) {

        VertexInputBindingDescription[] vertexBindings = vertexInputState.getVertexBindingDescriptions();
        VertexInputAttributeDescription[] vertexInputs = vertexInputState.getVertexAttributeDescriptions();
        if (vertexBindings == null || vertexInputs == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE + " null");
        }
        VkVertexInputBindingDescription.Buffer bindingDescriptor = VkVertexInputBindingDescription
                .calloc(vertexInputState.getVertexBindingCount());
        int index = 0;
        for (VertexInputBindingDescription binding : vertexBindings) {
            if (binding != null) {
                bindingDescriptor.get(index++)
                        .binding(binding.getBinding())
                        .stride(binding.getStride())
                        .inputRate(binding.getInputRate().value);
            }
        }

        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription
                .calloc(vertexInputState.getVertexInputCount());
        index = 0;
        for (VertexInputAttributeDescription vertexInput : vertexInputs) {
            if (vertexInput != null) {
                // Check format.
                FormatProperties properties = getFormatProperties(selectedDevice, vertexInput.format.value);
                attributeDescriptions.get(index++)
                        .binding(vertexInput.binding)
                        .location(vertexInput.location)
                        .format(vertexInput.format.value)
                        .offset(vertexInput.offset);
            }
        }

        VkPipelineVertexInputStateCreateInfo vi = VkPipelineVertexInputStateCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .pVertexBindingDescriptions(bindingDescriptor)
                .pVertexAttributeDescriptions(attributeDescriptions);

        return vi;
    }

    private void addValidation(boolean validate) {
        if (validate) {
            IntBuffer ib = MemoryUtil.memAllocInt(1);
            assertResult(VK12.vkEnumerateInstanceLayerProperties(ib, null));
            if (ib.get(0) > 0) {
                VkLayerProperties.Buffer availableLayers = VkLayerProperties.calloc(ib.get(0));
                assertResult(VK12.vkEnumerateInstanceLayerProperties(ib, availableLayers));
                boolean hasValidation = false;
                for (int j = 0; j < availableLayers.capacity(); j++) {
                    availableLayers.position(j);
                    Logger.d(getClass(), "Available layer: " + availableLayers.layerNameString());
                    if (VKLAYERKRHONOS_VALIDATION.contentEquals(availableLayers.layerNameString())) {
                        hasValidation = true;
                    }
                }
                if (!hasValidation) {
                    throw new IllegalArgumentException(
                            ErrorMessage.INVALID_STATE.message + ", could not find " + VKLAYERKRHONOS_VALIDATION
                                    + ", either install Vulkan (LUNARG) SDK or turn off validation");
                }
                validationLayers.put(KHRONOSVALIDATION);
            } else {
                throw new IllegalArgumentException(
                        ErrorMessage.FAILED_WITH_ERROR.message + "Could not find validation layer");
            }
            MemoryUtil.memFree(ib);
        }
    }

    private boolean addDebugExtension(boolean debugInfo) {
        if (debugInfo) {
            PointerBuffer debugExtension = MemoryUtil.memAllocPointer(1);
            debugExtension.put(Vulkan10.Extension.VK_EXT_debug_utils.createByteBuffer());
            debugExtension.flip();
            addExtensions(debugExtension);
            MemoryUtil.memFree(debugExtension);
        }
        return debugInfo;
    }

    private final VkDebugReportCallbackEXT debugRreportCallback = VkDebugReportCallbackEXT.create(
            (flags, objectType, object, location, messageCode, pLayerPrefix, pMessage, pUserData) -> {
                String type;
                if ((flags & EXTDebugReport.VK_DEBUG_REPORT_INFORMATION_BIT_EXT) != 0) {
                    type = "INFORMATION";
                } else if ((flags & EXTDebugReport.VK_DEBUG_REPORT_WARNING_BIT_EXT) != 0) {
                    type = "WARNING";
                } else if ((flags & EXTDebugReport.VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT) != 0) {
                    type = "PERFORMANCE WARNING";
                } else if ((flags & EXTDebugReport.VK_DEBUG_REPORT_ERROR_BIT_EXT) != 0) {
                    type = "ERROR";
                } else if ((flags & EXTDebugReport.VK_DEBUG_REPORT_DEBUG_BIT_EXT) != 0) {
                    type = "DEBUG";
                } else {
                    type = "UNKNOWN";
                }

                System.err.format(
                        "%s: [%s] Code %d : %s\n",
                        type, MemoryUtil.memASCII(pLayerPrefix), messageCode,
                        VkDebugReportCallbackEXT.getString(pMessage));

                /*
                 * false indicates that layer should not bail-out of an
                 * API call that had validation failures. This may mean that the
                 * app dies inside the driver due to invalid parameter(s).
                 * That's what would happen without validation layers, so we'll
                 * keep that behavior here.
                 */
                return VK12.VK_FALSE;
            });

    @Override
    public void createDescriptorPool(DescriptorPoolCreateInfo poolInfo) {
        if (descriptorPool != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already created DescriptorPool");
        }
        DescriptorPoolSize[] poolSizes = poolInfo.getpPoolSizes();
        VkDescriptorPoolSize.Buffer vkPoolSize = VkDescriptorPoolSize.calloc(poolSizes.length);
        int index = 0;
        for (DescriptorPoolSize poolSize : poolSizes) {
            vkPoolSize.get(index++)
                    .type(poolSize.getType().value)
                    .descriptorCount(poolSize.getDescriptorCount());
        }

        VkDescriptorPoolCreateInfo vkDescriptorPool = VkDescriptorPoolCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .maxSets(poolInfo.getMaxSets())
                .flags(poolInfo.getFlagsValue())
                .pPoolSizes(vkPoolSize);

        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkCreateDescriptorPool(deviceInstance, vkDescriptorPool, null, lb));
        descriptorPool = new DescriptorPool(lb.get(0));
        MemoryUtil.memFree(lb);
    }

    @Override
    public DescriptorSetLayout createDescriptorSetLayout(DescriptorSetLayoutCreateInfo layoutInfo) {
        DescriptorSetLayoutBinding setLayoutBinding = layoutInfo.getDescriptorSetLayout();
        VkDescriptorSetLayoutBinding.Buffer layoutBinding = VkDescriptorSetLayoutBinding.calloc(1);
        int index = 0;
        layoutBinding.get(index++).binding(setLayoutBinding.getBinding())
                .descriptorType(setLayoutBinding.getDescriptorType().value)
                .descriptorCount(setLayoutBinding.getDescriptorCount())
                .stageFlags(setLayoutBinding.getFlagsValue());
        VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .pBindings(layoutBinding)
                .flags(layoutInfo.getFlagsValue());

        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkCreateDescriptorSetLayout(deviceInstance, createInfo, null, lb));
        DescriptorSetLayout setLayout = new DescriptorSetLayout(lb.get(0), setLayoutBinding, layoutInfo.getFlags());
        MemoryUtil.memFree(lb);
        Logger.d(getClass(),
                "Created DescriptorSetLayout for layoutbinding " + setLayoutBinding);
        return setLayout;
    }

    @Override
    public void destroyDescriptorSetLayout(DescriptorSetLayout layout) {
        long layoutPtr = layout.getDescriptorSetLayout();
        if (layoutPtr == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", layoutpointer is zero");
        }
        VK12.vkDestroyDescriptorSetLayout(deviceInstance, layout.getDescriptorSetLayout(), null);
        layout.getDescriptorSetLayoutBuffer().put(0);
    }

    @Override
    public LogicalDevice<VkDevice> getLogicalDevice() {
        return logicalDevice;
    }

    @Override
    protected PhysicalDeviceMemoryProperties getMemoryProperties() {
        return memoryProperties;
    }

    @Override
    public Sampler createSampler(SamplerCreateInfo samplerInfo) {
        VkSamplerCreateInfo createInfo = VkSamplerCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .addressModeU(samplerInfo.addressModeU.value)
                .addressModeV(samplerInfo.addressModeV.value)
                .addressModeW(samplerInfo.addressModeW.value)
                .anisotropyEnable(samplerInfo.isAnisotropyEnable())
                .borderColor(samplerInfo.borderColor.value)
                .compareEnable(samplerInfo.compareEnable)
                .compareOp(samplerInfo.compareOp.value)
                .flags(BitFlags.getFlagsValue(samplerInfo.flags))
                .magFilter(samplerInfo.magFilter.value)
                .maxAnisotropy(samplerInfo.getMaxAnisotropy())
                .maxLod(samplerInfo.maxLod)
                .minFilter(samplerInfo.minFilter.value)
                .minLod(samplerInfo.minLod)
                .mipLodBias(samplerInfo.mipLodBias)
                .mipmapMode(samplerInfo.mipmapMode.value);

        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkCreateSampler(deviceInstance, createInfo, null, lb));
        Sampler sampler = new Sampler(lb.get(0), samplerInfo);
        MemoryUtil.memFree(lb);
        return sampler;
    }

    @Override
    protected SurfaceCapabilitiesKHR getSurfaceCapabilities(PhysicalDevice device) {
        VkPhysicalDevice vkDevice = ((Device) device).getVkPhysicalDevice();
        VkSurfaceCapabilitiesKHR surfaceCaps = VkSurfaceCapabilitiesKHR.calloc();
        assertResult(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vkDevice, surface, surfaceCaps));
        Extent2D minExtent = new Extent2D(surfaceCaps.minImageExtent().width(), surfaceCaps.minImageExtent().height());
        Extent2D maxExtent = new Extent2D(surfaceCaps.maxImageExtent().width(), surfaceCaps.maxImageExtent().height());
        Extent2D currentExtent = new Extent2D(surfaceCaps.currentExtent().width(),
                surfaceCaps.currentExtent().height());
        SurfaceTransformFlagBitsKHR[] supportedTransforms = BitFlags
                .getBitFlags(surfaceCaps.supportedTransforms(), SurfaceTransformFlagBitsKHR.values())
                .toArray(new SurfaceTransformFlagBitsKHR[0]);
        SurfaceTransformFlagBitsKHR currentTransform = (SurfaceTransformFlagBitsKHR) BitFlags
                .getBitFlag(surfaceCaps.currentTransform(), SurfaceTransformFlagBitsKHR.values());
        CompositeAlphaFlagBitsKHR[] supportedCompositeAlpha = BitFlags
                .getBitFlags(surfaceCaps.supportedCompositeAlpha(), CompositeAlphaFlagBitsKHR.values())
                .toArray(new CompositeAlphaFlagBitsKHR[0]);
        ImageUsageFlagBits[] usageFlags = BitFlags
                .getBitFlags(surfaceCaps.supportedUsageFlags(), ImageUsageFlagBits.values())
                .toArray(new ImageUsageFlagBits[0]);
        return new SurfaceCapabilitiesKHR(surfaceCaps.minImageCount(),
                surfaceCaps.maxImageCount(),
                minExtent,
                maxExtent,
                currentExtent,
                surfaceCaps.maxImageArrayLayers(),
                supportedTransforms,
                currentTransform,
                supportedCompositeAlpha,
                usageFlags);
    }

    @Override
    public FormatProperties getFormatProperties(PhysicalDevice device, int formatValue) {
        VkPhysicalDevice vkDevice = ((Device) device).getVkPhysicalDevice();
        VkFormatProperties properties = VkFormatProperties.calloc();
        VK12.vkGetPhysicalDeviceFormatProperties(vkDevice, formatValue, properties);
        return new FormatProperties(
                BitFlags.getBitFlags(properties.linearTilingFeatures(), FormatFeatureFlagBits.values())
                        .toArray(new FormatFeatureFlagBits[0]),
                BitFlags.getBitFlags(properties.optimalTilingFeatures(), FormatFeatureFlagBits.values())
                        .toArray(new FormatFeatureFlagBits[0]),
                BitFlags.getBitFlags(properties.bufferFeatures(), FormatFeatureFlagBits.values())
                        .toArray(new FormatFeatureFlagBits[0]));
    }

    @Override
    public Format[] getSupportedFormats(ImageTiling tiling, FormatFeatureFlagBits... features) {
        ArrayList<Format> result = new ArrayList<Format>();
        for (Vulkan10.Format format : Format.values()) {
            FormatProperties properties = getFormatProperties(selectedDevice, format.value);
            if (properties.supportsFeature(tiling, features)) {
                result.add(format);
            }
        }
        return result.toArray(new Format[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Queue createQueue(QueueFamilyProperties selectedQueue, CommandBuffers<?> pool) {
        PointerBuffer pb = MemoryUtil.memAllocPointer(1);
        VK12.vkGetDeviceQueue(deviceInstance, selectedQueue.getQueueIndex(), 0, pb);
        VkQueue vkQueue = new VkQueue(pb.get(0), deviceInstance);
        MemoryUtil.memFree(pb);
        return new LWJGLVulkan12Queue(vkQueue, (CommandBuffers) pool);
    }

    @Override
    public CommandBuffers<?> createCommandPool(QueueFamilyProperties queueFamily, int commandBufferCount) {
        if (queueFamily == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(VK12.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                .queueFamilyIndex(queueFamily.getQueueIndex());

        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkCreateCommandPool(deviceInstance, poolInfo, null, lb));

        VkCommandBufferAllocateInfo cmd = VkCommandBufferAllocateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .pNext(MemoryUtil.NULL)
                .commandPool(lb.get(0))
                .level(VK12.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(commandBufferCount);

        PointerBuffer pointers = MemoryUtil.memAllocPointer(commandBufferCount);
        assertResult(VK12.vkAllocateCommandBuffers(deviceInstance, cmd, pointers));
        LWJGLCommandBuffer[] commands = new LWJGLCommandBuffer[commandBufferCount];
        for (int i = 0; i < commandBufferCount; i++) {
            commands[i] = new LWJGLCommandBuffer(new VkCommandBuffer(pointers.get(), deviceInstance));
        }
        MemoryUtil.memFree(pointers);
        Logger.d(getClass(), "Created command pool with " + commandBufferCount + " commandbuffers");
        CommandBuffers<?> commandBuffers = new LWJGLCommandBuffers(lb.get(0), queueFamily, commands);
        MemoryUtil.memFree(lb);
        return commandBuffers;
    }

    @Override
    public Semaphore createSemaphore(SemaphoreCreateInfo createInfo) {
        VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(createInfo.getFlagsValue());
        LongBuffer lb = MemoryUtil.memAllocLong(1);

        assertResult(VK12.vkCreateSemaphore(deviceInstance, semaphoreCreateInfo, null, lb));
        Semaphore semaphore = new Semaphore(lb.get(0));
        MemoryUtil.memFree(lb);
        return semaphore;
    }

    @Override
    public TimelineSemaphore createTimelineSemaphore() {
        VkSemaphoreTypeCreateInfo timelineCreateInfo = VkSemaphoreTypeCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_SEMAPHORE_TYPE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .semaphoreType(VK12.VK_SEMAPHORE_TYPE_TIMELINE)
                .initialValue(0);

        VkSemaphoreCreateInfo createInfo = VkSemaphoreCreateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
                .pNext(timelineCreateInfo)
                .flags(0);

        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkCreateSemaphore(deviceInstance, createInfo, null, lb));
        TimelineSemaphore semaphore = new TimelineSemaphore(lb.get(0));
        MemoryUtil.memFree(lb);
        timelineCreateInfo.free();
        createInfo.free();
        return semaphore;
    }

    @Override
    public TimelineSemaphoreSubmitInfo createTimelineSemaphoreSubmitInfo(long waitValue, long submitValue) {

        VkTimelineSemaphoreSubmitInfo timelineInfo = VkTimelineSemaphoreSubmitInfo.calloc();

        TimelineSemaphoreSubmitInfo timelineSubmitInfo = new TimelineSemaphoreSubmitInfo(timelineInfo.address(),
                waitValue, submitValue);

        timelineInfo.sType(VK12.VK_STRUCTURE_TYPE_TIMELINE_SEMAPHORE_SUBMIT_INFO)
                .pNext(MemoryUtil.NULL)
                .waitSemaphoreValueCount(1)
                .pWaitSemaphoreValues(timelineSubmitInfo.getWaitValue())
                .signalSemaphoreValueCount(1)
                .pSignalSemaphoreValues(timelineSubmitInfo.getSignalValue());
        return timelineSubmitInfo;
    }

    @Override
    public SubmitInfo createSubmitInfo(TimelineSemaphoreSubmitInfo timelineInfo, TimelineSemaphore wait,
            TimelineSemaphore signal, ByteBuffer commandBuffers) {

        PointerBuffer pCommandBuffers = PointerBuffer.create(commandBuffers);

        VkSubmitInfo info = VkSubmitInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(timelineInfo.getPointer())
                .waitSemaphoreCount(1)
                .pWaitSemaphores(wait.getSemaphoreBuffer())
                .pSignalSemaphores(signal.getSemaphoreBuffer())
                .pCommandBuffers(pCommandBuffers);

        return new SubmitInfo();
    }

    @Override
    public void waitSemaphores(TimelineSemaphore waitSemaphore, long waitValue) {

        LongBuffer waitBuffer = Buffers.createLongBuffer(1);
        waitBuffer.put(waitValue);

        VkSemaphoreWaitInfo waitInfo = VkSemaphoreWaitInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_SEMAPHORE_WAIT_INFO)
                .pNext(MemoryUtil.NULL)
                .flags(0)
                .semaphoreCount(1)
                .pSemaphores(waitSemaphore.getSemaphoreBuffer())
                .pValues(waitBuffer.position(0));

        assertResult(VK12.vkWaitSemaphores(deviceInstance, waitInfo, Long.MAX_VALUE));
        waitInfo.free();
    }

    @Override
    public void destroySemaphore(Semaphore semaphore) {
        VK12.vkDestroySemaphore(deviceInstance, semaphore.destroy(), null);
    }

    @Override
    public void updateDescriptorSets(TextureDescriptor[] descriptorImageInfos, DescriptorSet descriptorSet) {
        DescriptorSetLayoutBinding layoutBinding = descriptorSet.getLayoutBinding();
        VkWriteDescriptorSet.Buffer write = VkWriteDescriptorSet.calloc(1)
                .sType(VK12.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .pNext(MemoryUtil.NULL)
                .dstSet(descriptorSet.getDescriptorSet())
                .dstBinding(layoutBinding.getBinding())
                .descriptorType(layoutBinding.getDescriptorType().value);

        if (descriptorImageInfos != null) {
            VkDescriptorImageInfo.Buffer descriptorSets = VkDescriptorImageInfo.calloc(descriptorImageInfos.length);
            for (int index = 0; index < descriptorImageInfos.length; index++) {
                DescriptorImageInfo info = (DescriptorImageInfo) descriptorImageInfos[index];
                VkDescriptorImageInfo vkInfo = descriptorSets.get(index);
                if (info != null) {
                    vkInfo.sampler(info.sampler.getHandle())
                            .imageView(info.imageView.getImageView())
                            .imageLayout(info.imageLayout.value);
                } else {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "VkDescriptorImageInfo is null");
                }
                Logger.d(getClass(), "Updating descriptor for: " + info.imageView.type + " at binding " + (layoutBinding.getBinding() + index + ", set: " + descriptorSet.getDescriptorSet()));
            }
            write.descriptorCount(descriptorImageInfos.length).pImageInfo(descriptorSets);
        }

        VK12.vkUpdateDescriptorSets(deviceInstance, write, null);
    }

    @Override
    public void updateDescriptorSets(DescriptorBufferInfo[] descriptorBufferInfos, DescriptorSet[] descriptorSets) {
        if (descriptorBufferInfos == null || descriptorSets == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        VkWriteDescriptorSet.Buffer write = VkWriteDescriptorSet.calloc(descriptorBufferInfos.length);

        for (int set = 0; set < descriptorSets.length; set++) {
            DescriptorSet descriptorSet = descriptorSets[set];
            DescriptorBufferInfo bufferInfo = descriptorBufferInfos[set];
            VkDescriptorBufferInfo.Buffer vkInfo = null;
            VkWriteDescriptorSetAccelerationStructureKHR vkWrite = null;
            if (descriptorSet.getLayoutBinding().getDescriptorType() == DescriptorType.VK_DESCRIPTOR_TYPE_ACCELERATION_STRUCTURE_KHR) {
                AccelerationStructureDescriptorInfo asInfo = (AccelerationStructureDescriptorInfo) bufferInfo;
                vkWrite = VkWriteDescriptorSetAccelerationStructureKHR.calloc()
                        .sType(KHRAccelerationStructure.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET_ACCELERATION_STRUCTURE_KHR)
                        .pNext(MemoryUtil.NULL)
                        .pAccelerationStructures(asInfo.handles);
            } else {
                vkInfo = VkDescriptorBufferInfo.calloc(1);
                vkInfo.get(0).buffer(bufferInfo.getBuffer().getPointer())
                        .offset(bufferInfo.offset)
                        .range(bufferInfo.range);
            }

            write.get()
                    .sType(VK12.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .pNext(vkWrite != null ? vkWrite.address() : MemoryUtil.NULL)
                    .dstSet(descriptorSet.getDescriptorSet())
                    .dstBinding(descriptorSet.getLayoutBinding().getBinding())
                    .descriptorType(descriptorSet.getLayoutBinding().getDescriptorType().value)
                    .descriptorCount(1)
                    .pBufferInfo(vkInfo);
            Logger.d(getClass(), "UpdateDescriptorSet: " + descriptorSet.toString() + ",  Buffer: " + bufferInfo.toString());

        }
        write.clear();
        VK12.vkUpdateDescriptorSets(deviceInstance, write, null);
    }

    @Override
    public DescriptorSet allocateDescriptorSet(DescriptorSetLayout descriptorLayout) {
        VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                .pNext(MemoryUtil.NULL)
                .descriptorPool(descriptorPool.getDescriptorPool())
                .pSetLayouts(descriptorLayout.getDescriptorSetLayoutBuffer());

        LongBuffer lb = MemoryUtil.memAllocLong(1);
        assertResult(VK12.vkAllocateDescriptorSets(deviceInstance, allocateInfo, lb));
        DescriptorSet descriptorSet = new DescriptorSet(lb.get(0), descriptorLayout);
        MemoryUtil.memFree(lb);
        allocateInfo.free();
        Logger.d(getClass(), "Allocated descriptorset for: " + descriptorLayout + ", set:" + descriptorSet.getDescriptorSet());
        return descriptorSet;
    }

    @Override
    public void freeDescriptorSet(DescriptorSet descriptorSet) {
        if (descriptorSet.getDescriptorSet() == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", Already released descriptor.");
        }
        Logger.d(getClass(), "Freeing descriptorset " + descriptorSet.getDescriptorSet());
        assertResult(VK12.vkFreeDescriptorSets(deviceInstance, descriptorPool.getDescriptorPool(), descriptorSet.getDescriptorSetBuffer()));
        descriptorSet.getDescriptorSetBuffer().put(0);
    }

    @Override
    public PhysicalDeviceFeatures getPhysicalDeviceFeatures() {
        return selectedDevice.getFeatures().getPhysicalDeviceFeatures();
    }

    private void assertExtensionEnabled(VulkanExtension extension) {
        if (!org.varg.vulkan.Vulkan10.Extension.contains(extension.getName(), getLogicalDevice().getEnabledExtensions())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + " extension not enabled: " + extension.getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public KHRSwapchain<LWJGLVulkan12Queue> getKHRSwapchain() {
        if (khrSwapchainExtension == null) {
            assertExtensionEnabled(Vulkan10.Extension.VK_KHR_swapchain);
            khrSwapchainExtension = new LWJGL3KHRSwapchain(deviceInstance, surface);
        }
        return (KHRSwapchain<LWJGLVulkan12Queue>) khrSwapchainExtension;
    }

    @Override
    public EXTHDRMetadata getEXTHDRMetadata() {
        if (extHdrMetadata == null) {
            assertExtensionEnabled(Vulkan10.Extension.VK_EXT_hdr_metadata);
            extHdrMetadata = new LWJGL3HDRMetadata(deviceInstance);
        }
        return extHdrMetadata;
    }

    @Override
    public org.varg.vulkan.extensions.EXTMeshShader<?> getEXTMeshShader() {
        if (extMeshShader == null) {
            assertExtensionEnabled(Vulkan10.Extension.VK_EXT_mesh_shader);
            extMeshShader = new LWJGL3EXTMeshShader(logicalDevice.getFeatures().getPhysicalDeviceFeatureExtensions().getPhysicalDeviceMeshShaderFeaturesEXT(),
                    (PhysicalDeviceMeshShaderPropertiesEXT) selectedDevice.getProperties().getProperties(Vulkan10.Extension.VK_EXT_mesh_shader));
        }
        return extMeshShader;
    }

    @Override
    public KHRRayTracingPipeline<?> getKHRRayTracingPipeline() {
        if (khrRayTracingPipeline == null) {
            assertExtensionEnabled(Vulkan10.Extension.VK_KHR_ray_tracing_pipeline);
            khrRayTracingPipeline = new LWJGL3KHRRayTracingPipeline(deviceInstance, logicalDevice.getFeatures().getPhysicalDeviceFeatureExtensions().getPhysicalDeviceRayTracingPipelineFeaturesKHR(),
                    (PhysicalDeviceRayTracingPipelinePropertiesKHR) selectedDevice.getProperties().getProperties(Vulkan10.Extension.VK_KHR_ray_tracing_pipeline));
        }
        return khrRayTracingPipeline;
    }

    @Override
    public KHRFragmentShadingRate getKHRFragmentShadingRate() {
        if (khrFragmentShadingRate == null) {
            assertExtensionEnabled(Vulkan10.Extension.VK_KHR_fragment_shading_rate);
            khrFragmentShadingRate = new LWJGL3KHRFragmentShadingRate(logicalDevice.getFeatures().getPhysicalDeviceFeatureExtensions().getPhysicalDeviceFragmentShadingRateFeaturesKHR(),
                    (PhysicalDeviceFragmentShadingRatePropertiesKHR) selectedDevice.getProperties().getProperties(Vulkan10.Extension.VK_KHR_fragment_shading_rate));
        }
        return khrFragmentShadingRate;
    }

    @Override
    protected void createBackend(CreateDevice callback) throws BackendException {
        stack = MemoryStack.stackPush();
        createDeviceInstance();
        createLogicalDevice(callback, window);
    }

    @Override
    public SubresourceLayout getSubresourceLayout(long vkImage) {
        return getSubresourceLayout(deviceInstance, vkImage);
    }

    /**
     * Creates a subresourcelayout for the image using miplevel 0 and arraylayer 0
     * 
     * @param device
     * @param vkImage
     * @return
     */
    public static SubresourceLayout getSubresourceLayout(VkDevice device, long vkImage) {
        VkImageSubresource subres = VkImageSubresource.calloc()
                .aspectMask(VK12.VK_IMAGE_ASPECT_COLOR_BIT)
                .mipLevel(0)
                .arrayLayer(0);
        VkSubresourceLayout layout = VkSubresourceLayout.calloc();
        VK12.vkGetImageSubresourceLayout(device, vkImage, subres, layout);
        SubresourceLayout result = new SubresourceLayout(layout.offset(), layout.size(), layout.rowPitch(), layout
                .arrayPitch(),
                layout.depthPitch());
        subres.free();
        layout.free();
        return result;
    }

    @Override
    public org.varg.vulkan.extensions.KHRAccelerationStructure<?> getKHRAccelerationStructure() {
        if (khrAccelerationStructure == null) {
            assertExtensionEnabled(Vulkan10.Extension.VK_KHR_acceleration_structure);
            khrAccelerationStructure = new LWJGL3KHRAccelerationStructure(deviceInstance);
        }
        return khrAccelerationStructure;

    }

    @Override
    public QueryPool createQueryPool(QueryPoolCreateInfo createInfo) {
        VkQueryPoolCreateInfo vkInfo = VkQueryPoolCreateInfo.calloc()
                .sType(VK10.VK_STRUCTURE_TYPE_QUERY_POOL_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .queryType(createInfo.queryType.value)
                .queryCount(createInfo.queryCount)
                .pipelineStatistics(BitFlags.getFlagsValue(createInfo.pipelineStatistics));

        LongBuffer handle = BufferUtils.createLongBuffer(1);
        VK12.vkCreateQueryPool(deviceInstance, vkInfo, null, handle);
        QueryPool result = new QueryPool(handle.get(0), createInfo);
        vkInfo.free();
        return result;
    }

    @Override
    public int getQueryPoolResults(QueryPool queryPool, int firstQuery, int queryCount, ByteBuffer data, int stride, QueryResultFlagBits... flags) {
        int flagValue = BitFlags.getFlagsValue(flags);
        return VK12.vkGetQueryPoolResults(deviceInstance, queryPool.getHandle(), firstQuery, queryCount, data, stride, flagValue);
    }

}

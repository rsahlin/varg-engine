
package org.varg.vulkan.structs;

import org.gltfio.lib.AllowPublic;

/**
 * Abstraction of VkPhysicalDeviceVulkan12Features
 * Member names must be intact since reflection is used to copy values to/from native Vk structs.
 *
 */
public class PhysicalDeviceVulkan12Features extends PhysicalDeviceVulkan11Features {

    public enum Vulkan12Features implements VulkanPhysicalDeviceFeatures {
        samplerMirrorClampToEdge(),
        drawIndirectCount(),
        storageBuffer8BitAccess(),
        uniformAndStorageBuffer8BitAccess(),
        storagePushConstant8(),
        shaderBufferInt64Atomics(),
        shaderSharedInt64Atomics(),
        shaderFloat16(),
        shaderInt8(),
        descriptorIndexing(),
        shaderInputAttachmentArrayDynamicIndexing(),
        shaderUniformTexelBufferArrayDynamicIndexing(),
        shaderStorageTexelBufferArrayDynamicIndexing(),
        shaderUniformBufferArrayNonUniformIndexing(),
        shaderSampledImageArrayNonUniformIndexing(),
        shaderStorageBufferArrayNonUniformIndexing(),
        shaderStorageImageArrayNonUniformIndexing(),
        shaderInputAttachmentArrayNonUniformIndexing(),
        shaderUniformTexelBufferArrayNonUniformIndexing(),
        shaderStorageTexelBufferArrayNonUniformIndexing(),
        descriptorBindingUniformBufferUpdateAfterBind(),
        descriptorBindingSampledImageUpdateAfterBind(),
        descriptorBindingStorageImageUpdateAfterBind(),
        descriptorBindingStorageBufferUpdateAfterBind(),
        descriptorBindingUniformTexelBufferUpdateAfterBind(),
        descriptorBindingStorageTexelBufferUpdateAfterBind(),
        descriptorBindingUpdateUnusedWhilePending(),
        descriptorBindingPartiallyBound(),
        descriptorBindingVariableDescriptorCount(),
        runtimeDescriptorArray(),
        samplerFilterMinmax(),
        scalarBlockLayout(),
        imagelessFramebuffer(),
        uniformBufferStandardLayout(),
        shaderSubgroupExtendedTypes(),
        separateDepthStencilLayouts(),
        hostQueryReset(),
        timelineSemaphore(),
        bufferDeviceAddress(),
        bufferDeviceAddressCaptureReplay(),
        bufferDeviceAddressMultiDevice(),
        vulkanMemoryModel(),
        vulkanMemoryModelDeviceScope(),
        vulkanMemoryModelAvailabilityVisibilityChains(),
        shaderOutputViewportIndex(),
        shaderOutputLayer(),
        subgroupBroadcastDynamicId();
    }

    @AllowPublic
    public boolean samplerMirrorClampToEdge;
    @AllowPublic
    public boolean drawIndirectCount;
    @AllowPublic
    public boolean storageBuffer8BitAccess;
    @AllowPublic
    public boolean uniformAndStorageBuffer8BitAccess;
    @AllowPublic
    public boolean storagePushConstant8;
    @AllowPublic
    public boolean shaderBufferInt64Atomics;
    @AllowPublic
    public boolean shaderSharedInt64Atomics;
    @AllowPublic
    public boolean shaderFloat16;
    @AllowPublic
    public boolean shaderInt8;
    @AllowPublic
    public boolean descriptorIndexing;
    @AllowPublic
    public boolean shaderInputAttachmentArrayDynamicIndexing;
    @AllowPublic
    public boolean shaderUniformTexelBufferArrayDynamicIndexing;
    @AllowPublic
    public boolean shaderStorageTexelBufferArrayDynamicIndexing;
    @AllowPublic
    public boolean shaderUniformBufferArrayNonUniformIndexing;
    @AllowPublic
    public boolean shaderSampledImageArrayNonUniformIndexing;
    @AllowPublic
    public boolean shaderStorageBufferArrayNonUniformIndexing;
    @AllowPublic
    public boolean shaderStorageImageArrayNonUniformIndexing;
    @AllowPublic
    public boolean shaderInputAttachmentArrayNonUniformIndexing;
    @AllowPublic
    public boolean shaderUniformTexelBufferArrayNonUniformIndexing;
    @AllowPublic
    public boolean shaderStorageTexelBufferArrayNonUniformIndexing;
    @AllowPublic
    public boolean descriptorBindingUniformBufferUpdateAfterBind;
    @AllowPublic
    public boolean descriptorBindingSampledImageUpdateAfterBind;
    @AllowPublic
    public boolean descriptorBindingStorageImageUpdateAfterBind;
    @AllowPublic
    public boolean descriptorBindingStorageBufferUpdateAfterBind;
    @AllowPublic
    public boolean descriptorBindingUniformTexelBufferUpdateAfterBind;
    @AllowPublic
    public boolean descriptorBindingStorageTexelBufferUpdateAfterBind;
    @AllowPublic
    public boolean descriptorBindingUpdateUnusedWhilePending;
    @AllowPublic
    public boolean descriptorBindingPartiallyBound;
    @AllowPublic
    public boolean descriptorBindingVariableDescriptorCount;
    @AllowPublic
    public boolean runtimeDescriptorArray;
    @AllowPublic
    public boolean samplerFilterMinmax;
    @AllowPublic
    public boolean scalarBlockLayout;
    @AllowPublic
    public boolean imagelessFramebuffer;
    @AllowPublic
    public boolean uniformBufferStandardLayout;
    @AllowPublic
    public boolean shaderSubgroupExtendedTypes;
    @AllowPublic
    public boolean separateDepthStencilLayouts;
    @AllowPublic
    public boolean hostQueryReset;
    @AllowPublic
    public boolean timelineSemaphore;
    @AllowPublic
    public boolean bufferDeviceAddress;
    @AllowPublic
    public boolean bufferDeviceAddressCaptureReplay;
    @AllowPublic
    public boolean bufferDeviceAddressMultiDevice;
    @AllowPublic
    public boolean vulkanMemoryModel;
    @AllowPublic
    public boolean vulkanMemoryModelDeviceScope;
    @AllowPublic
    public boolean vulkanMemoryModelAvailabilityVisibilityChains;
    @AllowPublic
    public boolean shaderOutputViewportIndex;
    @AllowPublic
    public boolean shaderOutputLayer;
    @AllowPublic
    public boolean subgroupBroadcastDynamicId;

    /**
     * Gets the VkPhysicalDeviceVulkan12Features from the source struct
     * 
     * @param source The VkPhysicalDeviceVulkan12Features where boolean fields are read from
     */
    public void getVulkan12Features(Object source) {
        copyBooleanFieldsFromStruct(source, PhysicalDeviceVulkan12Features.class);
    }

    /**
     * Stores the features into a VkPhysicalDeviceVulkan12Features object.
     * 
     * @param destination The destination platform object where boolean fields are stored
     */
    public void setVulkan12Features(Object destination) {
        copyBooleanFieldToMethod(destination, this,
                PhysicalDeviceVulkan12Features.class.getDeclaredFields());
    }

}


package org.varg.vulkan.structs;

/**
 * Abstraction for VkPhysicalDeviceVulkan13Features
 * File must be excluded from checkstyle otherwise membername textureCompressionASTC_HDR is reported as illegal.
 * Member names must be intact since reflection is used to copy values to/from native Vk structs.
 *
 */
public class PhysicalDeviceVulkan13Features extends PhysicalDeviceVulkan12Features {

    public enum Vulkan13Features implements VulkanPhysicalDeviceFeatures {
        robustImageAccess(),
        inlineUniformBlock(),
        descriptorBindingInlineUniformBlockUpdateAfterBind(),
        pipelineCreationCacheControl(),
        privateData(),
        shaderDemoteToHelperInvocation(),
        shaderTerminateInvocation(),
        subgroupSizeControl(),
        computeFullSubgroups(),
        synchronization2(),
        textureCompressionASTC_HDR(),
        shaderZeroInitializeWorkgroupMemory(),
        dynamicRendering(),
        shaderIntegerDotProduct(),
        maintenance4();
    }

    public boolean robustImageAccess;
    public boolean inlineUniformBlock;
    public boolean descriptorBindingInlineUniformBlockUpdateAfterBind;
    public boolean pipelineCreationCacheControl;
    public boolean privateData;
    public boolean shaderDemoteToHelperInvocation;
    public boolean shaderTerminateInvocation;
    public boolean subgroupSizeControl;
    public boolean computeFullSubgroups;
    public boolean synchronization2;
    public boolean textureCompressionASTC_HDR;
    public boolean shaderZeroInitializeWorkgroupMemory;
    public boolean dynamicRendering;
    public boolean shaderIntegerDotProduct;
    public boolean maintenance4;

    /**
     * Gets the VkPhysicalDeviceVulkan13Features from the source struct
     * 
     * @param source The VkPhysicalDeviceVulkan13Features where boolean fields are read from.
     */
    public void getVulkan13Features(Object source) {
        copyBooleanFieldsFromStruct(source, PhysicalDeviceVulkan13Features.class);
    }

    /**
     * Stores the features into a VkPhysicalDeviceVulkan13Features object.
     * 
     * @param destination The destination platform object where boolean fields are stored
     */
    public void setVulkan13Features(Object destination) {
        copyBooleanFieldToMethod(destination, this,
                PhysicalDeviceVulkan13Features.class.getDeclaredFields());
    }

}

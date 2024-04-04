
package org.varg.vulkan.structs;

import org.gltfio.lib.AllowPublic;

/**
 * Abstraction of VkPhysicalDeviceVulkan11Features
 * Member names must be intact since reflection is used to copy values to/from native Vk structs.
 *
 */
public class PhysicalDeviceVulkan11Features extends PhysicalDeviceFeatures {

    public enum Vulkan11Features implements VulkanPhysicalDeviceFeatures {
        storageBuffer16BitAccess(),
        uniformAndStorageBuffer16BitAccess(),
        storagePushConstant16(),
        storageInputOutput16(),
        multiview(),
        multiviewGeometryShader(),
        multiviewTessellationShader(),
        variablePointersStorageBuffer(),
        variablePointers(),
        protectedMemory(),
        samplerYcbcrConversion(),
        shaderDrawParameters();
    }

    @AllowPublic
    public boolean storageBuffer16BitAccess;
    @AllowPublic
    public boolean uniformAndStorageBuffer16BitAccess;
    @AllowPublic
    public boolean storagePushConstant16;
    @AllowPublic
    public boolean storageInputOutput16;
    @AllowPublic
    public boolean multiview;
    @AllowPublic
    public boolean multiviewGeometryShader;
    @AllowPublic
    public boolean multiviewTessellationShader;
    @AllowPublic
    public boolean variablePointersStorageBuffer;
    @AllowPublic
    public boolean variablePointers;
    @AllowPublic
    public boolean protectedMemory;
    @AllowPublic
    public boolean samplerYcbcrConversion;
    @AllowPublic
    public boolean shaderDrawParameters;

    /**
     * Gets the VkPhysicalDeviceVulkan11Features from the source struct
     * 
     * @param source The VkPhysicalDeviceVulkan11Features where boolean features are read from.
     */
    public void getVulkan11Features(Object source) {
        copyBooleanFieldsFromStruct(source, PhysicalDeviceVulkan11Features.class);
    }
    /**
     * Stores the features into a VkPhysicalDeviceVulkan11Features object.
     * 
     * @param destination The destination platform object where boolean fields are stored
     */
    public void setVulkan11Features(Object destination) {
        copyBooleanFieldToMethod(destination, this,
                PhysicalDeviceVulkan11Features.class.getDeclaredFields());
    }

}

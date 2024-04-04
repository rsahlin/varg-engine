
package org.varg.vulkan.structs;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.gltfio.lib.Logger;
import org.varg.vulkan.structs.PhysicalDeviceVulkan11Features.Vulkan11Features;
import org.varg.vulkan.structs.PhysicalDeviceVulkan12Features.Vulkan12Features;
import org.varg.vulkan.structs.PhysicalDeviceVulkan13Features.Vulkan13Features;

/**
 * 
 * Abstraction of VkPhysicalDeviceVulkan10Features
 * File must be excluded from checkstyle otherwise membername textureCompressionASTC_HDR is reported as illegal.
 * Member names must be intact since reflection is used to copy values to/from native Vk structs.
 *
 */
public abstract class PhysicalDeviceFeatures extends PlatformStruct {

    public interface VulkanPhysicalDeviceFeatures {
        String name();
    }

    public enum VulkanFeatures implements VulkanPhysicalDeviceFeatures {
        robustBufferAccess(),
        fullDrawIndexUint32(),
        imageCubeArray(),
        independentBlend(),
        geometryShader(),
        tessellationShader(),
        sampleRateShading(),
        dualSrcBlend(),
        logicOp(),
        multiDrawIndirect(),
        drawIndirectFirstInstance(),
        depthClamp(),
        depthBiasClamp(),
        fillModeNonSolid(),
        depthBounds(),
        wideLines(),
        largePoints(),
        alphaToOne(),
        multiViewport(),
        samplerAnisotropy(),
        textureCompressionETC2(),
        textureCompressionASTC_LDR(),
        textureCompressionBC(),
        occlusionQueryPrecise(),
        pipelineStatisticsQuery(),
        vertexPipelineStoresAndAtomics(),
        fragmentStoresAndAtomics(),
        shaderTessellationAndGeometryPointSize(),
        shaderImageGatherExtended(),
        shaderStorageImageExtendedFormats(),
        shaderStorageImageMultisample(),
        shaderStorageImageReadWithoutFormat(),
        shaderStorageImageWriteWithoutFormat(),
        shaderUniformBufferArrayDynamicIndexing(),
        shaderSampledImageArrayDynamicIndexing(),
        shaderStorageBufferArrayDynamicIndexing(),
        shaderStorageImageArrayDynamicIndexing(),
        shaderClipDistance(),
        shaderCullDistance(),
        shaderFloat64(),
        shaderInt64(),
        shaderInt16(),
        shaderResourceResidency(),
        shaderResourceMinLod(),
        sparseBinding(),
        sparseResidencyBuffer(),
        sparseResidencyImage2D(),
        sparseResidencyImage3D(),
        sparseResidency2Samples(),
        sparseResidency4Samples(),
        sparseResidency8Samples(),
        sparseResidency16Samples(),
        sparseResidencyAliased(),
        variableMultisampleRate(),
        inheritedQueries();
    }

    /**
     * Fields declared in VkPhysicalDeviceFeatures
     */
    public boolean robustBufferAccess = false;
    public boolean fullDrawIndexUint32 = false;
    public boolean imageCubeArray = false;
    public boolean independentBlend = false;
    public boolean geometryShader = false;
    public boolean tessellationShader = false;
    public boolean sampleRateShading = false;
    public boolean dualSrcBlend = false;
    public boolean logicOp = false;
    public boolean multiDrawIndirect = false;
    public boolean drawIndirectFirstInstance = false;
    public boolean depthClamp = false;
    public boolean depthBiasClamp = false;
    public boolean fillModeNonSolid = false;
    public boolean depthBounds = false;
    public boolean wideLines = false;
    public boolean largePoints = false;
    public boolean alphaToOne = false;
    public boolean multiViewport = false;
    public boolean samplerAnisotropy = false;
    public boolean textureCompressionETC2 = false;
    public boolean textureCompressionASTC_LDR = false;
    public boolean textureCompressionBC = false;
    public boolean occlusionQueryPrecise = false;
    public boolean pipelineStatisticsQuery = false;
    public boolean vertexPipelineStoresAndAtomics = false;
    public boolean fragmentStoresAndAtomics = false;
    public boolean shaderTessellationAndGeometryPointSize = false;
    public boolean shaderImageGatherExtended = false;
    public boolean shaderStorageImageExtendedFormats = false;
    public boolean shaderStorageImageMultisample = false;
    public boolean shaderStorageImageReadWithoutFormat = false;
    public boolean shaderStorageImageWriteWithoutFormat = false;
    public boolean shaderUniformBufferArrayDynamicIndexing = false;
    public boolean shaderSampledImageArrayDynamicIndexing = false;
    public boolean shaderStorageBufferArrayDynamicIndexing = false;
    public boolean shaderStorageImageArrayDynamicIndexing = false;
    public boolean shaderClipDistance = false;
    public boolean shaderCullDistance = false;
    public boolean shaderFloat64 = false;
    public boolean shaderInt64 = false;
    public boolean shaderInt16 = false;
    public boolean shaderResourceResidency = false;
    public boolean shaderResourceMinLod = false;
    public boolean sparseBinding = false;
    public boolean sparseResidencyBuffer = false;
    public boolean sparseResidencyImage2D = false;
    public boolean sparseResidencyImage3D = false;
    public boolean sparseResidency2Samples = false;
    public boolean sparseResidency4Samples = false;
    public boolean sparseResidency8Samples = false;
    public boolean sparseResidency16Samples = false;
    public boolean sparseResidencyAliased = false;
    public boolean variableMultisampleRate = false;
    public boolean inheritedQueries = false;

    /**
     * Gets the physical device features from the source struct
     * 
     * @param source The source VkPhysicalDeviceFeatures where boolean fields are read from.
     */
    public void getFeatures(Object source) {
        copyBooleanFieldsFromStruct(source, PhysicalDeviceFeatures.class);
    }

    @Override
    public String toString() {
        String result = "";
        for (Field scanField : getClass().getSuperclass().getDeclaredFields()) {
            String fieldName = scanField.getName();
            try {
                Field field = getClass().getSuperclass().getDeclaredField(fieldName);
                Object read = field.get(this);
                result += fieldName + "= " + toString(read) + "\n";
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
                Logger.d(getClass(), e.toString());
            }
        }
        return result;
    }

    protected String toString(Object read) {
        if (read instanceof int[]) {
            return Arrays.toString((int[]) read);
        }
        if (read instanceof float[]) {
            return Arrays.toString((float[]) read);
        }
        if (read instanceof long[]) {
            return Arrays.toString((long[]) read);
        }
        if (read instanceof byte[]) {
            return Arrays.toString((byte[]) read);
        }
        return read != null ? read.toString() : null;
    }

    /**
     * Stores the features into a VkPhysicalDeviceVulkanFeatures object.
     * 
     * @param destination The destination platform object where boolean fields are stored
     */
    public void setVulkanFeatures(Object destination) {
        copyBooleanFieldToMethod(destination, this,
                PhysicalDeviceFeatures.class.getDeclaredFields());
    }

    /**
     * Returns true if the features are supported
     * 
     * @param features
     * @return
     */
    public boolean hasFeatures(VulkanPhysicalDeviceFeatures... features) {
        boolean flag;
        for (VulkanPhysicalDeviceFeatures feature : features) {
            try {
                if (feature instanceof Vulkan13Features) {
                    flag = getBooleanField(PhysicalDeviceVulkan13Features.class.getDeclaredField(feature.name()));
                } else if (feature instanceof Vulkan12Features) {
                    flag = getBooleanField(PhysicalDeviceVulkan12Features.class.getDeclaredField(feature.name()));
                } else if (feature instanceof Vulkan11Features) {
                    flag = getBooleanField(PhysicalDeviceVulkan11Features.class.getDeclaredField(feature.name()));
                } else {
                    flag = getBooleanField(PhysicalDeviceFeatures.class.getDeclaredField(feature.name()));
                }
                if (!flag) {
                    return false;
                }
            } catch (NoSuchFieldException | SecurityException e) {
                Logger.e(getClass(), e.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the given features to the boolean value of flag.
     * 
     * @param flag
     * @param features
     */
    public void setBooleanFields(boolean flag, VulkanPhysicalDeviceFeatures... features) {
        for (VulkanPhysicalDeviceFeatures feature : features) {
            try {
                if (feature instanceof Vulkan13Features) {
                    setBooleanFields(flag, PhysicalDeviceVulkan13Features.class.getDeclaredField(feature.name()));
                } else if (feature instanceof Vulkan12Features) {
                    setBooleanFields(flag, PhysicalDeviceVulkan12Features.class.getDeclaredField(feature.name()));
                } else if (feature instanceof Vulkan11Features) {
                    setBooleanFields(flag, PhysicalDeviceVulkan11Features.class.getDeclaredField(feature.name()));
                } else {
                    setBooleanFields(flag, PhysicalDeviceFeatures.class.getDeclaredField(feature.name()));
                }
            } catch (NoSuchFieldException | SecurityException e) {
                Logger.e(getClass(), e.toString());
            }
        }
    }

    protected void setRequestedFields(PhysicalDeviceFeatures available, VulkanPhysicalDeviceFeatures... features) {

    }

}

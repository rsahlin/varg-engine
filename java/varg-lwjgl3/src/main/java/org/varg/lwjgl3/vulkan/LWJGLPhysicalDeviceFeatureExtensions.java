
package org.varg.lwjgl3.vulkan;

import org.eclipse.jdt.annotation.NonNull;
import org.lwjgl.vulkan.EXTConditionalRendering;
import org.lwjgl.vulkan.EXTIndexTypeUint8;
import org.lwjgl.vulkan.EXTMeshShader;
import org.lwjgl.vulkan.EXTRobustness2;
import org.lwjgl.vulkan.KHRFragmentShadingRate;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDevice16BitStorageFeatures;
import org.lwjgl.vulkan.VkPhysicalDevice8BitStorageFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceConditionalRenderingFeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceFragmentShadingRateFeaturesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceIndexTypeUint8FeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceMeshShaderFeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceRobustness2FeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceShaderFloat16Int8Features;
import org.varg.vulkan.extensions.EXTRobustness2.PhysicalDeviceRobustness2FeaturesEXT;
import org.varg.vulkan.extensions.PhysicalDeviceAccelerationStructureFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceFragmentShadingRateFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderFeaturesEXT;
import org.varg.vulkan.structs.PhysicalDevice16BitStorageFeatures;
import org.varg.vulkan.structs.PhysicalDevice8BitStorageFeatures;
import org.varg.vulkan.structs.PhysicalDeviceFeatureExtensions;
import org.varg.vulkan.structs.PhysicalDeviceShaderFloat16Int8Features;

public class LWJGLPhysicalDeviceFeatureExtensions extends PhysicalDeviceFeatureExtensions {

    @SuppressWarnings("checkstyle:linelength")
    public LWJGLPhysicalDeviceFeatureExtensions(@NonNull VkPhysicalDevice device) {
        VkPhysicalDeviceConditionalRenderingFeaturesEXT conRender = VkPhysicalDeviceConditionalRenderingFeaturesEXT
                .calloc()
                .sType(EXTConditionalRendering.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_CONDITIONAL_RENDERING_FEATURES_EXT);
        VkPhysicalDeviceIndexTypeUint8FeaturesEXT indexedUint8 = VkPhysicalDeviceIndexTypeUint8FeaturesEXT.calloc()
                .sType(EXTIndexTypeUint8.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_INDEX_TYPE_UINT8_FEATURES_EXT)
                .pNext(conRender.address());
        VkPhysicalDeviceMeshShaderFeaturesEXT meshShader = VkPhysicalDeviceMeshShaderFeaturesEXT.calloc()
                .sType(EXTMeshShader.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MESH_SHADER_FEATURES_EXT)
                .pNext(indexedUint8.address());
        VkPhysicalDevice16BitStorageFeatures storage16Bit = VkPhysicalDevice16BitStorageFeatures.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_16BIT_STORAGE_FEATURES)
                .pNext(meshShader.address());
        VkPhysicalDevice8BitStorageFeatures storage8Bit = VkPhysicalDevice8BitStorageFeatures.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_8BIT_STORAGE_FEATURES)
                .pNext(storage16Bit.address());
        VkPhysicalDeviceShaderFloat16Int8Features shader16Bit = VkPhysicalDeviceShaderFloat16Int8Features.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_FLOAT16_INT8_FEATURES)
                .pNext(storage8Bit.address());
        VkPhysicalDeviceRobustness2FeaturesEXT robustness2 = VkPhysicalDeviceRobustness2FeaturesEXT.calloc()
                .sType(EXTRobustness2.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_ROBUSTNESS_2_FEATURES_EXT)
                .pNext(shader16Bit.address());
        VkPhysicalDeviceFragmentShadingRateFeaturesKHR fragmentShadingRate =
                VkPhysicalDeviceFragmentShadingRateFeaturesKHR.calloc()
                        .sType(KHRFragmentShadingRate.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FRAGMENT_SHADING_RATE_FEATURES_KHR)
                        .pNext(robustness2.address());
        org.lwjgl.vulkan.VkPhysicalDeviceAccelerationStructureFeaturesKHR accelerationStructure =
                org.lwjgl.vulkan.VkPhysicalDeviceAccelerationStructureFeaturesKHR.calloc()
                        .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_ACCELERATION_STRUCTURE_FEATURES_KHR)
                        .pNext(fragmentShadingRate.address());

        VkPhysicalDeviceFeatures2 features = VkPhysicalDeviceFeatures2.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2)
                .pNext(accelerationStructure.address());

        VK12.vkGetPhysicalDeviceFeatures2(device, features);
        copyBooleanField(indexedUint8, getClass().getSuperclass(), "indexTypeUint8");
        copyBooleanField(conRender, getClass().getSuperclass(), "conditionalRendering");
        meshShaderFeatures = new PhysicalDeviceMeshShaderFeaturesEXT(meshShader);
        storage16BitFeatures = new PhysicalDevice16BitStorageFeatures(storage16Bit);
        storage8BitFeatures = new PhysicalDevice8BitStorageFeatures(storage8Bit);
        shaderFloat16Features = new PhysicalDeviceShaderFloat16Int8Features(shader16Bit);
        robustness2Features = new PhysicalDeviceRobustness2FeaturesEXT(robustness2);
        boolean att = fragmentShadingRate.attachmentFragmentShadingRate();
        boolean prim = fragmentShadingRate.primitiveFragmentShadingRate();
        boolean pipe = fragmentShadingRate.pipelineFragmentShadingRate();

        fragmentShadingRateFeatures = new PhysicalDeviceFragmentShadingRateFeaturesKHR(fragmentShadingRate);
        accelerationStructureFeatures = new PhysicalDeviceAccelerationStructureFeaturesKHR(accelerationStructure);
    }

}


package org.varg.vulkan.structs;

import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.DescriptorType;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;

/**
 * Agnostic abstraction of DeviceLimits
 *
 */
public abstract class DeviceLimits extends PlatformStruct {

    protected DeviceLimits() {
    }

    protected int maxImageDimension1D = Constants.NO_VALUE;
    protected int maxImageDimension2D = Constants.NO_VALUE;
    protected int maxImageDimension3D = Constants.NO_VALUE;
    protected int maxImageDimensionCube = Constants.NO_VALUE;
    protected int maxImageArrayLayers = Constants.NO_VALUE;
    protected int maxTexelBufferElements = Constants.NO_VALUE;
    protected int maxUniformBufferRange = Constants.NO_VALUE;
    protected int maxStorageBufferRange = Constants.NO_VALUE;
    protected int maxPushConstantsSize = Constants.NO_VALUE;
    protected int maxMemoryAllocationCount = Constants.NO_VALUE;
    protected int maxSamplerAllocationCount = Constants.NO_VALUE;
    protected long bufferImageGranularity = Constants.NO_VALUE;
    protected long sparseAddressSpaceSize = Constants.NO_VALUE;
    protected int maxBoundDescriptorSets = Constants.NO_VALUE;
    protected int maxPerStageDescriptorSamplers = Constants.NO_VALUE;
    protected int maxPerStageDescriptorUniformBuffers = Constants.NO_VALUE;
    protected int maxPerStageDescriptorStorageBuffers = Constants.NO_VALUE;
    protected int maxPerStageDescriptorSampledImages = Constants.NO_VALUE;
    protected int maxPerStageDescriptorStorageImages = Constants.NO_VALUE;
    protected int maxPerStageDescriptorInputAttachments = Constants.NO_VALUE;
    protected int maxPerStageResources = Constants.NO_VALUE;
    protected int maxDescriptorSetSamplers = Constants.NO_VALUE;
    protected int maxDescriptorSetUniformBuffers = Constants.NO_VALUE;
    protected int maxDescriptorSetUniformBuffersDynamic = Constants.NO_VALUE;
    protected int maxDescriptorSetStorageBuffers = Constants.NO_VALUE;
    protected int maxDescriptorSetStorageBuffersDynamic = Constants.NO_VALUE;
    protected int maxDescriptorSetSampledImages = Constants.NO_VALUE;
    protected int maxDescriptorSetStorageImages = Constants.NO_VALUE;
    protected int maxDescriptorSetInputAttachments = Constants.NO_VALUE;
    protected int maxVertexInputAttributes = Constants.NO_VALUE;
    protected int maxVertexInputBindings = Constants.NO_VALUE;
    protected int maxVertexInputAttributeOffset = Constants.NO_VALUE;
    protected int maxVertexInputBindingStride = Constants.NO_VALUE;
    protected int maxVertexOutputComponents = Constants.NO_VALUE;
    protected int maxTessellationGenerationLevel = Constants.NO_VALUE;
    protected int maxTessellationPatchSize = Constants.NO_VALUE;
    protected int maxTessellationControlPerVertexInputComponents = Constants.NO_VALUE;
    protected int maxTessellationControlPerVertexOutputComponents = Constants.NO_VALUE;
    protected int maxTessellationControlPerPatchOutputComponents = Constants.NO_VALUE;
    protected int maxTessellationControlTotalOutputComponents = Constants.NO_VALUE;
    protected int maxTessellationEvaluationInputComponents = Constants.NO_VALUE;
    protected int maxTessellationEvaluationOutputComponents = Constants.NO_VALUE;
    protected int maxGeometryShaderInvocations = Constants.NO_VALUE;
    protected int maxGeometryInputComponents = Constants.NO_VALUE;
    protected int maxGeometryOutputComponents = Constants.NO_VALUE;
    protected int maxGeometryOutputVertices = Constants.NO_VALUE;
    protected int maxGeometryTotalOutputComponents = Constants.NO_VALUE;
    protected int maxFragmentInputComponents = Constants.NO_VALUE;
    protected int maxFragmentOutputAttachments = Constants.NO_VALUE;
    protected int maxFragmentDualSrcAttachments = Constants.NO_VALUE;
    protected int maxFragmentCombinedOutputResources = Constants.NO_VALUE;
    protected int maxComputeSharedMemorySize = Constants.NO_VALUE;
    protected int[] maxComputeWorkGroupCount;
    protected int maxComputeWorkGroupInvocations = Constants.NO_VALUE;
    protected int[] maxComputeWorkGroupSize;
    protected int subPixelPrecisionBits = Constants.NO_VALUE;
    protected int subTexelPrecisionBits = Constants.NO_VALUE;
    protected int mipmapPrecisionBits = Constants.NO_VALUE;
    protected int maxDrawIndexedIndexValue = Constants.NO_VALUE;
    protected int maxDrawIndirectCount = Constants.NO_VALUE;
    protected float maxSamplerLodBias = Constants.NO_VALUE;
    protected float maxSamplerAnisotropy = Constants.NO_VALUE;
    protected int maxViewports = Constants.NO_VALUE;
    protected int[] maxViewportDimensions;
    protected float[] viewportBoundsRange;
    protected int viewportSubPixelBits = Constants.NO_VALUE;
    protected long minMemoryMapAlignment = Constants.NO_VALUE;
    protected long minTexelBufferOffsetAlignment = Constants.NO_VALUE;
    protected long minUniformBufferOffsetAlignment = Constants.NO_VALUE;
    protected long minStorageBufferOffsetAlignment = Constants.NO_VALUE;
    protected int minTexelOffset = Constants.NO_VALUE;
    protected int maxTexelOffset = Constants.NO_VALUE;
    protected int minTexelGatherOffset = Constants.NO_VALUE;
    protected int maxTexelGatherOffset = Constants.NO_VALUE;
    protected float minInterpolationOffset = Constants.NO_VALUE;
    protected float maxInterpolationOffset = Constants.NO_VALUE;
    protected int subPixelInterpolationOffsetBits = Constants.NO_VALUE;
    protected int maxFramebufferWidth = Constants.NO_VALUE;
    protected int maxFramebufferHeight = Constants.NO_VALUE;
    protected int maxFramebufferLayers = Constants.NO_VALUE;
    protected SampleCountFlagBit[] framebufferColorSampleCounts;
    protected SampleCountFlagBit[] framebufferDepthSampleCounts;
    protected SampleCountFlagBit[] framebufferStencilSampleCounts;
    protected SampleCountFlagBit[] framebufferNoAttachmentsSampleCounts;
    protected int maxColorAttachments = Constants.NO_VALUE;
    protected SampleCountFlagBit[] sampledImageColorSampleCounts;
    protected SampleCountFlagBit[] sampledImageIntegerSampleCounts;
    protected SampleCountFlagBit[] sampledImageDepthSampleCounts;
    protected SampleCountFlagBit[] sampledImageStencilSampleCounts;
    protected SampleCountFlagBit[] storageImageSampleCounts;
    protected int maxSampleMaskWords = Constants.NO_VALUE;
    protected boolean timestampComputeAndGraphics;
    protected float timestampPeriod = Constants.NO_VALUE;
    protected int maxClipDistances = Constants.NO_VALUE;
    protected int maxCullDistances = Constants.NO_VALUE;
    protected int maxCombinedClipAndCullDistances = Constants.NO_VALUE;
    protected int discreteQueuePriorities = Constants.NO_VALUE;
    protected float[] pointSizeRange;
    protected float[] lineWidthRange;
    protected float pointSizeGranularity = Constants.NO_VALUE;
    protected float lineWidthGranularity = Constants.NO_VALUE;
    protected boolean strictLines;
    protected boolean standardSampleLocations;
    protected long optimalBufferCopyOffsetAlignment = Constants.NO_VALUE;
    protected long optimalBufferCopyRowPitchAlignment = Constants.NO_VALUE;
    protected long nonCoherentAtomSize = Constants.NO_VALUE;

    public final float getMaxSamplerAnisotropy() {
        return maxSamplerAnisotropy;
    }

    public final int getMaxImageDimension1D() {
        return maxImageDimension1D;
    }

    public final int getMaxImageDimension2D() {
        return maxImageDimension2D;
    }

    public final int getMaxImageDimension3D() {
        return maxImageDimension3D;
    }

    public final int getMaxImageDimensionCube() {
        return maxImageDimensionCube;
    }

    public final int getMaxMemoryAllocationCount() {
        return maxMemoryAllocationCount;
    }

    public final long getSparseAddressSpaceSize() {
        return sparseAddressSpaceSize;
    }

    public final int getMaxComputeSharedMemorySize() {
        return maxComputeSharedMemorySize;
    }

    public final int[] getMaxComputeWorkGroupCount() {
        return maxComputeWorkGroupCount;
    }

    public final int getMaxComputeWorkGroupInvocations() {
        return maxComputeWorkGroupInvocations;
    }

    public final int[] getMaxComputeWorkGroupSize() {
        return maxComputeWorkGroupSize;
    }

    public final int getMaxViewports() {
        return maxViewports;
    }

    public final int[] getMaxViewportDimensions() {
        return maxViewportDimensions;
    }

    public final int getMaxFramebufferWidth() {
        return maxFramebufferWidth;
    }

    public final int getMaxFramebufferHeight() {
        return maxFramebufferHeight;
    }

    public final int getMaxFramebufferLayers() {
        return maxFramebufferLayers;
    }

    public final SampleCountFlagBit[] getFramebufferColorSampleCounts() {
        return framebufferColorSampleCounts;
    }

    public final SampleCountFlagBit[] getFramebufferDepthSampleCounts() {
        return framebufferDepthSampleCounts;
    }

    public final SampleCountFlagBit[] getFramebufferStencilSampleCounts() {
        return framebufferStencilSampleCounts;
    }

    public final SampleCountFlagBit[] getFramebufferNoAttachmentsSampleCounts() {
        return framebufferNoAttachmentsSampleCounts;
    }

    public final int getMaxColorAttachments() {
        return maxColorAttachments;
    }

    public final long getMinMemoryMapAlignment() {
        return minMemoryMapAlignment;
    }

    public final long getMinTexelBufferOffsetAlignment() {
        return minTexelBufferOffsetAlignment;
    }

    public final long getMinUniformBufferOffsetAlignment() {
        return minUniformBufferOffsetAlignment;
    }

    public final long getMinStorageBufferOffsetAlignment() {
        return minStorageBufferOffsetAlignment;
    }

    public final int getMaxVertexInputAttributeOffset() {
        return maxVertexInputAttributeOffset;
    }

    public final int getMaxSamplerAllocationCount() {
        return maxSamplerAllocationCount;
    }

    public final int getMaxDescriptorSetSamplers() {
        return maxDescriptorSetSamplers;
    }

    public final int getMaxPerStageDescriptorSamplers() {
        return maxPerStageDescriptorSamplers;
    }

    public final long getMinBufferAlignment(DescriptorType descriptorType) {
        switch (descriptorType) {
            case VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER:
            case VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC:
                return getMinUniformBufferOffsetAlignment();
            case VK_DESCRIPTOR_TYPE_STORAGE_BUFFER:
            case VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC:
                return getMinStorageBufferOffsetAlignment();
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
        }
    }

    public final int getMaxUniformBufferRange() {
        return maxUniformBufferRange;
    }

    public final int getMaxStorageBufferRange() {
        return maxStorageBufferRange;
    }

}

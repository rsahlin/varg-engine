package org.varg.lwjgl3.vulkan.extensions;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONTexture.ComponentMapping;
import org.gltfio.gltf2.JSONTexture.ComponentSwizzle;
import org.gltfio.lib.Settings;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkAcquireNextImageInfoKHR;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.varg.lwjgl3.vulkan.LWJGLVulkan12Queue;
import org.varg.vulkan.PhysicalDevice;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.VulkanBackend;
import org.varg.vulkan.Vulkan10.ImageUsageFlagBits;
import org.varg.vulkan.Vulkan10.PresentModeKHR;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.Vulkan10.SwapchainCreateFlagBitsKHR;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.image.Image;
import org.varg.vulkan.image.ImageCreateInfo;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.Semaphore;
import org.varg.vulkan.structs.SubresourceLayout;
import org.varg.vulkan.structs.SurfaceCapabilitiesKHR;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;

public class LWJGL3KHRSwapchain extends org.varg.vulkan.extensions.KHRSwapchain<LWJGLVulkan12Queue> {

    private final long surface;
    private final VkDevice deviceInstance;

    public LWJGL3KHRSwapchain(@NonNull VkDevice deviceInstance, long surface) {
        this.deviceInstance = deviceInstance;
        this.surface = surface;
    }

    @Override
    public void createSwapchainKHR(Vulkan10Backend<?> backend, Extent2D extent, ImageUsageFlagBits[] usage) {
        int imageCount = Settings.getInstance().getBoolean(BackendProperties.DOUBLEBUFFER) ? 2 : 1;
        PhysicalDevice device = backend.getSelectedDevice();
        SurfaceCapabilitiesKHR surfaceCapabilities = backend.getSurfaceCapabilities();
        SurfaceFormat surfaceFormat = backend.getSurfaceFormat();
        PresentModeKHR presentMode = backend.getPresentMode();
        if (device.getExtension(org.varg.vulkan.Vulkan10.Extension.VK_KHR_swapchain.name()) == null) {
            throw new IllegalArgumentException("Device has no support for "
                    + org.varg.vulkan.Vulkan10.Extension.VK_KHR_swapchain);
        }

        int preTransform;
        if ((surfaceCapabilities.getSupportedTransformsValue()
                & KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR) != 0) {
            preTransform = KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
        } else {
            preTransform = surfaceCapabilities.getCurrentTransform().value;
        }

        VkExtent2D scExtent = VkExtent2D.calloc();
        scExtent.set(extent.width, extent.height);
        VkSwapchainCreateInfoKHR swapchainInfo = VkSwapchainCreateInfoKHR.calloc()
                .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                .pNext(MemoryUtil.NULL)
                .flags(BitFlags.getFlagsValue(new SwapchainCreateFlagBitsKHR[] {}))
                .surface(surface)
                .minImageCount(imageCount)
                .imageFormat(surfaceFormat.getFormat().value)
                .imageColorSpace(surfaceFormat.getColorSpace().value)
                .imageExtent(scExtent)
                .imageArrayLayers(1)
                .imageUsage(BitFlags.getFlagsValue(usage))
                .imageSharingMode(VK12.VK_SHARING_MODE_EXCLUSIVE)
                .pQueueFamilyIndices(null)
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(presentMode.value)
                .preTransform(preTransform)
                .oldSwapchain(VK12.VK_NULL_HANDLE)
                .clipped(true);
        long start = System.currentTimeMillis();
        LongBuffer lb = MemoryUtil.memAllocLong(1);
        VulkanBackend.assertResult(KHRSwapchain.vkCreateSwapchainKHR(deviceInstance, swapchainInfo, null, lb));
        Logger.d(getClass(),
                "vkCreateSwapchainKHR with format " + surfaceFormat.getFormat() + " in colorspace "
                        + surfaceFormat.getColorSpace() + " for usage " + BitFlags.toString(usage) + ", took "
                        + (System.currentTimeMillis() - start) + " millis");
        final long swapChain = lb.get(0);
        // If we just re-created an existing swapchain, we should destroy the old
        // swapchain at this point.
        IntBuffer ib = MemoryUtil.memAllocInt(1);
        VulkanBackend.assertResult(KHRSwapchain.vkGetSwapchainImagesKHR(deviceInstance, swapChain, ib, null));
        imageCount = ib.get(0);
        LongBuffer swapchainImages = Buffers.createLongBuffer(imageCount);
        VulkanBackend
                .assertResult(KHRSwapchain.vkGetSwapchainImagesKHR(deviceInstance, swapChain, ib, swapchainImages));

        // TODO - this should be read from the created swapchain images
        ImageCreateInfo imageCreateInfo = new ImageCreateInfo(surfaceFormat.getFormat().value, extent, usage,
                SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT);
        Image[] images = new Image[imageCount];
        for (int i = 0; i < imageCount; i++) {
            long vkImage = swapchainImages.get(i);
            SubresourceLayout subLayout = backend.getSubresourceLayout(vkImage);
            images[i] = new Image(vkImage, imageCreateInfo, subLayout);
        }

        ComponentMapping components = new ComponentMapping(ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY,
                ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY, ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY,
                ComponentSwizzle.COMPONENT_SWIZZLE_IDENTITY);
        ImageView[] swapChainViews = backend.createImageViews(images, components, 1, 1);
        Logger.d(getClass(), "Created " + swapChainViews.length + " image views.");

        MemoryUtil.memFree(ib);
        MemoryUtil.memFree(lb);
        createInfo = new SwapChainCreateInfoKHR(swapChain, swapChainViews, surfaceFormat, extent, presentMode);
    }

    @Override
    public int acquireNextImageKHR(Semaphore imageAquire, IntBuffer indexBuffer) {

        VkAcquireNextImageInfoKHR info = VkAcquireNextImageInfoKHR.calloc()
                .sType(KHRSwapchain.VK_STRUCTURE_TYPE_ACQUIRE_NEXT_IMAGE_INFO_KHR)
                .semaphore(imageAquire.getSemaphore())
                .fence(MemoryUtil.NULL)
                .timeout(~2000000L)
                .swapchain(getSwapChain())
                .deviceMask(1)
                .pNext(MemoryUtil.NULL);

        int resultVal = KHRSwapchain.vkAcquireNextImage2KHR(deviceInstance, info, indexBuffer);
        Vulkan10.Result result = Vulkan10.Result.getResult(resultVal);
        switch (result) {
            case VK_SUBOPTIMAL_KHR:
                Logger.d(getClass(), "Suboptimal");
            case VK_SUCCESS:
            case VK_TIMEOUT:
            case VK_NOT_READY:
                return resultVal;
            default:
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_STATE.message + " could not aquire next image: " + result);
        }
    }

    @Override
    public int queuePresentKHR(LWJGLVulkan12Queue queue, Semaphore waitSemaphores, IntBuffer indexBuffer) {

        VkPresentInfoKHR present = VkPresentInfoKHR.calloc()
                .sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                .pNext(MemoryUtil.NULL)
                .pWaitSemaphores(waitSemaphores.getSemaphoreBuffer())
                .swapchainCount(1)
                .pSwapchains(getSwapChainBuffer())
                .pImageIndices(indexBuffer);

        // TODO - figure out a way to properly encapsulate the queue.
        // Could this extension be created with the VkQueue object, and remove the queue from this method?
        int resultVal = KHRSwapchain.vkQueuePresentKHR(queue.getQueue(), present);
        present.free();
        Vulkan10.Result result = Vulkan10.Result.getResult(resultVal);
        switch (result) {
            case VK_SUCCESS:
            case VK_SUBOPTIMAL_KHR:
                return resultVal;
            default:
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_STATE.message + "Queue present failed with " + result);
        }
    }

}

package org.varg.vulkan.extensions;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.Vulkan10.ImageUsageFlagBits;
import org.varg.vulkan.Vulkan10.PresentModeKHR;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.image.Image;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.Semaphore;

/**
 * Handles the VK_KHR_swapchain extension
 *
 */
public abstract class KHRSwapchain<Q extends Queue> {

    public static class SwapChainCreateInfoKHR {

        public final SurfaceFormat surfaceFormat;
        public final Extent2D extent;
        public final PresentModeKHR presentMode;
        private final LongBuffer swapChainBuffer = Buffers.createLongBuffer(1);
        private final ImageView[] imageViews;

        public SwapChainCreateInfoKHR(long swapChain, ImageView[] imageViews, SurfaceFormat surfaceFormat,
                Extent2D extent,
                PresentModeKHR presentMode) {
            if (swapChain == 0 || imageViews == null) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
            }
            Image img = imageViews[0].image;
            for (int i = 1; i < imageViews.length; i++) {
                if (img.getExtent() != imageViews[i].image.getExtent()) {
                    throw new IllegalArgumentException(
                            ErrorMessage.INVALID_VALUE.message + "Image sizes must match");
                }
                if (img.getUsageFlags() != imageViews[i].image.getUsageFlags()) {
                    throw new IllegalArgumentException(
                            ErrorMessage.INVALID_VALUE.message + "Image usage flags must match");
                }
            }
            swapChainBuffer.put(swapChain);
            this.imageViews = imageViews;
            this.surfaceFormat = surfaceFormat;
            this.extent = extent;
            this.presentMode = presentMode;
        }
    }

    protected SwapChainCreateInfoKHR createInfo;

    private int currentBuffer = 0;

    /**
     * Returns the createinfo for this swapchain
     * 
     * @return
     */
    public SwapChainCreateInfoKHR getCreateInfo() {
        return createInfo;
    }

    /**
     * Returns the native pointer to the swapchain
     * 
     * @return
     */
    public long getSwapChain() {
        return createInfo.swapChainBuffer.get(0);
    }

    /**
     * Returns the buffer containing the native pointer to the swapchain, buffer positioned at 0
     * 
     * @return
     */
    public LongBuffer getSwapChainBuffer() {
        createInfo.swapChainBuffer.position(0);
        return createInfo.swapChainBuffer;
    }

    /**
     * Returns the current image
     * 
     * @return
     */
    public Image getCurrentImage() {
        return createInfo.imageViews[currentBuffer].image;
    }

    /**
     * Returns the imageview at index, index must be >= 0 < number of images
     * 
     * @param index
     * @return
     */
    public ImageView getImageView(int index) {
        return (index >= 0 && index < createInfo.imageViews.length) ? createInfo.imageViews[index] : null;
    }

    /**
     * Returns the swapchain extent
     * 
     * @return
     */
    public Extent2D getExtent() {
        return createInfo.imageViews[0].image.getExtent();
    }

    /**
     * Returns the number of images in swapchain
     * 
     * @return
     */
    public int getImageCount() {
        return createInfo.imageViews.length;
    }

    /**
     * Creates the swapchain - but not the images needed for the swapchain
     * 
     * @param backend
     * @param extent
     * @param usage Usage flags
     * @return The SwapChain wrapper, the images in swapchain are not created yet.
     */
    public abstract void createSwapchainKHR(Vulkan10Backend<?> backend, Extent2D extent, ImageUsageFlagBits[] usage);

    /**
     * Acquires next presentable image to be rendered into.
     * 
     * @param imageAquire
     * @param intBuffer
     * @return
     */
    public abstract int acquireNextImageKHR(Semaphore imageAquire, IntBuffer intBuffer);

    /**
     * Presents the image
     * 
     * @param queue
     * @param waitSemaphores
     * @param indexBuffer
     * @return
     */
    public abstract int queuePresentKHR(Q queue, Semaphore waitSemaphores, IntBuffer indexBuffer);

}

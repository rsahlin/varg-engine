
package org.varg.renderer;

import java.nio.IntBuffer;

import org.gltfio.gltf2.JSONScene;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.BACKGROUND;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.KHREnvironmentMapReference;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.Settings;
import org.ktximageio.ktx.ImageUtils;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.VulkanBackend.BackendIntProperties;
import org.varg.vulkan.VulkanBackend.IntArrayProperties;
import org.varg.vulkan.cmd.ClearColorValue;
import org.varg.vulkan.framebuffer.FrameBuffer;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.renderpass.ClearValue;
import org.varg.vulkan.renderpass.RenderPassBeginInfo;
import org.varg.vulkan.renderpass.RenderPassCreateInfo;
import org.varg.vulkan.renderpass.SubpassDescription2;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.Rect2D;

/**
 * Data and methods for handling swapbuffer funtionality
 *
 */
public class SwapBuffer {

    protected final FrameBuffer[] frameBuffers;
    protected final ClearValue[] clearValues;
    private final Rect2D renderArea;
    private final SurfaceFormat surfaceFormat;

    /**
     * The buffer that will be used to draw into
     */
    private IntBuffer currentBuffer = Buffers.createIntBuffer(1);

    public SwapBuffer(SurfaceFormat surfaceFormat, Extent2D extent, FrameBuffer[] buffers, ClearValue[] clear) {
        this.surfaceFormat = surfaceFormat;
        frameBuffers = buffers;
        clearValues = clear;
        renderArea = new Rect2D(0, 0, extent.width, extent.height);
    }

    /**
     * Sets the float clear value
     * 
     * @param rgba
     */
    public void setClearValue(float... rgba) {
        for (int i = 0; i < clearValues.length; i++) {
            clearValues[i].setClearColor(new ClearColorValue(rgba[0], rgba[1], rgba[2], rgba[3]));
        }
    }

    private void setClearValue(int... rgba) {
        for (int i = 0; i < clearValues.length; i++) {
            clearValues[i].setClearColor(new ClearColorValue(rgba));
        }
    }

    /**
     * Sets the swapbuffer clearvalue, first the settings value from CLEAR_COLOR is checked,
     * if not set the glTF is searched for asset metadata
     * 
     * @param glTF
     */
    public void setClearValue(JSONScene scene) {
        int[] rgba = Settings.getInstance().getIntArray(IntArrayProperties.CLEAR_COLOR);
        if (!Settings.getInstance().isPropertySet(IntArrayProperties.CLEAR_COLOR)) {
            KHREnvironmentMapReference envMap = scene != null ? scene.getEnvironmentExtension() : null;
            if (envMap != null) {
                BACKGROUND bgHint = envMap.getBackgroundHint();
                if (bgHint != null && bgHint == BACKGROUND.CLEAR) {
                    float[] clearValue = envMap.getClearValue();
                    if (clearValue != null) {
                        rgba = new int[] { (int) (clearValue[0] * 255), (int) (clearValue[1] * 255),
                                (int) (clearValue[2] * 255), (int) (clearValue[3] * 255) };
                    }
                }
            }
        }
        if (surfaceFormat.format.isSRGB()) {
            ImageUtils.toLinear(rgba);
        }
        // Adjust clearscreen color if hdr ouput
        if (surfaceFormat.getColorSpace().isPQColorSpace()) {
            float hdrFactor = (float) Settings.getInstance().getInt(BackendIntProperties.HDR_MAX_CONTENT_LIGHTLEVEL)
                    / 10000;
            // rgba[0] = (int) (rgba[0] * hdrFactor);
            // rgba[1] = (int) (rgba[1] * hdrFactor);
            // rgba[2] = (int) (rgba[2] * hdrFactor);
        }
        setClearValue(rgba);
    }

    /**
     * Swapbuffer begin renderpass.
     * Returns the renderpass begin info, use this to issue command before rendering to the framebuffer.
     * 
     * @return
     */
    public RenderPassBeginInfo getRenderPassBeginInfo() {
        int index = currentBuffer.get(0);
        return new RenderPassBeginInfo(frameBuffers[index].createInfo.renderPass, frameBuffers[index], renderArea,
                clearValues);
    }

    /**
     * Returns the width and height of render area, in pixels
     * 
     * @return
     */
    public Rect2D getRenderArea() {
        return renderArea;
    }

    /**
     * Returns the clear values
     * 
     * @return
     */
    public ClearValue[] getClearValues() {
        return clearValues;
    }

    /**
     * Returns the current active image index from the swapchain
     * 
     * @return The index to the current image
     */
    public int getCurrentImage() {
        return currentBuffer.get(0);
    }

    /**
     * Returns the current (resolved) imageview, this will not be multisampled
     * 
     * @return
     */
    public ImageView getCurrentImageView() {
        ImageView[] attachements = frameBuffers[getCurrentImage()].createInfo.pAttachments;
        if (attachements.length > SubpassDescription2.RESOLVE_ATTACHMENT_INDEX) {
            return attachements[SubpassDescription2.RESOLVE_ATTACHMENT_INDEX];
        }
        return attachements[SubpassDescription2.COLOR_ATTACHMENT_INDEX];
    }

    /**
     * Returns the current index buffer
     * 
     * @return
     */
    public IntBuffer getCurrentIndexBuffer() {
        currentBuffer.position(0);
        return currentBuffer;
    }

    /**
     * Returns the renderpasscreateinfo, this may only be used for reading values
     * 
     * @return
     */
    public RenderPassCreateInfo getRenderPassCreateInfo() {
        return frameBuffers[0].createInfo.renderPass.getRenderPassCreateInfo();
    }

}

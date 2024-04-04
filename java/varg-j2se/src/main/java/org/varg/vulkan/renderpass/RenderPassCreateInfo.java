
package org.varg.vulkan.renderpass;

import org.gltfio.lib.Settings;
import org.ktximageio.ktx.ImageReader.ImageFormat;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.AttachmentLoadOp;
import org.varg.vulkan.Vulkan10.AttachmentStoreOp;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.VulkanBackend.IntArrayProperties;

/**
 * Wrapper for VkRenderPassCreateInfo
 */
public class RenderPassCreateInfo {
    // Reserved for future use - VkRenderPassCreateFlags flags;
    public final AttachmentDescription[] attachments;
    public final SubpassDescription2[] subpasses;
    public final SubpassDependency[] dependencies;

    /**
     * Internal constructor do not use - use
     * {@link #createRenderPass(SampleCountFlagBit, ImageFormat, ImageFormat)}
     * 
     * @param formats
     * @param attachmentRefs Attachments shall be in the order as defined by
     * {@link SubpassDescription2#COLOR_ATTACHMENT_INDEX} and {@link SubpassDescription2#DEPTH_ATTACHMENT_INDEX}
     */
    private RenderPassCreateInfo(Vulkan10.Format[] formats, ImageLayout[] layouts, AttachmentReference[] attachmentRefs,
            AttachmentLoadOp[] loadOps,
            AttachmentStoreOp[] storeOps, SampleCountFlagBit[] samples) {
        attachments = new AttachmentDescription[attachmentRefs.length];
        int index = 0;
        for (AttachmentReference ref : attachmentRefs) {
            attachments[ref.attachment] = new AttachmentDescription(formats[ref.attachment], null,
                    loadOps[index], storeOps[index], samples[index], ImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
                    layouts[index]);
            index++;
        }
        subpasses = new SubpassDescription2[] { new SubpassDescription2(attachmentRefs) };
        dependencies = null;
    }

    public static RenderPassCreateInfo createRenderPass(SampleCountFlagBit samples, SurfaceFormat colorFormat,
            ImageFormat depthFormat) {
        RenderPassCreateInfo renderPassInfo = new RenderPassCreateInfo(
                createFormats(samples, colorFormat.format, Vulkan10.Format.VK_FORMAT_D16_UNORM),
                createLayouts(samples),
                createAttachmentReference(samples),
                createAttachmentLoadOp(samples), createAttachmentStoreOp(samples), createSamples(samples));
        return renderPassInfo;

    }

    private static ImageLayout[] createLayouts(SampleCountFlagBit samples) {
        ImageLayout[] created = samples == SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT ? new ImageLayout[2]
                : new ImageLayout[3];
        created[SubpassDescription2.COLOR_ATTACHMENT_INDEX] = ImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
        created[SubpassDescription2.DEPTH_ATTACHMENT_INDEX] =
                ImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
        if (created.length > 2) {
            created[SubpassDescription2.RESOLVE_ATTACHMENT_INDEX] =
                    ImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
        }

        return created;
    }

    private static SampleCountFlagBit[] createSamples(SampleCountFlagBit samples) {
        SampleCountFlagBit[] created = samples == SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT ? new SampleCountFlagBit[2]
                : new SampleCountFlagBit[3];
        created[SubpassDescription2.COLOR_ATTACHMENT_INDEX] = samples;
        created[SubpassDescription2.DEPTH_ATTACHMENT_INDEX] = samples;
        if (created.length > 2) {
            created[SubpassDescription2.RESOLVE_ATTACHMENT_INDEX] = SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT;
        }

        return created;
    }

    private static Vulkan10.Format[] createFormats(SampleCountFlagBit samples, Vulkan10.Format surfaceFormat,
            Vulkan10.Format depthFormat) {
        Vulkan10.Format[] formats = samples == SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT ? new Vulkan10.Format[2]
                : new Vulkan10.Format[3];
        formats[SubpassDescription2.COLOR_ATTACHMENT_INDEX] = surfaceFormat;
        formats[SubpassDescription2.DEPTH_ATTACHMENT_INDEX] = depthFormat;
        if (formats.length > 2) {
            formats[SubpassDescription2.RESOLVE_ATTACHMENT_INDEX] = surfaceFormat;
        }
        return formats;
    }

    private static AttachmentReference[] createAttachmentReference(SampleCountFlagBit samples) {
        AttachmentReference[] attachmentRefs = samples == SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT
                ? new AttachmentReference[2]
                : new AttachmentReference[3];
        attachmentRefs[SubpassDescription2.COLOR_ATTACHMENT_INDEX] = new AttachmentReference(
                SubpassDescription2.COLOR_ATTACHMENT_INDEX, ImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
        attachmentRefs[SubpassDescription2.DEPTH_ATTACHMENT_INDEX] = new AttachmentReference(
                SubpassDescription2.DEPTH_ATTACHMENT_INDEX,
                ImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
        if (attachmentRefs.length > 2) {
            attachmentRefs[SubpassDescription2.RESOLVE_ATTACHMENT_INDEX] = new AttachmentReference(
                    SubpassDescription2.RESOLVE_ATTACHMENT_INDEX, ImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
        }
        return attachmentRefs;
    }

    private static AttachmentLoadOp[] createAttachmentLoadOp(SampleCountFlagBit samples) {
        int[] clearColor = Settings.getInstance().getIntArray(IntArrayProperties.CLEAR_COLOR);
        AttachmentLoadOp colorLoadOp = clearColor != null ? AttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR
                : AttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
        if (samples == SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT) {
            return new AttachmentLoadOp[] { colorLoadOp, AttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR };
        } else {
            return new AttachmentLoadOp[] { colorLoadOp, AttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR,
                    AttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE };
        }
    }

    private static AttachmentStoreOp[] createAttachmentStoreOp(SampleCountFlagBit samples) {
        if (samples == SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT) {
            return new AttachmentStoreOp[] { AttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE,
                    AttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE };
        } else {
            return new AttachmentStoreOp[] { AttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE,
                    AttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE,
                    AttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE };
        }
    }

}

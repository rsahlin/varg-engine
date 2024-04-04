
package org.varg.lwjgl3.test;

import java.nio.ByteBuffer;

import org.gltfio.gltf2.JSONBufferView;
import org.gltfio.gltf2.JSONImage;
import org.gltfio.gltf2.RenderableScene;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.ktximageio.ktx.AwtImageUtils;
import org.ktximageio.ktx.ImageBuffer;
import org.ktximageio.ktx.ImageReader.ImageFormat;
import org.varg.assets.TextureImages;
import org.varg.assets.SourceImages.VulkanImageBuffer;
import org.varg.assets.TextureImages.TextureImageInfo;
import org.varg.gltf.VulkanMesh;
import org.varg.lwjgl3.apps.LWJGL3Application;
import org.varg.renderer.GltfRenderer;
import org.varg.renderer.Renderers;
import org.varg.shader.Gltf2GraphicsShader.GraphicsShaderType;
import org.varg.uniform.BindBuffer;
import org.varg.uniform.GltfStorageBuffers;
import org.varg.vulkan.Features;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.Vulkan10.ImageAspectFlagBit;
import org.varg.vulkan.Vulkan10Backend.CreateDevice;
import org.varg.vulkan.image.ImageSubresourceLayers;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.structs.RequestedFeatures;

public abstract class LWJGLBaseTest extends LWJGL3Application implements CreateDevice {

    public LWJGLBaseTest(String[] args, Renderers version, String title) {
        super(args, version, title);
    }

    @Override
    public Features getRequestedDeviceFeatures(Features availableFeatures) {
        RequestedFeatures requestedFeatures = getDefaultRequestedFeatures(availableFeatures);
        return requestedFeatures.getFeatures();
    }

    protected void validateVertexBuffers(RenderableScene glTF) {
        GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer = getRenderer();
        JSONBufferView[] bufferViews = glTF.getBufferViews();
        MemoryBuffer[] buffers = getRenderer().getAssets().getVertexBuffers(glTF).getMemoryBuffers();
        MemoryBuffer[] resultBuffers = new MemoryBuffer[buffers.length];
        renderer.getQueue().queueBegin();
        for (int i = 0; i < buffers.length; i++) {
            MemoryBuffer b = buffers[i];
            resultBuffers[i] = renderer.getBufferFactory().copyFromDeviceMemory(b, (int) b.size, renderer.getQueue());
        }
        renderer.getQueue().queueWaitIdle();

        for (int i = 0; i < resultBuffers.length; i++) {
            MemoryBuffer staging = resultBuffers[i];
            ByteBuffer bb = Buffers.createByteBuffer((int) staging.size);
            renderer.getBufferFactory().copyToBuffer(staging, bb);
            renderer.getBufferFactory().freeBuffer(staging);
            bb.position(0);
            compareBuffer(bb, bufferViews[i]);
        }
    }

    protected void validateUniformBuffers() {
        GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer = getRenderer();
        GltfStorageBuffers uniforms = (GltfStorageBuffers) renderer.getAssets().getStorageBuffers(
                GraphicsShaderType.GLTF2);
        BindBuffer[] buffers = uniforms.getBuffers();
        MemoryBuffer[] resultBuffers = new MemoryBuffer[buffers.length];
        renderer.getQueue().queueBegin();
        for (int i = 0; i < buffers.length; i++) {
            MemoryBuffer bm = buffers[i].getBuffer();
            resultBuffers[i] = renderer.getBufferFactory().copyFromDeviceMemory(bm, (int) bm.size, renderer.getQueue());
        }
        renderer.getQueue().queueWaitIdle();
        for (int i = 0; i < resultBuffers.length; i++) {
            compareBuffer(buffers[i].getBackingBuffer(), resultBuffers[i]);
            renderer.getBufferFactory().freeBuffer(resultBuffers[i]);
        }
    }

    private static class Diff {
        private int max = 0;
        private int min = 512;
        private long total = 0;
        private int count = 0;

        void add(int diff) {
            count++;
            max = Math.max(max, diff);
            min = Math.min(min, diff);
            total += diff;
        }

    }

    protected void validateTextureBuffers(RenderableScene glTF) {
        GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer = getRenderer();
        TextureImages textures = renderer.getAssets().getTextureImages(glTF);

        JSONImage[] images = glTF.getImages();
        for (int imageNumber = 0; imageNumber < images.length; imageNumber++) {
            renderer.getQueue().queueBegin();
            TextureImageInfo info = textures.getTextureImage(images[imageNumber].getUri());
            MemoryBuffer[] mipStaging = new MemoryBuffer[info.texture.getImage().getMipLevels()];
            org.varg.vulkan.image.Image img = info.texture.getImage();
            for (int loop = 0; loop < mipStaging.length; loop++) {
                ImageSubresourceLayers subLayers = new ImageSubresourceLayers(
                        ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT, loop, info.layer, 1);
                mipStaging[loop] = renderer.getBufferFactory().copyFromDeviceMemory(img,
                        ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT, subLayers, renderer.getQueue());
            }
            Vulkan10.Format sourceFormat = info.texture.format;
            renderer.getQueue().queueWaitIdle();
            VulkanImageBuffer vib = renderer.getAssets().getSource(glTF, images[imageNumber].getSourceId());
            // Display the source image
            AwtImageUtils.displayBuffer(vib.sourceImageBuffer.getAsByteArray(), null, img.getExtent().width, img
                    .getExtent().height, ImageFormat.VK_FORMAT_B8G8R8_UNORM, "Source image");

            int width = img.getExtent().width;
            int height = img.getExtent().height;
            Vulkan10.Format format = Vulkan10.Format.get(vib.sourceImageBuffer.format.value);
            Diff diff = comparePixels(vib.sourceImageBuffer.getAsByteArray(), format, mipStaging[0], sourceFormat);
            Logger.d(getClass(), "DIFF max:" + diff.max + ", min: " + diff.min + ", average " + diff.total
                    / diff.count);
            for (int loop = 0; loop < mipStaging.length; loop++) {
                ByteBuffer bb = Buffers.createByteBuffer((int) mipStaging[loop].size);
                renderer.getBufferFactory().copyToBuffer(mipStaging[loop], bb);
                bb.position(0);
                byte[] p = AwtImageUtils.copyRGBAToBGR(bb);
                AwtImageUtils.displayBuffer(p, null, width, height, ImageFormat.VK_FORMAT_B8G8R8_UNORM,
                        "Device memory dump, miplevel " + loop + " size " + width + ", " + height);
                renderer.getBufferFactory().freeBuffer(mipStaging[loop]);
                width = width / 2;
                height = height / 2;
            }
        }

    }

    protected Diff comparePixels(byte[] source, Vulkan10.Format sourceFormat, MemoryBuffer staging,
            Vulkan10.Format deviceFormat) {
        ByteBuffer bb = Buffers.createByteBuffer((int) staging.size);
        getRenderer().getBufferFactory().copyToBuffer(staging, bb);
        bb.position(0);

        switch (sourceFormat) {
            case VK_FORMAT_B8G8R8_UNORM:
                switch (deviceFormat) {
                    case VK_FORMAT_R8G8B8A8_UNORM:
                        return compareBGRToRGBA(source, bb);
                    default:
                        throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + deviceFormat);
                }
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + sourceFormat);
        }
    }

    private Diff compareBGRToRGBA(byte[] source, ByteBuffer device) {
        if (source.length / 3f != device.remaining() / 4f) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Arrays not matching "
                    + source.length + ", " + device.remaining());
        }
        Diff diff = new Diff();
        byte[] deviceArray = new byte[device.remaining()];
        int sourceIndex = 0;
        int deviceIndex = 0;
        while (sourceIndex < source.length) {
            diff.add(Math.abs((source[sourceIndex + 2] & 0x0ff) - (deviceArray[deviceIndex++] & 0x0ff)));
            diff.add(Math.abs((source[sourceIndex + 1] & 0x0ff) - (deviceArray[deviceIndex++] & 0x0ff)));
            diff.add(Math.abs((source[sourceIndex] & 0x0ff) - (deviceArray[deviceIndex++] & 0x0ff)));
            deviceIndex++;
            sourceIndex += 3;
        }
        return diff;
    }

    protected void compareBuffer(ByteBuffer source, MemoryBuffer staging) {
        ByteBuffer bb = Buffers.createByteBuffer((int) staging.size);
        getRenderer().getBufferFactory().copyToBuffer(staging, bb);
        bb.position(0);
        compareBuffer(source, bb);
    }

    protected void compareBuffer(ByteBuffer byteBuffer, JSONBufferView bufferView) {
        compareBuffer(byteBuffer, bufferView.getReadByteBuffer(0));
    }

    protected void compareBuffer(ByteBuffer byteBuffer, ImageBuffer image) {
        compareBuffer(byteBuffer, image.getBuffer());
    }

    protected void compareBuffer(ByteBuffer byteBuffer, ByteBuffer source) {
        if (byteBuffer.remaining() != source.remaining()) {
            throw new IllegalArgumentException("Not same size " + byteBuffer.remaining() + ", " + source.remaining());
        }
        for (int i = 0; i < byteBuffer.remaining(); i++) {
            if (byteBuffer.get() != source.get()) {
                int position = byteBuffer.position();
                Logger.d(getClass(), Buffers.toString(byteBuffer, 0, 100, 0));
                Logger.d(getClass(), Buffers.toString(source, 0, 100, 0));
                throw new IllegalArgumentException("Not matching at position " + (position - 1));
            }
        }
    }

}

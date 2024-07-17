package org.varg.vulkan.pipeline;

import java.nio.ByteBuffer;

import org.varg.vulkan.extensions.KHRAccelerationStructure.DeviceOrHostAddress;
import org.varg.vulkan.extensions.KHRRayTracingPipeline.RayTracingPipelineCreateInfoKHR;
import org.varg.vulkan.extensions.KHRRayTracingPipeline.StridedDeviceAddressRegionKHR;

/**
 * Needs extension VK_KHR_ray_tracing_pipeline
 */
public class RayTracingPipeline extends Pipeline {

    private DeviceOrHostAddress sbtDeviceMemory;
    private StridedDeviceAddressRegionKHR[] sbtAdresses;
    private ByteBuffer sbt;
    private int byteOffset;
    private int limit;
    private int[] alignedSizes;
    private int groupCount;

    public RayTracingPipeline(long pipeline, RayTracingPipelineCreateInfoKHR createInfo) {
        super(pipeline, createInfo.getPipelineLayout());
    }

    /**
     * Sets the sbt buffer, the position and limit will be recorded and used when buffer is fetched.
     * 
     * @param sbt
     */
    public void setSBT(ByteBuffer sbt, DeviceOrHostAddress sbtDeviceMemory, int[] alignedSizes, int groupCount) {
        this.sbt = sbt;
        this.sbtDeviceMemory = sbtDeviceMemory;
        this.alignedSizes = alignedSizes;
        this.groupCount = groupCount;
        byteOffset = sbt.position();
        limit = sbt.limit();

        sbtAdresses = new StridedDeviceAddressRegionKHR[4];
        long adress = sbtDeviceMemory.deviceAddress;
        for (int i = 0; i < sbtAdresses.length; i++) {
            sbtAdresses[i] = new StridedDeviceAddressRegionKHR(adress, alignedSizes[i] * groupCount, alignedSizes[i] * groupCount);
            adress += alignedSizes[i] * groupCount;
        }
    }

    /**
     * Returns an array holding the sizes for sbt entries:
     * raygen
     * miss
     * hit
     * callable
     * 
     * @return
     */
    public int[] getSBTSizes(int handleSize, int groupCount) {
        return new int[] { handleSize * groupCount, handleSize * groupCount, handleSize * groupCount, handleSize * groupCount };
    }

    /**
     * Returns the sbt buffer, with position and limit set.
     * 
     * @return
     */
    public ByteBuffer getSBTByteBuffer() {
        return sbt.limit(limit).position(byteOffset);
    }

    /**
     * returns the device memory for sbt
     * 
     * @return
     */
    public DeviceOrHostAddress getDeviceOrHostMemory() {
        return sbtDeviceMemory;
    }

    /**
     * Returns the aligned sizes for the sbt handles:
     * raygen
     * miss
     * hit
     * callable
     * 
     * @return
     */
    public int[] getAlignedSBTSizes() {
        return alignedSizes;
    }

    /**
     * Returns the adresses for the sbt
     * 
     * @return
     */
    public StridedDeviceAddressRegionKHR[] getDeviceAdresses() {
        return sbtAdresses;
    }

}

package org.varg.vulkan;

import java.nio.ByteBuffer;

import org.varg.renderer.Renderers;
import org.varg.vulkan.extensions.KHRAccelerationStructure;
import org.varg.vulkan.extensions.KHRRayTracingPipeline;
import org.varg.vulkan.structs.SubmitInfo;
import org.varg.vulkan.structs.TimelineSemaphore;
import org.varg.vulkan.structs.TimelineSemaphore.TimelineSemaphoreSubmitInfo;

public abstract class Vulkan12Backend<T> extends Vulkan10Backend<T> {

    protected Vulkan12Backend(Renderers version) {
        super(version);
    }

    /**
     * Creates a timeline semaphore
     * 
     * @return
     */
    public abstract TimelineSemaphore createTimelineSemaphore();

    public abstract TimelineSemaphoreSubmitInfo createTimelineSemaphoreSubmitInfo(long waitValue, long submitValue);

    public abstract SubmitInfo createSubmitInfo(TimelineSemaphoreSubmitInfo timelineInfo, TimelineSemaphore wait,
            TimelineSemaphore signal, ByteBuffer commandBuffers);

    public abstract void waitSemaphores(TimelineSemaphore waitSemaphore, long waitValue);

    /**
     * Returns the VK_KHR_acceleration_structure extension if enabled
     * 
     * @return
     */
    public abstract KHRAccelerationStructure<?> getKHRAccelerationStructure();

    /**
     * Returns the VK_KHR_ray_tracing_pipeline
     * 
     * @return
     */
    public abstract KHRRayTracingPipeline<?> getKHRRayTracingPipeline();

}

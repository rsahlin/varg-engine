package org.varg.lwjgl3.vulkan;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.Buffer;

import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Logger;
import org.lwjgl.vulkan.VkPhysicalDeviceLimits;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;
import org.varg.vulkan.structs.DeviceLimits;

public class LWJGLVulkanLimits extends DeviceLimits {

    /**
     * Copies the limits
     * 
     * TODO Cleanup and move set operations to PlatformStruct
     * 
     * @param limits
     */
    public void copy(VkPhysicalDeviceLimits limits) {
        for (Field scanField : getClass().getSuperclass().getDeclaredFields()) {
            int count = limits.maxPerStageDescriptorSamplers();
            String fieldName = scanField.getName();
            try {
                Field field = getClass().getSuperclass().getDeclaredField(fieldName);
                Method source = limits.getClass().getDeclaredMethod(fieldName, null);
                Object read = source.invoke(limits, null);
                if (read instanceof Buffer) {
                    field.set(this, getFromBuffer((Buffer) read));
                } else if (field.getType() == SampleCountFlagBit.class) {
                    field.set(this, SampleCountFlagBit.get((Integer) read));
                } else if (field.getType() == SampleCountFlagBit[].class) {
                    field.set(this, BitFlags.getBitFlags((int) read, SampleCountFlagBit.values())
                            .toArray(new SampleCountFlagBit[0]));
                } else if (field.getType() == int.class || (field.getType() == Integer.class)) {
                    // Make sure value is not -1
                    int val = (Integer) read;
                    if (val == -1) {
                        val = Integer.MAX_VALUE;
                    }
                    field.set(this, val);
                } else {
                    field.set(this, read);
                }
            } catch (NoSuchFieldException | NoSuchMethodException | SecurityException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                Logger.e(getClass(), e.toString());
            }
        }
    }
}

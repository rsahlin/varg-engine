
package org.varg.vulkan.cmd;

import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Settings;
import org.varg.vulkan.VulkanBackend.IntArrayProperties;

public class ClearColorValue {

    /**
     * Do not modify values - only read
     */
    public final float[] float32 = new float[4];
    /**
     * Do not modify values - only read
     */
    public final int[] int32 = new int[4];
    /**
     * Do not modify values - only read
     */
    public final int[] uint32 = new int[4];

    public ClearColorValue(ClearColorValue source) {
        System.arraycopy(source.float32, 0, float32, 0, 4);
        System.arraycopy(source.int32, 0, int32, 0, 4);
        System.arraycopy(source.uint32, 0, uint32, 0, 4);
    }

    public ClearColorValue(int... rgba) {
        setValues(rgba);
    }

    public ClearColorValue(float r, float g, float b, float a) {
        if (r < 0 || g < 0 || b < 0 || a < 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", clear value is negative");
        }
        if (r > 1 || g > 1 || g > 1 || a > 1) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", clear value is > 1");
        }
        setFloat32(r, g, b, a);
        setInt32(r, g, b, a);
        setUint32(r, g, b, a);
    }

    private void setValues(int... rgba) {
        if (rgba == null) {
            rgba = Settings.getInstance().getIntArray(IntArrayProperties.CLEAR_COLOR);
        }
        if (rgba != null) {
            setFloat32(rgba);
            setInt32(rgba);
            setUint32(rgba);
        }
    }

    private void setFloat32(float... rgba) {
        float32[0] = rgba[0];
        float32[1] = rgba[1];
        float32[2] = rgba[2];
        float32[3] = rgba[3];
    }

    private void setFloat32(int... rgba) {
        float32[0] = (float) (((rgba[0]) & 0xff)) / 255;
        float32[1] = (float) (((rgba[1]) & 0xff)) / 255;
        float32[2] = (float) (((rgba[2]) & 0xff)) / 255;
        float32[3] = (float) (((rgba[3]) & 0xff)) / 255;
    }

    private void setInt32(int... rgba) {
        int32[0] = rgba[0];
        int32[1] = rgba[1];
        int32[2] = rgba[2];
        int32[3] = rgba[3];
    }

    private void setInt32(float... rgba) {
        int32[0] = (int) (rgba[0] * 255);
        int32[1] = (int) (rgba[1] * 255);
        int32[2] = (int) (rgba[2] * 255);
        int32[3] = (int) (rgba[3] * 255);
    }

    private void setUint32(int... rgba) {
        uint32[0] = (rgba[0] & 0xff);
        uint32[1] = (rgba[1] & 0xff);
        uint32[2] = (rgba[2] & 0xff);
        uint32[3] = (rgba[3] & 0xff);
    }

    private void setUint32(float... rgba) {
        uint32[0] = ((int) (rgba[0] * 255) & 0xff);
        uint32[1] = ((int) (rgba[1] * 255) & 0xff);
        uint32[2] = ((int) (rgba[2] * 255) & 0xff);
        uint32[3] = ((int) (rgba[3] * 255) & 0xff);
    }

}

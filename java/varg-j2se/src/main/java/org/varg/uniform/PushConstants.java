
package org.varg.uniform;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.gltfio.gltf2.JSONPrimitive;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.structs.PushConstantRange;

/**
 * Pushconstants for PBRMetallicRoughness material
 *
 */
public class PushConstants {

    public static final int MATERIAL_INDEX = 0;

    public static final int PUSHFLOAT_DATASIZE = 0;
    public static final int PUSH_INT_DATASIZE = 2;
    public static final int PUSH_DATASIZE = PUSHFLOAT_DATASIZE + PUSH_INT_DATASIZE;

    final PushConstantRange[] pushConstants;
    final ByteBuffer byteBuffer;
    final FloatBuffer floatBuffer;
    final IntBuffer intBuffer;

    public PushConstants(PushConstantRange[] pushConstants) {
        if (pushConstants == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        int size = 0;
        for (PushConstantRange push : pushConstants) {
            size += Math.max(push.size + push.offset, size);
        }
        this.pushConstants = pushConstants;
        byteBuffer = Buffers.createByteBuffer(size);
        byteBuffer.position(0);
        floatBuffer = byteBuffer.asFloatBuffer();
        intBuffer = byteBuffer.asIntBuffer();
    }

    /**
     * Returns the pushconstants
     * 
     * @return
     */
    public PushConstantRange[] getPushConstantRange() {
        return pushConstants;
    }

    /**
     * Returns the pushconstants bytebuffer positioned at 0
     * 
     * @return
     */
    public ByteBuffer getByteBuffer() {
        byteBuffer.position(0);
        return byteBuffer;
    }

    /**
     * Stores the primitive push constants
     * 
     * @param primitive
     */
    public void setPushConstants(JSONPrimitive primitive, int matrixIndex) {
        intBuffer.position(MATERIAL_INDEX);
        intBuffer.put(primitive.getMaterialIndex());
        intBuffer.put(matrixIndex);
    }

}

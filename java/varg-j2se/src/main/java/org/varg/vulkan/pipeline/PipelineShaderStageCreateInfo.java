
package org.varg.vulkan.pipeline;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.PipelineCreateFlagBit;
import org.varg.vulkan.Vulkan10.ShaderStageFlagBit;
import org.varg.vulkan.structs.ShaderModule;

/**
 * Wrapper for VkPipelineShaderStageCreateInfo
 */
public class PipelineShaderStageCreateInfo {

    public interface SpecializationConstant {
        int getId();

        int getByteSize();

        int getOffset();

        static int getTotalByteSize(SpecializationConstant... values) {
            int size = 0;
            for (SpecializationConstant s : values) {
                size += s.getByteSize();
            }
            return size;
        }

    }

    public enum ComputeSpecializationConstant implements SpecializationConstant {
        NONE(0, 4, 0);

        public final int constantId;
        public final int byteSize;
        public final int offset;

        ComputeSpecializationConstant(int constantId, int byteSize, int offset) {
            this.constantId = constantId;
            this.byteSize = byteSize;
            this.offset = offset;
        }

        @Override
        public int getId() {
            return constantId;
        }

        @Override
        public int getByteSize() {
            return byteSize;
        }

        @Override
        public int getOffset() {
            return offset;
        }

    }

    public enum GLTF2SpecializationConstant implements SpecializationConstant {
        TEXTURE_TRANSFORM_COUNT(0, 4, 0),
        MATERIAL_COUNT(1, 4, 4),
        MATERIAL_SAMPLER_COUNT(2, 4, 8),
        DIRECTIONAL_LIGHT_COUNT(3, 4, 12),
        POINT_LIGHT_COUNT(4, 4, 16),
        MATRIX_COUNT(5, 4, 20);

        public final int constantId;
        public final int byteSize;
        public final int offset;

        GLTF2SpecializationConstant(int constantId, int byteSize, int offset) {
            this.constantId = constantId;
            this.byteSize = byteSize;
            this.offset = offset;
        }

        @Override
        public int getId() {
            return constantId;
        }

        @Override
        public int getByteSize() {
            return byteSize;
        }

        @Override
        public int getOffset() {
            return offset;
        }
    }

    public static class SpecializationMapEntry {
        final int constantID;
        final int offset;
        final int size;

        public SpecializationMapEntry(SpecializationConstant constant, ByteBuffer buffer, int value) {
            this.constantID = constant.getId();
            this.offset = constant.getOffset();
            this.size = constant.getByteSize();
            buffer.position(constant.getOffset());
            buffer.asIntBuffer().put(value);
        }

        /**
         * Returns the constant id
         * 
         * @return
         */
        public int getConstantID() {
            return constantID;
        }

        /**
         * Returns the offset
         * 
         * @return
         */
        public int getOffset() {
            return offset;
        }

        /**
         * Returns the size
         * 
         * @return
         */
        public int getSize() {
            return size;
        }

    }

    public static class SpecializationInfo {
        final SpecializationMapEntry[] mapEntries;
        final ByteBuffer data;

        public SpecializationInfo(@NonNull SpecializationMapEntry[] mapEntries, @NonNull ByteBuffer data) {
            this.data = data;
            IntBuffer intBuffer = getBuffer().asIntBuffer();
            if (mapEntries == null || data == null) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
            }
            HashSet<Integer> constantIDS = new HashSet<>();
            for (SpecializationMapEntry entry : mapEntries) {
                if (constantIDS.contains(entry.constantID)) {
                    throw new IllegalArgumentException(
                            ErrorMessage.INVALID_VALUE.message + "Already contains constantID " + entry.constantID);
                }
                if (intBuffer.get(entry.offset >> 2) <= 0) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                            + "Invalid specialization constants value: " + intBuffer.get(entry.constantID));
                }
                constantIDS.add(entry.constantID);
            }
            this.mapEntries = mapEntries;
        }

        /**
         * Returns the specialization mapentry array
         * 
         * @return
         */
        public SpecializationMapEntry[] getEntries() {
            return mapEntries;
        }

        /**
         * Returns the buffer at position 0
         * 
         * @return
         */
        public ByteBuffer getBuffer() {
            data.position(0);
            return data;
        }

        /**
         * Returns the map entry for the constant if present, or null
         * 
         * @param constant
         * @return Map entry for the constant or null
         */
        public SpecializationMapEntry getEntry(SpecializationConstant constant) {
            for (SpecializationMapEntry e : mapEntries) {
                if (e.constantID == constant.getId()) {
                    return e;
                }
            }
            return null;
        }

        /**
         * Returns the map entry as int
         * 
         * @param entry
         * @return Int value for entry, or -1 if entry is null
         */
        public int getIntEntry(SpecializationMapEntry entry) {
            if (entry != null) {
                data.position(entry.offset);
                return data.asIntBuffer().get();
            }
            return -1;
        }

    }

    private final PipelineCreateFlagBit[] flags;
    private final ShaderStageFlagBit stage;
    private final ShaderModule module;
    private final ByteBuffer pName;
    private final SpecializationInfo specializationInfo;

    public PipelineShaderStageCreateInfo(PipelineCreateFlagBit[] flags,
            ShaderStageFlagBit stage, ShaderModule module, String name, SpecializationInfo specializationInfo) {
        if (stage == null || module == null || name == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Parameter is null");
        }
        this.flags = flags;
        this.stage = stage;
        this.module = module;
        pName = Buffers.createByteBuffer(name);
        this.specializationInfo = specializationInfo;
    }

    /**
     * Returns the (null terminated) name of the shader entrypoint
     * 
     * @return
     */
    public ByteBuffer getName() {
        return pName.position(0).asReadOnlyBuffer();
    }

    /**
     * Returns the stage for this shader
     * 
     * @return
     */
    public ShaderStageFlagBit getStage() {
        return stage;
    }

    /**
     * Returns the shader module
     * 
     * @return
     */
    public ShaderModule getModule() {
        return module;
    }

    /**
     * Returns the specialization info
     * 
     * @return
     */
    public SpecializationInfo getSpecializationInfo() {
        return specializationInfo;
    }

}

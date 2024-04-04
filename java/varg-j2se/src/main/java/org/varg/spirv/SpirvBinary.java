
package org.varg.spirv;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;

/**
 * Container for SPIR-V binary
 */
public class SpirvBinary {

    public static final String SPIRV_END_MARKER = "END!";
    public static final byte[] SPRIRV_END_ARRAY = new byte[] { 69, 78, 68, 33 };
    public static final int SPIRV_END_VALUE = (0x45) | (0x4E << 8) | (0x44 << 16) | (0x21 << 24);

    private static class SpirvInstruction {

        public static final int OPFUNCTIONEND = 56;
        public static final int OPFUNCTIONSTART = 54;

        int stream;
        int wordCount;
        int opCode;

        private SpirvInstruction(int stream) {
            if (stream == 0) {
                throw new IllegalArgumentException("Stream is 0");
            }
            this.stream = stream;
            wordCount = (stream >>> 16) & 0x0ffff;
            opCode = stream & 0x0ffff;
        }

        boolean isFunctionEnd() {
            return opCode == OPFUNCTIONEND;
        }

        boolean isFunctionStart() {
            return opCode == OPFUNCTIONSTART;
        }

        public boolean isEndMarker() {
            String str = getString();
            return stream == SPIRV_END_VALUE;
        }

        public String getString() {
            return new String(new byte[] { (byte) (stream & 0x0ff),
                    (byte) (stream >>> 8 & 0x0ff), (byte) (stream >>> 16 & 0x0ff),
                    (byte) (stream >>> 24 & 0x0ff) });

        }

    }

    public static class SpirvStream {

        public int getOffset() {
            return offset;
        }

        public int getTotalWordCount() {
            return totalWordCount;
        }

        public int getOpStartCount() {
            return opStartCount;
        }

        public int getOpEndCount() {
            return opEndCount;
        }

        int offset;
        int totalWordCount;
        int opStartCount = 0;
        int opEndCount = 0;
        IntBuffer spirvBuffer;

        private SpirvStream(IntBuffer spirvBuffer, int offset) {
            this.spirvBuffer = spirvBuffer;
            this.offset = offset;
        }

        public boolean isValid() {
            // TODO: How to know it's really valid?
            if (totalWordCount + INSTRUCTION_STREAM_INDEX != offset) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE + "Wordcount and offset");
            }
            return (totalWordCount > 0 && opStartCount == opEndCount);
        }

        /**
         * Advances the stream for the instruction
         * 
         * @param instruction
         */
        void advanceStream(SpirvInstruction instruction) {
            totalWordCount += instruction.wordCount;
            offset += instruction.wordCount;
        }

        /**
         * Set the int buffers limit to the current offset
         */
        void setLimit() {
            spirvBuffer.limit(offset);
        }

    }

    public static final int SPIR_V_MAGIC = 0x07230203;

    public static final int MAGIC_INDEX = 0;
    public static final int VERSION_INDEX = 1;
    public static final int GENERATOR_MAGIC_INDEX = 2;
    public static final int BOUND_INDEX = 3;
    public static final int RESERVED_INDEX = 4;
    public static final int INSTRUCTION_STREAM_INDEX = 5;

    public final ByteBuffer spirv;
    public final int totalWords;

    public SpirvBinary(ByteBuffer spirv, int totalSpirvWords) {
        this.spirv = createSpirv(spirv, totalSpirvWords);
        if (this.spirv == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE + "Spirv");
        }
        totalWords = this.spirv.limit() / 4;
    }

    /**
     * Returns the bytebuffer containing the spir-v binary code.
     * 
     * @return
     */
    public ByteBuffer getSpirv() {
        return spirv;
    }

    /**
     * Creates Spirv ByteBuffer from buffer source, the spirv data will be copied into a new buffer.
     * 
     * @param Buffer containing spirv, spirv must start at current buffer position
     * @param total number of 32 bit words in spirv data - this is the size of the complete spirv binary
     * @return Created buffer containing spirv data
     */
    public static ByteBuffer createSpirv(ByteBuffer spirv, int totalSpirvWords) {
        int offset = spirv.position();
        if (getSPIRVMagicOffset(spirv) == offset) {
            // Java has BIG_ENDIAN order but the spirv is coming from a byte stream so we need to use LITTLE_ENDIAN
            ByteBuffer bytes = Buffers.createByteBuffer(totalSpirvWords * Integer.BYTES);
            bytes.put(spirv);
            bytes.rewind();
            return bytes;
        }
        return null;
    }

    /**
     * Check that byte array starts with spirv magic and that all instruction streams are included.
     * Position is set after last found instruction.
     * 
     * @param Buffer containing spirv, spirv must start at current buffer position
     * @param spirv Stream
     */
    public static SpirvStream getStream(ByteBuffer spirv) {
        int offset = spirv.position();
        if (getSPIRVMagicOffset(spirv) == offset) {
            IntBuffer spirvInt = spirv.asIntBuffer();
            if (spirvInt.get(MAGIC_INDEX) == SPIR_V_MAGIC) {
                int version = spirvInt.get(VERSION_INDEX);
                int bound = spirvInt.get(BOUND_INDEX);
                return getTotalWordCount(spirvInt, INSTRUCTION_STREAM_INDEX);
            }
        }
        return null;
    }

    /**
     * Return the wordcount at the specified offset
     * 
     * @param spirv
     * @param stream
     * @return Wordcount att offset or -1 if offset is outside array
     */
    private static SpirvInstruction getInstruction(IntBuffer spirv, SpirvStream stream) {
        if (stream.offset >= spirv.capacity()) {
            return null;
        }
        int read = spirv.get(stream.offset);
        if (read == 0) {
            throw new IllegalArgumentException("Read is 0 at offset " + stream.offset);
        }
        return new SpirvInstruction(read);
    }

    /**
     * Returns the wordcount beginning at offset, this will add upp all wordcounts to the end.
     * 
     * @param spirv
     * @param offset
     * @return The found stream
     */
    private static SpirvStream getTotalWordCount(IntBuffer spirv, int offset) {
        SpirvStream stream = new SpirvStream(spirv, offset);
        SpirvInstruction instruction = null;
        while ((instruction = getInstruction(spirv, stream)) != null) {
            if (instruction.isEndMarker()) {
                if (instruction.isEndMarker()) {
                    return stream;
                }
            } else {
                stream.advanceStream(instruction);
            }
            if (instruction.opCode == SpirvInstruction.OPFUNCTIONSTART) {
                stream.opStartCount++;
            }
            if (instruction.opCode == SpirvInstruction.OPFUNCTIONEND) {
                stream.opEndCount++;
            }
        }
        if (stream.opStartCount != stream.opEndCount) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + ", OpFunctionStart does not match OpFunctionEnd "
                            + stream.opStartCount + ", " + stream.opEndCount);
        }
        return stream;
    }

    /**
     * Returns offset to SPIRV magic offset or -1 if not found
     * 
     * @param spirv
     * @param offset Offset into SPIRV where to look for magic
     * @param length Max length of spirv to check, including offset. length - offset bytes will be checked
     * @return Total offset, from beginning of spirv, of magic. -1 if not found
     */
    public static int getSPIRVMagicOffset(byte[] spirv, int offset, int length) {
        while (offset < (spirv.length - 4) && (length >= 4)) {
            if (spirv[offset] == (SPIR_V_MAGIC & 0xff) &&
                    spirv[offset + 1] == ((SPIR_V_MAGIC >>> 8) & 0xff) &&
                    spirv[offset + 2] == ((SPIR_V_MAGIC >>> 16) & 0xff) &&
                    spirv[offset + 3] == ((SPIR_V_MAGIC >>> 24) & 0xff)) {
                return offset;
            }
            offset++;
            length--;
        }
        return -1;
    }

    /**
     * Returns offset to SPIRV magic offset or -1 if not found.
     * If magic is found the position is updated to start of SPIR_V_MAGIC, if magic not found position remains
     * unchanged
     * 
     * @param buffer
     * @return Total offset, from beginning of buffer, of magic. -1 if not found
     */
    public static int getSPIRVMagicOffset(ByteBuffer buffer) {
        int offset = buffer.position();
        int remaining = buffer.remaining();
        while (remaining >= Integer.BYTES) {
            if (buffer.get(offset) == (SPIR_V_MAGIC & 0xff) &&
                    buffer.get(offset + 1) == ((SPIR_V_MAGIC >>> 8) & 0xff) &&
                    buffer.get(offset + 2) == ((SPIR_V_MAGIC >>> 16) & 0xff) &&
                    buffer.get(offset + 3) == ((SPIR_V_MAGIC >>> 24) & 0xff)) {
                buffer.position(offset);
                return offset;
            }
            offset++;
            remaining--;
        }
        return -1;
    }
}

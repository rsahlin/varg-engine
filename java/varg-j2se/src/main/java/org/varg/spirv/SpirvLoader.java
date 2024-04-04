
package org.varg.spirv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.gltfio.lib.FileUtils;
import org.gltfio.lib.StreamUtils;
import org.varg.spirv.SpirvBinary.SpirvStream;
import org.gltfio.lib.Logger;

/**
 * Used to load spirv binary from a stream.
 * 
 */
public class SpirvLoader {

    /**
     * Searches for the spirv magic number, then loads data until end of spirv word stream
     * 
     * @param inputstream
     * @param Data loaded into this buffer at current position
     * @param Max number of millis to wait for data to become available when reading
     */
    public SpirvStream loadSpirv(InputStream stream, ByteBuffer buffer, int readTimeoutMillis) throws IOException {
        waitForMagic(stream, buffer, readTimeoutMillis);
        boolean valid = false;
        while (!valid) {
            int len = FileUtils.getInstance().waitForAvailable(stream, readTimeoutMillis);
            if (len <= 0) {
                throw new IllegalArgumentException("No data to read");
            }
            buffer.rewind();
            valid = SpirvBinary.getStream(buffer).isValid();

        }
        SpirvStream spirStream = SpirvBinary.getStream(buffer);
        Logger.d(getClass(), "Loaded spirv with " + spirStream.totalWordCount + " words, OpStart: "
                + spirStream.opStartCount + ", OpEnd: " + spirStream.opEndCount);
        // Make sure stream int buffer limit is correct.
        spirStream.setLimit();
        return spirStream;
    }

    /**
     * '
     * Calls waitForAvailable on the stream and checks for spirv magic
     * 
     * @param stream
     * @param buffer
     * @param readTimeoutMillis
     * @throws IOException
     */
    public void waitForMagic(InputStream stream, ByteBuffer buffer, int readTimeoutMillis) throws IOException {
        // Find spir-v magic number
        int offset = 0;
        while ((offset = SpirvBinary.getSPIRVMagicOffset(buffer)) < 0) {
            int len = FileUtils.getInstance().waitForAvailable(stream, readTimeoutMillis);
            if (len <= 0) {
                throw new IllegalArgumentException("No data to read - end of stream or timeout");
            }
            len = StreamUtils.readFromStream(stream, buffer, len);
            buffer.flip();
        }
        Logger.d(getClass(), "Found spirv magic at offset " + offset);
    }

}

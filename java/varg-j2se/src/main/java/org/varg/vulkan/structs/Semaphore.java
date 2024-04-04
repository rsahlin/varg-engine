
package org.varg.vulkan.structs;

import java.nio.LongBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;

public class Semaphore {

    private final LongBuffer semaphore = Buffers.createLongBuffer(1);

    public Semaphore(long semaphore) {
        if (semaphore == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Semaphore is 0");
        }
        this.semaphore.put(semaphore);
    }

    /**
     * Returns the native adress of the semaphore
     * 
     * @return
     */
    public long getSemaphore() {
        return semaphore.get(0);
    }

    /**
     * Returns the buffer holding the semaphore, position will be at beginning
     * 
     * @return
     */
    public LongBuffer getSemaphoreBuffer() {
        semaphore.position(0);
        return semaphore;
    }

    /**
     * Destroys the resources allocated
     * 
     * @return The native adress of this semaphore
     */
    public long destroy() {
        long adress = this.semaphore.get(0);
        this.semaphore.put(0, 0);
        return adress;
    }

}

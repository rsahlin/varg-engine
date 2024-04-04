package org.varg.vulkan.structs;

import java.nio.LongBuffer;

import org.gltfio.lib.Buffers;

public class TimelineSemaphore extends Semaphore {

    public static class TimelineSemaphoreSubmitInfo {

        private final LongBuffer pointer = Buffers.createLongBuffer(1);

        public final LongBuffer waitValue = Buffers.createLongBuffer(1);
        public final LongBuffer signalValue = Buffers.createLongBuffer(1);

        public TimelineSemaphoreSubmitInfo(long nativeptr, long waitValue, long signalValue) {
            pointer.put(nativeptr);
            this.waitValue.put(waitValue);
            this.signalValue.put(signalValue);
        }

        /**
         * Returns the pointer
         * 
         * @return
         */
        public long getPointer() {
            return pointer.get(0);
        }

        /**
         * Returns the buffer containing the pointer
         * 
         * @return
         */
        public LongBuffer getPointerBuffer() {
            return pointer.position(0);
        }

        /**
         * 
         * @return
         */
        public LongBuffer getWaitValue() {
            return waitValue.position(0);
        }

        /**
         * 
         * @return
         */
        public LongBuffer getSignalValue() {
            return signalValue.position(0);
        }

    }

    public TimelineSemaphore(long pointer) {
        super(pointer);
    }

}

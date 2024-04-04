
package org.varg.vulkan;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Queue.State;

public class CommandBuffer<T> {

    /**
     * DO NOT ACCESS!!!!
     * May only be used from the Queue
     */
    public final T commandBuffer;
    private State state;

    protected CommandBuffer(T commandBuffer) {
        if (commandBuffer == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "CommandBuffer is null");
        }
        this.commandBuffer = commandBuffer;
        this.state = State.INITIAL;
    }

    /**
     * 
     * @param newState
     */
    protected void updateState(State newState) {
        this.state = newState;
    }

    /**
     * 
     * @return
     */
    public State getState() {
        return state;
    }

}


package org.varg.vulkan;

import java.nio.LongBuffer;
import java.util.ArrayDeque;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Queue.State;
import org.varg.vulkan.structs.QueueFamilyProperties;

/**
 * Handles access to CommandBuffers, this is to encapsulate usage of CommandBuffers and tracking state,
 * it also handles a pool of CommandBuffers
 *
 * Use {@link #transitionToState(Category, State)} for all CommandBuffers
 *
 * @param <T> The command buffer platform object
 */
public abstract class CommandBuffers<T> {

    public interface Category {
        int getId();

        String getName();
    }

    private final LongBuffer pool = Buffers.createLongBuffer(1);
    private final QueueFamilyProperties family;
    private final ArrayDeque<CommandBuffer<T>> queue = new ArrayDeque<CommandBuffer<T>>();
    private CommandBuffer<T> commands;

    protected CommandBuffers(long pool, QueueFamilyProperties family, CommandBuffer<T>[] commands) {
        if (pool == 0 || family == null || commands == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Pool or family is null");
        }
        this.family = family;
        this.pool.put(pool);
        for (CommandBuffer<T> cb : commands) {
            queue.add(cb);
        }
    }

    /**
     * 
     * @return
     */
    public long getPool() {
        return pool.get(0);
    }

    /**
     * 
     * @return
     */
    public QueueFamilyProperties getFamily() {
        return family;
    }

    /**
     * Internal method - fetches a CommandBUffer from the queue.
     * - do not use - use {@link #transitionToState(Category, State)}
     * 
     * @return Next CommandBuffer removed from the queue
     */
    CommandBuffer<T> fetchCommandBuffer() {
        return queue.remove();
    }

    /**
     * CommandBuffer must be in state IDLE
     * 
     * @param commandBuffer
     */
    protected void releaseCommandBuffer(CommandBuffer<T> commandBuffer) {
        if (commandBuffer.getState() != State.INITIAL) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + "Wrong state " + commandBuffer.getState());
        }
        queue.add(commandBuffer);
    }

    /**
     * Returns the current commandbuffer
     * 
     * @return
     * @throws IllegalArgumentException If state has not been started
     */
    public CommandBuffer<T> getCommandBuffer() {
        if (commands != null) {
            return commands;
        }
        throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "No commandbuffer, invalid state? ");
    }

    /**
     * 
     * @return
     */
    public boolean hasCommandBuffer() {
        return commands != null;
    }

    /**
     * Transitions from one state to another - use this to go through the states.
     * 
     * @param id
     * @param state
     * @return
     * @throws IllegalArgumentException If queue is in wrong state to transition to new state
     */
    public CommandBuffer<T> transitionToState(State state) {
        if (commands != null && commands.getState() == state) {
            return commands;
        }
        switch (state) {
            case RECORDING:
                if (commands != null && (commands.getState() == State.PENDING
                        || commands.getState() == State.INVALID)) {
                    throw new IllegalArgumentException(
                            ErrorMessage.INVALID_STATE.message + "Invalid state, current state is "
                                    + commands.getState());
                }
                if (commands == null) {
                    commands = fetchCommandBuffer();
                }
                commands.updateState(state);
                break;
            case EXECUTABLE:
            case PENDING:
                if (commands == null || commands.getState().value != (state.value - 1)) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + commands != null
                            ? ("Wrong state: " + commands.getState()) : "Null");
                }
                commands.updateState(state);
                break;
            case INITIAL:
                if (commands == null || commands.getState() != State.PENDING) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Null or wrong state");
                }
                CommandBuffer<T> commandBuffer = commands;
                commands = null;
                commandBuffer.updateState(state);
                releaseCommandBuffer(commandBuffer);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + state);
        }
        return commands;
    }

}

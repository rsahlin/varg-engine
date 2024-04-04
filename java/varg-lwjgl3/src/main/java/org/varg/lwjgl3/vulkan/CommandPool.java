
package org.varg.lwjgl3.vulkan;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;

import org.gltfio.lib.ErrorMessage;

class CommandPool<T> {

    ArrayDeque<T> pool = new ArrayDeque<T>();
    ArrayList<T> leasedCommands = new ArrayList<T>();
    Method malloc;

    CommandPool(Class<?> clazz) {
        try {
            malloc = getMethod(clazz);
            if (malloc == null) {
                throw new RuntimeException(
                        ErrorMessage.INVALID_STATE + "Class does not have malloc() : " + clazz.getCanonicalName());
            }
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected Method getMethod(Class<?> clazz) throws NoSuchMethodException, SecurityException {
        return clazz.getMethod("malloc");
    }

    protected T fetchCommand() {
        synchronized (pool) {
            T command = pool.poll();
            if (command == null) {
                try {
                    command = createInstance();
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                pool.add(command);
            }
            leasedCommands.add(command);
            return command;
        }
    }

    @SuppressWarnings("unchecked")
    protected T createInstance() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (T) malloc.invoke(null);
    }

    protected void releaseCommands() {
        synchronized (pool) {
            pool.addAll(leasedCommands);
            leasedCommands.clear();
        }
    }

}

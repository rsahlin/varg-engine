
package org.varg.lwjgl3.vulkan;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BufferCommandPool<T> extends CommandPool<T> {

    int bufferCount;

    public BufferCommandPool(Class<?> clazz, int bufferCount) {
        super(clazz);
        this.bufferCount = bufferCount;
    }

    @Override
    protected Method getMethod(Class<?> clazz) throws NoSuchMethodException, SecurityException {
        return clazz.getMethod("malloc", int.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T createInstance() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (T) malloc.invoke(null, bufferCount);
    }

}

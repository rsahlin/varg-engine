
package org.varg.vulkan.structs;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.gltfio.lib.Logger;
import org.varg.vulkan.Vulkan10;

public abstract class PlatformStruct {

    /**
     * Sets the given field names to the boolean value of flag.
     * 
     * @param flag
     * @param fieldNames
     */
    public void setBooleanFields(boolean flag, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                setBooleanFields(flag, getClass().getDeclaredField(fieldName));
            } catch (NoSuchFieldException | SecurityException e) {
                Logger.e(getClass(), e.toString());
            }
        }
    }

    /**
     * Sets the boolean fields to the value of flag, Field.setAccessible(true) is called before setting the value.
     * 
     * @param flag
     * @param fields
     */
    protected void setBooleanFields(boolean flag, Field... fields) {
        for (Field field : fields) {
            try {
                // Field field = clazz.getDeclaredField(fieldName);
                if (boolean.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    field.set(this, flag);
                } else {
                    Logger.i(getClass(),
                            "Skipping " + field.getName() + ", field is not boolean: " + field.getType());
                }
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
                Logger.e(getClass(), e.toString());
            }
        }
    }

    /**
     * Copies boolean fields, source of boolean fields are taken from destClass, boolean value read from struct using
     * method call with same name as field.
     * 
     * @param sourceStruct
     * @param destClass Field names are taken from this, value read from sourceStruct is copied here
     */
    protected void copyBooleanFieldsFromStruct(Object sourceStruct, Class<?> destClass) {
        for (Field field : destClass.getDeclaredFields()) {
            copyBooleanFieldUsingMethod(sourceStruct, field);
        }
    }

    /**
     * Returns true if fields that are boolean and set to true in source, are also true in this.
     * Ignores fields that are not boolean
     * 
     * @param source
     * @return
     */
    public boolean checkBooleansTrue(Object source, Field... fields) {
        for (Field field : fields) {
            if (boolean.class.isAssignableFrom(field.getType())) {
                boolean flag = getBooleanField(source, field);
                if (flag) {
                    if (!getBooleanField(this, field)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Copies fields, source of fields are taken from destClass, value read from struct using
     * method call with same name as field.
     * 
     * @param sourceStruct
     * @param destClass Field names are taken from this, value read from sourceStruct is copied here
     */
    protected void copyFieldsFromStruct(Object sourceStruct, Class<?> destClass) {
        try {
            for (Field field : destClass.getDeclaredFields()) {
                if (int.class.isAssignableFrom(field.getType())) {
                    copyIntFieldUsingMethod(sourceStruct, field);
                } else if (boolean.class.isAssignableFrom(field.getType())) {
                    copyBooleanFieldUsingMethod(sourceStruct, field);
                } else if (field.getType().getComponentType() == int.class) {
                    copyIntArrayUsingMethod(sourceStruct, field);
                } else {
                    if (Extent2D.class.isAssignableFrom(field.getType())) {
                        Method source;
                        source = sourceStruct.getClass().getDeclaredMethod(field.getName());
                        Object vkExtentRead = source.invoke(sourceStruct);
                        Method wMethod = vkExtentRead.getClass().getMethod("width");
                        Object width = vkExtentRead.getClass().getMethod("width").invoke(vkExtentRead);
                        Object height = vkExtentRead.getClass().getMethod("height").invoke(vkExtentRead);
                        field.setAccessible(true);
                        Extent2D extent = new Extent2D((Integer) width, (Integer) height);
                        field.set(this, extent);
                    } else if (Vulkan10.SampleCountFlagBit.class.isAssignableFrom(field.getType())) {
                        Method source = sourceStruct.getClass().getDeclaredMethod(field.getName());
                        Integer count = (Integer) source.invoke(sourceStruct);
                        field.setAccessible(true);
                        Vulkan10.SampleCountFlagBit sampleCount = Vulkan10.SampleCountFlagBit.get(count);
                        field.set(this, sampleCount);
                    } else {
                        Logger.e(getClass(), "Could not copy field: " + field.getName());
                    }
                }
            }
        } catch (NoSuchMethodException | SecurityException | InvocationTargetException
                | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Copies int arrays, source of int arrays are taken from destClass, int array read from struct using
     * method call with same name as field.
     * 
     * @param sourceStruct
     * @param destClass Field names are taken from this, values read from sourceStruct is copied here
     */
    protected void copyIntArrayUsingMethod(Object sourceStruct, Field... fields) {
        for (Field field : fields) {
            String fieldName = field.getName();
            try {
                // Field field = clazz.getDeclaredField(fieldName);
                if (field.getType().getComponentType() != null && field.getType().getComponentType() == int.class) {
                    Method source = sourceStruct.getClass().getDeclaredMethod(fieldName, null);
                    Object read = source.invoke(sourceStruct, null);
                    field.setAccessible(true);
                    if (read instanceof IntBuffer) {
                        IntBuffer intBuff = (IntBuffer) read;
                        intBuff.position(0);
                        int[] values = new int[intBuff.capacity()];
                        for (int i = 0; i < values.length; i++) {
                            values[i] = intBuff.get();
                        }
                        read = values;
                    }
                    field.set(this, read);
                } else {
                    Logger.i(getClass(),
                            "Skipping " + field.getName() + ", field is not int array: " + field.getType());
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                Logger.e(getClass(), e.toString());
            }
        }
    }

    /**
     * Copies one or more int value from struct using method name, to read int value, from the field name.
     * 
     * @param sourceStruct Source where method is invoked, using methodname of field, to retrieve int value
     * @param fields int value read from struct is stored here.
     */
    protected void copyIntFieldUsingMethod(Object sourceStruct, Field... fields) {
        for (Field field : fields) {
            String fieldName = field.getName();
            try {
                // Field field = clazz.getDeclaredField(fieldName);
                if (int.class.isAssignableFrom(field.getType())) {
                    Method source = sourceStruct.getClass().getDeclaredMethod(fieldName, null);
                    Object read = source.invoke(sourceStruct, null);
                    field.setAccessible(true);
                    field.set(this, read);
                } else {
                    Logger.i(getClass(),
                            "Skipping " + field.getName() + ", field is not int: " + field.getType());
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                Logger.e(getClass(), e.toString());
            }
        }
    }

    /**
     * Copies the field with the given name from sourceStruct into destClass
     * 
     * @param sourceStruct
     * @param destClass
     * @param fieldName
     */
    protected void copyBooleanField(Object sourceStruct, Class<?> destClass, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                copyBooleanFieldUsingMethod(sourceStruct, destClass.getDeclaredField(fieldName));
            } catch (NoSuchFieldException | SecurityException e) {
                Logger.e(getClass(), e.toString());
            }
        }
    }

    /**
     * Copies one or more boolean value from struct using method name, to read boolean value, from the field name.
     * 
     * @param sourceStruct Source where method is invoked, using methodname of field, to retrieve boolean value
     * @param fields boolean value read from struct is stored here.
     */
    protected void copyBooleanFieldUsingMethod(Object sourceStruct, Field... fields) {
        for (Field field : fields) {
            String fieldName = field.getName();
            try {
                // Field field = clazz.getDeclaredField(fieldName);
                if (boolean.class.isAssignableFrom(field.getType())) {
                    Method source = sourceStruct.getClass().getDeclaredMethod(fieldName, null);
                    Object read = source.invoke(sourceStruct, null);
                    field.setAccessible(true);
                    field.set(this, read);
                } else {
                    Logger.i(getClass(), "Skipping " + field.getName() + ", field is not boolean: " + field.getType());
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                Logger.e(getClass(), e.toString());
            }
        }
    }

    /**
     * Copies the boolean fields from sourceObject into destStruct using method to set boolean value.
     * Method name to set value must match name of boolean field.
     * 
     * @param destStruct
     * @param sourceObject
     * @param fields
     */
    protected void copyBooleanFieldToMethod(Object destStruct, Object sourceObject, Field... fields) {
        for (Field field : fields) {
            String fieldName = field.getName();
            try {
                if (boolean.class.isAssignableFrom(field.getType())) {
                    Method dest = destStruct.getClass().getDeclaredMethod(fieldName, boolean.class);
                    field.setAccessible(true);
                    dest.invoke(destStruct, field.getBoolean(sourceObject));
                } else {
                    Logger.i(getClass(),
                            "Skipping " + field.getName() + ", field is not boolean: " + field.getType());
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                Logger.e(getClass(), e.toString());
            }
        }
    }

    @Override
    public String toString() {
        String result = "";
        for (Field scanField : getClass().getSuperclass().getDeclaredFields()) {
            String fieldName = scanField.getName();
            try {
                Field field = getClass().getSuperclass().getDeclaredField(fieldName);
                Object read = field.get(this);
                result += fieldName + "= " + toString(read) + "\n";
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
                Logger.d(getClass(), e.toString() + " at field: " + fieldName);
            }
        }
        return result;
    }

    private String toString(Object read) {
        if (read instanceof Object[]) {
            return Arrays.toString((Object[]) read);
        }
        // Need to check for data type array since they are not Object[]
        if (read instanceof int[]) {
            return Arrays.toString((int[]) read);
        }
        if (read instanceof float[]) {
            return Arrays.toString((float[]) read);
        }
        if (read instanceof long[]) {
            return Arrays.toString((long[]) read);
        }
        if (read instanceof byte[]) {
            return Arrays.toString((byte[]) read);
        }
        return read != null ? read.toString() : null;
    }

    /**
     * Reads array of int or float values from Buffer and returns as array
     * 
     * @param buffer
     * @return float or int array with values from buffer, or null if buffer is not IntBuffer or FloatBuffer
     */
    protected Object getFromBuffer(Buffer buffer) {
        if (buffer instanceof IntBuffer) {
            IntBuffer intBuffer = (IntBuffer) buffer;
            int[] result = new int[intBuffer.capacity()];
            intBuffer.position(0);
            intBuffer.get(result);
            return result;
        }
        if (buffer instanceof FloatBuffer) {
            FloatBuffer floatBuffer = (FloatBuffer) buffer;
            float[] result = new float[floatBuffer.capacity()];
            floatBuffer.position(0);
            floatBuffer.get(result);
            return result;
        }
        return null;
    }

    /**
     * Returns the values of the boolean field.
     * 
     * @param field
     * @return
     */
    public boolean getBooleanField(Field field) {
        return getBooleanField(this, field);
    }

    /**
     * Returns the value of the boolean field in the source
     * 
     * @param source
     * @param field
     * @return
     */
    public boolean getBooleanField(Object source, Field field) {
        try {
            // Field field = clazz.getDeclaredField(fieldName);
            if (boolean.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return (boolean) field.get(source);
            } else {
                Logger.i(getClass(),
                        "Skipping " + field.getName() + ", field is not boolean: " + field.getType());
            }
        } catch (SecurityException | IllegalAccessException
                | IllegalArgumentException e) {
            Logger.e(getClass(), e.toString());
        }
        return false;

    }

}

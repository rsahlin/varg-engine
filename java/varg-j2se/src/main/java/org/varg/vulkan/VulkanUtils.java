
package org.varg.vulkan;

import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONAccessor.ComponentType;
import org.gltfio.gltf2.JSONAccessor.Type;

import org.gltfio.lib.ErrorMessage;

public class VulkanUtils {

    private VulkanUtils() {
    }

    /**
     * Returns the vulkan format to be used for accessor buffer
     * 
     * @param accessor
     * @return
     */
    public static Vulkan10.Format getFormat(JSONAccessor accessor) {
        ComponentType componentType = accessor.getComponentType();
        Type type = accessor.getType();
        switch (componentType) {
            case FLOAT:
                return getFormatFloat(type);
            default:
                throw new IllegalArgumentException(
                        ErrorMessage.NOT_IMPLEMENTED.message + "ComponentType: " + componentType);
        }
    }

    public static Vulkan10.Format getFormatFloat(Type type) {
        switch (type) {
            case SCALAR:
                return Vulkan10.Format.VK_FORMAT_R32_SFLOAT;
            case VEC2:
                return Vulkan10.Format.VK_FORMAT_R32G32_SFLOAT;
            case VEC3:
                return Vulkan10.Format.VK_FORMAT_R32G32B32_SFLOAT;
            case VEC4:
                return Vulkan10.Format.VK_FORMAT_R32G32B32A32_SFLOAT;
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + "Type: " + type);
        }
    }

}


package org.varg.renderer;

import org.gltfio.gltf2.JSONAccessor.ComponentType;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.IndexType;
import org.varg.vulkan.Vulkan10.PrimitiveTopology;

/**
 * Utils for Gltf / Vulkan
 *
 */
public class GltfUtils {

    private GltfUtils() {
    }

    public static PrimitiveTopology getFromGltfMode(DrawMode mode) {
        if (mode != null) {
            switch (mode) {
                case LINE_STRIP:
                    return PrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP;
                case POINTS:
                    return PrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
                case TRIANGLES:
                    return PrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
                case TRIANGLE_FAN:
                    return PrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN;
                case TRIANGLE_STRIP:
                    return PrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP;
                case LINES:
                    return PrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_LIST;
                default:
                    throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + mode);
            }
        }
        return null;
    }

    public static IndexType getFromGltfType(ComponentType type) {
        switch (type) {
            case UNSIGNED_BYTE:
                return IndexType.VK_INDEX_TYPE_UINT8_EXT;
            case UNSIGNED_SHORT:
                return IndexType.VK_INDEX_TYPE_UINT16;
            case UNSIGNED_INT:
                return IndexType.VK_INDEX_TYPE_UINT32;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + type.name());
        }
    }

}

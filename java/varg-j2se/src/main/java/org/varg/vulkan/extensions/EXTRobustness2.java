package org.varg.vulkan.extensions;

import org.gltfio.lib.AllowPublic;
import org.varg.vulkan.structs.PlatformStruct;

/**
 * VK_EXT_Robustness2 extension
 */
public class EXTRobustness2 {

    public static class PhysicalDeviceRobustness2FeaturesEXT extends PlatformStruct {

        @AllowPublic
        public boolean robustBufferAccess2;
        @AllowPublic
        public boolean robustImageAccess2;
        @AllowPublic
        public boolean nullDescriptor;

        public PhysicalDeviceRobustness2FeaturesEXT(boolean robustBufferAccess2, boolean robustImageAccess2,
                boolean nullDescriptor) {
            this.robustBufferAccess2 = robustBufferAccess2;
            this.robustImageAccess2 = robustImageAccess2;
            this.nullDescriptor = nullDescriptor;
        }

        public PhysicalDeviceRobustness2FeaturesEXT(Object features) {
            super.copyBooleanFieldsFromStruct(features, PhysicalDeviceRobustness2FeaturesEXT.class);
        }

    }

}

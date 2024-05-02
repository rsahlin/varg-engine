package org.varg.vulkan.structs;

import org.gltfio.lib.AllowPublic;

public class PhysicalDevice16BitStorageFeatures extends PlatformStruct {

    @AllowPublic
    public boolean storageBuffer16BitAccess;
    @AllowPublic
    public boolean uniformAndStorageBuffer16BitAccess;
    @AllowPublic
    public boolean storagePushConstant16;
    @AllowPublic
    public boolean storageInputOutput16;

    public PhysicalDevice16BitStorageFeatures(boolean storageBuffer16BitAccess, boolean uniformAndStorageBuffer16BitAccess, boolean storagePushConstant16, boolean storageInputOutput16) {
        this.storageBuffer16BitAccess = storageBuffer16BitAccess;
        this.uniformAndStorageBuffer16BitAccess = uniformAndStorageBuffer16BitAccess;
        this.storagePushConstant16 = storagePushConstant16;
        this.storageInputOutput16 = storageInputOutput16;
    }

    public PhysicalDevice16BitStorageFeatures(Object features) {
        super.copyBooleanFieldsFromStruct(features, PhysicalDevice16BitStorageFeatures.class);
    }

}

package org.varg.vulkan.structs;

import org.gltfio.lib.AllowPublic;

public class PhysicalDevice8BitStorageFeatures extends PlatformStruct {

    @AllowPublic
    public boolean storageBuffer8BitAccess;
    @AllowPublic
    public boolean uniformAndStorageBuffer8BitAccess;
    @AllowPublic
    public boolean storagePushConstant8;

    public PhysicalDevice8BitStorageFeatures(boolean storageBuffer8BitAccess, boolean uniformAndStorageBuffer8BitAccess,
            boolean storagePushConstant8) {
        this.storageBuffer8BitAccess = storageBuffer8BitAccess;
        this.uniformAndStorageBuffer8BitAccess = uniformAndStorageBuffer8BitAccess;
        this.storagePushConstant8 = storagePushConstant8;
    }

    public PhysicalDevice8BitStorageFeatures(Object features) {
        super.copyBooleanFieldsFromStruct(features, PhysicalDevice8BitStorageFeatures.class);
    }

}

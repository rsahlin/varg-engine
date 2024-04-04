package org.varg.vulkan.structs;

import org.gltfio.lib.AllowPublic;

public class PhysicalDeviceShaderFloat16Int8Features extends PlatformStruct {

    @AllowPublic
    public boolean shaderFloat16;
    @AllowPublic
    public boolean shaderInt8;

    protected PhysicalDeviceShaderFloat16Int8Features(boolean shaderFloat16, boolean shaderInt8) {
        this.shaderFloat16 = shaderFloat16;
        this.shaderInt8 = shaderInt8;
    }

    public PhysicalDeviceShaderFloat16Int8Features(Object features) {
        super.copyBooleanFieldsFromStruct(features, PhysicalDeviceShaderFloat16Int8Features.class);
    }

}

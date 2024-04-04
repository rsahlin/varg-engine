package org.varg.vulkan.extensions;

import org.varg.vulkan.structs.PlatformStruct;

/**
 * The VK_KHR_acceleration_structure extension features
 */
public class PhysicalDeviceAccelerationStructureFeaturesKHR extends PlatformStruct {

    public boolean accelerationStructure;
    public boolean accelerationStructureCaptureReplay;
    public boolean accelerationStructureIndirectBuild;
    public boolean accelerationStructureHostCommands;
    public boolean descriptorBindingAccelerationStructureUpdateAfterBind;

    public PhysicalDeviceAccelerationStructureFeaturesKHR() {
        this.accelerationStructure = true;
    }

    public PhysicalDeviceAccelerationStructureFeaturesKHR(boolean accelerationStructure,
            boolean accelerationStructureCaptureReplay, boolean accelerationStructureIndirectBuild,
            boolean accelerationStructureHostCommands, boolean descriptorBindingAccelerationStructureUpdateAfterBind) {
        this.accelerationStructure = accelerationStructure;
        this.accelerationStructureCaptureReplay = accelerationStructureCaptureReplay;
        this.accelerationStructureIndirectBuild = accelerationStructureIndirectBuild;
        this.accelerationStructureHostCommands = accelerationStructureHostCommands;
        this.descriptorBindingAccelerationStructureUpdateAfterBind =
                descriptorBindingAccelerationStructureUpdateAfterBind;
    }

    public PhysicalDeviceAccelerationStructureFeaturesKHR(Object features) {
        super.copyBooleanFieldsFromStruct(features, PhysicalDeviceAccelerationStructureFeaturesKHR.class);
    }

}

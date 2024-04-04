package org.varg.vulkan.extensions;

import org.gltfio.lib.AllowPublic;
import org.varg.vulkan.structs.PlatformStruct;

/**
 * The VkPhysicalDeviceMeshShaderFeaturesEXT features
 *
 */
public class PhysicalDeviceMeshShaderFeaturesEXT extends PlatformStruct {

    @AllowPublic
    public boolean taskShader;
    @AllowPublic
    public boolean meshShader;
    @AllowPublic
    public boolean multiviewMeshShader;
    @AllowPublic
    public boolean primitiveFragmentShadingRateMeshShader;
    @AllowPublic
    public boolean meshShaderQueries;

    public PhysicalDeviceMeshShaderFeaturesEXT(boolean taskShader, boolean meshShader, boolean multiviewMeshShader,
            boolean primitiveFragmentShadingRateMeshShader, boolean meshShaderQueries) {
        this.taskShader = taskShader;
        this.meshShader = meshShader;
        this.multiviewMeshShader = multiviewMeshShader;
        this.primitiveFragmentShadingRateMeshShader = primitiveFragmentShadingRateMeshShader;
        this.meshShaderQueries = meshShaderQueries;
    }

    public PhysicalDeviceMeshShaderFeaturesEXT(Object features) {
        super.copyBooleanFieldsFromStruct(features, PhysicalDeviceMeshShaderFeaturesEXT.class);
    }

}

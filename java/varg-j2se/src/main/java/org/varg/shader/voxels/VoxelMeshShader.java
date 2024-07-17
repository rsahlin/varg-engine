package org.varg.shader.voxels;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Matrix;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.renderer.Renderers;
import org.varg.shader.BaseShaderImplementation;
import org.varg.shader.MeshShader;
import org.varg.shader.MeshShader.MeshShaderCreateInfo;
import org.varg.shader.Shader;
import org.varg.shader.ShaderBinary;
import org.varg.shader.VulkanShaderBinary;
import org.varg.shader.voxels.VoxelMeshShader.VoxelMeshShaderCreateInfo;
import org.varg.vulkan.GLSLCompiler;
import org.varg.vulkan.Vulkan10.Format;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderPropertiesEXT;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationInfo;

public class VoxelMeshShader extends BaseShaderImplementation<VoxelMeshShaderCreateInfo> implements MeshShader<VoxelMeshShaderCreateInfo> {

    private static final int INVOCATIONS_PER_CUBE = 4;

    public static final int[] TIA_COLOR_PALETTE = new int[] {
            0x000000, 0x444400, 0x702800, 0x841800, 0x880000, 0x78005C, 0x480078, 0x140084, 0x000088, 0x00187C,
            0x002C5C, 0x003C2C, 0x003C00, 0x143800, 0x2C3000, 0x442800,
            0x404040, 0x646410, 0x844414, 0x983418, 0x9C2020, 0x8C2074, 0x602090, 0x302098, 0x1C209C, 0x1C3890,
            0x1C4C78, 0x1C5C48, 0x205C20, 0x345C1C, 0x4C501C, 0x644818,
            0x6C6C6C, 0x848424, 0x985C28, 0xAC5030, 0xB03C3C, 0xA03C88, 0x783CA4, 0x4C3CAC, 0x3840B0, 0x3854A8,
            0x386890, 0x387C64, 0x407C40, 0x507C38, 0x687034, 0x846830,
            0x909090, 0xA0A034, 0xAC783C, 0xC06848, 0xC05858, 0xB0589C, 0x8C58B8, 0x6858C0, 0x505CC0, 0x5070BC,
            0x5084AC, 0x509C80, 0x5C9C5C, 0x6C9850, 0x848C4C, 0xA08444,

            0xB0B0B0, 0xB8B840, 0xBC8C4C, 0xD0805C, 0xD07070, 0xC070B0, 0xA070CC, 0x7C70D0, 0x6874D0, 0x6888CC,
            0x689CC0, 0x68B494, 0x74B474, 0x84B468, 0x9CA864, 0xB89C58,
            0xC8C8C8, 0xD0D050, 0xCCA05C, 0xE09470, 0xE08888, 0xD084C0, 0xB484DC, 0x9488E0, 0x7C8CE0, 0x7C9CDC,
            0x7CB4D4, 0x7CD0AC, 0x8CD08C, 0x9CCC7C, 0xB4C078, 0xD0B46C,
            0xDCDCDC, 0xE8E85C, 0xDCB468, 0xECA880, 0xECA0A0, 0xDC9CD0, 0xC49CEC, 0xA8A0EC, 0x90A4EC, 0x90B4EC,
            0x90CCE8, 0x90E4C0, 0xA4E4A4, 0xB4E490, 0xCCD488, 0xE8CC7C,
            0xECECEC, 0xFCFC68, 0xECC878, 0xFCBC94, 0xFCB4B4, 0xECB0E0, 0xD4B0FC, 0xBCB4FC, 0xA4B8FC, 0xA4C8FC,
            0xA4E0FC, 0xA4FCD4, 0xB8FCB8, 0xC8FCA4, 0xE0EC9C, 0xFCE08C
    };

    public static class VoxelMeshShaderCreateInfo extends MeshShaderCreateInfo {

        private final int spriteCount = 100;
        private final int cubeCount;
        private final int requestedCubeCount;
        private final PhysicalDeviceMeshShaderPropertiesEXT properties;
        private final int cubesPerWorkGroup;
        public final Format offsetFormat = Format.VK_FORMAT_R32_SFLOAT;

        public VoxelMeshShaderCreateInfo(int cubeCount, @NonNull Renderers version, PhysicalDeviceMeshShaderPropertiesEXT properties) {
            super(null, version, MeshShaderType.VOXELS);
            this.requestedCubeCount = cubeCount;
            int count = properties.maxPreferredMeshWorkGroupInvocations / INVOCATIONS_PER_CUBE;
            if (count * 4 > properties.maxMeshOutputVertices | count * 12 > properties.maxMeshOutputPrimitives) {
                int maxVerticeInvocations = properties.maxMeshOutputVertices / 4;
                int maxPrimitiveInvocations = properties.maxMeshOutputPrimitives / 12;
                count = Math.min(maxVerticeInvocations, maxPrimitiveInvocations) / INVOCATIONS_PER_CUBE;
            }
            this.cubesPerWorkGroup = count;
            this.properties = properties;
            int remainder = (cubeCount % getLocalSizes(Shader.Stage.TASK)[0]);
            this.cubeCount = cubeCount;
            // this.cubeCount = remainder != 0 ? cubeCount + ((getLocalSizes(Shader.Stage.TASK)[0] - remainder))
            // : cubeCount;
        }

        /**
         * Returns the max sprite count
         * 
         * @return
         */
        public int getMaxSpriteCount() {
            return spriteCount;
        }

        /**
         * Returns the number of cubes output per global workgroup
         * 
         * @return
         */
        public int getCubesPerWorkGroup(Shader.Stage stage) {
            switch (stage) {
                case MESH:
                    return cubesPerWorkGroup;
                case TASK:
                    return getLocalSizes(stage)[0];
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + stage.name);
            }
        }

        /**
         * Returns the cubecount
         * 
         * @return
         */
        public int getCubeCount() {
            return cubeCount;
        }

        @Override
        public int[] getWorkGroupCounts(Shader.Stage stage) {
            switch (stage) {
                case TASK:
                    return new int[] { cubeCount / getLocalSizes(stage)[0], 1, 1 };
                case MESH:
                    return new int[] { (getLocalSizes(Shader.Stage.TASK)[0] / cubesPerWorkGroup), 1, 1 };
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + stage.name);
            }
        }

        @Override
        public int[] getLocalSizes(Shader.Stage stage) {
            switch (stage) {
                case MESH:
                    return new int[] { INVOCATIONS_PER_CUBE,
                            cubesPerWorkGroup, 1 };
                case TASK:
                    return new int[] { properties.maxPreferredTaskWorkGroupInvocations, 1, 1 };
                default:
                    return null;
            }
        }

        @Override
        public int getBufferSize(DescriptorSetTarget target) {
            MeshDescriptorSetTarget meshTarget = MeshDescriptorSetTarget.get(target.getName());
            int floatSize = Format.VK_FORMAT_R16_SFLOAT.getComponentByteSize();
            switch (meshTarget) {
                case MATRIX:
                    return 3 * Matrix.MATRIX_ELEMENTS * floatSize;
                case OUTPUTS:
                    return 4 * Float.BYTES * cubeCount;
                case SPRITE:
                    return (4 * floatSize) +
                            (Matrix.MATRIX_ELEMENTS * floatSize) +
                            (8 * 4 * floatSize)
                                    * spriteCount;
                case DATA:
                    int size = 4 * offsetFormat.getComponentByteSize() * cubeCount; // Offsets
                    size += Format.VK_FORMAT_R8_UINT.getComponentByteSize() * cubeCount; // Palette indexes
                    return size;
                case GLOBAL:
                    // Color palette + EmitMeshTask + cameramatrix
                    return floatSize * TIA_COLOR_PALETTE.length * 4 +
                            4 * floatSize +
                            16 * floatSize;
                default:
                    throw new IllegalArgumentException(target.getName());
            }
        }

        @Override
        public int[] getBufferSizes(DescriptorSetTarget... descriptorSetTargets) {
            int[] result = new int[descriptorSetTargets.length];
            for (int i = 0; i < result.length; i++) {
                DescriptorSetTarget target = descriptorSetTargets[i];
                if (result[target.getSet()] != 0) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_STATE + "Conflicting sets");
                }
                result[target.getSet()] = getBufferSize(target);
            }
            return result;
        }

        @Override
        public VoxelMeshShader getInstance() {
            return new VoxelMeshShader(this);
        }

        @Override
        public int[] getDynamicOffsets(DescriptorSetTarget... descriptorSetTargets) {
            return new int[descriptorSetTargets.length];
        }

        @Override
        public void setMacros(GLSLCompiler compiler) {
            setMacros(compiler, Stage.MESH, Stage.TASK);
            compiler.addMacro("MAX_VOXEL_COUNT", Integer.toString(cubeCount), Shader.Stage.TASK, Shader.Stage.MESH);
            compiler.addMacro("MAX_SPRITE_COUNT", Integer.toString(spriteCount), Shader.Stage.TASK, Shader.Stage.MESH);
        }

    }

    VoxelMeshShader(VoxelMeshShaderCreateInfo info) {
        super(info);
    }

    @Override
    public void loadModules(Vulkan10Backend<?> backend, int pipelineHash) throws IOException {
        internalLoadModules(backend, this, shaderInfo, pipelineHash);
    }

    @Override
    public ShaderBinary getShaderSource(Shader.Stage stage) {
        switch (stage) {
            case TASK:
            case MESH:
            case FRAGMENT:
                VulkanShaderBinary spirv = new VulkanShaderBinary();
                return internalGetShaderSource(stage, getShaderInfo(), spirv);
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + stage);
        }
    }

    @Override
    public PipelineShaderStageCreateInfo[] createShaderStageInfos(SpecializationInfo specializationInfo) {
        return createShaderStageInfos(shaderInfo, specializationInfo);
    }

    @Override
    public boolean hasStage(Stage stage) {
        return internalHasStage(stage);
    }

    @Override
    public VoxelMeshShaderCreateInfo getShaderInfo() {
        return shaderInfo;
    }

}

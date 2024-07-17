package org.varg.shader;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Settings;
import org.gltfio.lib.Settings.Property;
import org.varg.renderer.Renderers;
import org.varg.shader.Shader.Stage;
import org.varg.shader.Shader.Subtype;
import org.varg.vulkan.GLSLCompiler.Macros;
import org.varg.vulkan.Vulkan10.ShaderStageFlagBit;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.VulkanBackend;
import org.varg.vulkan.VulkanBackend.BackendIntProperties;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationInfo;
import org.varg.vulkan.structs.ShaderModule;

public abstract class BaseShaderImplementation<T extends ShaderCreateInfo> {

    protected final T shaderInfo;

    protected BaseShaderImplementation(T shaderInfo) {
        this.shaderInfo = shaderInfo;
    }

    /**
     * GLSL Shader macros
     *
     */
    public enum ShaderProperties implements Macros {

        MAX_CUBEMAPS(BackendIntProperties.MAX_CUBEMAPS, "", Stage.MESH, Stage.FRAGMENT, Stage.VERTEX),
        MAX_D_LIGHTS(BackendIntProperties.MAX_DIRECTIONAL_LIGHTS, "", Stage.MESH, Stage.FRAGMENT,
                Stage.VERTEX),
        MAX_P_LIGHTS(BackendIntProperties.MAX_POINT_LIGHTS, "", Stage.MESH, Stage.FRAGMENT, Stage.VERTEX);

        private final Property property;
        private final String prefix;
        private final Shader.Stage[] stages;

        ShaderProperties(Property property, String prefix, Shader.Stage... stages) {
            this.property = property;
            this.prefix = prefix;
            this.stages = stages;
        }

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public Property getProperty() {
            return property;
        }

        @Override
        public String getPrefix() {
            return prefix != null ? prefix : "";
        }

        @Override
        public Stage[] getStages() {
            return stages;
        }
    }

    private static final HashMap<Integer, Shader<?>> SHADERS = new HashMap<Integer, Shader<?>>();
    private HashMap<Subtype, ShaderModule[]> shaderModules = new HashMap<Subtype, ShaderModule[]>();
    protected HashMap<Stage, HashMap<Subtype, ShaderBinary>> stageMap;
    protected HashMap<Subtype, ShaderBinary> geometryBinaries = new HashMap<>();
    protected HashMap<Subtype, ShaderBinary> vertexBinaries = new HashMap<>();
    protected HashMap<Subtype, ShaderBinary> fragmentBinaries = new HashMap<>();
    protected HashMap<Subtype, ShaderBinary> meshBinaries = new HashMap<>();

    protected static Shader getShader(int hash) {
        return SHADERS.get(hash);
    }

    protected static void putShader(int hash, Shader shader) {
        if (SHADERS.containsKey(hash)) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + ", already contains shader for key " + hash);
        }
        SHADERS.put(hash, shader);
    }

    /**
     * Stores a shader modules with shaderType as key
     * 
     * @param shaderType
     * @param modules
     * @throws IllegalArgumentException If a shader module has alredy been set for shaderType
     */
    protected void putShaderModules(@NonNull Subtype shaderType, @NonNull ShaderModule[] modules) {
        if (shaderModules.containsKey(shaderType)) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + ", already contains modules for " + shaderType);
        }
        shaderModules.put(shaderType, modules);
    }

    /**
     * Returns the set shader module for the shaderType, call {@link #putShaderModules(Subtype, ShaderModule[])} to
     * set a shaderModule.
     * 
     * @param shaderType
     * @return
     */
    protected ShaderModule[] getShaderModules(Subtype shaderType) {
        return shaderModules.get(shaderType);
    }

    /**
     * Fetches the binary for the specified stage
     * 
     * @param stage
     * @param info
     * @return
     */
    protected ShaderBinary getBinary(Stage stage, ShaderCreateInfo info) {
        HashMap<Subtype, ShaderBinary> subTypeMap = getMap(stage);
        return subTypeMap.get(info.shaderType);
    }

    /**
     * Adds a binary for the specified stage - binary is stored using shader type from shaderInfo.
     * 
     * @param stage
     * @param info
     * @param binary
     */
    protected void addBinary(@NonNull Stage stage, @NonNull ShaderCreateInfo info,
            @NonNull ShaderBinary binary) {
        HashMap<Subtype, ShaderBinary> subTypeMap = getMap(stage);
        subTypeMap.put(info.shaderType, binary);
    }

    private HashMap<Subtype, ShaderBinary> getMap(Stage stage) {
        if (stageMap == null) {
            stageMap = new HashMap<>();
        }
        HashMap<Subtype, ShaderBinary> subTypeMap = stageMap.get(stage);
        if (subTypeMap == null) {
            subTypeMap = new HashMap<>();
            stageMap.put(stage, subTypeMap);
        }
        return subTypeMap;
    }

    /**
     * Returns the name of the shader sourcefile for the stage and subtype.
     * 
     * @param type
     * @param subtype
     * @return
     */
    protected String getSourceFile(Stage type, Subtype subtype) {
        if (subtype != null && subtype.hasStage(type)) {
            return subtype.getSourceName();
        }
        return null;
    }

    /**
     * Creates the shader modules for shader info
     * 
     * @param backend
     * @param info
     * @return
     */
    protected ShaderModule[] createShaderModules(@NonNull Vulkan10Backend<?> backend,
            @NonNull ShaderCreateInfo info) {
        Subtype shaderType = info.shaderType;
        int index = 0;
        ShaderModule[] modules = new ShaderModule[shaderType.getStages().length];
        for (Stage stage : shaderType.getStages()) {
            modules[index++] = backend.createShaderModule(getBinary(stage, info));
        }
        return modules;
    }

    /**
     * Returns the folder name for shader subtype.
     * 
     * @param subtype
     * @return
     */
    protected String getFolder(Subtype subtype) {
        return subtype != null ? subtype.getFolder() : "";
    }

    /**
     * Creates the piplinestagecreateinfos for the shader type
     * 
     * @return
     */
    protected PipelineShaderStageCreateInfo[] createShaderStageInfos(ShaderCreateInfo srcShaderInfo, SpecializationInfo specializationInfo) {
        Subtype shaderType = srcShaderInfo.shaderType;
        String entrypoint = "main";
        PipelineShaderStageCreateInfo[] createInfos = new PipelineShaderStageCreateInfo[shaderType.getStages().length];
        ShaderStageFlagBit stageFlag = null;
        ShaderModule[] modules = getShaderModules(shaderType);
        if (modules == null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + ", Shadermodules not created for " + shaderType);
        }
        int index = 0;
        for (Stage stage : shaderType.getStages()) {
            switch (stage) {
                case VERTEX:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_VERTEX_BIT;
                    break;
                case FRAGMENT:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_FRAGMENT_BIT;
                    break;
                case GEOMETRY:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_GEOMETRY_BIT;
                    break;
                case MESH:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_MESH_BIT_EXT;
                    break;
                case TASK:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_TASK_BIT_EXT;
                    break;
                case COMPUTE:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_COMPUTE_BIT;
                    break;
                case RAYGEN:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_RAYGEN_BIT_KHR;
                    break;
                case ANYHIT:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_ANY_HIT_BIT_KHR;
                    break;
                case CLOSESTHIT:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_CLOSEST_HIT_BIT_KHR;
                    break;
                case MISS:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_MISS_BIT_KHR;
                    break;
                case INTERSECTION:
                    stageFlag = ShaderStageFlagBit.VK_SHADER_STAGE_INTERSECTION_BIT_KHR;
                    break;
                default:
                    throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
            }
            createInfos[index] = new PipelineShaderStageCreateInfo(null, stageFlag, modules[index], entrypoint,
                    specializationInfo);
            index++;
        }
        return createInfos;
    }

    /**
     * Loads the shader modules
     * 
     * @param backend
     * @param srcShaderInfo
     * @throws IOException
     */
    protected void internalLoadModules(Vulkan10Backend<?> backend, Shader<?> shader, ShaderCreateInfo srcShaderInfo, int pipelineHash) throws IOException {
        Stage[] stages = srcShaderInfo.shaderType.getStages();
        for (Stage stage : stages) {
            ShaderBinary shaderBinary = shader.getShaderSource(stage);
            if (Settings.getInstance().getBoolean(VulkanBackend.BackendProperties.RECOMPILE_SPIRV)) {
                shaderBinary.compileShader(srcShaderInfo, Integer.toString(pipelineHash));
            } else {
                shaderBinary.loadShader(srcShaderInfo, Integer.toString(pipelineHash));
            }
            addBinary(stage, srcShaderInfo, shaderBinary);
        }
        putShaderModules(srcShaderInfo.shaderType, createShaderModules(backend, srcShaderInfo));
    }

    /**
     * Internal method to create shader binary, returns null if sourcename is null
     * 
     * @param stage
     * @param spirv
     * @return
     */
    protected ShaderBinary internalGetShaderSource(Shader.Stage stage, ShaderCreateInfo info, ShaderBinary spirv) {
        String sourceName = getSourceFile(stage, info.shaderType);
        if (sourceName == null) {
            return null;
        }
        Renderers version = info.version;
        String sourceNameVersion = ShaderBinary.getSourceNameVersion(version);
        String sourcePath = ShaderBinary.PROGRAM_DIRECTORY + sourceNameVersion + getFolder(info.shaderType);
        spirv.set(sourcePath, sourceName, stage);
        return spirv;

    }

    /**
     * Internal method
     * 
     * @param stage
     * @return
     */
    protected boolean internalHasStage(Stage stage) {
        if (stageMap == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "No shader binary loaded");
        }
        return stageMap.containsKey(stage);
    }

}

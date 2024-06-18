
package org.varg.vulkan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONMaterial;
import org.gltfio.gltf2.JSONMaterial.AlphaMode;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.extensions.GltfExtensions;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.JSONExtension.ExtensionSetting;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.KHREnvironmentMapReference;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.Light.Type;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Platform;
import org.gltfio.lib.Settings;
import org.gltfio.lib.Settings.IntProperty;
import org.gltfio.lib.Settings.Property;
import org.gltfio.lib.Settings.StringProperty;
import org.gltfio.lib.StreamUtils;
import org.ktximageio.ktx.ImageReader.TransferFunction;
import org.varg.renderer.Renderers;
import org.varg.shader.Shader;
import org.varg.shader.Shader.Stage;
import org.varg.spirv.SpirvBinary;
import org.varg.spirv.SpirvBinary.SpirvStream;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;

/**
 * Used to compile GLSL to SPIR-V in runtime.
 * Singleton class.
 *
 */
public class GLSLCompiler {

    public interface Macros {

        String getName();

        String getPrefix();

        Shader.Stage[] getStages();

        Property getProperty();

    }

    private static final String SHADER_SOURCE_DIRECTORY = "src/main/resources";
    private static final String SHADER_JAVA_TARGET_DIRECTORY = "target/classes";

    /**
     * Set of macros and value, key is the macro name exluding preprocessor "-D"
     * value is the value to set, excluding whitespace and "="
     *
     */
    public static class MacroSet {
        private Hashtable<String, String> keyValues = new Hashtable<>();

        private void put(String key, String value) {
            if (keyValues.containsKey(key)) {
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_STATE.message + " already added key for: " + key);
            }
            checkKey(key);
            checkValue(value);
            keyValues.put(key, value);
        }

        private void checkKey(String key) {
            if (key.startsWith(PREPROCESSOR_MACRO) | key.endsWith("=")) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Key includes invalid chars: '"
                        + key + "'");
            }
        }

        private void checkValue(String value) {
            if (value.startsWith("=") | value.length() != value.trim().length()) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                        + "Value includes invalid chars: '"
                        + value + "'");

            }
        }

        /**
         * Returns the compile macro string
         * 
         * @return
         */
        public String getMacroString() {
            StringBuffer result = new StringBuffer();
            for (String str : keyValues.keySet()) {
                result.append(PREPROCESSOR_MACRO + str + "=" + keyValues.get(str) + " ");
            }
            return result.toString();
        }

    }

    private static final Channel[] DISABLE_TEXTURES = new Channel[] { Channel.NORMAL, Channel.METALLICROUGHNESS, Channel.OCCLUSION, Channel.EMISSIVE };
    private static final BackendProperties[] PROPERTY = new BackendProperties[] { BackendProperties.NO_NORMALTEXTURE, BackendProperties.NO_MRTEXTURE, BackendProperties.NO_OCCLUSIONTEXTURE, BackendProperties.NO_EMISSIVETEXTURE };

    /**
     * GLSL Shader macros
     *
     */
    private enum MacroProperties implements Macros {

        DEBUGCHANNEL(BackendStringProperties.DEBUGCHANNEL, "RAW_", Stage.FRAGMENT),
        BRDF(BackendStringProperties.BRDF, "BRDF_", Stage.FRAGMENT),
        FB_FORMAT(null, "FORMAT_", Stage.FRAGMENT),
        COLORSPACE(null, "COLORSPACE_", Stage.FRAGMENT),
        TEXTURE_TRANSFORM(null, "NOT USED", Stage.VERTEX, Stage.FRAGMENT),
        CUBEMAP(null, "NOT USED", Stage.MESH, Stage.VERTEX, Stage.FRAGMENT),
        MATRIX_COUNT(null, "NOT_USED", Stage.MESH, Stage.VERTEX, Stage.FRAGMENT);

        private final Property property;
        private final String prefix;
        private final Shader.Stage[] stages;

        MacroProperties(Property property, String prefix, Shader.Stage... stages) {
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
            return prefix;
        }

        @Override
        public Stage[] getStages() {
            return stages;
        }
    }

    public enum MacroIntProperties implements IntProperty {
        SIZE_X(),
        SIZE_Y(),
        SIZE_Z(),
        EMITARRAY_SIZE(),
        PUNCTUAL_LIGHTS(Shader.Stage.VERTEX, Shader.Stage.FRAGMENT);

        private final Shader.Stage[] stages;

        MacroIntProperties(Shader.Stage... stages) {
            this.stages = stages;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public String getKey() {
            return null;
        }

        @Override
        public String getDefault() {
            return null;
        }

    }

    public enum CompilerProperties implements StringProperty {
        GLSL_COMPILER("glsl.compiler", ShaderCompiler.SHADERC.name());

        private final String key;
        private final String defaultValue;

        CompilerProperties(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return defaultValue;
        }

    }

    public enum ShaderCompiler {
        GLSLANG(),
        SHADERC();

        public static ShaderCompiler get(String compiler) {
            for (ShaderCompiler sc : values()) {
                if (compiler.equalsIgnoreCase(sc.name())) {
                    return sc;
                }
            }
            return null;
        }
    }

    public static final String SPIRV_EXTENSION = ".spv";
    public static final String GLSL_EXTENSION = ".glsl";

    private static Hashtable<Renderers, GLSLCompiler> compilerTable = new Hashtable<>();

    private static final String GLSLC_COMPILE_COMMAND = "glslc";
    private static final String GLSLC_PREPROCESS_COMMAND = "-c";
    private static final String GLSLC_OPTIMIZE_COMMAND = "-O";
    private static final String GLSLC_TARGET_COMMAND = "--target-env=";

    private static final String GLSLANG_COMPILE_COMMAND = "glslangValidator";
    private static final String GLSLANG_TARGET_COMMAND = "--target-env ";

    private String compileString;
    private final ByteBuffer buffer = Buffers.createByteBuffer(64000);

    private static final String PREPROCESSOR_MACRO = "-D";

    private Hashtable<String, MacroSet> stageMacroTable = new Hashtable<>();

    private final ShaderCompiler compiler;

    private GLSLCompiler(@NonNull ShaderCompiler compiler) {
        this.compiler = compiler;
    }

    /**
     * Returns the glsl compiler instance used to compile GLSL into spir-v
     * You must add macros as needed before calling compile.
     * 
     * @return
     */
    public static GLSLCompiler getInstance(Renderers version) {
        GLSLCompiler compiler = compilerTable.get(version);
        if (compiler == null) {
            compiler = new GLSLCompiler(ShaderCompiler.get(Settings.getInstance().getProperty(CompilerProperties.GLSL_COMPILER)));
            compiler.glslcSetCompileString(version);
            compilerTable.put(version, compiler);
        }
        return compiler;
    }

    /**
     * Removes all macros
     */
    public void clearMacros() {
        stageMacroTable.clear();
    }

    /**
     * Gets the compile string, according to parameters, MINUS the filename and output parameters
     */
    private void glslcSetCompileString(Renderers version) {
        switch (compiler) {
            case SHADERC:
                compileString = GLSLC_COMPILE_COMMAND + " " + GLSLC_TARGET_COMMAND + version.toString() + " " + GLSLC_PREPROCESS_COMMAND + " " + GLSLC_OPTIMIZE_COMMAND;
                break;
            case GLSLANG:
                compileString = GLSLANG_COMPILE_COMMAND + " " + GLSLANG_TARGET_COMMAND + version.toString();
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private String getCompileSourceString(String sourceName, String output) {
        switch (compiler) {
            case SHADERC:
                return sourceName + " -o -";
            case GLSLANG:
                return sourceName + " -o " + output;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void setLights(RenderableScene glTF) {
        if (glTF == null) {
            return;
        }
        JSONNode[] lightNodes = glTF.getLightNodes();
        if (lightNodes != null) {
            // Add all different type of lights together
            int[] lights = glTF.getMaxPunctualLights();
            for (Type light : Type.values()) {
                if (lights[light.index] > 0) {
                    switch (light) {
                        case directional:
                        case point:
                        case spot:
                            addMacro(light.name().toUpperCase(), "1", MacroIntProperties.PUNCTUAL_LIGHTS.stages);
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    ErrorMessage.INVALID_VALUE.message + ": Not implemented for " + light);
                    }
                }
            }
        }
    }

    private void setTextureTransform(RenderableScene glTF) {
        if (glTF == null) {
            return;
        }
        GltfExtensions extensions = glTF.getRoot().getGltfExtensions();
        if (extensions != null) {
            if (extensions.getKHRTextureTransformCount() > 0) {
                addMacro(ExtensionTypes.KHR_texture_transform.name(), "1", MacroProperties.TEXTURE_TRANSFORM.stages);
            }
        }
    }

    private void setEnvironmentMap(RenderableScene scene) {
        if (scene == null) {
            return;
        }
        // Check for environmentmap settings
        KHREnvironmentMapReference envmapReference = scene.getEnvironmentExtension();
        ExtensionSetting[] settings = null;
        settings = envmapReference != null ? envmapReference.getSettings() : null;
        if (settings != null) {
            for (ExtensionSetting setting : settings) {
                addMacro(setting.getMacroName(), "1", MacroProperties.CUBEMAP.stages);
            }
        }
    }

    private boolean isTextureDisabled(Channel texture) {
        for (int i = 0; i < DISABLE_TEXTURES.length; i++) {
            if (DISABLE_TEXTURES[i] == texture && Settings.getInstance().getBoolean(PROPERTY[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the macros for the vertex attributes, texturechannels and extensions, call this once for each pipeline using this vertexinputstate
     * 
     * @param vertexInputState
     */
    public void setMacros(Attributes[] attributes, AlphaMode alphaMode, JSONMaterial material) {
        setTextureChannels(material.getTextureChannels(), Shader.Stage.VERTEX, Shader.Stage.FRAGMENT);
        setAttributes(attributes, Shader.Stage.VERTEX);
        addMacro(alphaMode.name(), "1", Stage.FRAGMENT);
        setCoat(material, Stage.FRAGMENT);
        setScatteredTransmission(material, Stage.FRAGMENT);
    }

    private void setScatteredTransmission(JSONMaterial material, Shader.Stage... stages) {
        if (material.getScatteredTransmissionFactor() != null) {
            if ((material.getTextureChannelsValue() & (Channel.SCATTERED_TRANSMISSION.value | Channel.SCATTERED_TRANSMISSION_COLOR.value)) == 0) {
                addMacro("SCATTEREDTRANSMIT", "1", stages);
            }
        }
    }

    /**
     * Compiler macros must match how the JSONPrimitive calculates the hash
     * 
     * @param material
     * @param stages
     */
    private void setCoat(JSONMaterial material, Shader.Stage... stages) {
        if (material.getClearcoatFactor() != null) {
            // Only set coat if no coat texturechannels are present.
            if ((material.getTextureChannelsValue() & (Channel.COAT_NORMAL.value | Channel.COAT_FACTOR.value | Channel.COAT_ROUGHNESS.value)) == 0) {
                addMacro("COAT", "1", stages);
            }
        }
    }

    private void setTextureChannels(Channel[] textureChannels, Shader.Stage... stages) {
        if (textureChannels != null) {
            for (Channel flag : textureChannels) {
                if (!isTextureDisabled(flag)) {
                    addMacro(flag.name(), "1", stages);
                } else {
                    Logger.d(getClass(), "Texture " + flag + " is disabled - not included in preprocessor macro");
                }
            }
        }
    }

    /**
     * Only set attributes that may be missing.
     * 
     */
    private void setAttributes(Attributes[] attributes, Shader.Stage... stages) {
        if (attributes != null) {
            for (Attributes a : attributes) {
                if (a == Attributes.COLOR_0) {
                    addMacro(Attributes.COLOR_0.name(), "1", stages);
                }
            }
        }
    }

    /**
     * Adds one or more macro define with int value, this will equal to compiler -D+key.macro+"="+value
     * Number of keys set will depend on number of values
     * 
     * @param key key and macro
     * @param value Array with values that will be set to stages, this controls number of macros set.
     * @param stages
     */
    public void addMacro(MacroIntProperties[] keys, int[] values, Shader.Stage... stages) {
        if (stages == null || stages.length == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "No shader stage specified");
        }
        if (keys == null || values == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid keys or values");
        }
        for (int i = 0; i < keys.length; i++) {
            addMacro(keys[i], values[i], stages);
        }
    }

    private void addMacro(MacroIntProperties key, int value, Shader.Stage... stages) {
        for (Shader.Stage stage : stages) {
            MacroSet macroSet = getMacroSet(stage.name());
            macroSet.put(key.getName(), Integer.toString(value));
        }
    }

    /**
     * Adds a macro and value to the specified stages
     * 
     * @param macro
     * @param value
     * @param stages
     */
    public void addMacro(String macro, String value, Shader.Stage... stages) {
        if (stages == null || stages.length == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "No stage specified");
        }
        for (Shader.Stage stage : stages) {
            MacroSet macroSet = getMacroSet(stage.name());
            macroSet.put(macro, value);
        }

    }

    private MacroSet getMacroSet(String stage) {
        MacroSet set = stageMacroTable.get(stage);
        if (set == null) {
            set = new MacroSet();
            stageMacroTable.put(stage, set);
        }
        return set;
    }

    /**
     * Sets the macros defined in MacroProperties
     * 
     * @param surface
     */
    public void setMacros(RenderableScene glTF, SurfaceFormat surface) {
        for (MacroProperties p : MacroProperties.values()) {
            switch (p) {
                case FB_FORMAT:
                    // Check surface format to know if shader should adjust before storing to FB
                    String fbFormat = surface.format.isSRGB() ? TransferFunction.SRGB.name() : TransferFunction.LINEAR.name();
                    addMacro(MacroProperties.FB_FORMAT.prefix + fbFormat, "1", p.stages);
                    break;
                case COLORSPACE:
                    addMacro(MacroProperties.COLORSPACE.prefix + surface.getTransferFunction().name(), "1", p.stages);
                    break;
                case TEXTURE_TRANSFORM:
                    setTextureTransform(glTF);
                    break;
                case CUBEMAP:
                    // TODO - How to handle multiple scenes?
                    if (glTF != null) {
                        setEnvironmentMap(glTF);
                    }
                    break;
                case MATRIX_COUNT:
                    // addMacro(p.name(), Integer.toString(glTF.getMaxNodeCount()), p.stages);
                    break;
                default:
                    Object val = Settings.getInstance().getPropertyObject(p.property);
                    if (val != null) {
                        if (val instanceof String) {
                            addMacro(p.prefix + val, "1", p.stages);
                        } else if (val instanceof Integer) {
                            int number = (int) val;
                            addMacro(p.getName(), Integer.toString(number), p.stages);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    }
            }
        }
        setLights(glTF);
    }

    public void setMacros(Macros... properties) {
        if (properties != null) {
            for (Macros p : properties) {
                Object val = Settings.getInstance().getPropertyObject(p.getProperty());
                if (val != null) {
                    if (val instanceof String) {
                        addMacro(p.getPrefix(), "1", p.getStages());
                    } else if (val instanceof Integer) {
                        int number = (int) val;
                        addMacro(p.getName(), Integer.toString(number), p.getStages());
                    } else {
                        throw new IllegalArgumentException();
                    }
                }

            }
        }

    }

    /**
     * Compiles the shader binary for the source (in GLSL) and stage, sourcename should NOT include file extension
     * (.glsl)
     * 
     * @param sourcePath Path to source, must end with path separator
     * @param sourceName Name of shader source to compile without file extension, will be used as the output
     * @param stage
     * @param destinationPath
     * @return
     * @throws IOException
     * @throws IllegalArgumentException If macros has not been set before calling this method.
     */
    public synchronized SpirvBinary compileStage(String sourcePath, String sourceName, Stage stage, String destinationPath, String outputHash) throws IOException {
        String sourceInput = sourcePath + sourceName + "." + stage.stage;
        buffer.clear();
        String resourceDirectory;
        try {
            resourceDirectory = GLSLCompiler.getCompileFolderPath(sourcePath);
            boolean isJar = false;
            // if (FileUtils.getInstance().isJAR(sourceInput, module)) {
            if (isJar) {
                String filePath;
                filePath = FileUtils.getInstance().getFileSystemPath(resourceDirectory, "vargj2se").toString();
                filePath = FileUtils.getInstance().replaceDirectorySeparator(filePath);
                Logger.d(getClass(), "Jar path");
                ClassLoader loader = getClass().getClassLoader();
                String fullPath = loader.getResource(filePath).toString();
                // Remove sourceInput folders and trailing /
                fullPath = fullPath.substring(0, fullPath.length() - sourceInput.length() - 1);
                // Remove 'jar:file:/" and get folder
                fullPath = FileUtils.getInstance().getFolder(fullPath.substring(4 + 6)) + "/";
                // Linux filepath fix
                if (fullPath.indexOf(":") < 0) {
                    // Start path with /
                    fullPath = "/" + fullPath;
                }
                Logger.d(getClass(), "Fullpath=" + fullPath);
                filePath = fullPath;
            }
        } catch (URISyntaxException | IOException e) {
            throw new IOException(e);
        }
        MacroSet stageMacros = getMacroSet(stage.name());
        String macroString = stageMacros.getMacroString();
        String output = destinationPath + sourceName + stage.name + outputHash + SPIRV_EXTENSION;
        compile(new String[] { resourceDirectory, compileString + " " + macroString +
                getCompileSourceString(sourceInput, resourceDirectory + output) }, buffer);
        switch (compiler) {
            case SHADERC:
                return saveAsSPIRV(buffer, resourceDirectory + output);
            default:
                return null;
        }
    }

    public static String getCompileFolderPath(String sourcePath) throws URISyntaxException, IOException {
        // This is a fix to make resource folder lookup work - TODO make into setting/property that can
        // be configured.
        String module = "vargj2se";
        return FileUtils.getInstance().getResourcePath(sourcePath, SHADER_JAVA_TARGET_DIRECTORY, SHADER_SOURCE_DIRECTORY, module);

    }

    private SpirvBinary saveAsSPIRV(ByteBuffer destination, String output) throws IOException {
        int offset = SpirvBinary.getSPIRVMagicOffset(destination);
        if (offset < 0) {
            String str = StandardCharsets.ISO_8859_1.decode(destination).toString();
            Logger.d(getClass(), str);
            throw new IllegalArgumentException("Could not find Spirv magic");
        }
        SpirvStream stream = SpirvBinary.getStream(destination);
        destination.limit(stream.getOffset() * 4);
        SpirvBinary spirv = new SpirvBinary(destination, stream.getOffset());
        // String outPath = filePath.replace(TARGET_CLASSES, DESTINATION_RESOURCES);
        StreamUtils.writeToStream(output, spirv.getSpirv());
        Logger.d(getClass(), "Written spirv to: " + output);
        return spirv;
    }

    /**
     * Starts a new command process.
     * Executes the command and returns the process - only call this if a process has not been started before.
     * 
     * @param commands
     * @param destination The buffer to store output from executed command, data is stored at beginning of buffer
     */
    private void compile(String[] commands, ByteBuffer destination) {
        Platform.getInstance().executeCommands(commands, destination);
        Logger.d(getClass(), "Captured " + destination.position() + " output bytes.");
        destination.position(0);
    }

}

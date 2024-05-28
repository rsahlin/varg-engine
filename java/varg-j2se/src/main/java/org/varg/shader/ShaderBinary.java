
package org.varg.shader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Logger;
import org.gltfio.lib.StreamUtils;
import org.varg.renderer.Renderers;
import org.varg.shader.Shader.Stage;
import org.varg.shader.Shader.Subtype;
import org.varg.vulkan.GLSLCompiler;

/**
 * Holds shader source (binary or bytecode) and data related to the source for a
 * shader This is either pre-compiled binary or byte-code (SPIR-V or similar)
 *
 */
public abstract class ShaderBinary {

    public static final String PROGRAM_DIRECTORY = "assets/";
    public static final String COMPILED_DIRECTORY = "compiled/";
    public static final String FILE_SUFFIX_SEPARATOR = ".";

    /**
     * Use for shader source names that are versioned 450
     */
    public static final String V450 = "v450";

    private String sourceName;
    private String path;
    private Stage stage;
    protected ByteBuffer data;

    protected ShaderBinary() {
    }

    /**
     * Creates a shadersource from sourcename and path
     * 
     * 
     * @param path
     * @param sourceName
     * @param stage
     */
    public ShaderBinary(String path, String sourceName, Stage stage) {
        set(path, sourceName, stage);
    }

    /**
     * Sets the path, sourceName and stage
     * 
     * @param setPath
     * @param setSourceName
     * @param setStage
     */
    protected void set(String setPath, String setSourceName, Stage setStage) {
        if (setPath == null || setStage == null) {
            throw new IllegalArgumentException("null parameter");
        }
        this.path = setPath;
        this.stage = setStage;
        this.sourceName = setSourceName;
    }

    /**
     * Returns the bytebuffer containing shader binary data.
     * 
     * @return The shader code
     */
    public ByteBuffer getBuffer() {
        data.position(0);
        return data;
    }

    /**
     * Returns the sourcename of the asset to be loaded.
     * The default behavior is to have one uber shader for each stage.
     * 
     * @param shaderType
     * @return The source name, without path.
     */
    public String getSourceName(Subtype shaderType) {
        return sourceName;
    }

    /**
     * Returns the path to the source, path ends with path separator
     * 
     * @param shaderType
     * @return The path to the source, ending with path separator
     */
    public String getSourcePath(Subtype shaderType) {
        return path;
    }

    /**
     * Returns the stage
     * 
     * @return
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Loads the source data for this shader binary, this shall call {@link #getSourceName()} to fetch the asset name.
     * After this call returns the shader binary can be fetched by calling {@link #getBuffer()}
     * It is invalid to call this method more than once or if {@link #compileShader()} already has been called.
     * 
     * @param shaderInfo
     * @throws IOException If there is an error loading the binary
     * @throws IllegalArgumentException If a binary already has been loaded or compiled.
     */
    public void loadShader(ShaderCreateInfo shaderInfo, String hash) throws IOException {
        internalLoadShader(shaderInfo, hash);
    }

    /**
     * Loads the source shader and compiles into shader binary, this shall call {@link #getSourceName()} to get
     * the name of the asset.
     * Compiler macros shall be setup before calling this method.
     * After this call returns the shader binary can be fetched by calling {@link #getBuffer()}
     * 
     * @param shaderInfo
     * @throws IOException
     * @throws IllegalArgumentException If a binary already has been loaded or compiled.
     */
    public abstract void compileShader(ShaderCreateInfo shaderInfo, String outputHash) throws IOException;

    /**
     * This is used to be able to load different shader sources depending on shading
     * language version.
     * Returns the common denominator for shader versions, ie 450 for Vulkan 10, 11
     * 
     * @param version Highest shading language version that is supported
     * @return Empty string "", or shader version to append to source name if
     * different shader source shall be used for a specific shader version.
     */
    public static String getSourceNameVersion(Renderers version) {
        switch (version) {
            case VULKAN10:
            case VULKAN11:
            case VULKAN12:
            case VULKAN13:
                return ShaderBinary.V450 + FileUtils.DIRECTORY_SEPARATOR;
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + version);
        }
    }

    /**
     * Internal method to load shader based on shader info
     * 
     * @param shaderInfo
     * @throws IOException
     */
    protected void internalLoadShader(ShaderCreateInfo shaderInfo, String hash) throws IOException {
        if (data != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Shader binary already present");
        }
        try {
            String binaryName = getSourcePath(shaderInfo.shaderType) + COMPILED_DIRECTORY + getSourceName(shaderInfo.shaderType) + stage.name + hash + GLSLCompiler.SPIRV_EXTENSION;
            String folder = GLSLCompiler.getCompileFolderPath(path);
            Logger.d(getClass(), "Loading shader binary: " + binaryName + ", from folder: " + folder);
            int length = (int) FileUtils.getInstance().getFileSize(folder, binaryName);
            if (length == -1) {
                throw new IllegalArgumentException("Could not find " + binaryName + " Shader sources not compiled?");
            }
            Logger.d(getClass(), "Length is: " + length);
            if (length == 0) {
                InputStream is = getClass().getResourceAsStream(FileUtils.getInstance().addStartingDirectorySeparator(binaryName));
                if (is == null) {
                    throw new IllegalArgumentException(binaryName);
                }
                byte[] readBuffer = StreamUtils.readFromStream(is);
                data = Buffers.createByteBuffer(readBuffer);
            } else {
                data = Buffers.createByteBuffer(length);
                int read = StreamUtils.readFromName(folder + binaryName, data);
                if (read != length) {
                    throw new IllegalArgumentException("Could not read all spirv, read " + read + ", length " + length);
                }
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

}

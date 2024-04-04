
package org.varg.vulkan.pipeline;

import java.util.Arrays;

import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.vertex.VertexInputAttributeDescription;
import org.varg.vulkan.vertex.VertexInputBindingDescription;

/**
 * Holds VertexInputBindingDescription and VertexInputAttributeDescription for pipeline vertex input state layout,
 * plus some drawmode and shader flags that can be used to determine if usage is new or same are already existing.
 * 
 * The same PipelineVertexInputState shall be used for pipelines that share the same drawmode and attributes.
 * 
 * This is used when creating the pipeline
 *
 */
public class PipelineVertexInputState {

    final String[] bindingNames;
    final VertexInputBindingDescription[] inputBindings;
    final VertexInputAttributeDescription[] vertexInputs;
    final Channel[] textureChannels;
    private final int hashCode;

    /**
     * Inputbindings and vertexinputs MUST be in sorted order
     * 
     * @param inputBindings
     * @param vertexInputs
     * @param textureChannels
     */
    public PipelineVertexInputState(Attributes[] attributes, VertexInputBindingDescription[] inputBindings,
            VertexInputAttributeDescription[] vertexInputs, Channel[] textureChannels) {
        if (inputBindings == null || vertexInputs == null || textureChannels == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.inputBindings = inputBindings;
        this.vertexInputs = vertexInputs;
        this.textureChannels = textureChannels;
        this.hashCode = hashCode();
        bindingNames = new String[attributes.length];
        for (int i = 0; i < bindingNames.length; i++) {
            if (attributes[i] != null) {
                bindingNames[i] = attributes[i].name();
            }
        }
    }

    /**
     * Returns the texture channels that are used
     * 
     * @return
     */
    public Channel[] getTextureChannels() {
        return textureChannels;
    }

    /**
     * Returns the vertex inputs
     * 
     * @return
     */
    public VertexInputAttributeDescription[] getVertexInputs() {
        return vertexInputs;
    }

    /**
     * Returns true if a vertex input is defined using the specified name
     * 
     * @param name
     * @return
     */
    public boolean usesInput(String name) {
        for (int i = 0; i < vertexInputs.length; i++) {
            if (vertexInputs[i] != null && name.contentEquals(bindingNames[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the pipeline vertex inputstate createinfo to be used when creating a graphics pipeline.
     * 
     * @return
     */
    public PipelineVertexInputStateCreateInfo getPipelineVertexInputStateCreateInfo() {
        return new PipelineVertexInputStateCreateInfo(inputBindings, vertexInputs);
    }

    /**
     * Returns the hashcode calculated in constructor - since all fields are final this works
     * 
     * @return
     */
    public int getHash() {
        return hashCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(inputBindings);
        result = prime * result + BitFlags.getFlagsValue(textureChannels);
        result = prime * result + Arrays.hashCode(vertexInputs);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }

    @Override
    public String toString() {
        return "Texture channels: " + BitFlags.toString(textureChannels) + " : " + Arrays.toString(inputBindings);
    }

}


package org.varg.vulkan;

import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.pipeline.PipelineVertexInputState;

/**
 * Vertex inputstate, drawmode and extensions for primitive
 *
 */
public class PrimitiveVertexInputState {

    private final PipelineVertexInputState inputState;
    private final DrawMode drawMode;
    private final int hash;

    public PrimitiveVertexInputState(PipelineVertexInputState inputState, DrawMode drawMode) {
        if (inputState == null || drawMode == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.inputState = inputState;
        this.drawMode = drawMode;
        this.hash = hashCode();
    }

    /**
     * Returns the pipeline vertex inputstate
     * 
     * @return
     */
    public PipelineVertexInputState getInputState() {
        return inputState;
    }

    /**
     * Returns the hashcode generated when this class was created
     * 
     * @return
     */
    public int getHash() {
        return hash;
    }

    /**
     * Returns the drawmode
     * 
     * @return
     */
    public DrawMode getDrawMode() {
        return drawMode;
    }

    @Override
    public String toString() {
        return drawMode + " : " + inputState.toString() + " (" + getHash() + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + drawMode.hashCode();
        result = prime * result + inputState.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        PrimitiveVertexInputState other = (PrimitiveVertexInputState) obj;
        if (drawMode != other.drawMode) {
            return false;
        } else if (inputState == null) {
            if (other.inputState != null) {
                return false;
            }
        } else if (!inputState.equals(other.inputState)) {
            return false;
        }
        return true;
    }

}


package org.varg.renderer;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONCamera;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Matrix;
import org.gltfio.lib.Matrix.MatrixStack;
import org.gltfio.lib.MatrixUtils;

/**
 * Encapsulation of the mvp matrices used when rendering, also handles matrix stack used when traversing nodetree
 *
 */
public class MVPMatrices {

    public interface StoreModelMatrixCallback {
        void storeMatrix(JSONNode<JSONMesh<JSONPrimitive>> node, float[] matrix);
    }

    /**
     * 3 matrices, one each for mode, view and projection
     */
    public static final int MATRIX_COUNT = 3;

    private MatrixStack modelStack = new MatrixStack(100);
    private MatrixStack viewStack = new MatrixStack(5);
    private MatrixStack projectionStack = new MatrixStack(5);

    private final float[][] matrices = new float[Matrices.values().length][];

    /**
     * Temp matrix - not threadsafe
     */
    private float[] tempMatrix = MatrixUtils.createMatrix();

    public enum Matrices {
        MODEL(0),
        VIEW(1),
        PROJECTION(2);

        public final int index;
        public static final String NAME = "uModelMatrix";

        Matrices(int i) {
            index = i;
        }

    }

    /**
     * Min number of stack elements to supports, this is the depth of the tree
     * TODO - this value shall be dynamic
     */
    public static final int MIN_STACKELEMENTS = 100;

    public MVPMatrices() {
        for (int i = 0; i < matrices.length; i++) {
            matrices[i] = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);
        }
    }

    /**
     * Pushes one or more matrices on stack, if parameter is null all matrices are pushed.
     * 
     * @param m One or more matrices to push, if null all matrices are pushed.
     */
    public void push(Matrices... m) {
        if (m == null || m.length == 0) {
            m = Matrices.values();
        }
        for (Matrices mat : m) {
            switch (mat) {
                case MODEL:
                    modelStack.push(matrices[Matrices.MODEL.index], 0);
                    break;
                case VIEW:
                    viewStack.push(matrices[Matrices.VIEW.index], 0);
                    break;
                case PROJECTION:
                    projectionStack.push(matrices[Matrices.PROJECTION.index], 0);
                    break;
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + mat);
            }
        }
    }

    /**
     * Pops one or more matrices on stack, if parameter is null all matrices are popped.
     * 
     * @param m One or more matrices to pop, if null all matrices are popped.
     */
    public void pop(Matrices... m) {
        if (m == null || m.length == 0) {
            m = Matrices.values();
        }
        for (Matrices mat : m) {
            switch (mat) {
                case MODEL:
                    modelStack.pop(matrices[Matrices.MODEL.index], 0);
                    break;
                case VIEW:
                    viewStack.pop(matrices[Matrices.VIEW.index], 0);
                    break;
                case PROJECTION:
                    projectionStack.pop(matrices[Matrices.PROJECTION.index], 0);
                    break;
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + mat);
            }
        }
    }

    @Override
    public String toString() {
        return "\nArray matrices:\n" +
                MatrixUtils.toString(matrices[0], 0) +
                MatrixUtils.toString(matrices[1], 0) +
                MatrixUtils.toString(matrices[2], 0);
    }

    /**
     * Sets the view and projection matrix according to the camera
     * 
     * @param camera
     * @param premultiplyPerspective Matrix to premultiply perspective with, may be null
     * @param viewMatrix
     * @param projectionMatrix
     */
    public void setViewProjectionMatrices(@NonNull JSONCamera camera, float[] premultiplyPerspective) {
        MatrixUtils.copy(camera.updateViewMatrix(), 0, matrices[Matrices.VIEW.index], 0);
        if (premultiplyPerspective != null) {
            camera.concatProjectionMatrix(premultiplyPerspective, matrices[Matrices.PROJECTION.index], 0);
        } else {
            camera.getProjectionMatrix(matrices[Matrices.PROJECTION.index], 0);
        }
    }

    /**
     * Sets the specified matrix
     * 
     * @param matrix
     * @param matrixValues
     */
    public void setMatrix(Matrices matrix, float[] matrixValues) {
        System.arraycopy(matrixValues, 0, matrices[matrix.index], 0, Matrix.MATRIX_ELEMENTS);
    }

    /**
     * Multiplies matrix1 with matrix2 and stores in destination
     * 
     * @param matrix1
     * @param matrix2
     * @param destination
     */
    public void concatMatrix(Matrices matrix1, float[] matrix2, float[] destination) {
        MatrixUtils.mul4(matrices[matrix1.index], matrix2, destination);
    }

    /**
     * Multiply this model matrix with the nodeMatrix and store in model matrix.
     * Use this when traversing nodetree
     * 
     * @param nodeMatrix
     */
    public void concatModelMatrix(float[] nodeMatrix) {
        MatrixUtils.mul4(matrices[Matrices.MODEL.index], nodeMatrix, tempMatrix);
        System.arraycopy(tempMatrix, 0, matrices[Matrices.MODEL.index], 0, Matrix.MATRIX_ELEMENTS);
    }

    /**
     * Returns the matrix data
     * 
     * @param matrix
     * @return
     */
    public final float[] getMatrix(Matrices matrix) {
        return matrices[matrix.index];
    }

    public final float[][] getMatrices() {
        return matrices;
    }

    /**
     * 
     * @param nodes
     */
    public void concatModelMatrices(JSONNode<JSONMesh<JSONPrimitive>>[] nodes, StoreModelMatrixCallback callback) {
        for (int i = 0; i < nodes.length; i++) {
            concatModelMatrix(nodes[i], callback);
        }

    }

    private void concatModelMatrix(JSONNode node, StoreModelMatrixCallback callback) {
        if (node != null && (node.getChildCount() > 0 || node.getMeshIndex() >= 0)) {
            push(Matrices.MODEL);
            concatModelMatrix(node.getTransform().updateMatrix());
            if (callback != null) {
                callback.storeMatrix(node, matrices[Matrices.MODEL.index]);
            }
            concatModelMatrices(node.getChildNodes(), callback);
            pop(Matrices.MODEL);
        }
    }

}

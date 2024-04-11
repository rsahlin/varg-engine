package org.varg.renderer;

import java.util.ArrayList;

import org.gltfio.data.FlattenedScene.PrimitiveSorter;
import org.gltfio.data.FlattenedScene.PrimitiveSorterMap;
import org.gltfio.data.VertexBuffer.VertexBufferBundle;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.lib.ErrorMessage;

/**
 * A number of drawcalls that will be issued together, using the already bound uniforms and textures.
 * Drawcall instanceindex will be unique for each rendered primitive
 */
public abstract class DrawCallBundle<T extends AbstractDrawCalls> {

    /**
     * Keep track of draw instances so that material and matrix can be indexed for multiple drawcalls.
     * In shader the instanceindex is used to fetch data from primitiveuniform array. This is one buffer
     * for drawcalls within one bundle
     */
    protected int firstInstance;

    /**
     * Array holding instance specific data
     */
    private final int[] primitiveUniformArray;
    /**
     * Number of ints for each instance - this is tied to the primitiveuniformarray and shader code.
     */
    private final int instanceDataSize;

    /**
     * Drawcalls
     */
    protected ArrayList<T> drawCalls = new ArrayList<T>();

    protected abstract T createDrawCalls(PrimitiveSorter primitives);

    /**
     * Creates the device memory for drawcall commands.
     */
    protected abstract void createMemory();

    /**
     * Frees the device memory for drawcall commands
     */
    public abstract void freeMemory();

    public abstract T[] getAllDrawCalls();

    /**
     * 
     * @param instanceCount Max number of instances that can be issued.
     * @param instanceDataSize
     */
    public DrawCallBundle(int instanceCount, int instanceDataSize) {
        this.instanceDataSize = instanceDataSize;
        primitiveUniformArray = new int[instanceCount * instanceDataSize];
    }

    /**
     * Creates the drawcalls
     * 
     * @param primitivesByPipeline
     * @param vertexBuffers
     */
    public void createDrawCalls(PrimitiveSorterMap primitivesByPipeline, VertexBufferBundle vertexBuffers) {
        for (PrimitiveSorter primitives : primitivesByPipeline.sort()) {
            drawCalls.add(createDrawCalls(primitives, vertexBuffers));
        }
        createMemory();
    }

    /**
     * Returns the array for uniform per primmitive data
     * 
     * @return
     */
    public int[] getPrimitiveUniformArray() {
        return primitiveUniformArray;
    }

    /**
     * Create the drawcalls for the primitives using the vertexoffsets (based on streamed/flattened vertex data)
     * instance index will increased for each added draw, it keeps track of material and matrix index.
     * When issuing drawcalls the instance must increase to match
     * 
     * @param primitiveList
     * @param vertexBundle
     */
    private T createDrawCalls(PrimitiveSorter primitiveList, VertexBufferBundle vertexBundle) {
        int[] vertexOffsets = vertexBundle.getVertexOffsets(primitiveList.attributeHash);
        int instanceCounter = firstInstance;
        T dc = createDrawCalls(primitiveList);
        if (primitiveList.hasIndexedMode()) {
            ArrayList<JSONPrimitive>[] indexedPrimitiveArray = primitiveList.getIndexedPrimitives();
            for (IndexType type : IndexType.values()) {
                int[] matrixIndexes = primitiveList.getIndexedMatrixIndexes(type);
                int primitiveIndex = 0;
                int[] indexOffsets = vertexBundle.getIndexOffsets(primitiveList.attributeHash, type);
                for (JSONPrimitive primitive : indexedPrimitiveArray[type.index]) {
                    int indexCount = primitive.getDrawCount();
                    primitiveUniformArray[firstInstance * instanceDataSize] = primitive.getMaterialIndex();
                    primitiveUniformArray[firstInstance * instanceDataSize + 1] = matrixIndexes[primitiveIndex++];
                    dc.addIndexedIndirectDraw(type, indexCount, 1, indexOffsets[primitive.getStreamIndicesIndex()], vertexOffsets[primitive.getStreamVertexIndex()], firstInstance);
                    firstInstance++;
                }
            }
        }
        int primitiveIndex = 0;
        int[] matrixIndexes = primitiveList.getArrayMatrixIndexes();
        for (JSONPrimitive primitive : primitiveList.getArrayPrimitives()) {
            int count = primitive.getDrawCount();
            primitiveUniformArray[firstInstance * instanceDataSize] = primitive.getMaterialIndex();
            primitiveUniformArray[firstInstance * instanceDataSize + 1] = matrixIndexes[primitiveIndex++];
            dc.addIndirectDraw(count, 1, vertexOffsets[primitive.getStreamVertexIndex()], firstInstance);
            firstInstance++;
        }
        if ((firstInstance - instanceCounter) != primitiveList.getPrimitiveCount()) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Added instances does not match");
        }
        return dc;
    }

}

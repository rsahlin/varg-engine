
package org.varg.shader.voxels;

import org.gltfio.lib.Matrix;
import org.gltfio.lib.MatrixUtils;
import org.gltfio.lib.Quaternion;
import org.ktximageio.ktx.HalfFloatImageBuffer.FP16Convert;
import org.varg.shader.MeshShader.MeshDescriptorSetTarget;
import org.varg.uniform.DescriptorBuffers;

/**
 * Encapsulation of voxel sprite rendered using mesh shader - the data in this class MUST match that
 * of the mesh shader layout
 * 
 */
public class VoxelSprites {

    public enum DataSets {
        /**
         * Offsets and palette indexes - readonly
         */
        VOXELDATA(),
        /**
         * Sprite positions, matrix and cube vertex positions.
         */
        SPRITEDATA();
    }

    protected final int maxSpriteCount;
    public final int spritePositionOffset;
    public final int spriteMatrixOffset;

    public VoxelSprites(int maxSpriteCount, float[] palette, float[] offsets) {
        this.maxSpriteCount = maxSpriteCount;
        this.fp16Palette = new short[palette.length];
        FP16Convert paletteConvert = new FP16Convert(this.fp16Palette);
        paletteConvert.convert(palette);

        this.offsets = offsets;

        this.fp16Positions = new short[4 * maxSpriteCount];
        positionConvert = new FP16Convert(this.fp16Positions);
        spritePositions = new float[4 * maxSpriteCount];

        spritePositionOffset = 0;
        spriteMatrixOffset = this.fp16Positions.length; // Offset is in number of fp16 (short)

        fp16Matrices = new short[16 * maxSpriteCount];
        matrices = new float[fp16Matrices.length];
        matrixConvert = new FP16Convert(fp16Matrices);
    }

    /**
     * VoxelData
     */
    private float[] offsets;
    private short[] fp16Offsets;
    private short[] fp16Palette;

    /**
     * SpriteData
     */
    private short[] fp16Positions;
    private short[] fp16Matrices;

    protected float[] matrices;
    protected float[] spritePositions;
    protected final float[] quat = new float[4];
    private final FP16Convert matrixConvert;
    private final FP16Convert positionConvert;

    /**
     * Returns the sprite offsets as half floats
     * 
     * @return
     */
    public short[] getShortOffsets() {
        if (fp16Offsets == null) {
            this.fp16Offsets = new short[offsets.length];
            FP16Convert offsetConvert = new FP16Convert(this.fp16Offsets);
            offsetConvert.convert(offsets);
        }
        return fp16Offsets;
    }

    /**
     * Returns the offsets;
     * 
     * @return
     */
    public float[] getOffsets() {
        return offsets;
    }

    /**
     * Returns the palette as half floats
     * 
     * @return
     */
    public short[] getPalette() {
        return fp16Palette;
    }

    /**
     * Sets the sprite y axis rotation in the matrix
     * 
     * @param rotation
     */
    public void setYAxisRotation(float rotation, int spriteIndex) {
        Quaternion.setYAxisRotation(rotation, quat);
        MatrixUtils.setQuaternionRotation(quat, matrices, spriteIndex * Matrix.MATRIX_ELEMENTS);
    }

    /**
     * Converts the current matrices and returns as fp16
     * 
     * @return
     */
    public short[] convertMatrices() {
        matrixConvert.reset();
        matrixConvert.convert(matrices);
        return fp16Matrices;
    }

    /**
     * Converts the current float sprite values and returns as fp16
     * 
     * @return
     */
    public short[] convertPositions() {
        positionConvert.reset();
        positionConvert.convert(spritePositions);
        return positionConvert.result;
    }

    /**
     * Stores fp16 sprite positions in destination buffer - does not convert float sprite positions
     * 
     * @param buffers
     */
    public void storePositions(DescriptorBuffers<?> buffers) {
        buffers.storeShortData(MeshDescriptorSetTarget.SPRITE, spritePositionOffset, fp16Positions);
    }

    /**
     * Stores the sprite matrices - does not convert float matrices
     * 
     * @param buffers
     */
    public void storeMatrices(DescriptorBuffers<?> buffers) {
        buffers.storeShortData(MeshDescriptorSetTarget.SPRITE, spriteMatrixOffset, fp16Matrices);
    }

    /**
     * Returns the current matrix values as fp16 - this does NOT convert the current matrix.
     * 
     * @return
     */
    public short[] getMatrix() {
        return fp16Matrices;
    }

    /**
     * Returns the sprite float array, if changes are made be sure to call {@link #convertPositions()} before storing
     * data
     * 
     * @return
     */
    public float[] getSpritePositionsFloat() {
        return spritePositions;
    }

}

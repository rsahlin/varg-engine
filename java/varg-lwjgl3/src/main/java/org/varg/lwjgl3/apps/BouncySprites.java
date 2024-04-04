
package org.varg.lwjgl3.apps;

import java.util.Random;

import org.gltfio.gltf2.MinMax;
import org.gltfio.lib.MatrixUtils;
import org.gltfio.lib.Transform;
import org.varg.shader.voxels.PixelsToVoxels.VoxelData;
import org.varg.shader.voxels.VoxelSprites;
import org.varg.uniform.DescriptorBuffers;

/**
 *
 */
public class BouncySprites extends VoxelSprites {

    private float[] rotation;
    private float[][] moveVector;
    private float[] width;
    private float[] height;
    private Random random = new Random(System.currentTimeMillis());
    private MinMax bounds;
    private float[] boundsDelta = new float[3];
    private float floorY;

    /**
     * @param maxSpriteCount
     * @param palette
     * @param offsets
     */
    public BouncySprites(int maxSpriteCount, float[] palette, VoxelData voxels, MinMax bounds) {
        super(maxSpriteCount, palette, voxels.getPositionsAsFloatArray());
        this.bounds = bounds;
        floorY = bounds.getMinY();
        bounds.getMaxDelta(boundsDelta);
        rotation = new float[maxSpriteCount];
        moveVector = new float[maxSpriteCount][3];
        width = new float[maxSpriteCount];
        height = new float[maxSpriteCount];
        width[0] = voxels.width;
        height[0] = voxels.height;
        initPositions();
        initMovement();
    }

    private void initPositions() {
        int index = 0;
        for (int i = 0; i < maxSpriteCount; i++) {
            spritePositions[index++] = 0; // boundsDelta[0] * random.nextFloat();
            spritePositions[index++] = 0; // (boundsDelta[1] / 2) * random.nextFloat() + boundsDelta[1] / 2;
            spritePositions[index++] = -2;
            spritePositions[index++] = 0;
        }
    }

    private void initMovement() {
        for (int i = 0; i < maxSpriteCount; i++) {
            moveVector[i][0] = (random.nextFloat() - 0.5f) * 3;
        }
    }

    /**
     * Call this method once per frame to process sprites and update to buffers as needed
     * 
     * @param frameDelta
     */
    public void processAndStoreData(float frameDelta, Transform sceneTransform, DescriptorBuffers<?> buffers) {
        updateSprites(frameDelta, sceneTransform);
        storePositions(buffers);
        storeMatrices(buffers);
    }

    private void updateSprites(float frameDelta, Transform sceneTransform) {
        for (int i = 0; i < maxSpriteCount; i++) {
            rotation[i] += 0.2f * frameDelta;
            // setYAxisRotation(rotation[i], i);
            setYAxisRotation(0, i);
            move(frameDelta, i);
        }
        // applySceneTransform(sceneTransform);
        convertMatrices();
        convertPositions();
    }

    private void applySceneTransform(Transform sceneTransform) {
        float[] sceneMatrix = sceneTransform.getMatrix();
        float[] matrixCopy = new float[16];
        for (int i = 0; i < maxSpriteCount; i++) {
            MatrixUtils.mul4(matrices, i * 16, sceneMatrix, 0, matrixCopy, 0);
            System.arraycopy(matrixCopy, 0, matrices, i * 16, 16);
        }
    }

    private void moveSprite(float frameDelta, int spriteIndex) {
        moveVector[spriteIndex][1] -= 8f * frameDelta;
        int index = spriteIndex << 2;
        spritePositions[index++] += moveVector[spriteIndex][0] * frameDelta;
        spritePositions[index++] += moveVector[spriteIndex][1] * frameDelta;
        spritePositions[index++] += moveVector[spriteIndex][2] * frameDelta;
    }

    private void move(float frameDelta, int spriteIndex) {
        moveSprite(frameDelta, spriteIndex);
        int index = spriteIndex << 2;
        // Check floor
        float ymin = height[spriteIndex] / 2 + floorY;
        if (spritePositions[index + 1] <= ymin) {
            spritePositions[index + 1] = ymin - (spritePositions[index + 1] - ymin);
            // moveVector[spriteIndex][1] = -moveVector[spriteIndex][1];
            moveVector[spriteIndex][1] = 0;
            moveVector[spriteIndex][0] = 0;
        }

        // Check left wall
        float xmin = -10 + width[spriteIndex] / 2;
        if (spritePositions[index] <= xmin) {
            spritePositions[index] = xmin - (spritePositions[spriteIndex] - xmin);
            moveVector[spriteIndex][0] = -moveVector[spriteIndex][0];
        }

        // Check right wall
        float xmax = 10 - width[spriteIndex] / 2;
        if (spritePositions[index] >= xmax) {
            spritePositions[index] = xmax - (xmax - spritePositions[spriteIndex]);
            moveVector[spriteIndex][0] = -moveVector[spriteIndex][0];
        }
    }

}

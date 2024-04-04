package org.varg.scene;

import java.awt.event.KeyEvent;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONCamera;
import org.gltfio.gltf2.MinMax;
import org.gltfio.lib.Axis;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Key;
import org.gltfio.lib.KeyListener;
import org.gltfio.lib.Logger;
import org.gltfio.lib.PointerListener;
import org.gltfio.lib.Quaternion;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.structs.Extent2D;

public class SceneControl implements PointerListener, KeyListener {

    public enum MODE {
        CAMERA(0),
        SCENE(1);

        public final int index;

        MODE(int setIndex) {
            index = setIndex;
        }
    }

    public enum DRAGACTION {
        TRANSLATE(0),
        ROTATE(1),
        SCALE(2);

        public final int index;

        DRAGACTION(int setIndex) {
            index = setIndex;
        }
    }

    public static class Rotation {
        private float[] xRotation = new float[] { 0, 0, 0, 1 };
        private float[] yRotation = new float[] { 0, 0, 0, 1 };
        private float[] zRotation = new float[] { 0, 0, 0, 1 };
        private float[] deltaRotation = new float[] { 0, 0, 0, 1 };
        private float[] currentRotation = new float[] { 0, 0, 0, 1 };

        /**
         * Clears all rotational values, x, y and z axis.
         */
        public void clear() {
            Quaternion.clear(xRotation);
            Quaternion.clear(yRotation);
            Quaternion.clear(zRotation);
        }

    }

    JSONCamera camera;
    VulkanRenderableScene scene;
    float cameraTranslateScale = 1;
    private Extent2D screenDimension;
    private MinMax bounds;
    private boolean mouseDown = false;

    private Rotation[] rotation = new Rotation[] { new Rotation(), new Rotation() };
    private float[] previousPos = null;
    private float[] dragDelta = new float[2];
    private float[] translate = new float[3];
    private float[] rotateInputFactors = new float[] { 1, 1 };

    private Axis axis = Axis.XY;
    private DRAGACTION action = DRAGACTION.ROTATE;
    private MODE currentMode = MODE.SCENE;

    /**
     * @param camera
     * @param screenSize
     */
    protected SceneControl(JSONCamera camera, VulkanRenderableScene scene, @NonNull Extent2D screenSize) {
        setCamera(camera);
        setScene(scene);
        screenDimension = screenSize;
        bounds = scene.calculateBounds();
    }

    /**
     * Sets the camera
     * 
     * @param camera
     * @throws IllegalArgumentException If camera is null
     */
    public void setCamera(JSONCamera camera) {
        if (camera == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Camera is null");
        }
        this.camera = camera;
    }

    /**
     * Sets the scene
     * 
     * @param scene
     */
    void setScene(VulkanRenderableScene scene) {
        this.scene = scene;
    }

    /**
     * Returns the current scene, or null if asset does not define a scene
     * 
     * @return
     */
    public VulkanRenderableScene getCurrentScene() {
        return scene;
    }

    /**
     * Returns the current camera
     * 
     * @return
     */
    public JSONCamera getCamera() {
        return camera;
    }

    private void setDragAction(DRAGACTION dragAction) {
        action = dragAction;
    }

    private void setMode(MODE mode) {
        this.currentMode = mode;
        switch (currentMode) {
            case CAMERA:
                rotateInputFactors[1] = 0;
                rotateInputFactors[0] = 1;
                break;
            case SCENE:
                rotateInputFactors[0] = 1;
                rotateInputFactors[1] = 1;
                break;
            default:
                throw new IllegalArgumentException(currentMode.name());
        }
    }

    /**
     * Sets the current axis of control
     * 
     * @param axis
     */
    public void setAxis(Axis axis) {
        this.axis = axis;
        Logger.d(getClass(), "Set axis to " + axis);
    }

    /**
     * Call this periodically to continue rotation after user has stopped dragging.
     */
    public void autoRotate() {
        if (!mouseDown) {
            Quaternion.mul(rotation[currentMode.index].deltaRotation, rotation[currentMode.index].currentRotation,
                    rotation[currentMode.index].currentRotation);
            updateRotation(currentMode);
        }
    }

    private void clearTranslation() {
        translate[0] = 0;
        translate[1] = 0;
        translate[2] = 0;
    }

    /**
     * Clears the rotation for the current selected mode
     */
    public void clearRotation(MODE mode) {
        rotation[mode.index].clear();
    }

    /**
     * Clears the delta rotation for the current selected mode
     */
    public void clearDeltaRotation(MODE mode) {
        Quaternion.clear(rotation[mode.index].deltaRotation);
    }

    private void setPreviousPosition(float[] previous) {
        previousPos = previous;
    }

    private void clearDragDelta() {
        dragDelta[0] = 0;
        dragDelta[1] = 0;
    }

    private void getDragDelta(MODE mode, PointerEvent event, float[] delta) {
        delta[0] = (event.position[0] - previousPos[0]) / screenDimension.width;
        delta[1] = (event.position[1] - previousPos[1]) / screenDimension.height;
    }

    private void mouseDrag(PointerEvent event) {
        Pointer[] pointers = event.getPointers();
        if (pointers[Pointer.SECOND.index] != null) {
            setMode(MODE.CAMERA);
        } else {
            setMode(MODE.SCENE);
        }
        getDragDelta(currentMode, event, dragDelta);
        switch (action) {
            case ROTATE:
                rotate(adjustDragDelta(dragDelta, rotateInputFactors));
                clearRotation(currentMode);
                break;
            case TRANSLATE:
                translate(dragDelta);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", " + action);
        }
        clearDragDelta();
    }

    private float[] adjustDragDelta(float[] delta, float[] factors) {
        float[] result = new float[delta.length];
        for (int i = 0; i < delta.length; i++) {
            result[i] = delta[i] * factors[i];
        }
        return result;
    }

    private void rotate(float... delta) {
        switch (axis) {
            case X:
                rotate(currentMode, delta);
                break;
            case Y:
                rotate(currentMode, delta);
                break;
            case Z:
                rotate(currentMode, delta);
                break;
            case XY:
                rotate(currentMode, delta);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", " + axis);
        }
        updateRotation(currentMode);
    }

    private void rotate(MODE mode, float... deltaTime) {
        switch (axis) {
            case X:
            case WIDTH:
                Quaternion.setXAxisRotation(deltaTime[1], rotation[mode.index].xRotation);
                Quaternion.mul(rotation[mode.index].currentRotation, rotation[mode.index].xRotation,
                        rotation[mode.index].currentRotation);
                break;
            case Y:
            case HEIGHT:
                Quaternion.setYAxisRotation(deltaTime[0], rotation[mode.index].yRotation);
                Quaternion.mul(rotation[mode.index].currentRotation, rotation[mode.index].yRotation,
                        rotation[mode.index].currentRotation);
                break;
            case Z:
            case DEPTH:
                Quaternion.setZAxisRotation(deltaTime[0], rotation[mode.index].zRotation);
                Quaternion.mul(rotation[mode.index].currentRotation, rotation[mode.index].zRotation,
                        rotation[mode.index].currentRotation);
                break;
            case XY:
                Quaternion.setYAxisRotation(deltaTime[0], rotation[mode.index].yRotation);
                Quaternion.setXAxisRotation(deltaTime[1], rotation[mode.index].xRotation);
                Quaternion.mul(rotation[mode.index].xRotation, rotation[mode.index].yRotation,
                        rotation[mode.index].deltaRotation);
                switch (currentMode) {
                    case CAMERA:
                        rotation[mode.index].deltaRotation[3] = -rotation[mode.index].deltaRotation[3];
                        Quaternion.mul(rotation[mode.index].deltaRotation, rotation[mode.index].currentRotation,
                                rotation[mode.index].currentRotation);
                        break;
                    case SCENE:
                        Quaternion.mul(rotation[mode.index].deltaRotation, rotation[mode.index].currentRotation,
                                rotation[mode.index].currentRotation);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", " + axis);
        }
    }

    private void translate(float... delta) {
        translate(axis, delta);
    }

    private void translate(Axis translateAxis, float... delta) {
        switch (translateAxis) {
            case X:
                translate[0] = delta[0];
                break;
            case Y:
                translate[1] = -delta[1];
                break;
            case Z:
                translate[2] = delta[1];
                break;
            case XY:
                translate[0] = delta[0];
                translate[1] = -delta[1];
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", " + axis);
        }
        updateTranslation(currentMode, translate);
        clearTranslation();
    }

    @Override
    public void pointerEvent(PointerEvent event) {

        switch (event.action) {
            case DOWN:
                previousPos = event.position;
                clearDragDelta();
                clearDeltaRotation(currentMode);
                mouseDown = true;
                break;
            case MOVE:
                mouseDrag(event);
                setPreviousPosition(event.position);
                break;
            case UP:
                setPreviousPosition(null);
                mouseDown = false;
                break;
            case WHEEL:
                // Wheel position is in increments of 1 - which translates to one meter, make it smaller.
                // Also change sign - scroll wheel down should move back, but negative z goes into the screen.
                float[] wheelMove = new float[] { event.position[0] * -0.1f, event.position[1] * -0.1f };
                translate(Axis.Z, wheelMove);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", " + event.action);
        }
    }

    /**
     * Store the rotation values
     */
    public void updateRotation(MODE mode) {
        switch (mode) {
            case CAMERA:
                camera.setCameraRotation(rotation[mode.index].currentRotation);
                break;
            case SCENE:
                if (scene != null) {
                    scene.getSceneTransform().setRotation(rotation[mode.index].currentRotation);
                }
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", " + mode);
        }
    }

    /**
     * Updates camera or model depending on mode
     * 
     * @param translation
     */
    public void updateTranslation(MODE mode, float... translation) {
        for (int i = 0; i < translation.length; i++) {
            translation[i] = translation[i] * cameraTranslateScale;
        }
        switch (mode) {
            case CAMERA:
                camera.translateCamera(translation);
                break;
            case SCENE:
                scene.getSceneTransform().translate(translation);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", " + mode);
        }

    }

    @Override
    public boolean keyEvent(Key key) {
        switch (key.getAction()) {
            case PRESSED:
                keyPressed(key.getKeyValue());
                break;
            case RELEASED:
                keyReleased(key.getKeyValue());
                break;
            default:
                // Do nothing
        }
        // Don't consume key
        return false;
    }

    /**
     * Key is pressed
     * 
     * @param key
     */
    protected void keyPressed(int key) {
        switch (key) {
            case KeyEvent.VK_G:
                setDragAction(DRAGACTION.TRANSLATE);
                break;
            case KeyEvent.VK_X:
                setAxis(Axis.X);
                break;
            case KeyEvent.VK_Y:
                setAxis(Axis.Y);
                break;
            case KeyEvent.VK_Z:
                setAxis(Axis.Z);
                break;
            default:
                // Do nothing
        }
    }

    /**
     * Key is released
     * 
     * @param key
     */
    protected void keyReleased(int key) {
        switch (key) {
            case KeyEvent.VK_G:
                setDragAction(DRAGACTION.ROTATE);
                break;
            case KeyEvent.VK_X:
            case KeyEvent.VK_Y:
            case KeyEvent.VK_Z:
                setAxis(Axis.XY);
            default:
                // Do nothing
        }
    }

}

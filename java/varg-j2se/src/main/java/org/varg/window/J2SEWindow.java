
package org.varg.window;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Key;
import org.gltfio.lib.KeyListener;
import org.gltfio.lib.PointerListener;
import org.gltfio.lib.PointerListener.Action;
import org.gltfio.lib.PointerListener.PointerEvent;
import org.gltfio.lib.WindowListener;
import org.gltfio.lib.WindowListener.WindowEvent;
import org.varg.J2SEWindowApplication.PropertySettings;
import org.varg.renderer.Renderers;

/**
 * Window that connects to the underlying graphics backend.
 * Use this on platforms where a window needs to be created.
 *
 */
public abstract class J2SEWindow {

    public class WindowHandle {
        public static final int INVALID = 0;
        public static final int HEADLESS = Constants.NO_VALUE;

        public final long handle;

        public WindowHandle(long handle) {
            this.handle = handle;
        }

    }

    public static class Size {
        private final int width;
        private final int height;

        public Size(Size source) {
            width = source.width;
            height = source.height;
        }

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Returns the width
         * 
         * @return
         */
        public int getWidth() {
            return width;
        }

        /**
         * Returns the height
         * 
         * @return
         */
        public int getHeight() {
            return height;
        }

        @Override
        public int hashCode() {
            return Objects.hash(height, width);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Size)) {
                return false;
            }
            Size s = (Size) obj;
            return s.width == width && s.height == height;
        }

        @Override
        public String toString() {
            return "Size " + width + ", " + height;
        }

    }

    public static class VideoMode extends Size {
        private boolean fullscreen = false;
        private int swapInterval = 1;

        public VideoMode(VideoMode source) {
            super(source.getWidth(), source.getHeight());
            fullscreen = source.fullscreen;
            swapInterval = source.swapInterval;
        }

        public VideoMode(Size size, boolean fullscreen, int swapInterval) {
            super(size);
            this.fullscreen = fullscreen;
            this.swapInterval = swapInterval;
        }

        public VideoMode(int width, int height) {
            super(width, height);
        }

        public VideoMode(int width, int height, boolean fullscreen, int swapInterval) {
            super(width, height);
            this.fullscreen = fullscreen;
            this.swapInterval = swapInterval;
        }

        /**
         * Returns true if fullscreen flag is set
         * 
         * @return
         */
        public boolean isFullScreen() {
            return fullscreen;
        }

        /**
         * Returns the swap interval
         * 
         * @return
         */
        public int getSwapInterval() {
            return swapInterval;
        }

        @Override
        public String toString() {
            return super.toString() + ", fullscreen=" + fullscreen;
        }

    }

    protected VideoMode videoMode;
    protected Renderers version;
    protected HashSet<PointerListener> pointerListeners = new HashSet<PointerListener>();
    protected ArrayList<KeyListener> keyListeners = new ArrayList<KeyListener>();
    protected HashSet<WindowListener> windowListeners = new HashSet<WindowListener>();
    protected final String title;

    public J2SEWindow(String title) {
        this.title = title;
    }

    /**
     * Creates the underlying window system and render backend
     * Subclasses must implement this method to setup the needed window system and render API.
     * Implementations may defer creation until window framework is up and running (via async callbacks)
     * 
     * 
     */
    public abstract void createWindow(PropertySettings appSettings);

    /**
     * Used to drive rendering for window types that does not provide paint callbacks.
     * Call this method until app should stop
     * Override in subclasses
     */
    public void drawFrame() {

    }

    /**
     * Shows or hides this window
     * 
     * @param visible
     */
    public abstract void setVisible(boolean visible);

    /**
     * Sets the title of the window
     * 
     * @param title
     */
    public abstract void setWindowTitle(String title);

    /**
     * Set the video mode, fullscreen or windowed and resolution
     * 
     * @param The video mode to set
     * @param Index to monitor to set the fullscreen mode for
     * @return
     */
    public abstract VideoMode setVideoMode(VideoMode videoMode, int monitorIndex);

    /**
     * Destroy the window(s) and release window resources
     */
    public abstract void destroy();

    /**
     * Returns the platform specific window handle
     * Only for internal use
     * 
     * @return
     */
    public abstract WindowHandle getWindowHandle();

    /**
     * Adds a pointerlister for pointer callbacks, after this method returns the listener will receive
     * callbacks for {@link PointerListener}
     * 
     * @param listener
     */
    public void addPointerListener(@NonNull PointerListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Listener is null");
        }
        pointerListeners.add(listener);
    }

    /**
     * Adds a keylistener for key event callbacks, after this method returns the listener will receive callbacks
     * for {@link KeyListener} if no prior listener consumes the key.
     * 
     * @param listener
     */
    public void addKeyListener(@NonNull KeyListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        if (!keyListeners.contains(listener)) {
            keyListeners.add(listener);
        }
    }

    /**
     * Adds a windowlistener for window event callbacks, after this method returns the listener will receive callbacks
     * for {@link WindowListener}
     * 
     * @param listener
     */
    public void addWindowListener(@NonNull WindowListener listener) {
        windowListeners.add(listener);
    }

    /**
     * Removes a pointerlister for pointer callbacks, after this method returns the listener will not receive
     * any {@link PointerListener} callbacks
     * 
     * @param listener
     */
    public void removePointerListener(PointerListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        pointerListeners.remove(listener);
    }

    /**
     * Internal method to dispatch a pointer event to listeners - pointer id 0 will be used.
     * 
     * @param action
     * @param pointer
     * @param position
     */
    protected synchronized void dispatchPointerEvent(Action action, int pointer, float... position) {
        PointerEvent event = new PointerEvent(action, System.currentTimeMillis(), pointer, position);
        for (PointerListener listener : pointerListeners) {
            listener.pointerEvent(event);
        }
    }

    /**
     * Dispatch a windowevent using WindowEvent
     * 
     * @param action
     */
    protected void dispatchWindowEvent(org.gltfio.lib.WindowListener.Action action) {
        for (WindowListener listener : windowListeners) {
            WindowEvent.dispatchEvent(listener, action);
        }
    }

    /**
     * Internal method to dispatch key event
     * 
     * @param key
     */
    protected void dispatchKeyEvent(Key key) {
        for (KeyListener listener : keyListeners) {
            if (listener.keyEvent(key)) {
                break;
            }
        }
    }

    /**
     * If fullscreen mode then switch to windowed mode, if window mode then exit.
     */
    protected void onBackPressed() {
        Logger.d(getClass(), "onBackPressed()");
        exit();
    }

    private void exit() {
        destroy();
        System.exit(0);
    }

}

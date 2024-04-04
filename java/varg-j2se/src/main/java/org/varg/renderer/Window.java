
package org.varg.renderer;

import org.gltfio.lib.Logger;
import org.varg.window.J2SEWindow;

/**
 * The size of the renderable area, this is a singleton class since only one instance of GL is supported.
 * Also keeps a reference to create J2SEWindow if created on the platform.
 *
 */
public class Window {

    private static Window window = null;

    int width;
    int height;
    J2SEWindow platformWindow;

    /**
     * Hide instantiation from clients.
     */
    private Window() {
    }

    /**
     * Returns the Window instance, this will always be the same.
     * 
     * @return Window instance (singleton)
     */
    public static Window getInstance() {
        if (window == null) {
            window = new Window();
        }
        return window;
    }

    /**
     * Sets the size of the window area, ie the renderable area. This is an internal method.
     * 
     * @param setWidth
     * @param setHeight
     */
    public void setSize(int setWidth, int setHeight) {
        Logger.d(getClass(), "setSize() " + setWidth + ", " + setHeight);
        width = setWidth;
        height = setHeight;
    }

    /**
     * Sets the window created on the platform, not valid for Android
     * 
     * @param setPlatformWindow
     */
    public void setPlatformWindow(J2SEWindow setPlatformWindow) {
        platformWindow = setPlatformWindow;
    }

    /**
     * Sets the title of the platform window
     * 
     * @param title
     */
    public void setTitle(String title) {
        if (platformWindow != null) {
            platformWindow.setWindowTitle(title);
        }
    }

    /**
     * Returns the width of the visible window
     * 
     * @return Width in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the visible window
     * 
     * @return Height in pixels
     */
    public int getHeight() {
        return height;
    }

}

package org.varg.lwjgl3;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Hashtable;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Key;
import org.gltfio.lib.Logger;
import org.gltfio.lib.PointerListener.Action;
import org.gltfio.lib.Settings;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowFocusCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryUtil;
import org.varg.J2SEWindowApplication.PropertySettings;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.VulkanBackend.IntArrayProperties;
import org.varg.window.J2SEWindow;

/**
 * The main window implementation for GLFW on LWJGL, windows will be created with GLFW
 * 
 *
 */
public abstract class GLFWWindow extends J2SEWindow {

    public static final int ICON_WIDTH = 64;
    public static final int ICON_HEIGHT = 64;

    public static final int DEFAULT_MONITOR_INDEX = 0;

    protected static final int MAX_MOUSE_BUTTONS = 3;
    // If Constants.NO_VALUE then we are in headless mode
    protected WindowHandle window;
    protected PointerBuffer monitors;
    protected int[] buttonActions = new int[MAX_MOUSE_BUTTONS];
    protected float[] cursorPosition = new float[2];
    protected int cursorButtons = 0;
    protected Hashtable<Integer, Integer> glfwKeycodes;
    protected int monitorIndex = DEFAULT_MONITOR_INDEX;

    public GLFWWindow(String title) {
        super(title);
    }

    /**
     * Call this to create window when glfw is initialized
     * 
     * @param appSettings
     * @return
     */
    protected VideoMode internalCreateWindow(PropertySettings appSettings) {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
        int wbuffer =
                Settings.getInstance().getBoolean(BackendProperties.DOUBLEBUFFER) ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE;
        GLFW.glfwWindowHint(GLFW.GLFW_DOUBLEBUFFER, wbuffer);
        monitors = GLFW.glfwGetMonitors();
        long monitorPointer = monitors.get(DEFAULT_MONITOR_INDEX);
        if (appSettings.fullscreen) {
            GLFWVidMode currentMode = GLFW.glfwGetVideoMode(monitorPointer);
            GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, currentMode.redBits());
            GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, currentMode.greenBits());
            GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, currentMode.blueBits());
            GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, GLFW.GLFW_DONT_CARE);
            GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);
            window = new WindowHandle(GLFW.glfwCreateWindow(currentMode.width(), currentMode.height(), "",
                    monitorPointer,
                    MemoryUtil.NULL));
            videoMode = new VideoMode(currentMode.width(), currentMode.height(), true, appSettings.swapInterval);
        } else {
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
            window = new WindowHandle(GLFW.glfwCreateWindow(appSettings.width, appSettings.height, "", MemoryUtil.NULL,
                    MemoryUtil.NULL));
            videoMode = new VideoMode(appSettings.width, appSettings.height, appSettings.fullscreen,
                    appSettings.swapInterval);
            setVideoMode(videoMode, DEFAULT_MONITOR_INDEX);
            int[] windowPosition = Settings.getInstance().getIntArray(IntArrayProperties.WINDOW_POSITION);
            if (windowPosition != null) {
                GLFW.glfwSetWindowPos(window.handle, windowPosition[0], windowPosition[1]);
            }
        }
        if (window.handle == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        setWindowTitle(title);
        // GLFWImage.Buffer buffer = createWindowIcon();
        // Sometimes crashes the VM
        // GLFW.glfwSetWindowIcon(window, buffer);
        setMouseCallback();
        setKeyCallback();
        Logger.d(getClass(), "Created window: " + videoMode.toString());
        return videoMode;
    }

    @Override
    public void drawFrame() {
        GLFW.glfwPollEvents();
    }

    @Override
    public void setVisible(boolean visible) {
        if (window.handle != WindowHandle.HEADLESS) {
            if (visible) {
                GLFW.glfwShowWindow(window.handle);
            } else {
                GLFW.glfwHideWindow(window.handle);
            }
        }

    }

    @Override
    public void setWindowTitle(String title) {
        if (window.handle != 0) {
            GLFW.glfwSetWindowTitle(window.handle, title);
        }
    }

    @Override
    public VideoMode setVideoMode(VideoMode videoMode, int index) {
        long monitor = monitors.get(index);
        VideoMode result = videoMode;
        if (index < monitors.capacity()) {
            if (videoMode.isFullScreen()) {
                GLFW.glfwSetWindowMonitor(window.handle, monitor, 0, 0, videoMode.getWidth(), videoMode.getHeight(),
                        GLFW.GLFW_DONT_CARE);
                GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
                Logger.d(getClass(), "Set monitor resolution to " + vidMode.width() + ", " + vidMode.height());
                // GLFW.glfwSetWindowIconifyCallback(monitor, this);
                result = new VideoMode(vidMode.width(), vidMode.height(), true, videoMode.getSwapInterval());
            } else {
                GLFW.glfwSetWindowMonitor(window.handle, MemoryUtil.NULL, 100, 100, videoMode.getWidth(),
                        videoMode.getHeight(),
                        GLFW.GLFW_DONT_CARE);
            }
        } else {
            Logger.d(getClass(), ErrorMessage.INVALID_VALUE.message + "For monitor index: " + index);
        }
        return result;
    }

    private Buffer createWindowIcon() {
        ByteBuffer buffer = Buffers.createByteBuffer(ICON_WIDTH * ICON_HEIGHT);
        GLFWImage image = GLFWImage.malloc();
        Buffer imageBuffer = GLFWImage.malloc(1);
        image.set(64, 64, buffer);
        imageBuffer.put(0, image);
        return imageBuffer;
    }

    private Hashtable<Integer, Integer> getGLFWKeys() {
        Hashtable<Integer, Integer> glfwFields = new Hashtable<>();
        for (Field scanField : GLFW.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(scanField.getModifiers())) {
                String fieldName = scanField.getName();
                if (fieldName.startsWith("GLFW_KEY_")) {
                    String key = fieldName.substring(9);
                    if (key.startsWith("LEFT_")) {
                        key = key.substring(5);
                    }
                    if (key.startsWith("RIGHT_")) {
                        key = key.substring(6);
                    }
                    try {
                        Field awtField = java.awt.event.KeyEvent.class.getField("VK_" + key);
                        Field field = GLFW.class.getField(fieldName);
                        int scanCode = field.getInt(null);
                        int awtKeyCode = awtField.getInt(null);
                        glfwFields.put(scanCode, awtKeyCode);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        Logger.d(getClass(), "Exception for: " + fieldName + " key: " + key);
                    }
                }
            }
        }
        return glfwFields;
    }

    private void setKeyCallback() {
        /**
         * Fetch scancode for fields that start with VK_ and store keycodes in array to convert scancode to AWT values
         */
        glfwKeycodes = getGLFWKeys();
        GLFW.glfwSetKeyCallback(window.handle, (windowHnd, key, scancode, action, mods) -> {
            Integer awtKey = glfwKeycodes.get(key);
            if (awtKey != null) {
                switch (action) {
                    case GLFW.GLFW_RELEASE:
                        super.dispatchKeyEvent(new Key(org.gltfio.lib.Key.Action.RELEASED, awtKey));
                        if (key == GLFW.GLFW_KEY_ESCAPE) {
                            onBackPressed();
                        }
                        break;
                    case GLFW.GLFW_PRESS:
                        super.dispatchKeyEvent(new Key(org.gltfio.lib.Key.Action.PRESSED, awtKey));
                        break;
                    default:
                        // Do nothing
                }
            } else {
                Logger.d(getClass(), "No AWT keycode for: " + key);
            }
        });

    }

    private void setMouseCallback() {
        GLFW.glfwSetCursorPosCallback(window.handle, new GLFWCursorPosCallbackI() {
            @Override
            public synchronized void invoke(long windowPointer, double xpos, double ypos) {
                cursorPosition[0] = (float) xpos;
                cursorPosition[1] = (float) ypos;
                if (((buttonActions[0] | buttonActions[1]) | buttonActions[2]) != 0) {
                    dispatchPointerEvent(Action.MOVE, cursorButtons, cursorPosition);
                }
            }
        });
        GLFW.glfwSetWindowCloseCallback(window.handle, new GLFWWindowCloseCallbackI() {
            @Override
            public synchronized void invoke(long windowPointer) {
                Logger.d(getClass(), "Window closed");
                destroy();
                System.exit(0);
            }
        });
        GLFW.glfwSetWindowFocusCallback(window.handle, new GLFWWindowFocusCallbackI() {

            @Override
            public void invoke(long windowPointer, boolean focused) {
                Logger.d(getClass(), "Window focus " + focused);
            }
        });

        GLFW.glfwSetWindowSizeCallback(window.handle, new GLFWWindowSizeCallbackI() {

            @Override
            public void invoke(long windowPointer, int width, int height) {
                Logger.d(getClass(), "Window size " + width + ", " + height);
                if (width == 0 || height == 0) {
                    dispatchWindowEvent(org.gltfio.lib.WindowListener.Action.DEACTIVATED);
                }
            }
        });

        GLFW.glfwSetMouseButtonCallback(window.handle, new GLFWMouseButtonCallbackI() {
            @Override
            public synchronized void invoke(long windowPointer, int button, int action, int mods) {
                if (button >= 0 && button < buttonActions.length) {
                    buttonActions[button] = action;
                    switch (action) {
                        case GLFW.GLFW_PRESS:
                            cursorButtons |= 1 << button;
                            dispatchPointerEvent(Action.DOWN, button, cursorPosition);
                            break;
                        case GLFW.GLFW_RELEASE:
                            cursorButtons ^= 1 << button;
                            dispatchPointerEvent(Action.UP, button, cursorPosition);
                            break;
                        default:
                            // Do nothing
                    }
                }
            }
        });

        GLFW.glfwSetScrollCallback(window.handle, new GLFWScrollCallbackI() {
            @Override
            public synchronized void invoke(long windowPointer, double xoffset, double yoffset) {
                dispatchPointerEvent(Action.WHEEL, 0, new float[] { (float) xoffset, (float) yoffset });
            }
        });

    }

    @Override
    public WindowHandle getWindowHandle() {
        return window;
    }

    @Override
    public void destroy() {
        Logger.d(getClass(), "destroy()");
        GLFW.glfwDestroyWindow(window.handle);
        window = null;
    }

}

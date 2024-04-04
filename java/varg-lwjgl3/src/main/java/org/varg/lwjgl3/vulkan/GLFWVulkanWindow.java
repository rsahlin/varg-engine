package org.varg.lwjgl3.vulkan;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVulkan;
import org.varg.J2SEWindowApplication.PropertySettings;
import org.varg.J2SEWindowApplication.WindowType;
import org.varg.lwjgl3.GLFWWindow;

/**
 * Window for Vulkan support
 *
 */
public class GLFWVulkanWindow extends GLFWWindow {

    public GLFWVulkanWindow(String title) {
        super(title);
    }

    @Override
    public void createWindow(PropertySettings appSettings) {
        GLFWErrorCallback.createPrint().set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver (ICD)");
        }
        if (appSettings.windowType != WindowType.HEADLESS) {
            internalCreateWindow(appSettings);
        } else {
            window = new WindowHandle(WindowHandle.HEADLESS);
        }
    }

}

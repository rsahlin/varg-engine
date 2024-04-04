
package org.varg.lwjgl3.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.varg.lwjgl3.apps.VARGViewer;
import org.varg.renderer.Renderers;

public class VARGTest {

    @Test
    public void testVulkan10Init() {
        VARGViewer viewer = new VARGViewer(null, Renderers.VULKAN10, "Test");
        assertTrue(viewer.getRenderer() != null);
        assertTrue(viewer.factory != null);
        assertTrue(viewer.getJ2SEWindow() != null);
        assertTrue(true);
    }

    @Test
    public void testVulkan11Init() {
        VARGViewer viewer = new VARGViewer(null, Renderers.VULKAN11, "Test");
        assertTrue(viewer.getRenderer() != null);
        assertTrue(viewer.factory != null);
        assertTrue(viewer.getJ2SEWindow() != null);
        assertTrue(true);
    }

    public void testVulkanGraphicsShader() {

    }

    public void testComputePipeline() {
        VARGViewer viewer = new VARGViewer(null, Renderers.VULKAN11, "Test");

    }

}

package org.varg.vulkan;

import java.util.ArrayList;

import org.varg.vulkan.Vulkan10.SurfaceFormat;

/**
 * Delegate selection of surface format and colorspace
 * Contains methods for selecting the wanted surface format and colorspace from list of available formats
 *
 */
public interface SurfaceFormatChooser {
    /**
     * Returns the wanted surface format from list of available formats.
     * This method must return a format.
     * 
     * @param formats
     * @return The SurfaceFormat to use
     */
    SurfaceFormat selectSurfaceFormat(ArrayList<SurfaceFormat> formats);
}

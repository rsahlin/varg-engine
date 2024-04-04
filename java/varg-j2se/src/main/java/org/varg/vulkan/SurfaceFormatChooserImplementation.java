package org.varg.vulkan;

import java.util.ArrayList;

import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Settings;
import org.ktximageio.ktx.ImageReader.TransferFunction;
import org.varg.vulkan.Vulkan10.ColorSpaceKHR;
import org.varg.vulkan.Vulkan10.SurfaceFormat;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;
import org.gltfio.lib.Logger;

public class SurfaceFormatChooserImplementation implements SurfaceFormatChooser {

    private ColorSpaceKHR[] wantedColorSpaces = new ColorSpaceKHR[] { ColorSpaceKHR.VK_COLOR_SPACE_HDR10_ST2084_EXT,
            ColorSpaceKHR.VK_COLOR_SPACE_BT2020_LINEAR_EXT,
            ColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR };

    /**
     * Selects the display chain surfaceformat
     * 
     * @param formats
     * @return
     * @throws IllegalArgumentException If no suitable surfaceformat could be found
     */
    @Override
    public SurfaceFormat selectSurfaceFormat(ArrayList<SurfaceFormat> formats) {
        ColorSpaceKHR selected = ColorSpaceKHR
                .get(Settings.getInstance().getProperty(BackendStringProperties.COLORSPACE));
        if (selected != null) {
            Logger.d(getClass(), "Surface format colorspace set using " + BackendStringProperties.COLORSPACE.getKey()
                    + " : " + selected);
        }
        ColorSpaceKHR[] spaces = selected != null ? new ColorSpaceKHR[] { selected } : wantedColorSpaces;
        ArrayList<ColorSpaceKHR> wantedColorspaces = filterByColorspace(formats, spaces);
        SurfaceFormat selectedFormat = getByColorspace(formats, wantedColorspaces);
        if (selectedFormat == null) {
            throw new IllegalArgumentException("No surfaceformat");
        }
        return selectedFormat;
    }

    /**
     * Returns a list with matching colorspaces in descending order
     * 
     * @param formats List of available surface formats.
     * @param colorSpace Priority list of colorspaces
     * @return List of colorspaces in descending order
     */
    private ArrayList<ColorSpaceKHR> filterByColorspace(ArrayList<SurfaceFormat> formats,
            ColorSpaceKHR[] colorSpace) {
        ArrayList<ColorSpaceKHR> result = new ArrayList<>();
        // Loop using specified colorspaces to get list ordered by priority.
        for (ColorSpaceKHR c : colorSpace) {
            for (SurfaceFormat sf : formats) {
                if (sf.space == c) {
                    if (!result.contains(c)) {
                        result.add(c);
                    }
                }
            }
        }
        return result;
    }

    private SurfaceFormat getByColorspace(ArrayList<SurfaceFormat> formats,
            ArrayList<ColorSpaceKHR> prioritizedColorspaces) {
        for (ColorSpaceKHR cs : prioritizedColorspaces) {
            Vulkan10.Format[] wantedFormats = getWantedFormats(cs);
            for (Vulkan10.Format format : wantedFormats) {
                SurfaceFormat chosen = getAvailableFormat(formats, cs, format);
                if (chosen != null) {
                    return chosen;
                }
            }
        }
        throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", no matching SurfaceFormat");
    }

    private Vulkan10.Format[] getWantedFormats(ColorSpaceKHR colorSpace) {
        switch (colorSpace) {
            case VK_COLOR_SPACE_BT2020_LINEAR_EXT:
            case VK_COLOR_SPACE_HDR10_ST2084_EXT:
            case VK_COLOR_SPACE_EXTENDED_SRGB_LINEAR_EXT:
                return new Vulkan10.Format[] { Vulkan10.Format.VK_FORMAT_A2B10G10R10_UNORM_PACK32,
                        Vulkan10.Format.VK_FORMAT_B10G11R11_UFLOAT_PACK32,
                        Vulkan10.Format.VK_FORMAT_A2R10G10B10_UNORM_PACK32,
                        Vulkan10.Format.VK_FORMAT_R16G16B16A16_SFLOAT, Vulkan10.Format.VK_FORMAT_R16G16B16_UNORM,
                        Vulkan10.Format.VK_FORMAT_R16G16B16A16_SFLOAT };
            case VK_COLOR_SPACE_SRGB_NONLINEAR_KHR:
                String format = Settings.getInstance().getProperty(BackendStringProperties.SURFACE_FORMAT);
                if (format == null || format.equalsIgnoreCase(TransferFunction.LINEAR.name())) {
                    return new Vulkan10.Format[] { Vulkan10.Format.VK_FORMAT_A2B10G10R10_UNORM_PACK32,
                            Vulkan10.Format.VK_FORMAT_A2R10G10B10_UNORM_PACK32,
                            Vulkan10.Format.VK_FORMAT_R8G8B8_UNORM, Vulkan10.Format.VK_FORMAT_R8G8B8A8_UNORM,
                            Vulkan10.Format.VK_FORMAT_B8G8R8A8_UNORM, Vulkan10.Format.VK_FORMAT_B8G8R8_UNORM };
                }
                if (format.contentEquals("888_UNORM")) {
                    return new Vulkan10.Format[] { Vulkan10.Format.VK_FORMAT_R8G8B8_UNORM,
                            Vulkan10.Format.VK_FORMAT_B8G8R8_UNORM };

                } else if (format.contentEquals("8888_UNORM")) {
                    return new Vulkan10.Format[] { Vulkan10.Format.VK_FORMAT_B8G8R8A8_UNORM,
                            Vulkan10.Format.VK_FORMAT_R8G8B8A8_UNORM };
                } else {
                    return new Vulkan10.Format[] { Vulkan10.Format.VK_FORMAT_R8G8B8_SRGB,
                            Vulkan10.Format.VK_FORMAT_R8G8B8A8_SRGB,
                            Vulkan10.Format.VK_FORMAT_B8G8R8A8_SRGB, Vulkan10.Format.VK_FORMAT_B8G8R8_SRGB };
                }
            default:
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_VALUE.message + ", not implemented support for " + colorSpace);
        }

    }

    private SurfaceFormat getAvailableFormat(ArrayList<SurfaceFormat> formats, ColorSpaceKHR colorSpace,
            Vulkan10.Format format) {
        for (SurfaceFormat sf : formats) {
            if (sf.getColorSpace() == colorSpace && sf.getFormat() == format) {
                return sf;
            }
        }
        return null;
    }

}

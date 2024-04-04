package org.varg.vulkan.structs;

import org.ktximageio.itu.BT2020;

public class HDRMetadata {

    private XYColor primaryRed;
    private XYColor primaryGreen;
    private XYColor primaryBlue;
    private XYColor whitePoint;

    public final float maxLuminance;
    public final float minLuminance;
    public final float maxContentLightLevel;
    public final float maxFrameAverageLightLevel;

    public HDRMetadata(float[] primaryRed, float[] primaryGreen, float[] primaryBlue, float[] whitePoint,
            float maxLuminance, float minLuminance, float maxContentLightLevel, float maxFrameAverageLightLevel) {
        this.primaryRed = new XYColor(primaryRed);
        this.primaryGreen = new XYColor(primaryGreen);
        this.primaryBlue = new XYColor(primaryBlue);
        this.whitePoint = new XYColor(whitePoint);
        this.maxLuminance = maxLuminance;
        this.minLuminance = minLuminance;
        this.maxContentLightLevel = maxContentLightLevel;
        this.maxFrameAverageLightLevel = maxFrameAverageLightLevel;
    }

    /**
     * Creates a new default BT.2020 metadata with min/max luminance at 0 / 10000
     */
    public HDRMetadata() {
        primaryRed = new XYColor(BT2020.RED_PRIMARY);
        primaryGreen = new XYColor(BT2020.GREEN_PRIMARY);
        primaryBlue = new XYColor(BT2020.BLUE_PRIMARY);
        whitePoint = new XYColor(BT2020.WHITEPOINT);
        maxLuminance = 10000;
        minLuminance = 0;
        maxContentLightLevel = 10000;
        maxFrameAverageLightLevel = 10000;
    }

    /**
     * Returns the display primary red
     * 
     * @return
     */
    public XYColor getPrimaryRed() {
        return primaryRed;
    }

    /**
     * Returns the display primary green
     * 
     * @return
     */
    public XYColor getPrimaryGreen() {
        return primaryGreen;
    }

    /**
     * Returns the display primary blue
     * 
     * @return
     */
    public XYColor getPrimaryBlue() {
        return primaryBlue;
    }

    /**
     * Returns the display whitepoint
     * 
     * @return
     */
    public XYColor getWhitePoint() {
        return whitePoint;
    }

}

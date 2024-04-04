
package org.varg.renderer;

import org.gltfio.lib.ErrorMessage;

/**
 * Class that specifies the surface configuration, bitdepth, zbuffer samples and other
 * surface specific configurations.
 * 
 * @author Richard Sahlin
 */
public class SurfaceConfiguration {

    public static final int DEFAULT_RED_BITS = 8;
    public static final int DEFAULT_GREEN_BITS = 8;
    public static final int DEFAULT_BLUE_BITS = 8;
    public static final int DEFAULT_ALPHA_BITS = 0;
    public static final int DEFAULT_DEPTH_BITS = 16;
    public static final int DEFAULT_STENCIL_BITS = 0;
    public static final int DEFAULT_SAMPLES = 0;

    /**
     * The display red bit (colour) depth
     */
    protected int redBits = DEFAULT_RED_BITS;
    /**
     * The display green bit (colour) depth
     */
    protected int greenBits = DEFAULT_GREEN_BITS;
    /**
     * The display blue bit (colour) depth
     */
    protected int blueBits = DEFAULT_BLUE_BITS;

    /**
     * Number of bits to use for alpha.
     */
    protected int alphaBits = DEFAULT_ALPHA_BITS;

    /**
     * Number of bits to use for the depth buffer.
     */
    protected int depthBits = DEFAULT_DEPTH_BITS;

    /**
     * Number of bits to use for stencil
     */
    protected int stencilBits = DEFAULT_STENCIL_BITS;

    /**
     * Surface type bitmask
     */
    protected int surfaceType;

    /**
     * Number of samples to require in SampleBuffers.
     */
    protected int samples = DEFAULT_SAMPLES;

    /**
     * Constructs a SurfaceConfiguration that can used when
     * selecting EGL configuration.
     * All parameters are set to default values.
     */
    public SurfaceConfiguration() {
    }

    /**
     *
     * @param rBits Number of red bits
     * @param gBits Number of green bits
     * @param bBits Number of blue bits
     * @param aBits Number of alpha bits
     * @param dBits Number of depth bits
     * @param sampleCount The number of samples for each pixel.
     */
    public SurfaceConfiguration(int rBits, int gBits, int bBits, int aBits, int dBits, int sampleCount) {
        redBits = rBits;
        greenBits = gBits;
        blueBits = bBits;
        alphaBits = aBits;
        depthBits = dBits;
        samples = sampleCount;
    }

    /**
     * Return the number of bits to use for red.
     * 
     * @return Number of bits to use for red.
     */
    public int getRedBits() {
        return redBits;
    }

    /**
     * Return the number of bits to use for green.
     * 
     * @return Number of bits to use for green.
     */
    public int getGreenBits() {
        return greenBits;
    }

    /**
     * Return the number of bits to use for blue.
     * 
     * @return Number of bits to use for blue.
     */
    public int getBlueBits() {
        return blueBits;
    }

    /**
     * Return the number of bits to use for alpha.
     * 
     * @return Number of alpha bits.
     */
    public int getAlphaBits() {
        return alphaBits;
    }

    /**
     * Return the number of bits to use for depth buffer.
     *
     * @return Number of bits to use in depth buffer.
     */
    public int getDepthBits() {
        return depthBits;
    }

    /**
     * Returns the number of bits to use for stencil buffer
     * 
     * @return
     */
    public int getStencilBits() {
        return stencilBits;
    }

    /**
     * Return the number of samples required for this configuration.
     * 
     * @return The number of samples required for this configuration.
     */
    public int getSamples() {
        return samples;
    }

    /**
     * Sets the wanted number of samples for the EGL buffer.
     * 
     * @param sampleCount The number of samples
     * @throws IllegalArgumentException If samples is negative
     */
    public void setSamples(int sampleCount) {
        if (sampleCount < 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Samples=" + sampleCount);
        }
        samples = sampleCount;
    }

    /**
     * Sets the number of wanted redbits, at least this value - may get a config with more.
     * 
     * @param redbits
     */
    public void setRedBits(int redbits) {
        this.redBits = redbits;
    }

    /**
     * Sets the number of wanted greenbits, at least this value - may get a config with more.
     * 
     * @param greenbits
     */
    public void setGreenBits(int greenbits) {
        this.greenBits = greenbits;
    }

    /**
     * Sets the number of wanted bluebits, at least this value - may get a config with more.
     * 
     * @param bluebits
     */
    public void setBlueBits(int bluebits) {
        this.blueBits = bluebits;
    }

    /**
     * Sets the number of wanted alphabits, at least this value - may get a config with more.
     * 
     * @param alphabits
     */
    public void setAlphaBits(int alphabits) {
        this.alphaBits = alphabits;
    }

    /**
     * Sets the number of wanted depthbits, at least this value - may get a config with more.
     * 
     * @param depthbits
     */
    public void setDepthBits(int depthbits) {
        this.depthBits = depthbits;
    }

    /**
     * Sets the number of wanted stencilbits, at least this value - may get config with more.
     * 
     * @param stencilbits
     */
    public void setStencilBits(int stencilbits) {
        this.stencilBits = stencilbits;
    }

    /**
     * Sets the surface type bitmask
     * 
     * @param surface
     */
    public void setSurfaceType(int surface) {
        surfaceType = surface;
    }
}

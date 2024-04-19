
package org.varg.shader.voxels;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Vec3;
import org.ktximageio.ktx.AwtImageUtils;
import org.ktximageio.ktx.ByteArrayImageBuffer;
import org.ktximageio.ktx.ImageBuffer;
import org.ktximageio.ktx.ImageHeader;
import org.ktximageio.ktx.ImageReader;
import org.ktximageio.ktx.ImageReader.ImageFormat;
import org.varg.vulkan.Vulkan10;

/**
 * Converts bitmap images to voxeldata
 */
public class PixelsToVoxels {

    public enum Mode {
        RGBDIFF(),
        XYY(),
        COLORVALUE(),
        DOTPRODUCT(),
        DOTXY();
    }

    public enum CompareSpace {
        sRGB(),
        LINEAR();
    }

    private Mode mode = Mode.DOTXY;
    private CompareSpace space = CompareSpace.LINEAR;

    public PixelsToVoxels() {
    }

    public static class VoxelData {
        public final FloatBuffer positions;
        public final ByteBuffer paletteIndexes;
        private final Vulkan10.Format vertexFormat;
        private final Vulkan10.Format paletteIndexFormat;
        public final float width; // In world units
        public final float height; // In world units

        private VoxelData(FloatBuffer positions, ByteBuffer paletteIndexes, Vulkan10.Format vertexFormat,
                Vulkan10.Format paletteIndexFormat, float width, float height) {
            this.positions = positions;
            this.paletteIndexes = paletteIndexes;
            this.vertexFormat = vertexFormat;
            this.paletteIndexFormat = paletteIndexFormat;
            this.width = width;
            this.height = height;
        }

        public int getVoxelCount() {
            return paletteIndexes.limit() / paletteIndexFormat.getComponentByteSize();
        }

        public float[] getPositionsAsFloatArray() {
            float[] result = new float[positions.limit()];
            positions.position(0);
            positions.get(result);
            return result;
        }

    }

    private void displayColors(Hashtable<Integer, Integer> lookups, int[] palette) {
        Logger.d(getClass(), "Image contains " + lookups.size() + " colors");
        int size = 100;
        int border = (size / 2);
        int width = 2000;
        int height = 1000;
        int rows = height / (size * 2);
        int count = (width / size) * (height / (size * 2));
        byte[] image = new byte[3 * width * (height + (1000 / (size * 2)) * border)];
        createMappedColorImage(image, width, height, size, border, lookups, palette);
        ImageBuffer buffer = ImageBuffer.create(image, ImageFormat.VK_FORMAT_R8G8B8_UNORM, 1, width, height + (1000
                / (size * 2)) * border, null);
        AwtImageUtils.displayBuffer(buffer, null);
    }

    private void createMappedColorImage(byte[] image, int width, int height, int size, int border,
            Hashtable<Integer, Integer> lookups, int[] palette) {

        int row = height / (size * 2);
        int column = width / size;
        int ypos = 0;
        Enumeration<Integer> list = lookups.keys();
        for (int y = 0; y < row; y++) {
            int xpos = 0;
            for (int x = 0; x < column; x++) {
                if (list.hasMoreElements()) {
                    int color = list.nextElement();
                    int pColor = palette[lookups.get(color)];
                    drawSquare(image, width, xpos, ypos, size, color);
                    drawSquare(image, width, xpos, ypos + size, size, pColor);
                }
                xpos += size;
            }
            ypos += size * 2 + border;
        }
    }

    private void drawSquare(byte[] image, int width, int xpos, int ypos, int size, int color) {
        int[] rgb = toArray(color);
        int index = width * ypos * 3 + (xpos * 3);
        for (int y = 0; y < size; y++) {
            int i = index;
            for (int x = 0; x < size; x++) {
                image[i++] = (byte) (rgb[0] & 0xff);
                image[i++] = (byte) (rgb[1] & 0xff);
                image[i++] = (byte) (rgb[2] & 0xff);
            }
            index += width * 3;
        }
    }

    private VoxelData toVoxels(byte[] rgba, int w, int h, float[] spacing, int[] palette, float[] offsets) {
        int index = 0;
        FloatBuffer positions = Buffers.createFloatBuffer(w * h * 4);
        ByteBuffer paletteIndexesByte = Buffers.createByteBuffer(w * h * 2).order(ByteOrder.LITTLE_ENDIAN);
        float centerX = -(w / 2) * spacing[0];
        float centerY = -(h / 2) * spacing[1];
        Hashtable<Integer, Integer> lookups = new Hashtable<>();
        for (int y = h - 1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
                if (rgba[index] == -1) {
                    positions.put((x * spacing[0]) + centerX + offsets[0]);
                    positions.put((y * spacing[1]) + centerY + offsets[1]);
                    positions.put(offsets[2]);
                    positions.put(0);
                    int color = (rgba[index + 1] & 0xff) | ((rgba[index + 2] << 8) & 0xff00) | (rgba[index + 3] << 16)
                            & 0xff0000;
                    Integer pIndex = lookups.get(color);
                    if (pIndex == null) {
                        switch (mode) {
                            case RGBDIFF:
                                pIndex = Integer.valueOf(getDeltaRGBDiff(color, palette));
                                break;
                            case XYY:
                                pIndex = Integer.valueOf(getDeltaXYZ(color, palette));
                                break;
                            case COLORVALUE:
                                pIndex = Integer.valueOf(getDeltaColorValue(color, palette));
                                break;
                            case DOTPRODUCT:
                                pIndex = Integer.valueOf(getDeltaDotProduct(color, palette));
                                break;
                            case DOTXY:
                                pIndex = Integer.valueOf(getDeltaDotRGB(color, palette));
                                break;
                            default:
                                throw new IllegalArgumentException();
                        }
                        lookups.put(color, pIndex);
                    }
                    paletteIndexesByte.put((byte) ((int) pIndex));
                }
                index += 4;
            }
        }
        // displayColors(lookups, palette);
        positions.flip();
        paletteIndexesByte.flip();
        Logger.d(getClass(), "Created voxel positions and color indexes for " + paletteIndexesByte.limit()
                + " voxels");
        return new VoxelData(positions, paletteIndexesByte, Vulkan10.Format.VK_FORMAT_R32_SFLOAT,
                Vulkan10.Format.VK_FORMAT_R8_UINT, w * spacing[0], h * spacing[1]);
    }

    private int getDeltaLinearRGB(byte[] bgr, int index, int[] palette) {
        DeltaCalculator calc = new DeltaCalculator();
        int b = (int) (sRGBToLinear(bgr[index++] & 0xff) * 255);
        int g = (int) (sRGBToLinear(bgr[index++] & 0xff) * 255);
        int r = (int) (sRGBToLinear(bgr[index++] & 0xff) * 255);
        for (int colorIndex = 0; colorIndex < palette.length; colorIndex++) {
            int color = palette[colorIndex];
            int deltaR = (int) (r - sRGBToLinear(((color >>> 16) & 0xff)) * 255);
            int deltaG = (int) (g - sRGBToLinear(((color >>> 8) & 0xff)) * 255);
            int deltaB = (int) (b - sRGBToLinear(((color) & 0xff)) * 255);
            int delta = (int) ((int) Math.pow(deltaR, 2) + Math.pow(deltaG, 2) + Math.pow(deltaB, 2));
            // int delta = 0;
            // if ((r + ((color >>> 16) & 0xff)) / 2 < 128) {
            // delta = (int) Math.sqrt(Math.pow(2 * deltaR, 2) + Math.pow(4 * deltaG, 2) + Math.pow(3 * deltaB, 2));
            // } else {
            // delta = (int) Math.sqrt(Math.pow(3 * deltaR, 2) + Math.pow(4 * deltaG, 2) + Math.pow(2 * deltaB, 2));
            // }
            calc.compare(delta, colorIndex);
        }
        return calc.index;
    }

    private int getDeltasRGB(byte[] bgr, int index, int[] palette) {
        DeltaCalculator calc = new DeltaCalculator();
        int b = (bgr[index++] & 0xff);
        int g = (bgr[index++] & 0xff);
        int r = (bgr[index++] & 0xff);
        for (int colorIndex = 0; colorIndex < palette.length; colorIndex++) {
            int color = palette[colorIndex];
            int deltaR = r - ((color >>> 16) & 0xff);
            int deltaG = g - ((color >>> 8) & 0xff);
            int deltaB = b - ((color) & 0xff);
            int delta = (int) ((int) Math.pow(deltaR, 2) + Math.pow(deltaG, 2) + Math.pow(deltaB, 2));
            calc.compare(delta, colorIndex);
        }
        return calc.index;
    }

    private class LinkedList {

        private final int[] palette;
        private final int[] paletteIndex;
        private final float[][] diff;
        int[] next;
        int firstIndex = -1;
        int currentIndex = 0;
        private final int color;

        private int checkIndex;
        private int prevIndex;

        private LinkedList(int count, int color, int[] palette) {
            this.paletteIndex = new int[count];
            this.diff = new float[count][2];
            this.next = new int[count];
            this.color = color;
            this.palette = palette;
            for (int i = 0; i < next.length; i++) {
                next[i] = -1;
            }
        }

        public int getFirstPaletteIndex() {
            if (firstIndex == -1) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "List is empty");
            }
            return paletteIndex[firstIndex];
        }

        private void put(int i, float... c) {
            float cDiff = 0;
            float chromaDiff;
            if (c.length > 2) {
                cDiff = c[0] + c[1] + c[2];
                chromaDiff = c[3];
            } else {
                cDiff = c[0];
                chromaDiff = c[1];
                if (cDiff < 0.0) {
                    throw new IllegalArgumentException();
                }
            }
            paletteIndex[currentIndex] = i;
            diff[currentIndex][0] = cDiff;
            diff[currentIndex][1] = chromaDiff;
            if (firstIndex == -1) {
                firstIndex = 0;
            } else {
                checkIndex = firstIndex;
                prevIndex = -1;
                while (next[currentIndex] == -1 && checkIndex != -1) {
                    if (cDiff < diff[checkIndex][0]) {
                        insertBefore();
                    } else if (cDiff > diff[checkIndex][0]) {
                        isAfter();
                    } else {
                        // Same chroma diff - check luma diff
                        if (chromaDiff < diff[checkIndex][1]) {
                            insertBefore();
                        } else if (chromaDiff > diff[checkIndex][1]) {
                            isAfter();
                        } else {
                            if (checkIndex == firstIndex) {
                                Logger.d(getClass(), "Same luma diff for color: " + Integer.toHexString(color));
                                Logger.d(getClass(), "First palette color: " + Integer.toHexString(
                                        palette[paletteIndex[checkIndex]]) + "(" + paletteIndex[checkIndex] + ")"
                                        + ", second: " + Integer.toHexString(
                                                palette[i])
                                        + "(" + i + ")");
                            }
                            insertBefore();
                        }
                    }
                }
            }
            currentIndex++;
        }

        private void insertBefore() {
            // Insert before
            if (prevIndex == -1) {
                // Insert first in list
                firstIndex = currentIndex;
            } else {
                next[prevIndex] = currentIndex;
            }
            next[currentIndex] = checkIndex;

        }

        private void isAfter() {
            prevIndex = checkIndex;
            checkIndex = next[checkIndex];
            if (checkIndex == -1) {
                // Last in list
                next[prevIndex] = currentIndex;
            }
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);
            // float[] colorXYZ = toXYY(color, cs);
            float[] colorXYZ = toXYY(color, cs);
            int[] linearColor = toLinearRGB(color);
            float colLuma = lumaFromSRGB(color);
            sb.append("\n");
            sb.append("Color: " + Integer.toHexString(color) + "\n");
            sb.append("Linear color: " + linearColor[0] + ", " + linearColor[1] + ", " + linearColor[2] + "\n");
            sb.append("Saturation: " + getSaturation(color) + "\n");
            sb.append("XYY: " + colorXYZ[0] + ", " + colorXYZ[1] + ", " + colorXYZ[2] + "\n");
            sb.append("Luma " + colLuma + "\n");
            sb.append("Brightness " + brightness(color) + "\n");
            int index = firstIndex;
            int counter = 1;
            while (index != -1) {
                int[] palLinearColor = toLinearRGB(palette[paletteIndex[index]]);
                float[] xyz = toXYY(palette[paletteIndex[index]], cs);
                sb.append("Pos " + counter + ", paletteindex: "
                        + paletteIndex[index] + ", pColor " + Integer.toHexString(palette[paletteIndex[index]])
                        + ", saturation " + getSaturation(palette[paletteIndex[index]])
                        + ", diff " + diff[index][0]
                        + ", RGB diff " + Math.abs(linearColor[0] - palLinearColor[0])
                        + ", " + Math.abs(linearColor[1] - palLinearColor[1])
                        + ", " + Math.abs(linearColor[2] - palLinearColor[2])
                        + ", sRGB diff " + getDiff(color, palette[paletteIndex[index]], 16)
                        + ", " + getDiff(color, palette[paletteIndex[index]], 8)
                        + ", " + getDiff(color, palette[paletteIndex[index]], 0)
                        + ", lumaDiff " + Math.abs(colLuma - lumaFromSRGB(palette[paletteIndex[index]]))
                        + ", brightness " + brightness(palette[paletteIndex[index]]));

                switch (mode) {
                    case XYY:
                        sb.append(", XYY " + xyz[0] + ", " + xyz[1] + ", " + xyz[2]
                                + ", zDiff " + diff[index][1]);
                        break;
                    case DOTPRODUCT:
                        sb.append(", Length diff " + diff[index][1]);
                        break;
                    default:
                }
                sb.append("\n");
                index = next[index];
                counter++;
            }
            return sb.toString();
        }

    }

    private float getSaturation(int color) {
        int[] colors = getColors(color);
        int total = colors[0] + colors[1] + colors[2];
        int max = Math.max(colors[0], Math.max(colors[1], colors[2]));
        if (max == 0) {
            return 0;
        }
        int min = Math.min(colors[0], Math.min(colors[1], colors[2]));
        int mid = total - max - min;
        if (min == 0) {
            min = mid;
            if (max - min == 0) {
                return 0;
            }
        }
        return (float) (max - min) / max;
    }

    private float getSaturation(float[] colors) {
        float total = colors[0] + colors[1] + colors[2];
        float max = Math.max(colors[0], Math.max(colors[1], colors[2]));
        if (max == 0) {
            return 0;
        }
        float min = Math.min(colors[0], Math.min(colors[1], colors[2]));
        if (min == 0) {
            min = total - max;
        }
        float delta = max - min;
        return delta / max;
    }

    private int getDeltaRGBWeighted(int color, int[] palette) {
        LinkedList list = new LinkedList(palette.length, color, palette);
        float[] linearRGB = toLinearRGBFloat(color);
        for (int colorIndex = 0; colorIndex < palette.length; colorIndex++) {
            float[] linearPalRGB = toLinearRGBFloat(palette[colorIndex]);
            float diffR = (float) Math.pow(Math.abs(linearRGB[0] - linearPalRGB[0]), 1.5f);
            float diffG = (float) Math.pow(Math.abs(linearRGB[1] - linearPalRGB[1]), 1.5f);
            float diffB = (float) Math.pow(Math.abs(linearRGB[2] - linearPalRGB[2]), 1.5f);
            float maxDiff = Math.max(Math.max(diffR, diffG), diffB);
            float minDiff = Math.min(Math.min(diffR, diffG), diffB);
            float delta = maxDiff - minDiff;
            float weight = 1;
            if (delta > 0) {
                weight = 1 + (delta / maxDiff) * 1f;
            }
            list.put(colorIndex, diffR * weight + diffG * weight + diffB * weight, diffR * weight);
        }
        int pIndex = list.getFirstPaletteIndex();
        int pColor = palette[pIndex];
        return pIndex;
    }

    private int getDeltaDotProduct(int color, int[] palette) {
        int[] colors = getColors(color);
        float[] vector = getVector(colors);
        adjustForSensitivity(vector);
        float[] normalized = Vec3.normalize(vector);
        float brightness = getBrightness(color);
        if (brightness < 6) {
            return getDeltaRGBDiff(color, palette);
        }
        LinkedList list = new LinkedList(palette.length, color, palette);
        float[] diffVector = new float[3];
        for (int colorIndex = 0; colorIndex < palette.length; colorIndex++) {
            int[] paletteColors = getColors(palette[colorIndex]);
            float[] paletteVector = getVector(paletteColors);
            adjustForSensitivity(paletteVector);
            float[] normalizedPalette = Vec3.normalize(paletteVector);
            Vec3.subtract(vector, 0, paletteVector, 0, diffVector, 0);
            float dot = Vec3.dot(normalized, 0, normalizedPalette, 0);
            float diff = Vec3.length(diffVector, 0);
            dot = dot > 1 ? 1 : dot; // for rounding errors
            float paletteBrightness = getBrightness(palette[colorIndex]);
            float brightnessDiff = Math.abs(paletteBrightness - brightness);
            if (brightnessDiff < 50) {
                float value = (1.0f - dot); // + (0.001f) * (brightnessDiff / 256);
                list.put(colorIndex, value, diff);
            }
        }
        return getPaletteIndex(list, null);
    }

    private int getDeltaDotRGB(int color, int[] palette) {
        float saturation = getSaturation(color);
        int[] colors = getColors(color);
        float[] vector = getVector(colors);
        adjustForSensitivity(vector);
        // float saturation = getSaturation(vector);
        float[] normalized = Vec3.normalize(vector);
        float brightness = getBrightness(color);
        if (brightness < 6) {
            return getDeltaRGBDiff(color, palette);
        }
        LinkedList list = new LinkedList(palette.length, color, palette);
        for (int colorIndex = 0; colorIndex < palette.length; colorIndex++) {
            float paletteSaturation = getSaturation(palette[colorIndex]);
            int[] paletteColors = getColors(palette[colorIndex]);
            float[] paletteVector = getVector(paletteColors);
            adjustForSensitivity(paletteVector);
            // float paletteSaturation = getSaturation(paletteVector);
            float[] normalizedPalette = Vec3.normalize(paletteVector);
            float dot = Vec3.dot(normalized, 0, normalizedPalette, 0);
            dot = dot > 1 ? 1 : dot; // for rounding errors
            float paletteBrightness = getBrightness(palette[colorIndex]);
            float brightnessDiff = Math.abs(paletteBrightness - brightness);
            if (brightnessDiff < 50) {
                float value = (1.0f - dot) + 0.06f * Math.abs(saturation - paletteSaturation);
                list.put(colorIndex, value, brightnessDiff);
            }
        }
        return getPaletteIndex(list, null);
    }

    private void adjustForSensitivity(float[] colors) {
        colors[0] *= 0.5;
        colors[1] *= 0.3;
        colors[2] *= 0.2;
    }

    private float[] getVector(int[] colors) {
        float[] vector = new float[3];
        vector[0] = colors[0] / 255f;
        vector[1] = colors[1] / 255f;
        vector[2] = colors[2] / 255f;
        return vector;
    }

    private int getDeltaColorValue(int color, int[] palette) {
        FilterList filteredList = filterByColorValue(palette, color);
        int[] filteredPalette = filteredList.getPalette();
        int filteredIndex = getDeltaRGBDiff(color, filteredPalette);
        return filteredList.getPaletteIndex(filteredIndex);

    }

    private int[] toIntArray(Integer[] list) {
        int[] result = new int[list.length];
        for (int i = 0; i < list.length; i++) {
            result[i] = list[i];
        }
        return result;
    }

    private class FilterList {
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        private void add(int index, int color) {
            indexes.add(index);
            colors.add(color);
        }

        private int[] getPalette() {
            int[] result = new int[indexes.size()];
            Integer[] array = colors.toArray(new Integer[0]);
            for (int i = 0; i < result.length; i++) {
                result[i] = array[i];
            }
            return result;
        }

        private int getPaletteIndex(int filteredIndex) {
            return indexes.get(filteredIndex);
        }

    }

    private FilterList filterByColorValue(int[] palette, int color) {
        int[] colorValue = getColorValue(color);
        Logger.d(getClass(), "Filter palette for color " + Integer.toHexString(color) + ", chroma " + getString(
                colorValue));
        int brightness = getBrightness(color);
        FilterList filteredList = new FilterList();
        for (int pIndex = 0; pIndex < palette.length; pIndex++) {
            int[] pColorValue = getColorValue(palette[pIndex]);
            int[] diff = getDiff(colorValue, pColorValue);
            int pBrightness = getBrightness(palette[pIndex]);
            if (diff[0] < 20 & diff[1] < 20 & diff[2] < 30 & Math.abs(brightness - pBrightness) < 75) {
                Logger.d(getClass(), "Adding color " + Integer.toHexString(palette[pIndex]) + ", chroma " + getString(
                        pColorValue) + ", chroma diff "
                        + getString(diff));
                filteredList.add(pIndex, palette[pIndex]);
            }
        }
        return filteredList;
    }

    private int getBrightness(int color) {
        int[] colors = getColors(color);
        return Math.max(colors[0], Math.max(colors[1], colors[2]));
    }

    private int[] getDiff(int[] color1, int[] color2) {
        int[] result = new int[color1.length];
        for (int i = 0; i < color1.length; i++) {
            result[i] = Math.abs(color1[i] - color2[i]);
        }
        return result;
    }

    private float[] getColorValueFloat(int color) {
        int[] colorValue = getColors(color);
        int max = colorValue[0] + colorValue[1] + colorValue[2];
        if (max == 0) {
            return new float[] { 0.334f, 0.334f, 0.334f };
        } else {
            return new float[] { (((float) colorValue[0] / max)), (((float) colorValue[0] / max)),
                    (((float) colorValue[0] / max)) };
        }
    }

    private int[] getColorValue(int color) {
        if (color == 0) {
            color = 0x010101;
        }
        int[] colorValue = getColors(color);
        // int max = Math.max(linear[0], Math.max(linear[1], linear[2]));
        int max = colorValue[0] + colorValue[1] + colorValue[2];
        if (max == 0) {
            colorValue[0] = 33;
            colorValue[1] = 33;
            colorValue[2] = 33;
        } else {
            colorValue[0] = (int) (((float) colorValue[0] / max) * 100);
            colorValue[1] = (int) (((float) colorValue[1] / max) * 100);
            colorValue[2] = (int) (((float) colorValue[2] / max) * 100);
        }
        return colorValue;
    }

    private int[] getColors(int color) {
        int[] colors = null;
        switch (space) {
            case LINEAR:
                colors = toLinearRGB(color);
                break;
            case sRGB:
                colors = toArray(color);
                break;
            default:
        }
        return colors;
    }

    private int getDeltaRGBDiff(int color, int[] palette) {
        LinkedList list = new LinkedList(palette.length, color, palette);
        int[] colors = getColors(color);
        for (int colorIndex = 0; colorIndex < palette.length; colorIndex++) {
            int[] paletteColors = getColors(palette[colorIndex]);
            float diffR = Math.abs(colors[0] - paletteColors[0]);
            float diffG = Math.abs(colors[1] - paletteColors[1]);
            float diffB = Math.abs(colors[2] - paletteColors[2]);
            float[] adjustedDiff = new float[3];
            if ((colors[0] + paletteColors[0]) / 2 < 128) {
                adjustedDiff[0] = diffR * 2;
                adjustedDiff[1] = diffG * 4;
                adjustedDiff[2] = diffB * 3;
            } else {
                adjustedDiff[0] = diffR * 3;
                adjustedDiff[1] = diffG * 4;
                adjustedDiff[2] = diffB * 2;
            }

            float maxDiff = Math.max(Math.max(diffR, diffG), diffB);
            float minDiff = Math.min(Math.min(diffR, diffG), diffB);
            float delta = maxDiff - minDiff;
            if (delta == 0) {
                delta = 1;
            }
            // float factor = (1 + (delta / 70));
            float factor = (1);
            float totalDiff = adjustedDiff[0] * adjustedDiff[0] + adjustedDiff[1] * adjustedDiff[1] + adjustedDiff[2]
                    * adjustedDiff[2];
            list.put(colorIndex, totalDiff * factor, maxDiff);
            // if ((r + ((color >>> 16) & 0xff)) / 2 < 128) {
            // delta = (int) Math.sqrt(Math.pow(2 * deltaR, 2) + Math.pow(4 * deltaG, 2) + Math.pow(3 * deltaB, 2));
            // } else {
            // delta = (int) Math.sqrt(Math.pow(3 * deltaR, 2) + Math.pow(4 * deltaG, 2) + Math.pow(2 * deltaB, 2));
            // }
        }
        return getPaletteIndex(list, null);
    }

    private float[] toXYY(int color, ColorSpace cs) {
        if (color == 0) {
            color = 0x010101;
        }
        float[] xyz = cs.fromRGB(new float[] { ((color >>> 16) & 0xff) / 255f, ((color >>> 8) & 0xff) / 255f, (color
                & 0xff) / 255f });
        float total = xyz[0] + xyz[1] + xyz[2];
        if (total > 0) {
            xyz[0] = (xyz[0] / total) * 255;
            xyz[1] = (xyz[1] / total) * 255;
            xyz[2] = xyz[2] * 255;
        }
        return xyz;
    }

    private float[] toXYZ(int color, ColorSpace cs) {
        float[] xyz = cs.fromRGB(new float[] { ((color >>> 16) & 0xff) / 255f, ((color >>> 8) & 0xff) / 255f, (color
                & 0xff) / 255f });
        xyz[0] = xyz[0] * 255f;
        xyz[1] = xyz[1] * 255f;
        xyz[2] = xyz[2] * 255f;
        return xyz;
    }

    private int getDeltaXYZ(int color, int[] palette) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);
        // int linearColor = getColor(toLinearRGB(color));
        LinkedList list = new LinkedList(palette.length, color, palette);
        float[] xyy = toXYY(color, cs);
        for (int colorIndex = 0; colorIndex < palette.length; colorIndex++) {
            // int paletteColor = getColor(toLinearRGB(palette[colorIndex]));
            float[] palXyy = toXYY(palette[colorIndex], cs);
            float diffX = (float) Math.pow((xyy[0] - palXyy[0]), 2);
            float diffY = (float) Math.pow((xyy[1] - palXyy[1]) * 4, 2);
            float diffZ = (float) Math.pow((xyy[2] - palXyy[2]), 2);
            list.put(colorIndex, (float) Math.sqrt(diffX + diffY), diffZ);
        }
        return getPaletteIndex(list, cs);
    }

    private int getPaletteIndex(LinkedList list, ColorSpace cs) {
        int pIndex = list.getFirstPaletteIndex();
        // Logger.d(getClass(), "Mapped sRGB " + Integer.toHexString(list.color) + " to " + Integer.toHexString(
        // list.palette[pIndex]));
        if (cs != null) {
            float[] xyy = toXYY(list.color, cs);
            float[] palXyy = toXYY(list.palette[pIndex], cs);
            Logger.d(getClass(), "Color xYY " + getString(palXyy) + ", palettecolor xYY " + getString(xyy));
        }
        return pIndex;
    }

    private String getString(float... values) {
        StringBuffer sb = new StringBuffer();
        for (float v : values) {
            sb.append(Float.toString(v));
            if (sb.length() > 0) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private String getString(int... values) {
        StringBuffer sb = new StringBuffer();
        for (int v : values) {
            sb.append(Integer.toString(v));
            if (sb.length() > 0) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private int getDeltaHSB(int color, int[] palette) {
        LinkedList list = new LinkedList(palette.length, color, palette);
        float[] hsb = toHSBFromsRGB(color);
        float hue = hsb[0] - 180f;
        for (int colorIndex = 0; colorIndex < palette.length; colorIndex++) {
            float[] palHSB = toHSBFromsRGB(palette[colorIndex]);
            float palHue = palHSB[0] - 180f;
            float diff = Math.abs(hue - palHue);
            list.put(colorIndex, diff, Math.abs(hsb[2] - palHSB[2]));
        }
        int pIndex = list.getFirstPaletteIndex();
        int pColor = palette[pIndex];
        return pIndex;
    }

    private float[] toHSBFromsRGB(int color) {
        float[] hsb = new float[3];
        Color.RGBtoHSB((color >>> 16) & 0xff, (color >>> 8) & 0xff, color & 0xff, hsb);
        float hue = hsb[0] * 360f;
        if (hue > 360) {
            throw new IllegalArgumentException();
        }
        hsb[0] = hue;
        return hsb;
    }

    private int getDiff(int col1, int col2, int shift) {
        return Math.abs(((col1 >>> shift) & 0xff) - ((col2 >>> shift) & 0xff));
    }

    private int getDeltaChroma(int color, int index, int[] palette) {
        LinkedList list = new LinkedList(palette.length, color, palette);
        float[] chroma = toChromaInt(color);
        for (int colorIndex = 0; colorIndex < palette.length; colorIndex++) {
            float[] palChroma = toChromaInt(palette[colorIndex]);
            int diffR = (int) Math.pow((palChroma[0] * 255 - chroma[0] * 255), 2);
            int diffG = (int) Math.pow((palChroma[1] * 255 - chroma[1] * 255), 2);
            int diffB = (int) Math.pow((palChroma[2] * 255 - chroma[2] * 255), 2);
            int diffL = (int) (Math.abs(palChroma[3] * 255 - chroma[3] * 255));
            // calc.compare(diffR + diffG + diffB + diffL, colorIndex);
            list.put(colorIndex, diffR, diffG, diffB, diffL);
        }
        return list.getFirstPaletteIndex();
    }

    private float f(float x) {
        if (x > 216.0f / 24389.0f) {
            return (float) Math.cbrt(x);
        } else {
            return (841.0f / 108.0f) * x + N;
        }
    }

    private static final float N = 4.0f / 29.0f;

    private int brightness(int color) {
        int r = ((color >>> 16) & 0xff);
        int g = ((color >>> 8) & 0xff);
        int b = ((color) & 0xff);
        return Math.max(r, Math.max(g, b));
    }

    private float lumaFromSRGB(int color) {
        int r = ((color >>> 16) & 0xff);
        int g = ((color >>> 8) & 0xff);
        int b = ((color) & 0xff);
        return (0.2126f * sRGBToLinear(r) + 0.7152f * sRGBToLinear(g) + 0.0722f * sRGBToLinear(b));
    }

    private int getColor(int[] rgb) {
        int color = (rgb[0] & 0xff) | ((rgb[1] << 8) & 0xff00) | (rgb[2] << 16) & 0xff0000;
        return color;
    }

    private int[] toArray(int color) {
        int r = ((color >>> 16) & 0xff);
        int g = ((color >>> 8) & 0xff);
        int b = ((color) & 0xff);
        return new int[] { r, g, b };
    }

    private int[] toLinearRGBInt(int r, int g, int b) {
        int[] rgb = new int[3];
        rgb[0] = (int) (sRGBToLinear(r) * 255);
        rgb[1] = (int) (sRGBToLinear(g) * 255);
        rgb[2] = (int) (sRGBToLinear(b) * 255);
        return rgb;
    }

    private float[] toLinearRGBFloat(int r, int g, int b) {
        float[] rgb = new float[3];
        rgb[0] = sRGBToLinear(r);
        rgb[1] = sRGBToLinear(g);
        rgb[2] = sRGBToLinear(b);
        return rgb;
    }

    private int[] toLinearRGB(int color) {
        int r = ((color >>> 16) & 0xff);
        int g = ((color >>> 8) & 0xff);
        int b = ((color) & 0xff);
        return toLinearRGBInt(r, g, b);
    }

    private float[] toLinearRGBFloat(int color) {
        int r = ((color >>> 16) & 0xff);
        int g = ((color >>> 8) & 0xff);
        int b = ((color) & 0xff);
        return toLinearRGBFloat(r, g, b);
    }

    private float[] toChromaInt(int color) {
        int r = ((color >>> 16) & 0xff);
        int g = ((color >>> 8) & 0xff);
        int b = ((color) & 0xff);
        return toChromaInt(r, g, b);
    }

    private float[] toChromaInt(int r, int g, int b) {
        float linearR = sRGBToLinear(r);
        float linearG = sRGBToLinear(g);
        float linearB = sRGBToLinear(b);
        float total = linearR + linearG + linearB;
        float[] chroma = new float[4];
        if (total > 0) {
            chroma[0] = linearR / total;
            chroma[1] = linearG / total;
            chroma[2] = linearB / total;
        }
        float luma = (0.2126f * sRGBToLinear(r) + 0.7152f * sRGBToLinear(g) + 0.0722f * sRGBToLinear(b));
        luma = (float) Math.pow(luma, 2.4f);
        // float luma = (0.2126f * r + 0.7152f * g + 0.0722f * b);
        float pLuma = (float) (Math.pow(luma, (1.0 / 3)) * 116 - 16);
        // luma = (0.2126f * r + 0.7152f * g + 0.0722f * b);
        chroma[3] = luma;
        return chroma;
    }

    private int[] toSRGBInt(int r, int g, int b) {
        int[] sRGB = new int[3];
        sRGB[0] = (int) (linearTosRGB(r) * 255);
        sRGB[1] = (int) (linearTosRGB(g) * 255);
        sRGB[2] = (int) (linearTosRGB(b) * 255);
        return sRGB;
    }

    private float sRGBToLinear(int col) {
        float color = col / 255f;
        if (color < 0.04045f) {
            return color / 12.92f;
        }
        return (float) Math.pow(((color + 0.055f) / 1.055f), 2.4f);
    }

    private float linearTosRGB(int col) {
        float color = col / 255f;
        return (float) Math.pow(((color * 1.055)), 1 / 2.4f) - 0.55f;
    }

    private class DeltaCalculator {
        int maxDelta = Integer.MAX_VALUE;
        int totalDiff = Integer.MAX_VALUE;
        float totalFloatDiff = Float.MAX_VALUE;
        int index;

        private void compare(int diff, int i) {
            if (diff <= totalDiff) {
                totalDiff = diff;
                index = i;
            }
        }

        private void compare(float floatDiff, int i) {
            if (floatDiff <= totalFloatDiff) {
                totalFloatDiff = floatDiff;
                index = i;
            }
        }
    }

    /**
     * Loads bitnmap and converts to voxel data
     * 
     * @param bitmap Path to bitmap, including asset directory. Eg "assets/voxels/bob-small-transparency.png"
     * @param spacing
     * @param palette
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public VoxelData convert(String bitmap, float[] spacing, int[] palette) throws URISyntaxException, IOException {
        long start = System.currentTimeMillis();
        String path = FileUtils.getInstance().getFolder(bitmap);
        String imagePath = FileUtils.getInstance().getResourcePath(path);
        String filename = imagePath + bitmap;
        ImageReader reader = ImageReader.getImageReader(filename);
        ImageHeader header = reader.read(Path.of(filename));
        int w = header.getWidth();
        int h = header.getHeight();
        ImageFormat format = header.getFormat();
        ImageBuffer data = header.getData();
        switch (format) {
            case VK_FORMAT_A8B8G8R8_UNORM_PACK32:
                ByteArrayImageBuffer bData = (ByteArrayImageBuffer) data;
                byte[] pixels = bData.getAsByteArray();
                VoxelData result = toVoxels(pixels, w, h, spacing, palette, new float[] { 0f, 0f, 0f });
                Logger.d(getClass(), "Load and convert of voxels took " + (System.currentTimeMillis() - start)
                        + " milliseconds");
                return result;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Not supported format "
                        + format);
        }
    }

}


package org.varg.vulkan.structs;

import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Vulkan10.QueueFlagBit;

public class QueueFamilyProperties {

    protected final int queueIndex;
    protected final QueueFlagBit[] queueFlags;
    protected final int queueCount;
    protected final int timestampValidBits;
    protected final Extent3D minImageTransferGranularity;
    protected final boolean surfaceSupportsPresent;

    public QueueFamilyProperties(int queueIndex, QueueFlagBit[] queueFlags, int queueCount, int timestampValidBits,
            Extent3D minImageTransferGranularity, boolean surfaceSupportsPresents) {
        this.queueIndex = queueIndex;
        this.queueFlags = queueFlags;
        this.queueCount = queueCount;
        this.timestampValidBits = timestampValidBits;
        this.minImageTransferGranularity = minImageTransferGranularity;
        this.surfaceSupportsPresent = surfaceSupportsPresents;
    }

    /**
     * Returns the queue support flags
     * 
     * @return
     */
    public int getFlagsValue() {
        return BitFlags.getFlagsValue(queueFlags);
    }

    /**
     * Returns true if queue has support for the queueFlag
     * 
     * @param queueFlag
     * @return
     */
    public boolean getFlagsValue(QueueFlagBit queueFlag) {
        return (queueFlag.getValue() & getFlagsValue()) != 0;
    }

    /**
     * Returns true if queue family supports present
     * 
     * @return
     */
    public boolean isSurfaceSupportsPresent() {
        return surfaceSupportsPresent;
    }

    /**
     * Returns the index of the queue
     * 
     * @return
     */
    public int getQueueIndex() {
        return queueIndex;
    }

    /**
     * Returns the queuecount
     * 
     * @return
     */
    public int getQueueCount() {
        return queueCount;
    }

    @Override
    public String toString() {
        return QueueFlagBit.toString(getFlagsValue()) + " - surface present: " +
                isSurfaceSupportsPresent() + "\n";
    }

}

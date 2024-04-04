
package org.varg.vulkan.descriptor;

import java.util.HashMap;

import org.gltfio.lib.ErrorMessage;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;

/**
 * Helper class to manage descriptorset layouts and descriptor sets
 * This can be used to store the layouts and descriptors needed for a number of pipelines.
 * Descriptor set index can be fetched by calling {@link #getDescriptorSetIndex(DescriptorSetTarget)}
 */
public class Descriptors {

    HashMap<DescriptorSetTarget, DescriptorSetLayout> setLayoutsMap =
            new HashMap<DescriptorSetTarget, DescriptorSetLayout>();
    HashMap<DescriptorSetTarget, DescriptorSet> descriptorSetsMap = new HashMap<DescriptorSetTarget, DescriptorSet>();

    /**
     * Returns the descriptorset layout for the target - or null if not added
     * 
     * @param target
     * @return
     */
    public DescriptorSetLayout getDescriptorLayout(DescriptorSetTarget target) {
        return setLayoutsMap.get(target);
    }

    /**
     * Stores the layout as value for the target - fetch by calling {@link #getDescriptorLayout(DescriptorSetTarget)}
     * Throws exception if layout already present for target.
     * 
     * @param target
     * @param layout
     * @throws IllegalArgumentException If layout already present for target, or if layout is null
     */
    public void addDescriptorLayout(DescriptorSetTarget target, DescriptorSetLayout layout) {
        if (layout == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Layout is null for target "
                    + target);
        }
        if (setLayoutsMap.containsKey(target)) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + "Already contains layout for " + target);
        }
        setLayoutsMap.put(target, layout);
    }

    /**
     * Stores the descriptorset for the target - fetch by calling {@link #getDescriptorSet(DescriptorSetTarget)}
     * Throws exception if descriptorset already present for the target.
     * 
     * @param target
     * @param descriptorSet
     * @throws IllegalArgumentException If descriptorset already present for target
     */
    public void addDescriptorSet(DescriptorSetTarget target, DescriptorSet descriptorSet) {
        if (descriptorSetsMap.containsKey(target)) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + "Already contains descriptorset for " + target);
        }
        descriptorSetsMap.put(target, descriptorSet);
    }

    /**
     * Removes and returns the descriptorsets for the target, returns null if no descriptorset present for target
     * 
     * @param target
     * @return
     */
    public DescriptorSet removeDescriptorSet(DescriptorSetTarget target) {
        return descriptorSetsMap.remove(target);
    }

    /**
     * Removes and returns the descriptorlayout of rht target, returns null if no layout present for target
     * 
     * @param target
     * @return
     */
    public DescriptorSetLayout removeDescriptorLayout(DescriptorSetTarget target) {
        return setLayoutsMap.remove(target);
    }

    /**
     * Returns the descriptorset for the target - or null if not added by calling
     * {@link #addDescriptorSet(DescriptorSetTarget, DescriptorSet)}
     * 
     * @param target
     * @return
     */
    public DescriptorSet getDescriptorSet(DescriptorSetTarget target) {
        return descriptorSetsMap.get(target);
    }

    /**
     * Returns the descriptorset layout as an array
     * The order of the returned layouts is important as it will control what sets get what index, this MUST
     * match that of the defined set indexes in shader programs.
     * 
     * @return
     */
    public DescriptorSetLayout[] getDescriptorLayouts(DescriptorSetTarget... targets) {
        if (targets == null || targets.length < 1) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "No descriptorset targets");
        }
        DescriptorSetLayout[] result = new DescriptorSetLayout[targets.length];
        for (DescriptorSetTarget target : targets) {
            DescriptorSetLayout l = setLayoutsMap.get(target);
            result[target.getSet()] = l;
        }
        return result;
    }

}

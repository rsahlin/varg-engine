package org.varg.vulkan.extensions;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.vulkan.structs.Extent2D;

public abstract class KHRFragmentShadingRate {

    public enum FragmentShadingRateCombinerOpKHR {
        VK_FRAGMENT_SHADING_RATE_COMBINER_OP_KEEP_KHR(0),
        VK_FRAGMENT_SHADING_RATE_COMBINER_OP_REPLACE_KHR(1),
        VK_FRAGMENT_SHADING_RATE_COMBINER_OP_MIN_KHR(2),
        VK_FRAGMENT_SHADING_RATE_COMBINER_OP_MAX_KHR(3),
        VK_FRAGMENT_SHADING_RATE_COMBINER_OP_MUL_KHR(4);

        public final int value;

        private FragmentShadingRateCombinerOpKHR(int value) {
            this.value = value;
        }

    }

    public static class PipelineFragmentShadingRateStateCreateInfoKHR {

        public final Extent2D fragmentSize;
        public final FragmentShadingRateCombinerOpKHR[] combinerOps;

        public PipelineFragmentShadingRateStateCreateInfoKHR(int sizeX, int sizeY) {
            fragmentSize = new Extent2D(sizeX, sizeY);
            combinerOps = new FragmentShadingRateCombinerOpKHR[] {
                    FragmentShadingRateCombinerOpKHR.VK_FRAGMENT_SHADING_RATE_COMBINER_OP_KEEP_KHR,
                    FragmentShadingRateCombinerOpKHR.VK_FRAGMENT_SHADING_RATE_COMBINER_OP_KEEP_KHR };
        }

    }

    private final PhysicalDeviceFragmentShadingRateFeaturesKHR fragmentShadingRateFeatures;
    private final PhysicalDeviceFragmentShadingRatePropertiesKHR fragmentShadingRateProperties;

    protected KHRFragmentShadingRate(@NonNull PhysicalDeviceFragmentShadingRateFeaturesKHR fragmentShadingRateFeatures,
            @NonNull PhysicalDeviceFragmentShadingRatePropertiesKHR fragmentShadingRateProperties) {
        this.fragmentShadingRateFeatures = fragmentShadingRateFeatures;
        this.fragmentShadingRateProperties = fragmentShadingRateProperties;
    }

}

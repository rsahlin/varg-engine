package org.varg.renderer;

import org.gltfio.lib.Settings.FloatProperty;

/**
 * Controls the BRDF behavior.
 */
public class BRDF {

    public static final float DEFAULT_NDF_FACTOR = 1.0f;
    public static final float DEFAULT_MIN_ROUGHNESS = 0.003f;

    public enum BRDFFloatProperties implements FloatProperty {
        /**
         * Controls the global amount of light that is reflected off materials - used as a main factor for controlling amount of reflected light, via the shader ndf.
         */
        NDF_FACTOR("brdf.ndffactor", DEFAULT_NDF_FACTOR),
        /**
         * Fudge value for controlling the size of specular lobe - since the NDF does not use solidangle this is based on minimum roughness
         */
        SOLIDANGLE_FUDGE("brdf.solidangle", DEFAULT_MIN_ROUGHNESS);

        private final String key;
        private final String defaultValue;

        BRDFFloatProperties(String key, Float defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue != null ? Float.toString(defaultValue) : null;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return defaultValue;
        }

    }

    private BRDF() {

    }

}


package org.varg.scene;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONCamera;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.KHRLightsPunctualReference;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.Light;
import org.gltfio.lib.Logger;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.structs.Extent2D;

/**
 * Class used to control behavior of scene manipulation - rotation, translate and scale
 * This can be used as the main UI control
 *
 */
public class GltfSceneControl extends SceneControl {

    public GltfSceneControl(@NonNull JSONCamera camera, @NonNull VulkanRenderableScene scene, @NonNull Extent2D screenSize) {
        super(scene, camera, screenSize);
    }

    /**
     * Adjusts the nodes containing punctual lights according to intensityFactor
     * 
     * @param delta
     * @param intensityFactor
     */
    public void adjustSceneLights(float delta, float intensityFactor) {
        JSONNode[] lights = scene.getLightNodes();
        if (lights != null) {
            for (JSONNode lightNode : lights) {
                KHRLightsPunctualReference lightRef = lightNode.getLight();
                if (lightRef != null) {
                    Light light = lightRef.getLight();
                    light.setIntensity(light.getIntensity() + delta * intensityFactor);
                    Logger.d(getClass(), "Lightintensity: " + light.getIntensity());
                }
            }
        }
    }

}

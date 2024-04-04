package org.varg.lwjgl3.apps;

import java.io.IOException;
import java.net.URISyntaxException;

import org.gltfio.deserialize.Ladda;
import org.gltfio.deserialize.Ladda.LaddaDefault;
import org.gltfio.glb2.Glb2Writer;
import org.gltfio.gltf2.AssetBaseObject;
import org.gltfio.lib.FileUtils.FilesystemProperties;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.varg.gltf.VulkanStreamingGltf;
import org.varg.renderer.Renderers;

/**
 * 
 */
public class LWJGL3Glb2Test extends VARGViewer {

    private boolean saveOutput = false;

    protected LWJGL3Glb2Test(String[] args, Renderers version, String title) {
        super(args, version, title);
    }

    public static void main(String[] args) {
        Settings.getInstance().setProperty(FilesystemProperties.JAVA_TARGET_DIRECTORY, "target/test-classes");
        Settings.getInstance().setProperty(FilesystemProperties.SOURCE_DIRECTORY, "src/test");
        LWJGL3Glb2Test test = new LWJGL3Glb2Test(args, Renderers.VULKAN13, "Streaming glTF test");
        test.createApp();
        test.run();
    }

    @Override
    protected void fetchGltfFilenames() throws IOException, URISyntaxException {
        // Nothing to do
    }

    @Override
    protected void loadDefaultModel() throws URISyntaxException, IOException {
        try {
            // String path = "C:/assets/test-assets/gltf/shaf";
            // String filename = "0000d5d9-b08a-4472-8bf8-2126c8e06edb";
            String path = "C:/assets/test-assets/gltf/10000asteroids";
            String filename = "asteroid_belt_01";
            String outpath = "C:/assets/test-assets/glb2/";
            if (saveOutput) {
                AssetBaseObject asset;
                asset = LaddaDefault.getInstance().loadGltf(path, filename + ".glb", null, null);
                Glb2Writer writer = Glb2Writer.getInstance();
                writer.write(asset.getScene(0), outpath + filename + ".glb2");
                Logger.d(getClass(), "Written " + filename + ".glb2");
            }
            Ladda.getInstance(VulkanStreamingGltf.class).loadStreamingGltf(outpath, filename + ".glb2", null,
                    null, this);

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

package org.varg.lwjgl3.apps;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;

import org.gltfio.deserialize.LaddaProperties;
import org.gltfio.gltf2.JSONCamera;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.ImageUtils;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.gltfio.lib.Settings.StringProperty;
import org.gltfio.lib.ThreadService;
import org.ktximageio.ktx.AwtImageUtils;
import org.ktximageio.ktx.ImageReader.ImageFormat;
import org.varg.renderer.Renderers;
import org.varg.vulkan.Features;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.Filter;
import org.varg.vulkan.Vulkan10.ImageAspectFlagBit;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.ImageTiling;
import org.varg.vulkan.Vulkan10.MemoryPropertyFlagBit;
import org.varg.vulkan.Vulkan10Backend.CreateDevice;
import org.varg.vulkan.Vulkan13.AccessFlagBits2;
import org.varg.vulkan.Vulkan13.PipelineStateFlagBits2;
import org.varg.vulkan.VulkanBackend.BackendIntProperties;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;
import org.varg.vulkan.VulkanBackend.IntArrayProperties;
import org.varg.vulkan.VulkanRenderableScene;
import org.varg.vulkan.image.Image;
import org.varg.vulkan.image.ImageCreateInfo;
import org.varg.vulkan.image.ImageSubresourceLayers;
import org.varg.vulkan.image.ImageSubresourceRange;
import org.varg.vulkan.image.ImageView;
import org.varg.vulkan.memory.ImageMemory;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.Extent3D;
import org.varg.vulkan.structs.RequestedFeatures;

public class ScreenGrabber extends LWJGL3Application implements CreateDevice {

    public enum OutputFormat {
        PNG("png"),
        JPEG("jpg");

        public final String type;

        OutputFormat(String type) {
            this.type = type;
        }

        public static OutputFormat get(String format) {
            if (format != null) {
                for (OutputFormat f : values()) {
                    if (f.name().equalsIgnoreCase(format)) {
                        return f;
                    }
                }
            }
            return null;
        }

    }

    public enum ScreenGrabberProperties implements StringProperty {

        /**
         * Specify the source folder
         */
        SOURCE_FOLDER("screengrabber.folder", "C:/assets/test-assets/shaf/"),
        /**
         * Name of file containing input names
         */
        INPUT_FILENAME("screengrabber.input", null),
        /**
         * Specify name of listfile
         */
        LIST_FILENAME("screengrabber.listfilename", null),
        /**
         * Set to true to only list filename - no render or output
         */
        SAVE_LISTFILE("screengrabber.savelistfile", null);

        private final String key;
        private final String defaultValue;

        ScreenGrabberProperties(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return this.name();
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

    public enum VIEWPOINT {

        FRONT_FACING(0, 0, 0, 1),
        THREEDEE_DESIGN(-0.3814576f, -0.1271525f, -0.0423842f, 0.9146179f),
        FLOORPLAN(-0.65f, 0, 0, 0.7071068f);

        VIEWPOINT(float... rotation) {
            this.rotation = rotation;

        }

        public final float[] rotation;
    }

    private static class CameraSetup {
        private final float[] rotation;
        private final String name;

        private CameraSetup(VIEWPOINT viewpoint) {
            this.rotation = viewpoint.rotation;
            this.name = viewpoint.name().toLowerCase();
        }

    }

    private class ImageSaveRunnable implements Runnable {

        private final String filename;
        private final Vulkan10.Format format;
        private final ByteBuffer buffer;
        private final int viewPointIndex;
        private final int[] size;
        private final Semaphore semaphore;

        private ImageSaveRunnable(Semaphore semaphore, String filename, int viewPointIndex, Vulkan10.Format format, ByteBuffer buffer, int... size) {
            this.filename = filename;
            this.format = format;
            this.buffer = buffer;
            this.size = size;
            this.viewPointIndex = viewPointIndex;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                // Copy to format that is AWT compatible - 8 bit BGR
                switch (format) {
                    case VK_FORMAT_B8G8R8A8_UNORM:
                        byte[] source = new byte[buffer.capacity()];
                        buffer.position(0).get(source);
                        byte[] destination = new byte[size[0] * size[1] * 3];
                        ImageUtils.getInstance().convertBGRAToBGR(source, destination, size[0], size[1]);
                        BufferedImage bImg = AwtImageUtils.toBufferedImage(destination, size[0], size[1],
                                ImageFormat.VK_FORMAT_B8G8R8_UNORM);
                        try {
                            if (!dryRun) {
                                int index = filename.indexOf(".");
                                String outputname = outputFolder + filename.substring(0, index) + "_" + viewpoints[viewPointIndex].name + "." + outputFormat.type;
                                saveImage(bImg, outputname, outputFormat.type);
                                Logger.d(getClass(), "Convert and save " + outputname + " " + size[0] + "," + size[1] + " " + (System.currentTimeMillis() - timeStart) + " millis");
                            }
                        } catch (IOException e) {
                            Logger.d(getClass(), "Exception saving to " + filename + "\n" + e.toString());
                            throw new RuntimeException(e);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "NO support for format " + format);
                }
            } catch (Exception e) {
                Logger.d(getClass(), "Exception saving " + filename + "\n" + e.toString());
                saveException = e;
            } finally {
                semaphore.release();
            }
        }

    }

    private static final CameraSetup[] DEFAULT_CAMERAS = new CameraSetup[] { new CameraSetup(VIEWPOINT.FRONT_FACING),
            new CameraSetup(VIEWPOINT.THREEDEE_DESIGN), new CameraSetup(VIEWPOINT.FLOORPLAN) };

    public ScreenGrabber(String[] args, Renderers version, String title) {
        super(args, version, title);
    }

    private VulkanRenderableScene scene;
    private JSONCamera camera;
    private int viewpointIndex = 0;
    private CameraSetup[] viewpoints;

    private String shafFolder;
    private String outputFolder;
    private ArrayList<String> shafNames;
    private OutputFormat outputFormat = OutputFormat.PNG;

    private int currentModel = 0;
    private long timeStart;
    // Set to true to skip saving of grabbed screen
    private boolean dryRun = false;
    private Semaphore lock = new Semaphore(0);
    private int lockCount = 0;
    private Exception saveException;

    public static void main(String[] args) {
        Settings.getInstance().setProperty(IntArrayProperties.CLEAR_COLOR, new int[] { 250, 250, 250, 255 });
        Settings.getInstance().setProperty(BackendStringProperties.COLORSPACE, "VK_COLOR_SPACE_SRGB_NONLINEAR_KHR");
        Settings.getInstance().setProperty(BackendStringProperties.SURFACE_FORMAT, "8888_UNORM");
        Settings.getInstance().setProperty(BackendStringProperties.SWAPCHAIN_USAGE, "VK_IMAGE_USAGE_TRANSFER_SRC_BIT");
        Settings.getInstance().setProperty(BackendIntProperties.SAMPLE_COUNT, 8);
        Settings.getInstance().setProperty(BackendIntProperties.MAX_WHITE, 1000);
        Settings.getInstance().setProperty(LaddaProperties.IRRADIANCEMAP, "intensity:600|irmap:STUDIO_5");
        Settings.getInstance().setProperty(LaddaProperties.DIRECTIONAL_LIGHT, "intensity:1000|color:1,1,1|position:0,10000,10000");
        Settings.getInstance().setProperty(BackendIntProperties.SURFACE_WIDTH, 1920);
        Settings.getInstance().setProperty(BackendIntProperties.SURFACE_HEIGHT, 1080);

        ScreenGrabber grabber = new ScreenGrabber(args, Renderers.VULKAN13, "VARG Model Viewer");
        try {
            if (!grabber.setupFiles()) {
                grabber.createApp();
                grabber.run();
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> listFilenames(String path, boolean listFolders, String... extensions) throws IOException,
            URISyntaxException {
        ArrayList<String> folders = (listFolders) ? FileUtils.getInstance().listResourceFolders(path)
                : new ArrayList<String>();
        if (folders.size() == 0) {
            folders.add("");
        }
        ArrayList<String> filenames = FileUtils.getInstance().listFiles(path, folders, extensions);
        Logger.d(getClass(), "Found " + filenames.size() + " files");
        return filenames;

    }

    /**
     * Returns true if the only action shall be to list and save filename - do not render or save anything
     * 
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private boolean setupFiles() throws IOException, URISyntaxException {
        shafFolder = Settings.getInstance().getProperty(ScreenGrabberProperties.SOURCE_FOLDER);
        outputFolder = shafFolder + "png/";
        shafNames = listFilenames(shafFolder, false, ".glb");
        String filename = Settings.getInstance().getProperty(ScreenGrabberProperties.LIST_FILENAME);
        saveFilenames(shafNames, shafFolder, filename != null ? filename : "shaffiles.txt");
        /**
         * Last check the input filename
         */
        String input = Settings.getInstance().getProperty(ScreenGrabberProperties.INPUT_FILENAME);
        if (input != null) {
            shafNames.clear();
            shafNames = loadFilenames(shafFolder, input);
        }
        return Settings.getInstance().getBoolean(ScreenGrabberProperties.SAVE_LISTFILE);
    }

    @Override
    public Features getRequestedDeviceFeatures(Features availableFeatures) {
        RequestedFeatures requestedFeatures = getDefaultRequestedFeatures(availableFeatures);
        return requestedFeatures.getFeatures();
    }

    @Override
    public void init() {
        try {
            viewpoints = DEFAULT_CAMERAS;
            // TODO Move creation of renderer to after loading of glTF JSON (not buffers and textures)
            createRenderer(this);
            createDescriptorPool(10, 0, 0, 10);
            timeStart = System.currentTimeMillis();
            startConsoleInput();
            setInitialized();
            loadModel();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private ArrayList<String> loadFilenames(String folder, String inputname) throws FileNotFoundException, URISyntaxException, IOException {
        FileInputStream fis = new FileInputStream(FileUtils.getInstance().getFile(folder, inputname));
        ArrayList<String> filenames = new ArrayList<String>();
        byte[] data = fis.readAllBytes();

        StringTokenizer st = new StringTokenizer(new String(data), "\n");
        while (st.hasMoreElements()) {
            filenames.add(st.nextToken());
        }
        return filenames;
    }

    private void saveFilenames(List<String> filenames, String folder, String outputname) throws FileNotFoundException,
            URISyntaxException,
            IOException {
        FileOutputStream fos = new FileOutputStream(FileUtils.getInstance().getFile(shafFolder, outputname));
        for (String filename : filenames) {
            fos.write(filename.getBytes());
            fos.write("\n".getBytes());
        }
        fos.flush();
        fos.close();
    }

    @Override
    protected void catchException(Throwable t) {
        Logger.d(getClass(), "Catching throwabe " + t.toString());
        if (currentModel < shafNames.size()) {
            try {
                List<String> remaining = shafNames.subList(currentModel, shafNames.size() - 1);
                saveFilenames(remaining, shafFolder, "remaining_shaffiles.txt");
            } catch (URISyntaxException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        super.catchException(t);
    }

    private void loadModel() {
        loadGltfAsset(shafFolder, shafNames.get(currentModel));
        scene = loadedAsset.getScene(0);
        Logger.d(getClass(), "Load asset " + shafNames.get(currentModel) + " took " + (System.currentTimeMillis()
                - timeStart)
                + " millis");
        // Returns the added default camera
        camera = loadedAsset.getCamera(0);
        camera.translateCamera(0, 0, 2);
        getRenderer().setCamera(camera);

    }

    private boolean hasMoreModels() {
        currentModel++;
        return currentModel < shafNames.size();
    }

    @Override
    protected void drawFrame() {
        if (saveException != null) {
            throw new RuntimeException(saveException);
        }
        timeStart = System.currentTimeMillis();
        camera.setCameraRotation(viewpoints[viewpointIndex].rotation);
        internalDrawFrame(scene);
        ImageView imageView = getRenderer().getSwapBuffer().getCurrentImageView();
        Image image = imageView.getImage();
        Vulkan10.Format format = Vulkan10.Format.get(image.getCreateInfo().formatValue);
        Extent3D size = image.getCreateInfo().extent;
        ImageSubresourceLayers subLayer = new ImageSubresourceLayers(ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT,
                0, 0,
                image.getArrayLayers());
        Queue queue = getRenderer().getQueue();
        queue.queueBegin();
        queue.transitionToLayout(image, ImageLayout.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, subLayer);
        ByteBuffer buffer = getBuffer(image, size);
        ImageSaveRunnable save = new ImageSaveRunnable(lock, shafNames.get(currentModel), viewpointIndex, format, buffer, size.width, size.height);
        ThreadService.getInstance().execute(save);
        lockCount++;
        viewpointIndex++;
        if (viewpointIndex >= viewpoints.length) {
            viewpointIndex = 0;
            deleteAssets(scene);
            loadModel();
            try {
                lock.acquire(lockCount);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            lockCount = 0;
            if (!hasMoreModels()) {
                setRunning(false);
            }
        }
    }

    private void saveImage(BufferedImage img, String filename, String type) throws IOException {
        File outputfile = new File(filename);
        boolean success = ImageIO.write(img, type, outputfile);
        if (!success) {
            Logger.d(getClass(), "Failed to save " + filename);
            throw new IllegalArgumentException();
        }
    }

    private ByteBuffer getBuffer(Image image, Extent3D size) {
        Queue queue = getRenderer().getQueue();
        MemoryBuffer pixels = getRenderer().getBufferFactory().copyFromDeviceMemory(image,
                ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT, queue);
        queue.cmdBufferMemoryBarrier2(PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                AccessFlagBits2.VK_ACCESS_2_MEMORY_READ_BIT.value,
                PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                AccessFlagBits2.VK_ACCESS_2_MEMORY_WRITE_BIT.value, pixels);
        ByteBuffer buffer = Buffers.createByteBuffer((int) pixels.size);
        queue.queueWaitIdle();
        getRenderer().getBufferFactory().copyToBuffer(pixels, buffer);
        getRenderer().getBufferFactory().freeBuffer(pixels);
        return buffer;
    }

    private int getBufferUsingBlit(Image image, ByteBuffer buffer, Extent3D size) {

        ImageSubresourceLayers subLayer = new ImageSubresourceLayers(ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT,
                0, 0,
                image.getArrayLayers());
        Queue queue = getRenderer().getQueue();
        queue.queueBegin();
        queue.transitionToLayout(image, ImageLayout.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, subLayer);

        ImageCreateInfo createInfo = new ImageCreateInfo(Vulkan10.Format.VK_FORMAT_A8B8G8R8_UINT_PACK32.value,
                new Extent2D(size.width, size.height), new Vulkan10.ImageUsageFlagBits[] {
                        Vulkan10.ImageUsageFlagBits.VK_IMAGE_USAGE_TRANSFER_DST_BIT,
                        Vulkan10.ImageUsageFlagBits.VK_IMAGE_USAGE_TRANSFER_SRC_BIT },
                ImageTiling.VK_IMAGE_TILING_OPTIMAL);
        ImageMemory imgMem = getRenderer().getBufferFactory().allocateImageMemory(createInfo,
                MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        queue.transitionToLayout(imgMem.getImage(), ImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, subLayer);

        ImageSubresourceLayers subLayers = new ImageSubresourceLayers();

        queue.cmdBlitImage(image, imgMem.getImage(), subLayers, subLayers, Filter.VK_FILTER_NEAREST);
        ImageSubresourceRange range = new ImageSubresourceRange();
        queue.cmdImageMemoryBarrier2(PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                AccessFlagBits2.VK_ACCESS_2_MEMORY_READ_BIT.value,
                PipelineStateFlagBits2.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT.value,
                AccessFlagBits2.VK_ACCESS_2_MEMORY_WRITE_BIT.value, image, range);

        queue.transitionToLayout(imgMem.getImage(), ImageLayout.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, subLayer);
        MemoryBuffer buf = getRenderer().getBufferFactory().copyFromDeviceMemory(imgMem.getImage(),
                ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT, getRenderer().getQueue());
        queue.queueWaitIdle();

        getRenderer().getBufferFactory().copyFromHostAvailableMemory(buf, buffer, getRenderer().getQueue());
        return createInfo.formatValue;
    }

}

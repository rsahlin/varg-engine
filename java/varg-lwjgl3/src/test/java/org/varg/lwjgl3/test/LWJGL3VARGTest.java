
package org.varg.lwjgl3.test;

import java.nio.ByteBuffer;

import org.gltfio.deserialize.Ladda.LaddaBooleanProperties;
import org.gltfio.deserialize.Ladda.LaddaFloatProperties;
import org.gltfio.deserialize.Ladda.LaddaProperties;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.FileUtils.FilesystemProperties;
import org.gltfio.lib.Settings;
import org.varg.BackendException;
import org.varg.lwjgl3.apps.VARGViewer;
import org.varg.renderer.BRDF.BRDFFloatProperties;
import org.varg.renderer.Renderers;
import org.varg.shader.PickingRayTracingShader.PickingRayTracingShaderCreateInfo;
import org.varg.shader.RayTracingShader;
import org.varg.shader.RayTracingShader.RayTracingShaderType;
import org.varg.uniform.StorageBuffers;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;
import org.varg.vulkan.Vulkan10.MemoryPropertyFlagBit;
import org.varg.vulkan.Vulkan10.QueryType;
import org.varg.vulkan.Vulkan12;
import org.varg.vulkan.VulkanBackend.BackendIntProperties;
import org.varg.vulkan.VulkanBackend.BackendProperties;
import org.varg.vulkan.VulkanBackend.BackendStringProperties;
import org.varg.vulkan.VulkanBackend.IntArrayProperties;
import org.varg.vulkan.extensions.KHRAccelerationStructure;
import org.varg.vulkan.extensions.KHRAccelerationStructure.AccelerationStructureBuildGeometryInfoKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.AccelerationStructureBuildRangeInfoKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.AccelerationStructureBuildSizesInfoKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.AccelerationStructureBuildTypeKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.AccelerationStructureCreateInfoKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.AccelerationStructureGeometryAabbsDataKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.AccelerationStructureGeometryInstancesDataKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.AccelerationStructureGeometryKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.AccelerationStructureKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.AccelerationStructureTypeKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.BuildAccelerationStructureFlagBitsKHR;
import org.varg.vulkan.extensions.KHRAccelerationStructure.DeviceOrHostAddress;
import org.varg.vulkan.extensions.KHRAccelerationStructure.GeometryTypeKHR;
import org.varg.vulkan.extensions.KHRRayTracingPipeline;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.Memory;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.structs.QueryPool;
import org.varg.vulkan.structs.QueryPoolCreateInfo;

public class LWJGL3VARGTest extends VARGViewer {

    private LWJGL3VARGTest(String[] args, Renderers version, String title) {
        super(args, version, title);
    }
    // -Dgltf.irradiancemap=intensity:2000
    // -Dgltf.cubemap=/assets/cubemap/footprint_court.ktx2

    @Override
    protected String getDefaultModelName() {
        // return "gltf/kudde/kudde.glb";
        // return "WaterBottle/glTF-Binary/WaterBottle.glb";
        // return "Corset/glTF-Binary/Corset.glb";
        // return "FlightHelmet/glTF/FlightHelmet.gltf";
        return "PlaysetLightTest/glTF-Binary/PlaysetLightTest.glb";
        // return "Duck/glTF-Binary/Duck.glb";
        // return "Avocado/glTF-Binary/Avocado.glb";
        // return "TextureTransformTest/glTF/TextureTransformTest.gltf";
        // return "2CylinderEngine/glTF/2CylinderEngine.gltf";
        // return "ClearcoatWicker/glTF-Binary/ClearcoatWicker.glb";
        // return "GearboxAssy/glTF/GearboxAssy.gltf";
        // return "OrientationTest/glTF/OrientationTest.gltf";
        // return "MetalRoughSpheresNoTextures/glTF-Binary/MetalRoughSpheresNoTextures.glb";
        // return "BoxVertexColors/glTF/BoxVertexColors.gltf";
        // return "BoxTextured/glTF/BoxTextured.gltf";
        // return "ABeautifulGame/glTF/AbeautifulGame.gltf";
        // return "Sponza/glTF/Sponza.gltf";
        // return "CREATOR:save2.gltf";
        // return "gltf/MacbethBalls/MacbethBalls.glb";
        // return "gltf/clearcoat_2.glb";
    }

    @Override
    protected String getResourcePath() {
        // return "C:/assets/test-assets/";
        return "C:/source/glTF-Sample-Assets/Models/";
        // return "C:/source/glTF-Sample-Models/2.0/";
    }

    @Override
    protected void init() {
        super.init();
        // sceneControl.getCurrentScene().getSceneTransform().setTranslate(new float[] { 0.29f, -0.0017777674f, -0.09600001f });
        // sceneControl.rotateScene(new float[] { -0.0060974793f, 0.99903536f, 0.0030282282f, 0.043017954f });
        // sceneControl.rotateScene(new float[] { 0f, -0.0871557f, 0f, 0.9961947f });
        try {
            boolean createTracer = false;
            if (createTracer) {
                createRayTracer();
            }
        } catch (BackendException e) {
            e.printStackTrace();
        }
    }

    private AccelerationStructureBuildGeometryInfoKHR createBuildGeometryInfoAABB(DeviceMemory allocator, int aabbCount,
            org.varg.vulkan.extensions.KHRAccelerationStructure<Queue> accelerationExtension) {
        int geometryUsageFlags = BitFlags.getFlagsValue(BufferUsageFlagBit.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
                org.varg.vulkan.extensions.KHRAccelerationStructure.BufferUsageFlagBit.VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_BUILD_INPUT_READ_ONLY_BIT_KHR);
        MemoryBuffer geometryBuffer = allocator.createBuffer(aabbCount * AccelerationStructureGeometryAabbsDataKHR.AABB_SIZE_IN_BYTES, geometryUsageFlags);
        Memory geometryMemory = allocator.allocateMemory(geometryBuffer.allocationSize,
                BitFlags.getFlagsValue(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT),
                BitFlags.getFlagsValue(Vulkan12.MemoryAllocateFlagBits.VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT), 0);
        allocator.bindBufferMemory(geometryBuffer, geometryMemory, 0);
        DeviceOrHostAddress geometryAddress = accelerationExtension.getBufferDeviceAddress(geometryBuffer);
        AccelerationStructureGeometryAabbsDataKHR geometries = new AccelerationStructureGeometryAabbsDataKHR(geometryAddress);
        AccelerationStructureGeometryKHR geometry = new AccelerationStructureGeometryKHR(GeometryTypeKHR.VK_GEOMETRY_TYPE_AABBS_KHR, geometries);

        AccelerationStructureBuildGeometryInfoKHR geometryInfo = new AccelerationStructureBuildGeometryInfoKHR(AccelerationStructureTypeKHR.VK_ACCELERATION_STRUCTURE_TYPE_BOTTOM_LEVEL_KHR, geometry, aabbCount,
                BuildAccelerationStructureFlagBitsKHR.VK_BUILD_ACCELERATION_STRUCTURE_PREFER_FAST_TRACE_BIT_KHR);

        return geometryInfo;
    }

    private AccelerationStructureBuildGeometryInfoKHR createInstanceGeometryInfo(DeviceMemory allocator,
            org.varg.vulkan.extensions.KHRAccelerationStructure<Queue> accelerationExtension) {
        int geometryUsageFlags = BitFlags.getFlagsValue(BufferUsageFlagBit.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
                org.varg.vulkan.extensions.KHRAccelerationStructure.BufferUsageFlagBit.VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_BUILD_INPUT_READ_ONLY_BIT_KHR);
        MemoryBuffer geometryBuffer = allocator.createBuffer(AccelerationStructureGeometryInstancesDataKHR.INSTANCE_SIZE_IN_BYTES,
                geometryUsageFlags);
        Memory geometryMemory = allocator.allocateMemory(geometryBuffer.allocationSize, BitFlags.getFlagsValue(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT),
                BitFlags.getFlagsValue(Vulkan12.MemoryAllocateFlagBits.VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT), 0);
        allocator.bindBufferMemory(geometryBuffer, geometryMemory, 0);
        DeviceOrHostAddress geometryAddress = accelerationExtension.getBufferDeviceAddress(geometryBuffer);
        AccelerationStructureGeometryInstancesDataKHR instances = new AccelerationStructureGeometryInstancesDataKHR(geometryAddress);
        AccelerationStructureGeometryKHR geometry = new AccelerationStructureGeometryKHR(GeometryTypeKHR.VK_GEOMETRY_TYPE_INSTANCES_KHR, instances);

        AccelerationStructureBuildGeometryInfoKHR geometryInfo = new AccelerationStructureBuildGeometryInfoKHR(AccelerationStructureTypeKHR.VK_ACCELERATION_STRUCTURE_TYPE_TOP_LEVEL_KHR, geometry, 1,
                BuildAccelerationStructureFlagBitsKHR.VK_BUILD_ACCELERATION_STRUCTURE_PREFER_FAST_TRACE_BIT_KHR);

        return geometryInfo;

    }

    private MemoryBuffer createAccelerationBuffer(DeviceMemory allocator,
            AccelerationStructureBuildSizesInfoKHR buildSize,
            org.varg.vulkan.extensions.KHRAccelerationStructure<Queue> accelerationExtension) {

        MemoryBuffer accelerationBuffer = allocator.createBuffer(buildSize.accelerationStructureSize, org.lwjgl.vulkan.KHRAccelerationStructure.VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_STORAGE_BIT_KHR);
        Memory accelerationMemory = allocator.allocateMemory(accelerationBuffer.allocationSize, BitFlags.getFlagsValue(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));
        allocator.bindBufferMemory(accelerationBuffer, accelerationMemory, 0);
        return accelerationBuffer;
    }

    private AccelerationStructureKHR createAccelerationStructure(DeviceMemory allocator, AccelerationStructureBuildSizesInfoKHR buildSize, MemoryBuffer accelerationBuffer,
            org.varg.vulkan.extensions.KHRAccelerationStructure<Queue> accelerationExtension) {
        AccelerationStructureCreateInfoKHR createInfo = new AccelerationStructureCreateInfoKHR(accelerationBuffer, buildSize.accelerationStructureSize, buildSize.getBuildInfo().type);

        AccelerationStructureKHR acceleration = accelerationExtension.createAccelerationStructureKHR(createInfo);
        return acceleration;
    }

    private MemoryBuffer createScratchBuffer(DeviceMemory allocator, AccelerationStructureBuildSizesInfoKHR buildSize) {
        int usageFlags = BitFlags.getFlagsValue(BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, BufferUsageFlagBit.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT);
        MemoryBuffer scratchBuffer = allocator.createBuffer(buildSize.buildScratchSize, usageFlags);
        Memory scratchMemory = allocator.allocateMemory(scratchBuffer.allocationSize, BitFlags.getFlagsValue(MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT),
                BitFlags.getFlagsValue(Vulkan12.MemoryAllocateFlagBits.VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT), 0);
        allocator.bindBufferMemory(scratchBuffer, scratchMemory, 0);
        return scratchBuffer;
    }

    private void createRayTracer() throws BackendException {
        DeviceMemory allocator = getRenderer().getBufferFactory();

        org.varg.vulkan.extensions.KHRAccelerationStructure<Queue> accelerationExtension = (KHRAccelerationStructure<Queue>) getRenderer().getBackend().getKHRAccelerationStructure();

        int aabbCount = 1;
        // Create a raytracing shader
        AccelerationStructureBuildGeometryInfoKHR geometryInfo = createBuildGeometryInfoAABB(allocator, aabbCount, accelerationExtension);

        AccelerationStructureBuildSizesInfoKHR buildSize = accelerationExtension.getAccelerationStructureBuildSizesKHR(AccelerationStructureBuildTypeKHR.VK_ACCELERATION_STRUCTURE_BUILD_TYPE_DEVICE_KHR, geometryInfo, aabbCount);

        MemoryBuffer accelerationBuffer = createAccelerationBuffer(allocator, buildSize, accelerationExtension);

        AccelerationStructureKHR acceleration = createAccelerationStructure(allocator, buildSize, accelerationBuffer, accelerationExtension);

        MemoryBuffer scratchBuffer = createScratchBuffer(allocator, buildSize);

        DeviceOrHostAddress scratchDeviceAddress = accelerationExtension.getBufferDeviceAddress(scratchBuffer);
        geometryInfo.setAccelerationStruct(acceleration, scratchDeviceAddress);

        AccelerationStructureBuildRangeInfoKHR rangeInfo = new AccelerationStructureBuildRangeInfoKHR(aabbCount, 0);

        AccelerationStructureBuildGeometryInfoKHR[] infos = new AccelerationStructureBuildGeometryInfoKHR[] { geometryInfo };
        AccelerationStructureBuildRangeInfoKHR[] buildRangeInfos = new AccelerationStructureBuildRangeInfoKHR[] { rangeInfo };
        Queue queue = getRenderer().getQueue();
        accelerationExtension.cmdBuildAccelerationStructuresKHR(queue, infos, buildRangeInfos);

        /**
         * Create the instance acceleration data
         */
        AccelerationStructureBuildGeometryInfoKHR instanceInfo = createInstanceGeometryInfo(allocator, accelerationExtension);
        AccelerationStructureBuildSizesInfoKHR instanceSize = accelerationExtension.getAccelerationStructureBuildSizesKHR(AccelerationStructureBuildTypeKHR.VK_ACCELERATION_STRUCTURE_BUILD_TYPE_DEVICE_KHR, instanceInfo, aabbCount);
        MemoryBuffer accelerationInstanceBuffer = createAccelerationBuffer(allocator, instanceSize, accelerationExtension);
        AccelerationStructureKHR accelerationInstance = createAccelerationStructure(allocator, instanceSize, accelerationInstanceBuffer, accelerationExtension);

        MemoryBuffer scratchInstanceBuffer = createScratchBuffer(allocator, instanceSize);
        DeviceOrHostAddress instanceDeviceAddress = accelerationExtension.getBufferDeviceAddress(scratchInstanceBuffer);
        instanceInfo.setAccelerationStruct(accelerationInstance, instanceDeviceAddress);

        AccelerationStructureBuildRangeInfoKHR rangeInstanceInfo = new AccelerationStructureBuildRangeInfoKHR(aabbCount, 0);

        AccelerationStructureBuildGeometryInfoKHR[] instanceInfos = new AccelerationStructureBuildGeometryInfoKHR[] { instanceInfo };
        AccelerationStructureBuildRangeInfoKHR[] buildRangeInstanceInfos = new AccelerationStructureBuildRangeInfoKHR[] { rangeInstanceInfo };
        accelerationExtension.cmdBuildAccelerationStructuresKHR(queue, instanceInfos, buildRangeInstanceInfos);

        /**
         * Query size of AS and copy to GPU
         */
        org.varg.vulkan.extensions.KHRRayTracingPipeline<Queue> rayTracingExtension = (KHRRayTracingPipeline<Queue>) getRenderer().getBackend().getKHRRayTracingPipeline();

        QueryPoolCreateInfo queryPoolInfo = new QueryPoolCreateInfo(QueryType.VK_QUERY_TYPE_ACCELERATION_STRUCTURE_SERIALIZATION_SIZE_KHR, 1, null);

        QueryPool qp = getRenderer().getBackend().createQueryPool(queryPoolInfo);
        queue.cmdResetQueryPool(qp, 0, queryPoolInfo.queryCount);
        accelerationExtension.cmdWriteAccelerationStructuresPropertiesKHR(queue, Vulkan10.QueryType.VK_QUERY_TYPE_ACCELERATION_STRUCTURE_SERIALIZATION_SIZE_KHR, qp, 0, accelerationInstance);
        queue.queueWaitIdle();
        queue.queueBegin();

        int resultBufferSize = queryPoolInfo.queryCount * 4;
        ByteBuffer writeSizes = Buffers.createByteBuffer(resultBufferSize);
        getRenderer().getBackend().getQueryPoolResults(qp, 0, queryPoolInfo.queryCount, writeSizes, 0, Vulkan10.QueryResultFlagBits.VK_QUERY_RESULT_WAIT_BIT);

        PickingRayTracingShaderCreateInfo shaderInfo = new PickingRayTracingShaderCreateInfo(version, RayTracingShaderType.PICKING_SHADER);
        RayTracingShader rayShader = shaderInfo.getInstance();

        StorageBuffers buffers = new StorageBuffers();
        getRenderer().getAssets().createStorageBuffers(shaderInfo, shaderInfo, buffers);
        getRenderer().getPipelines().createRayTracingPipeline(shaderInfo);

    }

    public static void main(String[] args) {
        Settings settings = Settings.getInstance();
        settings.setProperty(LaddaBooleanProperties.SELECT_RUNTIME_CAMERA, false);
        settings.setProperty(FilesystemProperties.JAVA_TARGET_DIRECTORY, "target/test-classes");
        settings.setProperty(FilesystemProperties.SOURCE_DIRECTORY, "src/test");
        settings.setProperty(BackendIntProperties.SURFACE_WIDTH, 1920);
        settings.setProperty(BackendIntProperties.SURFACE_HEIGHT, 1080);
        settings.setProperty(BackendProperties.FULLSCREEN, false);
        settings.setProperty(BackendIntProperties.BACKGROUND_FRAGMENTSIZE, 1);
        settings.setProperty(BackendIntProperties.FRAGMENTSIZE, 1);
        settings.setProperty(BackendIntProperties.SAMPLE_COUNT, 8);
        settings.setProperty(BackendStringProperties.COLORSPACE, "VK_COLOR_SPACE_SRGB_NONLINEAR_KHR");
        settings.setProperty(BackendIntProperties.MAX_WHITE, 1500);
        settings.setProperty(IntArrayProperties.CLEAR_COLOR, 90, 90, 90, 255);
        settings.setProperty(LaddaFloatProperties.MATERIAL_ABSORPTION, 0.2f);
        settings.setProperty(BRDFFloatProperties.NDF_FACTOR, 3.0f);
        settings.setProperty(BRDFFloatProperties.SOLIDANGLE_FUDGE, 0.001f);
        settings.setProperty(LaddaFloatProperties.BACKGROUND_INTENSITY_SCALE, 0.4f);

        settings.setProperty(LaddaProperties.IRRADIANCEMAP, "intensity:600|irmap:STUDIO_5");
        settings.setProperty(LaddaProperties.ENVMAP_BACKGROUND, "SH");
        settings.setProperty(LaddaProperties.DIRECTIONAL_LIGHT, "intensity:1500|color:1,1,1|position:0,10000,10000");
        // settings.setProperty(LaddaProperties.DIRECTIONAL_LIGHT1, "intensity:5000|color:1,0,0|position:-1000,0,10000");
        // settings.setProperty(LaddaProperties.DIRECTIONAL_LIGHT2, "intensity:5000|color:0,1,0|position:1000,0,10000");
        // settings.setProperty(LaddaProperties.DIRECTIONAL_LIGHT3, "intensity:5000|color:0,0,1|position:0,10000,00");
        // settings.setProperty(LaddaProperties.ENVIRONMENTMAP,
        // "C:/assets/test-assets/ibl/milkyway1.ktx2|intensity:5000");

        VARGViewer varg = new LWJGL3VARGTest(args, Renderers.VULKAN13, "VARG Viewer");
        varg.createApp();
        varg.run();
    }
}

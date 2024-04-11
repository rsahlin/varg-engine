
package org.varg.vulkan;

import java.nio.ByteBuffer;

import org.gltfio.lib.BitFlag;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.ktximageio.itu.BT2100;
import org.ktximageio.ktx.ImageReader;

public interface Vulkan10 {

    /** API Constants */
    long VK_WHOLE_SIZE = (~0L);
    int VK_REMAINING_ARRAY_LAYERS = (~0);
    int VK_REMAINING_MIP_LEVELS = (~0);

    enum Result {
        VK_SUCCESS(0),
        VK_NOT_READY(1),
        VK_TIMEOUT(2),
        VK_EVENT_SET(3),
        VK_EVENT_RESET(4),
        VK_INCOMPLETE(5),
        VK_ERROR_OUT_OF_HOST_MEMORY(-1),
        VK_ERROR_OUT_OF_DEVICE_MEMORY(-2),
        VK_VK_ERROR_INITIALIZATION_FAILED(-3),
        VK_ERROR_DEVICE_LOST(-4),
        VK_ERROR_MEMORY_MAP_FAILED(-5),
        VK_ERROR_LAYER_NOT_PRESENT(-6),
        VK_ERROR_EXTENSION_NOT_PRESENT(-7),
        VK_ERROR_FEATURE_NOT_PRESENT(-8),
        VK_ERROR_INCOMPATIBLE_DRIVER(-9),
        VK_ERROR_TOO_MANY_OBJECTS(-10),
        VK_ERROR_FORMAT_NOT_SUPPORTED(-11),
        VK_ERROR_FRAGMENTED_POOL(-12),
        VK_ERROR_OUT_OF_POOL_MEMORY(-1000069000),
        VK_ERROR_INVALID_EXTERNAL_HANDLE(-1000072003),
        VK_ERROR_SURFACE_LOST_KHR(-1000000000),
        VK_ERROR_NATIVE_WINDOW_IN_USE_KHR(-1000000001),
        VK_SUBOPTIMAL_KHR(1000001003),
        VK_ERROR_OUT_OF_DATE_KHR(-1000001004),
        VK_ERROR_INCOMPATIBLE_DISPLAY_KHR(-1000003001),
        VK_ERROR_VALIDATION_FAILED_EXT(-1000011001),
        VK_ERROR_INVALID_SHADER_NV(-1000012000),
        VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT(-1000158000),
        VK_ERROR_FRAGMENTATION_EXT(-1000161000),
        VK_ERROR_NOT_PERMITTED_EXT(-1000174001),
        VK_ERROR_INVALID_DEVICE_ADDRESS_EXT(-1000244000),
        VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT(-1000255000),
        VK_ERROR_OUT_OF_POOL_MEMORY_KHR(VK_ERROR_OUT_OF_POOL_MEMORY.value),
        VK_ERROR_INVALID_EXTERNAL_HANDLE_KHR(VK_ERROR_INVALID_EXTERNAL_HANDLE.value),
        VK_RESULT_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        Result(int value) {
            this.value = value;
        }

        public static Result getResult(int value) {
            for (Result r : values()) {
                if (r.value == value) {
                    return r;
                }
            }
            return null;
        }

    }

    enum QueryResultFlagBits implements BitFlag {
        VK_QUERY_RESULT_64_BIT(0x00000001),
        VK_QUERY_RESULT_WAIT_BIT(0x00000002),
        VK_QUERY_RESULT_WITH_AVAILABILITY_BIT(0x00000004),
        VK_QUERY_RESULT_PARTIAL_BIT(0x00000008),
        // Provided by VK_KHR_video_queue
        VK_QUERY_RESULT_WITH_STATUS_BIT_KHR(0x00000010);

        public final int value;

        QueryResultFlagBits(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum QueryType implements BitFlag {
        VK_QUERY_TYPE_OCCLUSION(0),
        VK_QUERY_TYPE_PIPELINE_STATISTICS(1),
        VK_QUERY_TYPE_TIMESTAMP(2),
        // Provided by VK_KHR_video_queue
        VK_QUERY_TYPE_RESULT_STATUS_ONLY_KHR(1000023000),
        // Provided by VK_EXT_transform_feedback
        VK_QUERY_TYPE_TRANSFORM_FEEDBACK_STREAM_EXT(1000028004),
        // Provided by VK_KHR_performance_query
        VK_QUERY_TYPE_PERFORMANCE_QUERY_KHR(1000116000),
        // Provided by VK_KHR_acceleration_structure
        VK_QUERY_TYPE_ACCELERATION_STRUCTURE_COMPACTED_SIZE_KHR(1000150000),
        // Provided by VK_KHR_acceleration_structure
        VK_QUERY_TYPE_ACCELERATION_STRUCTURE_SERIALIZATION_SIZE_KHR(1000150001),
        // Provided by VK_NV_ray_tracing
        VK_QUERY_TYPE_ACCELERATION_STRUCTURE_COMPACTED_SIZE_NV(1000165000),
        // Provided by VK_INTEL_performance_query
        VK_QUERY_TYPE_PERFORMANCE_QUERY_INTEL(1000210000),
        // Provided by VK_KHR_video_encode_queue
        VK_QUERY_TYPE_VIDEO_ENCODE_FEEDBACK_KHR(1000299000),
        // Provided by VK_EXT_mesh_shader
        VK_QUERY_TYPE_MESH_PRIMITIVES_GENERATED_EXT(1000328000),
        // Provided by VK_EXT_primitives_generated_query
        VK_QUERY_TYPE_PRIMITIVES_GENERATED_EXT(1000382000),
        // Provided by VK_KHR_ray_tracing_maintenance1
        VK_QUERY_TYPE_ACCELERATION_STRUCTURE_SERIALIZATION_BOTTOM_LEVEL_POINTERS_KHR(1000386000),
        // Provided by VK_KHR_ray_tracing_maintenance1
        VK_QUERY_TYPE_ACCELERATION_STRUCTURE_SIZE_KHR(1000386001),
        // Provided by VK_EXT_opacity_micromap
        VK_QUERY_TYPE_MICROMAP_SERIALIZATION_SIZE_EXT(1000396000),
        // Provided by VK_EXT_opacity_micromap
        VK_QUERY_TYPE_MICROMAP_COMPACTED_SIZE_EXT(1000396001);

        public final int value;

        QueryType(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum FramebufferCreateFlagBits implements BitFlag {
        VK_FRAMEBUFFER_CREATE_IMAGELESS_BIT(0x00000001),
        VK_FRAMEBUFFER_CREATE_IMAGELESS_BIT_KHR(VK_FRAMEBUFFER_CREATE_IMAGELESS_BIT.value),
        VK_FRAMEBUFFER_CREATE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        FramebufferCreateFlagBits(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

        public static FramebufferCreateFlagBits[] getBitFlags(FramebufferCreateFlagBits... bits) {
            return bits;
        }

    }

    enum IndexType {
        VK_INDEX_TYPE_UINT16(0),
        VK_INDEX_TYPE_UINT32(1),
        VK_INDEX_TYPE_NONE_KHR(1000165000),
        VK_INDEX_TYPE_UINT8_EXT(0x3B9ED528),
        VK_INDEX_TYPE_NONE_NV(VK_INDEX_TYPE_NONE_KHR.value),
        VK_INDEX_TYPE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        IndexType(int value) {
            this.value = value;
        }
    }

    enum AttachmentDescriptionFlagBit implements BitFlag {
        VK_ATTACHMENT_DESCRIPTION_MAY_ALIAS_BIT(0x00000001),
        VK_ATTACHMENT_DESCRIPTION_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        AttachmentDescriptionFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

        public static AttachmentDescriptionFlagBit[] getBitFlags(AttachmentDescriptionFlagBit... bits) {
            return bits;
        }

    }

    enum SubpassContents {
        VK_SUBPASS_CONTENTS_INLINE(0),
        VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS(1),
        VK_SUBPASS_CONTENTS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        SubpassContents(int value) {
            this.value = value;
        }

    }

    enum QueryControlFlagBits implements BitFlag {
        VK_QUERY_CONTROL_PRECISE_BIT(0x00000001),
        VK_QUERY_CONTROL_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        QueryControlFlagBits(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    enum QueryPipelineStatisticFlagBits implements BitFlag {
        VK_QUERY_PIPELINE_STATISTIC_INPUT_ASSEMBLY_VERTICES_BIT(0x00000001),
        VK_QUERY_PIPELINE_STATISTIC_INPUT_ASSEMBLY_PRIMITIVES_BIT(0x00000002),
        VK_QUERY_PIPELINE_STATISTIC_VERTEX_SHADER_INVOCATIONS_BIT(0x00000004),
        VK_QUERY_PIPELINE_STATISTIC_GEOMETRY_SHADER_INVOCATIONS_BIT(0x00000008),
        VK_QUERY_PIPELINE_STATISTIC_GEOMETRY_SHADER_PRIMITIVES_BIT(0x00000010),
        VK_QUERY_PIPELINE_STATISTIC_CLIPPING_INVOCATIONS_BIT(0x00000020),
        VK_QUERY_PIPELINE_STATISTIC_CLIPPING_PRIMITIVES_BIT(0x00000040),
        VK_QUERY_PIPELINE_STATISTIC_FRAGMENT_SHADER_INVOCATIONS_BIT(0x00000080),
        VK_QUERY_PIPELINE_STATISTIC_TESSELLATION_CONTROL_SHADER_PATCHES_BIT(0x00000100),
        VK_QUERY_PIPELINE_STATISTIC_TESSELLATION_EVALUATION_SHADER_INVOCATIONS_BIT(0x00000200),
        VK_QUERY_PIPELINE_STATISTIC_COMPUTE_SHADER_INVOCATIONS_BIT(0x00000400),
        // Provided by VK_EXT_mesh_shader
        VK_QUERY_PIPELINE_STATISTIC_TASK_SHADER_INVOCATIONS_BIT_EXT(0x00000800),
        // Provided by VK_EXT_mesh_shader
        VK_QUERY_PIPELINE_STATISTIC_MESH_SHADER_INVOCATIONS_BIT_EXT(0x00001000),
        // Provided by VK_HUAWEI_cluster_culling_shader
        VK_QUERY_PIPELINE_STATISTIC_CLUSTER_CULLING_SHADER_INVOCATIONS_BIT_HUAWEI(0x00002000);

        public final int value;

        QueryPipelineStatisticFlagBits(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum CommandBufferUsageFlagBits implements BitFlag {
        VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT(0x00000001),
        VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT(0x00000002),
        VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT(0x00000004),
        VK_COMMAND_BUFFER_USAGE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        CommandBufferUsageFlagBits(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    enum ImageCreateFlagBits implements BitFlag {
        VK_IMAGE_CREATE_SPARSE_BINDING_BIT(0x00000001),
        VK_IMAGE_CREATE_SPARSE_RESIDENCY_BIT(0x00000002),
        VK_IMAGE_CREATE_SPARSE_ALIASED_BIT(0x00000004),
        VK_IMAGE_CREATE_MUTABLE_FORMAT_BIT(0x00000008),
        VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT(0x00000010),
        VK_IMAGE_CREATE_ALIAS_BIT(0x00000400),
        VK_IMAGE_CREATE_SPLIT_INSTANCE_BIND_REGIONS_BIT(0x00000040),
        VK_IMAGE_CREATE_2D_ARRAY_COMPATIBLE_BIT(0x00000020),
        VK_IMAGE_CREATE_BLOCK_TEXEL_VIEW_COMPATIBLE_BIT(0x00000080),
        VK_IMAGE_CREATE_EXTENDED_USAGE_BIT(0x00000100),
        VK_IMAGE_CREATE_PROTECTED_BIT(0x00000800),
        VK_IMAGE_CREATE_DISJOINT_BIT(0x00000200),
        VK_IMAGE_CREATE_CORNER_SAMPLED_BIT_NV(0x00002000),
        VK_IMAGE_CREATE_SAMPLE_LOCATIONS_COMPATIBLE_DEPTH_BIT_EXT(0x00001000),
        VK_IMAGE_CREATE_SUBSAMPLED_BIT_EXT(0x00004000),
        VK_IMAGE_CREATE_SPLIT_INSTANCE_BIND_REGIONS_BIT_KHR(VK_IMAGE_CREATE_SPLIT_INSTANCE_BIND_REGIONS_BIT.value),
        VK_IMAGE_CREATE_2D_ARRAY_COMPATIBLE_BIT_KHR(VK_IMAGE_CREATE_2D_ARRAY_COMPATIBLE_BIT.value),
        VK_IMAGE_CREATE_BLOCK_TEXEL_VIEW_COMPATIBLE_BIT_KHR(VK_IMAGE_CREATE_BLOCK_TEXEL_VIEW_COMPATIBLE_BIT.value),
        VK_IMAGE_CREATE_EXTENDED_USAGE_BIT_KHR(VK_IMAGE_CREATE_EXTENDED_USAGE_BIT.value),
        VK_IMAGE_CREATE_DISJOINT_BIT_KHR(VK_IMAGE_CREATE_DISJOINT_BIT.value),
        VK_IMAGE_CREATE_ALIAS_BIT_KHR(VK_IMAGE_CREATE_ALIAS_BIT.value),
        VK_IMAGE_CREATE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        ImageCreateFlagBits(int value) {
            this.value = value;
        }

        public static ImageCreateFlagBits[] getFlags(ImageCreateFlagBits... value) {
            return value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum ImageType implements BitFlag {
        VK_IMAGE_TYPE_1D(0),
        VK_IMAGE_TYPE_2D(1),
        VK_IMAGE_TYPE_3D(2),
        VK_IMAGE_TYPE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        ImageType(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    enum SharingMode implements BitFlag {
        VK_SHARING_MODE_EXCLUSIVE(0),
        VK_SHARING_MODE_CONCURRENT(1),
        VK_SHARING_MODE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        SharingMode(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    enum FormatFeatureFlagBits implements BitFlag {
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT(0x00000001),
        VK_FORMAT_FEATURE_STORAGE_IMAGE_BIT(0x00000002),
        VK_FORMAT_FEATURE_STORAGE_IMAGE_ATOMIC_BIT(0x00000004),
        VK_FORMAT_FEATURE_UNIFORM_TEXEL_BUFFER_BIT(0x00000008),
        VK_FORMAT_FEATURE_STORAGE_TEXEL_BUFFER_BIT(0x00000010),
        VK_FORMAT_FEATURE_STORAGE_TEXEL_BUFFER_ATOMIC_BIT(0x00000020),
        VK_FORMAT_FEATURE_VERTEX_BUFFER_BIT(0x00000040),
        VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BIT(0x00000080),
        VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BLEND_BIT(0x00000100),
        VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT(0x00000200),
        VK_FORMAT_FEATURE_BLIT_SRC_BIT(0x00000400),
        VK_FORMAT_FEATURE_BLIT_DST_BIT(0x00000800),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT(0x00001000),
        VK_FORMAT_FEATURE_TRANSFER_SRC_BIT(0x00004000),
        VK_FORMAT_FEATURE_TRANSFER_DST_BIT(0x00008000),
        VK_FORMAT_FEATURE_MIDPOINT_CHROMA_SAMPLES_BIT(0x00020000),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_LINEAR_FILTER_BIT(0x00040000),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_SEPARATE_RECONSTRUCTION_FILTER_BIT(0x00080000),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_CHROMA_RECONSTRUCTION_EXPLICIT_BIT(0x00100000),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_CHROMA_RECONSTRUCTION_EXPLICIT_FORCEABLE_BIT(0x00200000),
        VK_FORMAT_FEATURE_DISJOINT_BIT(0x00400000),
        VK_FORMAT_FEATURE_COSITED_CHROMA_SAMPLES_BIT(0x00800000),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_MINMAX_BIT(0x00010000),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_CUBIC_BIT_IMG(0x00002000),
        VK_FORMAT_FEATURE_FRAGMENT_DENSITY_MAP_BIT_EXT(0x01000000),
        VK_FORMAT_FEATURE_TRANSFER_SRC_BIT_KHR(VK_FORMAT_FEATURE_TRANSFER_SRC_BIT.value),
        VK_FORMAT_FEATURE_TRANSFER_DST_BIT_KHR(VK_FORMAT_FEATURE_TRANSFER_DST_BIT.value),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_MINMAX_BIT_EXT(VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_MINMAX_BIT.value),
        VK_FORMAT_FEATURE_MIDPOINT_CHROMA_SAMPLES_BIT_KHR(VK_FORMAT_FEATURE_MIDPOINT_CHROMA_SAMPLES_BIT.value),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_LINEAR_FILTER_BIT_KHR(
                VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_LINEAR_FILTER_BIT.value),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_SEPARATE_RECONSTRUCTION_FILTER_BIT_KHR(
                VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_SEPARATE_RECONSTRUCTION_FILTER_BIT.value),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_CHROMA_RECONSTRUCTION_EXPLICIT_BIT_KHR(
                VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_CHROMA_RECONSTRUCTION_EXPLICIT_BIT.value),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_CHROMA_RECONSTRUCTION_EXPLICIT_FORCEABLE_BIT_KHR(
                VK_FORMAT_FEATURE_SAMPLED_IMAGE_YCBCR_CONVERSION_CHROMA_RECONSTRUCTION_EXPLICIT_FORCEABLE_BIT.value),
        VK_FORMAT_FEATURE_DISJOINT_BIT_KHR(VK_FORMAT_FEATURE_DISJOINT_BIT.value),
        VK_FORMAT_FEATURE_COSITED_CHROMA_SAMPLES_BIT_KHR(VK_FORMAT_FEATURE_COSITED_CHROMA_SAMPLES_BIT.value),
        VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_CUBIC_BIT_EXT(
                VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_CUBIC_BIT_IMG.value),
        VK_FORMAT_FEATURE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        FormatFeatureFlagBits(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum CompositeAlphaFlagBitsKHR implements BitFlag {
        VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR(0x00000001),
        VK_COMPOSITE_ALPHA_PRE_MULTIPLIED_BIT_KHR(0x00000002),
        VK_COMPOSITE_ALPHA_POST_MULTIPLIED_BIT_KHR(0x00000004),
        VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR(0x00000008),
        VK_COMPOSITE_ALPHA_FLAG_BITS_MAX_ENUM_KHR(0x7FFFFFFF);

        public final int value;

        CompositeAlphaFlagBitsKHR(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum SwapchainCreateFlagBitsKHR implements BitFlag {

        // Provided by VK_KHR_swapchain with VK_VERSION_1_1, VK_KHR_device_group with VK_KHR_swapchain
        VK_SWAPCHAIN_CREATE_SPLIT_INSTANCE_BIND_REGIONS_BIT_KHR(0x00000001),
        // Provided by VK_KHR_swapchain with VK_VERSION_1_1
        VK_SWAPCHAIN_CREATE_PROTECTED_BIT_KHR(0x00000002),
        // Provided by VK_KHR_swapchain_mutable_format
        VK_SWAPCHAIN_CREATE_MUTABLE_FORMAT_BIT_KHR(0x00000004);

        public final int value;

        SwapchainCreateFlagBitsKHR(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum SurfaceTransformFlagBitsKHR implements BitFlag {
        VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR(0x00000001),
        VK_SURFACE_TRANSFORM_ROTATE_90_BIT_KHR(0x00000002),
        VK_SURFACE_TRANSFORM_ROTATE_180_BIT_KHR(0x00000004),
        VK_SURFACE_TRANSFORM_ROTATE_270_BIT_KHR(0x00000008),
        VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_BIT_KHR(0x00000010),
        VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_90_BIT_KHR(0x00000020),
        VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_180_BIT_KHR(0x00000040),
        VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_270_BIT_KHR(0x00000080),
        VK_SURFACE_TRANSFORM_INHERIT_BIT_KHR(0x00000100),
        VK_SURFACE_TRANSFORM_FLAG_BITS_MAX_ENUM_KHR(0x7FFFFFFF);

        public final int value;

        SurfaceTransformFlagBitsKHR(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum ImageUsageFlagBits implements BitFlag {
        VK_IMAGE_USAGE_TRANSFER_SRC_BIT(0x00000001),
        VK_IMAGE_USAGE_TRANSFER_DST_BIT(0x00000002),
        VK_IMAGE_USAGE_SAMPLED_BIT(0x00000004),
        VK_IMAGE_USAGE_STORAGE_BIT(0x00000008),
        VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT(0x00000010),
        VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT(0x00000020),
        VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT(0x00000040),
        VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT(0x00000080),
        VK_IMAGE_USAGE_SHADING_RATE_IMAGE_BIT_NV(0x00000100),
        VK_IMAGE_USAGE_FRAGMENT_DENSITY_MAP_BIT_EXT(0x00000200),
        VK_IMAGE_USAGE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        ImageUsageFlagBits(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

        public static ImageUsageFlagBits[] getBitFlags(ImageUsageFlagBits... bits) {
            return bits;
        }

        public static ImageUsageFlagBits get(String usage) {
            for (ImageUsageFlagBits u : values()) {
                if (u.name().contentEquals(usage)) {
                    return u;
                }
            }
            return null;
        }

    }

    enum ImageTiling implements BitFlag {

        VK_IMAGE_TILING_OPTIMAL(0),
        VK_IMAGE_TILING_LINEAR(1),
        VK_IMAGE_TILING_DRM_FORMAT_MODIFIER_EXT(1000158000),
        VK_IMAGE_TILING_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        ImageTiling(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum MemoryHeapFlagBits implements BitFlag {
        VK_MEMORY_HEAP_DEVICE_LOCAL_BIT(1),
        VK_MEMORY_HEAP_MULTI_INSTANCE_BIT(2),
        VK_MEMORY_HEAP_MULTI_INSTANCE_BIT_KHR(VK_MEMORY_HEAP_MULTI_INSTANCE_BIT.value);

        public final int value;

        MemoryHeapFlagBits(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    };

    enum MemoryPropertyFlagBit implements BitFlag {
        VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT(1),
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT(2),
        VK_MEMORY_PROPERTY_HOST_COHERENT_BIT(4),
        VK_MEMORY_PROPERTY_HOST_CACHED_BIT(8),
        VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT(10),
        VK_MEMORY_PROPERTY_PROTECTED_BIT(20);

        public final int value;

        MemoryPropertyFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

        public static MemoryPropertyFlagBit[] getBitFlags(MemoryPropertyFlagBit... bits) {
            return bits;
        }

    };

    enum BorderColor implements BitFlag {
        VK_BORDER_COLOR_FLOAT_TRANSPARENT_BLACK(0),
        VK_BORDER_COLOR_INT_TRANSPARENT_BLACK(1),
        VK_BORDER_COLOR_FLOAT_OPAQUE_BLACK(2),
        VK_BORDER_COLOR_INT_OPAQUE_BLACK(3),
        VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE(4),
        VK_BORDER_COLOR_INT_OPAQUE_WHITE(5),
        VK_BORDER_COLOR_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        BorderColor(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum SamplerAddressMode implements BitFlag {
        VK_SAMPLER_ADDRESS_MODE_REPEAT(0),
        VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT(1),
        VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE(2),
        VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER(3),
        VK_SAMPLER_ADDRESS_MODE_MIRROR_CLAMP_TO_EDGE(4),
        VK_SAMPLER_ADDRESS_MODE_MIRROR_CLAMP_TO_EDGE_KHR(VK_SAMPLER_ADDRESS_MODE_MIRROR_CLAMP_TO_EDGE.value),
        VK_SAMPLER_ADDRESS_MODE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        SamplerAddressMode(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum SamplerMipmapMode implements BitFlag {
        VK_SAMPLER_MIPMAP_MODE_NEAREST(0),
        VK_SAMPLER_MIPMAP_MODE_LINEAR(1),
        VK_SAMPLER_MIPMAP_MODE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        SamplerMipmapMode(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum GLWrapMode {
        GL_REPEAT(0x2901, SamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT),
        GL_CLAMP_TO_EDGE(0x812F, SamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE),
        GL_MIRRORED_REPEAT(0x8370, SamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT);

        public final int value;
        public final SamplerAddressMode mode;

        GLWrapMode(int value, SamplerAddressMode mode) {
            this.value = value;
            this.mode = mode;
        }

        public static GLWrapMode get(int wrapMode) {
            for (GLWrapMode m : values()) {
                if (m.value == wrapMode) {
                    return m;
                }
            }
            return null;
        }

    }

    enum GLFilter {
        GL_NEAREST(0x2600, Filter.VK_FILTER_NEAREST, SamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_NEAREST),
        GL_LINEAR(0x2601, Filter.VK_FILTER_LINEAR, SamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_LINEAR),
        GL_NEAREST_MIPMAP_NEAREST(0x2700, Filter.VK_FILTER_NEAREST, SamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_NEAREST),
        GL_LINEAR_MIPMAP_NEAREST(0x2701, Filter.VK_FILTER_LINEAR, SamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_LINEAR),
        GL_NEAREST_MIPMAP_LINEAR(0x2702, Filter.VK_FILTER_NEAREST, SamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_LINEAR),
        GL_LINEAR_MIPMAP_LINEAR(0x2703, Filter.VK_FILTER_LINEAR, SamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_LINEAR);

        public final int value;
        public final Filter filter;
        public final SamplerMipmapMode mipmapMode;

        GLFilter(int value, Filter filter, SamplerMipmapMode mipmapMode) {
            this.value = value;
            this.filter = filter;
            this.mipmapMode = mipmapMode;
        }

        public static GLFilter get(int texparameter) {
            for (GLFilter f : values()) {
                if (f.value == texparameter) {
                    return f;
                }
            }
            return GLFilter.GL_LINEAR;
        }

    }

    enum Filter implements BitFlag {
        VK_FILTER_NEAREST(0),
        VK_FILTER_LINEAR(1),
        VK_FILTER_CUBIC_IMG(1000015000),
        VK_FILTER_CUBIC_EXT(VK_FILTER_CUBIC_IMG.value),
        VK_FILTER_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        Filter(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    enum SamplerCreateFlagBits implements BitFlag {
        VK_SAMPLER_CREATE_SUBSAMPLED_BIT_EXT(0x00000001),
        VK_SAMPLER_CREATE_SUBSAMPLED_COARSE_RECONSTRUCTION_BIT_EXT(0x00000002),
        VK_SAMPLER_CREATE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        private final int value;

        SamplerCreateFlagBits(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum BufferUsageFlagBit implements BitFlag {
        VK_BUFFER_USAGE_TRANSFER_SRC_BIT(0x00000001),
        VK_BUFFER_USAGE_TRANSFER_DST_BIT(0x00000002),
        VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT(0x00000004),
        VK_BUFFER_USAGE_STORAGE_TEXEL_BUFFER_BIT(0x00000008),
        VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT(0x00000010),
        VK_BUFFER_USAGE_STORAGE_BUFFER_BIT(0x00000020),
        VK_BUFFER_USAGE_INDEX_BUFFER_BIT(0x00000040),
        VK_BUFFER_USAGE_VERTEX_BUFFER_BIT(0x00000080),
        VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT(0x00000100),
        VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT(0x00020000),
        VK_BUFFER_USAGE_TRANSFORM_FEEDBACK_BUFFER_BIT_EXT(0x00000800),
        VK_BUFFER_USAGE_TRANSFORM_FEEDBACK_COUNTER_BUFFER_BIT_EXT(0x00001000),
        VK_BUFFER_USAGE_CONDITIONAL_RENDERING_BIT_EXT(0x00000200),
        VK_BUFFER_USAGE_RAY_TRACING_BIT_NV(0x00000400),
        VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT_EXT(VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT.value),
        VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT_KHR(VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT.value),
        VK_BUFFER_USAGE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        BufferUsageFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

        public static BufferUsageFlagBit get(String name) {
            for (BufferUsageFlagBit usage : values()) {
                if (usage.name().contentEquals(name)) {
                    return usage;
                }
            }
            return null;
        }

        public static BufferUsageFlagBit[] getBitFlags(BufferUsageFlagBit... bits) {
            return bits;
        }

    }

    enum ShaderStageFlagBit implements BitFlag {
        VK_SHADER_STAGE_VERTEX_BIT(0x00000001),
        VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT(0x00000002),
        VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT(0x00000004),
        VK_SHADER_STAGE_GEOMETRY_BIT(0x00000008),
        VK_SHADER_STAGE_FRAGMENT_BIT(0x00000010),
        VK_SHADER_STAGE_COMPUTE_BIT(0x00000020),
        VK_SHADER_STAGE_ALL_GRAPHICS(0x0000001F),
        VK_SHADER_STAGE_ALL(0x7FFFFFFF),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_SHADER_STAGE_RAYGEN_BIT_KHR(0x00000100),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_SHADER_STAGE_ANY_HIT_BIT_KHR(0x00000200),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_SHADER_STAGE_CLOSEST_HIT_BIT_KHR(0x00000400),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_SHADER_STAGE_MISS_BIT_KHR(0x00000800),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_SHADER_STAGE_INTERSECTION_BIT_KHR(0x00001000),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_SHADER_STAGE_CALLABLE_BIT_KHR(0x00002000),
        // Provided by VK_EXT_mesh_shader
        VK_SHADER_STAGE_TASK_BIT_EXT(0x00000040),
        // Provided by VK_EXT_mesh_shader
        VK_SHADER_STAGE_MESH_BIT_EXT(0x00000080),
        // Provided by VK_HUAWEI_subpass_shading
        VK_SHADER_STAGE_SUBPASS_SHADING_BIT_HUAWEI(0x00004000),
        // Provided by VK_NV_ray_tracing
        VK_SHADER_STAGE_RAYGEN_BIT_NV(VK_SHADER_STAGE_RAYGEN_BIT_KHR.value),
        // Provided by VK_NV_ray_tracing
        VK_SHADER_STAGE_ANY_HIT_BIT_NV(VK_SHADER_STAGE_ANY_HIT_BIT_KHR.value),
        // Provided by VK_NV_ray_tracing
        VK_SHADER_STAGE_CLOSEST_HIT_BIT_NV(VK_SHADER_STAGE_CLOSEST_HIT_BIT_KHR.value),
        // Provided by VK_NV_ray_tracing
        VK_SHADER_STAGE_MISS_BIT_NV(VK_SHADER_STAGE_MISS_BIT_KHR.value),
        // Provided by VK_NV_ray_tracing
        VK_SHADER_STAGE_INTERSECTION_BIT_NV(VK_SHADER_STAGE_INTERSECTION_BIT_KHR.value),
        // Provided by VK_NV_ray_tracing
        VK_SHADER_STAGE_CALLABLE_BIT_NV(VK_SHADER_STAGE_CALLABLE_BIT_KHR.value),
        // Provided by VK_NV_mesh_shader
        VK_SHADER_STAGE_TASK_BIT_NV(VK_SHADER_STAGE_TASK_BIT_EXT.value),
        // Provided by VK_NV_mesh_shader
        VK_SHADER_STAGE_MESH_BIT_NV(VK_SHADER_STAGE_MESH_BIT_EXT.value);

        public final int value;

        ShaderStageFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

        public static ShaderStageFlagBit[] getBitFlags(ShaderStageFlagBit... bits) {
            return bits;
        }

        public static PipelineBindPoint getPipelineBindPoint(ShaderStageFlagBit... bits) {
            PipelineBindPoint bind = null;
            for (ShaderStageFlagBit flag : bits) {
                switch (flag) {
                    case VK_SHADER_STAGE_ALL_GRAPHICS:
                    case VK_SHADER_STAGE_FRAGMENT_BIT:
                    case VK_SHADER_STAGE_GEOMETRY_BIT:
                    case VK_SHADER_STAGE_MESH_BIT_EXT:
                    case VK_SHADER_STAGE_MESH_BIT_NV:
                    case VK_SHADER_STAGE_TASK_BIT_EXT:
                    case VK_SHADER_STAGE_TASK_BIT_NV:
                    case VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT:
                    case VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT:
                    case VK_SHADER_STAGE_VERTEX_BIT:
                        if (bind != null && bind != PipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS) {
                            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + flag);
                        }
                        bind = PipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS;
                        break;
                    case VK_SHADER_STAGE_COMPUTE_BIT:
                        if (bind != null && bind != PipelineBindPoint.VK_PIPELINE_BIND_POINT_COMPUTE) {
                            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + flag);
                        }
                        bind = PipelineBindPoint.VK_PIPELINE_BIND_POINT_COMPUTE;
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + flag);
                }
            }
            return bind;
        }

    }

    enum ImageAspectFlagBit implements BitFlag {
        VK_IMAGE_ASPECT_COLOR_BIT(1),
        VK_IMAGE_ASPECT_DEPTH_BIT(2),
        VK_IMAGE_ASPECT_STENCIL_BIT(4),
        VK_IMAGE_ASPECT_METADATA_BIT(0x00000008),
        VK_IMAGE_ASPECT_PLANE_0_BIT(0x00000010),
        VK_IMAGE_ASPECT_PLANE_1_BIT(0x00000020),
        VK_IMAGE_ASPECT_PLANE_2_BIT(0x00000040),
        VK_IMAGE_ASPECT_MEMORY_PLANE_0_BIT_EXT(0x00000080),
        VK_IMAGE_ASPECT_MEMORY_PLANE_1_BIT_EXT(0x00000100),
        VK_IMAGE_ASPECT_MEMORY_PLANE_2_BIT_EXT(0x00000200),
        VK_IMAGE_ASPECT_MEMORY_PLANE_3_BIT_EXT(0x00000400),
        VK_IMAGE_ASPECT_PLANE_0_BIT_KHR(VK_IMAGE_ASPECT_PLANE_0_BIT.value),
        VK_IMAGE_ASPECT_PLANE_1_BIT_KHR(VK_IMAGE_ASPECT_PLANE_1_BIT.value),
        VK_IMAGE_ASPECT_PLANE_2_BIT_KHR(VK_IMAGE_ASPECT_PLANE_2_BIT.value);

        public final int value;

        ImageAspectFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

        public static ImageAspectFlagBit[] getBitFlags(ImageAspectFlagBit... bits) {
            return bits;
        }
    };

    enum ImageViewType {
        VK_IMAGE_VIEW_TYPE_1D(0),
        VK_IMAGE_VIEW_TYPE_2D(1),
        VK_IMAGE_VIEW_TYPE_3D(2),
        VK_IMAGE_VIEW_TYPE_CUBE(3),
        VK_IMAGE_VIEW_TYPE_1D_ARRAY(4),
        VK_IMAGE_VIEW_TYPE_2D_ARRAY(5),
        VK_IMAGE_VIEW_TYPE_CUBE_ARRAY(6);

        public final int value;

        ImageViewType(int value) {
            this.value = value;
        }

        public static ImageViewType get(int value) {
            for (ImageViewType ivt : values()) {
                if (value == ivt.value) {
                    return ivt;
                }
            }
            return null;
        }

    };

    enum PresentModeKHR {
        VK_PRESENT_MODE_IMMEDIATE_KHR(0),
        VK_PRESENT_MODE_MAILBOX_KHR(1),
        VK_PRESENT_MODE_FIFO_KHR(2),
        VK_PRESENT_MODE_FIFO_RELAXED_KHR(3),
        VK_PRESENT_MODE_SHARED_DEMAND_REFRESH_KHR(1000111000),
        VK_PRESENT_MODE_SHARED_CONTINUOUS_REFRESH_KHR(1000111001);

        public final int value;

        PresentModeKHR(int value) {
            this.value = value;
        }

        public static PresentModeKHR get(int value) {
            for (PresentModeKHR p : values()) {
                if (value == p.value) {
                    return p;
                }
            }
            return null;
        }

    };

    class SurfaceFormat {
        public final Vulkan10.Format format;
        final ColorSpaceKHR space;

        public SurfaceFormat(Vulkan10.Format format, ColorSpaceKHR space) {
            this.format = format;
            this.space = space;
        }

        @Override
        public String toString() {
            return format + ", " + space;
        }

        /**
         * Returns the imageformat
         * 
         * @return
         */
        public Vulkan10.Format getFormat() {
            return format;
        }

        /**
         * Returns the colorspace
         * 
         * @return
         */
        public ColorSpaceKHR getColorSpace() {
            return space;
        }

        public final ImageReader.TransferFunction getTransferFunction() {
            switch (space) {
                case VK_COLOR_SPACE_DOLBYVISION_EXT:
                case VK_COLOR_SPACE_HDR10_ST2084_EXT:
                    return ImageReader.TransferFunction.PQ;
                case VK_COLOR_SPACE_SRGB_NONLINEAR_KHR:
                case VK_COLORSPACE_SRGB_NONLINEAR_KHR:
                    return ImageReader.TransferFunction.SRGB;
                default:
                    throw new IllegalArgumentException("Not implemented");
            }
        }

    }

    enum DescriptorSetLayoutCreateFlagBit implements BitFlag {
        VK_DESCRIPTOR_SET_LAYOUT_CREATE_UPDATE_AFTER_BIND_POOL_BIT(0x00000002),
        VK_DESCRIPTOR_SET_LAYOUT_CREATE_PUSH_DESCRIPTOR_BIT_KHR(0x00000001),
        VK_DESCRIPTOR_SET_LAYOUT_CREATE_UPDATE_AFTER_BIND_POOL_BIT_EXT(
                VK_DESCRIPTOR_SET_LAYOUT_CREATE_UPDATE_AFTER_BIND_POOL_BIT.value),
        VK_DESCRIPTOR_SET_LAYOUT_CREATE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        private final int value;

        DescriptorSetLayoutCreateFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum DescriptorPoolCreateFlagBits implements BitFlag {
        VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT(0x00000001),
        VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT(0x00000002),
        VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT_EXT(VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT.value),
        VK_DESCRIPTOR_POOL_CREATE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        DescriptorPoolCreateFlagBits(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum DescriptorType {
        VK_DESCRIPTOR_TYPE_SAMPLER(0),
        VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER(1),
        VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE(2),
        VK_DESCRIPTOR_TYPE_STORAGE_IMAGE(3),
        VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER(4),
        VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER(5),
        VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER(6),
        VK_DESCRIPTOR_TYPE_STORAGE_BUFFER(7),
        VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC(8),
        VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC(9),
        VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT(10),
        VK_DESCRIPTOR_TYPE_INLINE_UNIFORM_BLOCK_EXT(1000138000),
        VK_DESCRIPTOR_TYPE_ACCELERATION_STRUCTURE_NV(1000165000),
        VK_DESCRIPTOR_TYPE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        DescriptorType(int value) {
            this.value = value;
        }

        /**
         * Returns true if this descriptor type is dynamic
         * 
         * @return
         */
        public boolean isDynamic() {
            return (this == VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC) || (this
                    == VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC);
        }

    }

    enum Format {
        VK_FORMAT_UNDEFINED(0, -1, -1),
        VK_FORMAT_R4G4_UNORM_PACK8(1, 2, 1),
        VK_FORMAT_R4G4B4A4_UNORM_PACK16(2, 4, 2),
        VK_FORMAT_B4G4R4A4_UNORM_PACK16(3, 4, 2),
        VK_FORMAT_R5G6B5_UNORM_PACK16(4, 3, 2),
        VK_FORMAT_B5G6R5_UNORM_PACK16(5, 3, 2),
        VK_FORMAT_R5G5B5A1_UNORM_PACK16(6, 4, 2),
        VK_FORMAT_B5G5R5A1_UNORM_PACK16(7, 4, 2),
        VK_FORMAT_A1R5G5B5_UNORM_PACK16(8, 4, 2),
        VK_FORMAT_R8_UNORM(9, 1, 1),
        VK_FORMAT_R8_SNORM(10, 1, 1),
        VK_FORMAT_R8_USCALED(11, 1, 1),
        VK_FORMAT_R8_SSCALED(12, 1, 1),
        VK_FORMAT_R8_UINT(13, 1, 1),
        VK_FORMAT_R8_SINT(14, 1, 1),
        VK_FORMAT_R8_SRGB(15, 1, 1),
        VK_FORMAT_R8G8_UNORM(16, 2, 2),
        VK_FORMAT_R8G8_SNORM(17, 2, 2),
        VK_FORMAT_R8G8_USCALED(18, 2, 2),
        VK_FORMAT_R8G8_SSCALED(19, 2, 2),
        VK_FORMAT_R8G8_UINT(20, 2, 2),
        VK_FORMAT_R8G8_SINT(21, 2, 2),
        VK_FORMAT_R8G8_SRGB(22, 2, 2),
        VK_FORMAT_R8G8B8_UNORM(23, 3, 3),
        VK_FORMAT_R8G8B8_SNORM(24, 3, 3),
        VK_FORMAT_R8G8B8_USCALED(25, 3, 3),
        VK_FORMAT_R8G8B8_SSCALED(26, 3, 3),
        VK_FORMAT_R8G8B8_UINT(27, 3, 3),
        VK_FORMAT_R8G8B8_SINT(28, 3, 3),
        VK_FORMAT_R8G8B8_SRGB(29, 3, 3),
        VK_FORMAT_B8G8R8_UNORM(30, 3, 3),
        VK_FORMAT_B8G8R8_SNORM(31, 3, 3),
        VK_FORMAT_B8G8R8_USCALED(32, 3, 3),
        VK_FORMAT_B8G8R8_SSCALED(33, 3, 3),
        VK_FORMAT_B8G8R8_UINT(34, 3, 3),
        VK_FORMAT_B8G8R8_SINT(35, 3, 3),
        VK_FORMAT_B8G8R8_SRGB(36, 3, 3),
        VK_FORMAT_R8G8B8A8_UNORM(37, 4, 4),
        VK_FORMAT_R8G8B8A8_SNORM(38, 4, 4),
        VK_FORMAT_R8G8B8A8_USCALED(39, 4, 4),
        VK_FORMAT_R8G8B8A8_SSCALED(40, 4, 4),
        VK_FORMAT_R8G8B8A8_UINT(41, 4, 4),
        VK_FORMAT_R8G8B8A8_SINT(42, 4, 4),
        VK_FORMAT_R8G8B8A8_SRGB(43, 4, 4),
        VK_FORMAT_B8G8R8A8_UNORM(44, 4, 4),
        VK_FORMAT_B8G8R8A8_SNORM(45, 4, 4),
        VK_FORMAT_B8G8R8A8_USCALED(46, 4, 4),
        VK_FORMAT_B8G8R8A8_SSCALED(47, 4, 4),
        VK_FORMAT_B8G8R8A8_UINT(48, 4, 4),
        VK_FORMAT_B8G8R8A8_SINT(49, 4, 4),
        VK_FORMAT_B8G8R8A8_SRGB(50, 4, 4),
        VK_FORMAT_A8B8G8R8_UNORM_PACK32(51, 4, 4),
        VK_FORMAT_A8B8G8R8_SNORM_PACK32(52, 4, 4),
        VK_FORMAT_A8B8G8R8_USCALED_PACK32(53, 4, 4),
        VK_FORMAT_A8B8G8R8_SSCALED_PACK32(54, 4, 4),
        VK_FORMAT_A8B8G8R8_UINT_PACK32(55, 4, 4),
        VK_FORMAT_A8B8G8R8_SINT_PACK32(56, 4, 4),
        VK_FORMAT_A8B8G8R8_SRGB_PACK32(57, 4, 4),
        VK_FORMAT_A2R10G10B10_UNORM_PACK32(58, 4, 4),
        VK_FORMAT_A2R10G10B10_SNORM_PACK32(59, 4, 4),
        VK_FORMAT_A2R10G10B10_USCALED_PACK32(60, 4, 4),
        VK_FORMAT_A2R10G10B10_SSCALED_PACK32(61, 4, 4),
        VK_FORMAT_A2R10G10B10_UINT_PACK32(62, 4, 4),
        VK_FORMAT_A2R10G10B10_SINT_PACK32(63, 4, 4),
        VK_FORMAT_A2B10G10R10_UNORM_PACK32(64, 4, 4),
        VK_FORMAT_A2B10G10R10_SNORM_PACK325(65, 4, 4),
        VK_FORMAT_A2B10G10R10_USCALED_PACK32(66, 4, 4),
        VK_FORMAT_A2B10G10R10_SSCALED_PACK32(67, 4, 4),
        VK_FORMAT_A2B10G10R10_UINT_PACK32(68, 4, 4),
        VK_FORMAT_A2B10G10R10_SINT_PACK32(69, 4, 4),
        VK_FORMAT_R16_UNORM(70, 1, 2),
        VK_FORMAT_R16_SNORM(71, 1, 2),
        VK_FORMAT_R16_USCALED(72, 1, 2),
        VK_FORMAT_R16_SSCALED(73, 1, 2),
        VK_FORMAT_R16_UINT(74, 1, 2),
        VK_FORMAT_R16_SINT(75, 1, 2),
        VK_FORMAT_R16_SFLOAT(76, 1, 2),
        VK_FORMAT_R16G16_UNORM(77, 2, 4),
        VK_FORMAT_R16G16_SNORM8(78, 2, 4),
        VK_FORMAT_R16G16_USCALED(79, 2, 4),
        VK_FORMAT_R16G16_SSCALED(80, 2, 4),
        VK_FORMAT_R16G16_UINT(81, 2, 4),
        VK_FORMAT_R16G16_SINT(82, 2, 4),
        VK_FORMAT_R16G16_SFLOAT(83, 2, 4),
        VK_FORMAT_R16G16B16_UNORM(84, 3, 6),
        VK_FORMAT_R16G16B16_SNORM(85, 3, 6),
        VK_FORMAT_R16G16B16_USCALED(86, 3, 6),
        VK_FORMAT_R16G16B16_SSCALED(87, 3, 6),
        VK_FORMAT_R16G16B16_UINT(88, 3, 6),
        VK_FORMAT_R16G16B16_SINT(89, 3, 6),
        VK_FORMAT_R16G16B16_SFLOAT(90, 3, 6),
        VK_FORMAT_R16G16B16A16_UNORM(91, 4, 8),
        VK_FORMAT_R16G16B16A16_SNORM(92, 4, 8),
        VK_FORMAT_R16G16B16A16_USCALED(93, 4, 8),
        VK_FORMAT_R16G16B16A16_SSCALED(94, 4, 8),
        VK_FORMAT_R16G16B16A16_UINT(95, 4, 8),
        VK_FORMAT_R16G16B16A16_SINT(96, 4, 8),
        VK_FORMAT_R16G16B16A16_SFLOAT(97, 4, 8),
        VK_FORMAT_R32_UINT(98, 1, 4),
        VK_FORMAT_R32_SINT(99, 1, 4),
        VK_FORMAT_R32_SFLOAT(100, 1, 4),
        VK_FORMAT_R32G32_UINT(101, 2, 8),
        VK_FORMAT_R32G32_SINT(102, 2, 8),
        VK_FORMAT_R32G32_SFLOAT(103, 2, 8),
        VK_FORMAT_R32G32B32_UINT(104, 3, 12),
        VK_FORMAT_R32G32B32_SINT(105, 3, 12),
        VK_FORMAT_R32G32B32_SFLOAT(106, 3, 12),
        VK_FORMAT_R32G32B32A32_UINT(107, 4, 16),
        VK_FORMAT_R32G32B32A32_SINT(108, 4, 16),
        VK_FORMAT_R32G32B32A32_SFLOAT(109, 4, 16),
        VK_FORMAT_R64_UINT(110, 1, 8),
        VK_FORMAT_R64_SINT(111, 1, 8),
        VK_FORMAT_R64_SFLOAT(112, 1, 8),
        VK_FORMAT_R64G64_UINT(113, 2, 16),
        VK_FORMAT_R64G64_SINT(114, 2, 16),
        VK_FORMAT_R64G64_SFLOAT(115, 2, 16),
        VK_FORMAT_R64G64B64_UINT(116, 3, 24),
        VK_FORMAT_R64G64B64_SINT(117, 3, 24),
        VK_FORMAT_R64G64B64_SFLOAT(118, 3, 24),
        VK_FORMAT_R64G64B64A64_UINT(119, 4, 32),
        VK_FORMAT_R64G64B64A64_SINT(120, 4, 32),
        VK_FORMAT_R64G64B64A64_SFLOAT(121, 4, 32),
        VK_FORMAT_B10G11R11_UFLOAT_PACK32(122, 3, 4),
        VK_FORMAT_E5B9G9R9_UFLOAT_PACK32(123, 3, 4),
        VK_FORMAT_D16_UNORM(124, 1, 2),
        VK_FORMAT_X8_D24_UNORM_PACK32(125, 2, 4),
        VK_FORMAT_D32_SFLOAT(126, 1, 4),
        VK_FORMAT_S8_UINT(127, 1, 1),
        VK_FORMAT_D16_UNORM_S8_UINT(128, 1, 2),
        VK_FORMAT_D24_UNORM_S8_UINT(129, 1, 3),
        VK_FORMAT_D32_SFLOAT_S8_UINT(130, 1, 4),
        VK_FORMAT_BC1_RGB_UNORM_BLOCK(131, -1, -1),
        VK_FORMAT_BC1_RGB_SRGB_BLOCK(132, -1, -1),
        VK_FORMAT_BC1_RGBA_UNORM_BLOCK(133, -1, -1),
        VK_FORMAT_BC1_RGBA_SRGB_BLOCK(134, -1, -1),
        VK_FORMAT_BC2_UNORM_BLOCK(135, -1, -1),
        VK_FORMAT_BC2_SRGB_BLOCK(136, -1, -1),
        VK_FORMAT_BC3_UNORM_BLOCK(137, -1, -1),
        VK_FORMAT_BC3_SRGB_BLOCK(138, -1, -1),
        VK_FORMAT_BC4_UNORM_BLOCK(139, -1, -1),
        VK_FORMAT_BC4_SNORM_BLOCK(140, -1, -1),
        VK_FORMAT_BC5_UNORM_BLOCK(141, -1, -1),
        VK_FORMAT_BC5_SNORM_BLOCK(142, -1, -1),
        VK_FORMAT_BC6H_UFLOAT_BLOCK(143, -1, -1),
        VK_FORMAT_BC6H_SFLOAT_BLOCK(144, -1, -1),
        VK_FORMAT_BC7_UNORM_BLOCK(145, -1, -1),
        VK_FORMAT_BC7_SRGB_BLOCK(146, -1, -1),
        VK_FORMAT_ETC2_R8G8B8_UNORM_BLOCK(147, -1, -1),
        VK_FORMAT_ETC2_R8G8B8_SRGB_BLOCK(148, -1, -1),
        VK_FORMAT_ETC2_R8G8B8A1_UNORM_BLOCK(149, -1, -1),
        VK_FORMAT_ETC2_R8G8B8A1_SRGB_BLOCK(150, -1, -1),
        VK_FORMAT_ETC2_R8G8B8A8_UNORM_BLOCK(151, -1, -1),
        VK_FORMAT_ETC2_R8G8B8A8_SRGB_BLOCK(152, -1, -1),
        VK_FORMAT_EAC_R11_UNORM_BLOCK(153, -1, -1),
        VK_FORMAT_EAC_R11_SNORM_BLOCK(154, -1, -1),
        VK_FORMAT_EAC_R11G11_UNORM_BLOCK(155, -1, -1),
        VK_FORMAT_EAC_R11G11_SNORM_BLOCK(156, -1, -1),
        VK_FORMAT_ASTC_4x4_UNORM_BLOCK(157, -1, -1),
        VK_FORMAT_ASTC_4x4_SRGB_BLOCK(158, -1, -1),
        VK_FORMAT_ASTC_5x4_UNORM_BLOCK(159, -1, -1),
        VK_FORMAT_ASTC_5x4_SRGB_BLOCK(160, -1, -1),
        VK_FORMAT_ASTC_5x5_UNORM_BLOCK(161, -1, -1),
        VK_FORMAT_ASTC_5x5_SRGB_BLOCK(162, -1, -1),
        VK_FORMAT_ASTC_6x5_UNORM_BLOCK(163, -1, -1),
        VK_FORMAT_ASTC_6x5_SRGB_BLOCK(164, -1, -1),
        VK_FORMAT_ASTC_6x6_UNORM_BLOCK(165, -1, -1),
        VK_FORMAT_ASTC_6x6_SRGB_BLOCK(166, -1, -1),
        VK_FORMAT_ASTC_8x5_UNORM_BLOCK(167, -1, -1),
        VK_FORMAT_ASTC_8x5_SRGB_BLOCK(168, -1, -1),
        VK_FORMAT_ASTC_8x6_UNORM_BLOCK(169, -1, -1),
        VK_FORMAT_ASTC_8x6_SRGB_BLOCK(170, -1, -1),
        VK_FORMAT_ASTC_8x8_UNORM_BLOCK(171, -1, -1),
        VK_FORMAT_ASTC_8x8_SRGB_BLOCK(172, -1, -1),
        VK_FORMAT_ASTC_10x5_UNORM_BLOCK(173, -1, -1),
        VK_FORMAT_ASTC_10x5_SRGB_BLOCK(174, -1, -1),
        VK_FORMAT_ASTC_10x6_UNORM_BLOCK(175, -1, -1),
        VK_FORMAT_ASTC_10x6_SRGB_BLOCK(176, -1, -1),
        VK_FORMAT_ASTC_10x8_UNORM_BLOCK(177, -1, -1),
        VK_FORMAT_ASTC_10x8_SRGB_BLOCK(178, -1, -1),
        VK_FORMAT_ASTC_10x10_UNORM_BLOCK(179, -1, -1),
        VK_FORMAT_ASTC_10x10_SRGB_BLOCK(180, -1, -1),
        VK_FORMAT_ASTC_12x10_UNORM_BLOCK(181, -1, -1),
        VK_FORMAT_ASTC_12x10_SRGB_BLOCK(182, -1, -1),
        VK_FORMAT_ASTC_12x12_UNORM_BLOCK(183, -1, -1),
        VK_FORMAT_ASTC_12x12_SRGB_BLOCK(184, -1, -1);

        public final int value;
        public final int typeSize;
        public final int sizeInBytes;

        Format(int val, int size, int bytes) {
            value = val;
            typeSize = size;
            sizeInBytes = bytes;
        }

        public static String toString(Format[] formats) {
            StringBuffer result = new StringBuffer();
            for (Format f : formats) {
                if (result.length() > 0) {
                    result.append(" | " + f.name());
                } else {
                    result.append(f.name());
                }
            }
            return result.toString();
        }

        public static Format get(int value) {
            for (Format sf : values()) {
                if (sf.value == value) {
                    return sf;
                }
            }
            return null;
        }

        public boolean isSRGB() {
            // TODO - add block compressed formats
            switch (this) {
                case VK_FORMAT_A8B8G8R8_SRGB_PACK32:
                case VK_FORMAT_B8G8R8_SRGB:
                case VK_FORMAT_B8G8R8A8_SRGB:
                case VK_FORMAT_R8_SRGB:
                case VK_FORMAT_R8G8_SRGB:
                case VK_FORMAT_R8G8B8_SRGB:
                case VK_FORMAT_R8G8B8A8_SRGB:
                    return true;
                default:
                    return false;
            }
        }

        public boolean isFP16() {
            switch (this) {
                case VK_FORMAT_R16_SFLOAT:
                case VK_FORMAT_R16G16_SFLOAT:
                case VK_FORMAT_R16G16B16_SFLOAT:
                case VK_FORMAT_R16G16B16A16_SFLOAT:
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Returns the byte size of each component
         * 
         * @return
         */
        public int getComponentByteSize() {
            switch (this) {
                case VK_FORMAT_R16_SFLOAT:
                case VK_FORMAT_R16_UNORM:
                case VK_FORMAT_R16G16_SFLOAT:
                case VK_FORMAT_R16G16_UNORM:
                case VK_FORMAT_R16G16B16_SFLOAT:
                case VK_FORMAT_R16G16B16_UNORM:
                case VK_FORMAT_R16G16B16A16_SFLOAT:
                case VK_FORMAT_R16G16B16A16_UNORM:
                case VK_FORMAT_R16_UINT:
                    return 2;
                case VK_FORMAT_R8_UINT:
                case VK_FORMAT_R8_SINT:
                case VK_FORMAT_R8_SNORM:
                    return 1;
                case VK_FORMAT_R32_SFLOAT:
                case VK_FORMAT_R32_UINT:
                case VK_FORMAT_R32_SINT:
                    return 4;
                default:
                    throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + name());
            }

        }

    }

    enum ColorSpaceKHR {
        VK_COLOR_SPACE_SRGB_NONLINEAR_KHR(0),
        VK_COLOR_SPACE_DISPLAY_P3_NONLINEAR_EXT(1000104001),
        VK_COLOR_SPACE_EXTENDED_SRGB_LINEAR_EXT(1000104002),
        VK_COLOR_SPACE_DISPLAY_P3_LINEAR_EXT(1000104003),
        VK_COLOR_SPACE_DCI_P3_NONLINEAR_EXT(1000104004),
        VK_COLOR_SPACE_BT709_LINEAR_EXT(1000104005),
        VK_COLOR_SPACE_BT709_NONLINEAR_EXT(1000104006),
        VK_COLOR_SPACE_BT2020_LINEAR_EXT(1000104007),
        VK_COLOR_SPACE_HDR10_ST2084_EXT(1000104008),
        VK_COLOR_SPACE_DOLBYVISION_EXT(1000104009),
        VK_COLOR_SPACE_HDR10_HLG_EXT(1000104010),
        VK_COLOR_SPACE_ADOBERGB_LINEAR_EXT(1000104011),
        VK_COLOR_SPACE_ADOBERGB_NONLINEAR_EXT(1000104012),
        VK_COLOR_SPACE_PASS_THROUGH_EXT(1000104013),
        VK_COLOR_SPACE_EXTENDED_SRGB_NONLINEAR_EXT(1000104014),
        VK_COLOR_SPACE_DISPLAY_NATIVE_AMD(1000213000),
        VK_COLORSPACE_SRGB_NONLINEAR_KHR(VK_COLOR_SPACE_SRGB_NONLINEAR_KHR.value),
        VK_COLOR_SPACE_DCI_P3_LINEAR_EXT(VK_COLOR_SPACE_DISPLAY_P3_LINEAR_EXT.value);

        public final int value;

        ColorSpaceKHR(int value) {
            this.value = value;
        }

        public static ColorSpaceKHR get(int value) {
            for (ColorSpaceKHR space : values()) {
                if (value == space.value) {
                    return space;
                }
            }
            return null;
        }

        public static ColorSpaceKHR get(String name) {
            for (ColorSpaceKHR cs : values()) {
                if (cs.name().equalsIgnoreCase(name)) {
                    return cs;
                }
            }
            return null;
        }

        /**
         * Returns true if the colorspace uses PQ
         * 
         * @return
         */
        public boolean isPQColorSpace() {
            switch (this) {
                case VK_COLOR_SPACE_DOLBYVISION_EXT:
                case VK_COLOR_SPACE_HDR10_ST2084_EXT:
                    return true;
                default:
                    return false;
            }
        }

        public boolean isSRGB() {
            switch (this) {
                case VK_COLOR_SPACE_SRGB_NONLINEAR_KHR:
                case VK_COLORSPACE_SRGB_NONLINEAR_KHR:
                case VK_COLOR_SPACE_EXTENDED_SRGB_NONLINEAR_EXT:
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Returns the color conversion matrix to convert colors from BT709 to this colorspace
         * 
         * @return Color conversion matrix or null if no conversion is needed
         */
        public float[] getFromBT709Matrix() {
            switch (this) {
                case VK_COLOR_SPACE_BT2020_LINEAR_EXT:
                case VK_COLOR_SPACE_DOLBYVISION_EXT:
                case VK_COLOR_SPACE_HDR10_HLG_EXT:
                case VK_COLOR_SPACE_HDR10_ST2084_EXT:
                    return BT2100.getMatrixBT709To2020();
                case VK_COLOR_SPACE_SRGB_NONLINEAR_KHR:
                case VK_COLORSPACE_SRGB_NONLINEAR_KHR:
                case VK_COLOR_SPACE_BT709_LINEAR_EXT:
                    // Do nothing
                    return null;
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                            + ", not implemented for colorspace " + this);
            }
        }

    };

    enum PrimitiveTopology {
        VK_PRIMITIVE_TOPOLOGY_POINT_LIST(0),
        VK_PRIMITIVE_TOPOLOGY_LINE_LIST(1),
        VK_PRIMITIVE_TOPOLOGY_LINE_STRIP(2),
        VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST(3),
        VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP(4),
        VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN(5),
        VK_PRIMITIVE_TOPOLOGY_LINE_LIST_WITH_ADJACENCY(6),
        VK_PRIMITIVE_TOPOLOGY_LINE_STRIP_WITH_ADJACENCY(7),
        VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST_WITH_ADJACENCY(8),
        VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP_WITH_ADJACENCY(9),
        VK_PRIMITIVE_TOPOLOGY_PATCH_LIST(10),
        VK_PRIMITIVE_TOPOLOGY_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        PrimitiveTopology(int value) {
            this.value = value;
        }

    };

    enum PipelineCreateFlagBit implements BitFlag {
        VK_PIPELINE_CREATE_DISABLE_OPTIMIZATION_BIT(0x00000001),
        VK_PIPELINE_CREATE_ALLOW_DERIVATIVES_BIT(0x00000002),
        VK_PIPELINE_CREATE_DERIVATIVE_BIT(0x00000004),
        // Provided by VK_VERSION_1_1
        VK_PIPELINE_CREATE_VIEW_INDEX_FROM_DEVICE_INDEX_BIT(0x00000008),
        // Provided by VK_VERSION_1_1
        VK_PIPELINE_CREATE_DISPATCH_BASE_BIT(0x00000010),
        // Provided by VK_VERSION_1_3
        VK_PIPELINE_CREATE_FAIL_ON_PIPELINE_COMPILE_REQUIRED_BIT(0x00000100),
        // Provided by VK_VERSION_1_3
        VK_PIPELINE_CREATE_EARLY_RETURN_ON_FAILURE_BIT(0x00000200),
        // Provided by VK_KHR_dynamic_rendering with VK_KHR_fragment_shading_rate
        VK_PIPELINE_CREATE_RENDERING_FRAGMENT_SHADING_RATE_ATTACHMENT_BIT_KHR(0x00200000),
        // Provided by VK_KHR_dynamic_rendering with VK_EXT_fragment_density_map
        VK_PIPELINE_CREATE_RENDERING_FRAGMENT_DENSITY_MAP_ATTACHMENT_BIT_EXT(0x00400000),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_PIPELINE_CREATE_RAY_TRACING_NO_NULL_ANY_HIT_SHADERS_BIT_KHR(0x00004000),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_PIPELINE_CREATE_RAY_TRACING_NO_NULL_CLOSEST_HIT_SHADERS_BIT_KHR(0x00008000),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_PIPELINE_CREATE_RAY_TRACING_NO_NULL_MISS_SHADERS_BIT_KHR(0x00010000),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_PIPELINE_CREATE_RAY_TRACING_NO_NULL_INTERSECTION_SHADERS_BIT_KHR(0x00020000),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_PIPELINE_CREATE_RAY_TRACING_SKIP_TRIANGLES_BIT_KHR(0x00001000),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_PIPELINE_CREATE_RAY_TRACING_SKIP_AABBS_BIT_KHR(0x00002000),
        // Provided by VK_KHR_ray_tracing_pipeline
        VK_PIPELINE_CREATE_RAY_TRACING_SHADER_GROUP_HANDLE_CAPTURE_REPLAY_BIT_KHR(0x00080000),
        // Provided by VK_NV_ray_tracing
        VK_PIPELINE_CREATE_DEFER_COMPILE_BIT_NV(0x00000020),
        // Provided by VK_KHR_pipeline_executable_properties
        VK_PIPELINE_CREATE_CAPTURE_STATISTICS_BIT_KHR(0x00000040),
        // Provided by VK_KHR_pipeline_executable_properties
        VK_PIPELINE_CREATE_CAPTURE_INTERNAL_REPRESENTATIONS_BIT_KHR(0x00000080),
        // Provided by VK_NV_device_generated_commands
        VK_PIPELINE_CREATE_INDIRECT_BINDABLE_BIT_NV(0x00040000),
        // Provided by VK_KHR_pipeline_library
        VK_PIPELINE_CREATE_LIBRARY_BIT_KHR(0x00000800),
        // Provided by VK_EXT_descriptor_buffer
        VK_PIPELINE_CREATE_DESCRIPTOR_BUFFER_BIT_EXT(0x20000000),
        // Provided by VK_EXT_graphics_pipeline_library
        VK_PIPELINE_CREATE_RETAIN_LINK_TIME_OPTIMIZATION_INFO_BIT_EXT(0x00800000),
        // Provided by VK_EXT_graphics_pipeline_library
        VK_PIPELINE_CREATE_LINK_TIME_OPTIMIZATION_BIT_EXT(0x00000400),
        // Provided by VK_NV_ray_tracing_motion_blur
        VK_PIPELINE_CREATE_RAY_TRACING_ALLOW_MOTION_BIT_NV(0x00100000),
        // Provided by VK_EXT_attachment_feedback_loop_layout
        VK_PIPELINE_CREATE_COLOR_ATTACHMENT_FEEDBACK_LOOP_BIT_EXT(0x02000000),
        // Provided by VK_EXT_attachment_feedback_loop_layout
        VK_PIPELINE_CREATE_DEPTH_STENCIL_ATTACHMENT_FEEDBACK_LOOP_BIT_EXT(0x04000000),
        // Provided by VK_EXT_opacity_micromap
        VK_PIPELINE_CREATE_RAY_TRACING_OPACITY_MICROMAP_BIT_EXT(0x01000000),
        // Provided by VK_NV_displacement_micromap
        VK_PIPELINE_CREATE_RAY_TRACING_DISPLACEMENT_MICROMAP_BIT_NV(0x10000000),
        // Provided by VK_EXT_pipeline_protected_access
        VK_PIPELINE_CREATE_NO_PROTECTED_ACCESS_BIT_EXT(0x08000000),
        // Provided by VK_EXT_pipeline_protected_access
        VK_PIPELINE_CREATE_PROTECTED_ACCESS_ONLY_BIT_EXT(0x40000000),
        // Provided by VK_VERSION_1_1
        VK_PIPELINE_CREATE_DISPATCH_BASE(VK_PIPELINE_CREATE_DISPATCH_BASE_BIT.value),
        // Provided by VK_KHR_dynamic_rendering with VK_KHR_fragment_shading_rate
        VK_PIPELINE_RASTERIZATION_STATE_CREATE_FRAGMENT_SHADING_RATE_ATTACHMENT_BIT_KHR(
                VK_PIPELINE_CREATE_RENDERING_FRAGMENT_SHADING_RATE_ATTACHMENT_BIT_KHR.value),
        // Provided by VK_KHR_dynamic_rendering with VK_EXT_fragment_density_map
        VK_PIPELINE_RASTERIZATION_STATE_CREATE_FRAGMENT_DENSITY_MAP_ATTACHMENT_BIT_EXT(
                VK_PIPELINE_CREATE_RENDERING_FRAGMENT_DENSITY_MAP_ATTACHMENT_BIT_EXT.value),
        // Provided by VK_KHR_device_group
        VK_PIPELINE_CREATE_VIEW_INDEX_FROM_DEVICE_INDEX_BIT_KHR(
                VK_PIPELINE_CREATE_VIEW_INDEX_FROM_DEVICE_INDEX_BIT.value),
        // Provided by VK_KHR_device_group
        VK_PIPELINE_CREATE_DISPATCH_BASE_KHR(VK_PIPELINE_CREATE_DISPATCH_BASE.value),
        // Provided by VK_EXT_pipeline_creation_cache_control
        VK_PIPELINE_CREATE_FAIL_ON_PIPELINE_COMPILE_REQUIRED_BIT_EXT(
                VK_PIPELINE_CREATE_FAIL_ON_PIPELINE_COMPILE_REQUIRED_BIT.value),
        // Provided by VK_EXT_pipeline_creation_cache_control
        VK_PIPELINE_CREATE_EARLY_RETURN_ON_FAILURE_BIT_EXT(VK_PIPELINE_CREATE_EARLY_RETURN_ON_FAILURE_BIT.value);

        private final int value;

        PipelineCreateFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    }

    enum PolygonMode {
        VK_POLYGON_MODE_FILL(0),
        VK_POLYGON_MODE_LINE(1),
        VK_POLYGON_MODE_POINT(2),
        VK_POLYGON_MODE_FILL_RECTANGLE_NV(1000153000),
        VK_POLYGON_MODE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        PolygonMode(int value) {
            this.value = value;
        }
    }

    enum CullModeFlagBit implements BitFlag {
        VK_CULL_MODE_NONE(0),
        VK_CULL_MODE_FRONT_BIT(0x00000001),
        VK_CULL_MODE_BACK_BIT(0x00000002),
        VK_CULL_MODE_FRONT_AND_BACK(0x00000003),
        VK_CULL_MODE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        CullModeFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    enum FrontFace {
        VK_FRONT_FACE_COUNTER_CLOCKWISE(0),
        VK_FRONT_FACE_CLOCKWISE(1),
        VK_FRONT_FACE_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        FrontFace(int value) {
            this.value = value;
        }
    }

    enum SampleCountFlagBit implements BitFlag {
        VK_SAMPLE_COUNT_1_BIT(0x00000001),
        VK_SAMPLE_COUNT_2_BIT(0x00000002),
        VK_SAMPLE_COUNT_4_BIT(0x00000004),
        VK_SAMPLE_COUNT_8_BIT(0x00000008),
        VK_SAMPLE_COUNT_16_BIT(0x00000010),
        VK_SAMPLE_COUNT_32_BIT(0x00000020),
        VK_SAMPLE_COUNT_64_BIT(0x00000040),
        VK_SAMPLE_COUNT_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        SampleCountFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

        /**
         * Returns the enum that matches the number of samples in count, or
         * {@link SampleCountFlagBit#VK_SAMPLE_COUNT_1_BIT} if no match
         * 
         * @param count
         */
        public static SampleCountFlagBit get(int count) {
            for (SampleCountFlagBit s : SampleCountFlagBit.values()) {
                if (count == s.value) {
                    return s;
                }
            }
            return SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT;
        }
    }

    enum CompareOp {
        VK_COMPARE_OP_NEVER(0),
        VK_COMPARE_OP_LESS(1),
        VK_COMPARE_OP_EQUAL(2),
        VK_COMPARE_OP_LESS_OR_EQUAL(3),
        VK_COMPARE_OP_GREATER(4),
        VK_COMPARE_OP_NOT_EQUAL(5),
        VK_COMPARE_OP_GREATER_OR_EQUAL(6),
        VK_COMPARE_OP_ALWAYS(7),
        VK_COMPARE_OP_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        CompareOp(int value) {
            this.value = value;
        }
    }

    enum LogicOp {
        VK_LOGIC_OP_CLEAR(0),
        VK_LOGIC_OP_AND(1),
        VK_LOGIC_OP_AND_REVERSE(2),
        VK_LOGIC_OP_COPY(3),
        VK_LOGIC_OP_AND_INVERTED(4),
        VK_LOGIC_OP_NO_OP(5),
        VK_LOGIC_OP_XOR(6),
        VK_LOGIC_OP_OR(7),
        VK_LOGIC_OP_NOR(8),
        VK_LOGIC_OP_EQUIVALENT(9),
        VK_LOGIC_OP_INVERT(10),
        VK_LOGIC_OP_OR_REVERSE(11),
        VK_LOGIC_OP_COPY_INVERTED(12),
        VK_LOGIC_OP_OR_INVERTED(13),
        VK_LOGIC_OP_NAND(14),
        VK_LOGIC_OP_SET(15),
        VK_LOGIC_OP_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        LogicOp(int value) {
            this.value = value;
        }
    }

    enum BlendOp {
        VK_BLEND_OP_ADD(0),
        VK_BLEND_OP_SUBTRACT(1),
        VK_BLEND_OP_REVERSE_SUBTRACT(2),
        VK_BLEND_OP_MIN(3),
        VK_BLEND_OP_MAX(4),
        VK_BLEND_OP_ZERO_EXT(1000148000),
        VK_BLEND_OP_SRC_EXT(1000148001),
        VK_BLEND_OP_DST_EXT(1000148002),
        VK_BLEND_OP_SRC_OVER_EXT(1000148003),
        VK_BLEND_OP_DST_OVER_EXT(1000148004),
        VK_BLEND_OP_SRC_IN_EXT(1000148005),
        VK_BLEND_OP_DST_IN_EXT(1000148006),
        VK_BLEND_OP_SRC_OUT_EXT(1000148007),
        VK_BLEND_OP_DST_OUT_EXT(1000148008),
        VK_BLEND_OP_SRC_ATOP_EXT(1000148009),
        VK_BLEND_OP_DST_ATOP_EXT(1000148010),
        VK_BLEND_OP_XOR_EXT(1000148011),
        VK_BLEND_OP_MULTIPLY_EXT(1000148012),
        VK_BLEND_OP_SCREEN_EXT(1000148013),
        VK_BLEND_OP_OVERLAY_EXT(1000148014),
        VK_BLEND_OP_DARKEN_EXT(1000148015),
        VK_BLEND_OP_LIGHTEN_EXT(1000148016),
        VK_BLEND_OP_COLORDODGE_EXT(1000148017),
        VK_BLEND_OP_COLORBURN_EXT(1000148018),
        VK_BLEND_OP_HARDLIGHT_EXT(1000148019),
        VK_BLEND_OP_SOFTLIGHT_EXT(1000148020),
        VK_BLEND_OP_DIFFERENCE_EXT(1000148021),
        VK_BLEND_OP_EXCLUSION_EXT(1000148022),
        VK_BLEND_OP_INVERT_EXT(1000148023),
        VK_BLEND_OP_INVERT_RGB_EXT(1000148024),
        VK_BLEND_OP_LINEARDODGE_EXT(1000148025),
        VK_BLEND_OP_LINEARBURN_EXT(1000148026),
        VK_BLEND_OP_VIVIDLIGHT_EXT(1000148027),
        VK_BLEND_OP_LINEARLIGHT_EXT(1000148028),
        VK_BLEND_OP_PINLIGHT_EXT(1000148029),
        VK_BLEND_OP_HARDMIX_EXT(1000148030),
        VK_BLEND_OP_HSL_HUE_EXT(1000148031),
        VK_BLEND_OP_HSL_SATURATION_EXT(1000148032),
        VK_BLEND_OP_HSL_COLOR_EXT(1000148033),
        VK_BLEND_OP_HSL_LUMINOSITY_EXT(1000148034),
        VK_BLEND_OP_PLUS_EXT(1000148035),
        VK_BLEND_OP_PLUS_CLAMPED_EXT(1000148036),
        VK_BLEND_OP_PLUS_CLAMPED_ALPHA_EXT(1000148037),
        VK_BLEND_OP_PLUS_DARKER_EXT(1000148038),
        VK_BLEND_OP_MINUS_EXT(1000148039),
        VK_BLEND_OP_MINUS_CLAMPED_EXT(1000148040),
        VK_BLEND_OP_CONTRAST_EXT(1000148041),
        VK_BLEND_OP_INVERT_OVG_EXT(1000148042),
        VK_BLEND_OP_RED_EXT(1000148043),
        VK_BLEND_OP_GREEN_EXT(1000148044),
        VK_BLEND_OP_BLUE_EXT(1000148045),
        VK_BLEND_OP_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        BlendOp(int value) {
            this.value = value;
        }
    }

    enum BlendFactor {
        VK_BLEND_FACTOR_ZERO(0),
        VK_BLEND_FACTOR_ONE(1),
        VK_BLEND_FACTOR_SRC_COLOR(2),
        VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR(3),
        VK_BLEND_FACTOR_DST_COLOR(4),
        VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR(5),
        VK_BLEND_FACTOR_SRC_ALPHA(6),
        VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA(7),
        VK_BLEND_FACTOR_DST_ALPHA(8),
        VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA(9),
        VK_BLEND_FACTOR_CONSTANT_COLOR(10),
        VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR(11),
        VK_BLEND_FACTOR_CONSTANT_ALPHA(12),
        VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA(13),
        VK_BLEND_FACTOR_SRC_ALPHA_SATURATE(14),
        VK_BLEND_FACTOR_SRC1_COLOR(15),
        VK_BLEND_FACTOR_ONE_MINUS_SRC1_COLOR(16),
        VK_BLEND_FACTOR_SRC1_ALPHA(17),
        VK_BLEND_FACTOR_ONE_MINUS_SRC1_ALPHA(18),
        VK_BLEND_FACTOR_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        BlendFactor(int value) {
            this.value = value;
        }
    }

    enum ColorComponentFlagBit implements BitFlag {
        VK_COLOR_COMPONENT_R_BIT(0x00000001),
        VK_COLOR_COMPONENT_G_BIT(0x00000002),
        VK_COLOR_COMPONENT_B_BIT(0x00000004),
        VK_COLOR_COMPONENT_A_BIT(0x00000008),
        VK_COLOR_COMPONENT_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        ColorComponentFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    enum AttachmentLoadOp {
        VK_ATTACHMENT_LOAD_OP_LOAD(0),
        VK_ATTACHMENT_LOAD_OP_CLEAR(1),
        VK_ATTACHMENT_LOAD_OP_DONT_CARE(2),
        VK_ATTACHMENT_LOAD_OP_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        AttachmentLoadOp(int value) {
            this.value = value;
        }
    }

    enum AttachmentStoreOp {
        VK_ATTACHMENT_STORE_OP_STORE(0),
        VK_ATTACHMENT_STORE_OP_DONT_CARE(1),
        VK_ATTACHMENT_STORE_OP_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        AttachmentStoreOp(int value) {
            this.value = value;
        }
    }

    enum ImageLayout {
        VK_IMAGE_LAYOUT_UNDEFINED(0),
        VK_IMAGE_LAYOUT_GENERAL(1),
        VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL(2),
        VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL(3),
        VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL(4),
        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL(5),
        VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL(6),
        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL(7),
        VK_IMAGE_LAYOUT_PREINITIALIZED(8),
        VK_IMAGE_LAYOUT_DEPTH_READ_ONLY_STENCIL_ATTACHMENT_OPTIMAL(1000117000),
        VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_STENCIL_READ_ONLY_OPTIMAL(1000117001),
        VK_IMAGE_LAYOUT_PRESENT_SRC_KHR(1000001002),
        VK_IMAGE_LAYOUT_SHARED_PRESENT_KHR(1000111000),
        VK_IMAGE_LAYOUT_SHADING_RATE_OPTIMAL_NV(1000164003),
        VK_IMAGE_LAYOUT_FRAGMENT_DENSITY_MAP_OPTIMAL_EXT(1000218000),
        VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL_KHR(1000241000),
        VK_IMAGE_LAYOUT_DEPTH_READ_ONLY_OPTIMAL_KHR(1000241001),
        VK_IMAGE_LAYOUT_STENCIL_ATTACHMENT_OPTIMAL_KHR(1000241002),
        VK_IMAGE_LAYOUT_STENCIL_READ_ONLY_OPTIMAL_KHR(1000241003),
        VK_IMAGE_LAYOUT_DEPTH_READ_ONLY_STENCIL_ATTACHMENT_OPTIMAL_KHR(
                VK_IMAGE_LAYOUT_DEPTH_READ_ONLY_STENCIL_ATTACHMENT_OPTIMAL.value),
        VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_STENCIL_READ_ONLY_OPTIMAL_KHR(
                VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_STENCIL_READ_ONLY_OPTIMAL.value),
        VK_IMAGE_LAYOUT_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        ImageLayout(int value) {
            this.value = value;
        }
    }

    enum SubpassDescriptionFlagBit implements BitFlag {
        VK_SUBPASS_DESCRIPTION_PER_VIEW_ATTRIBUTES_BIT_NVX(0x00000001),
        VK_SUBPASS_DESCRIPTION_PER_VIEW_POSITION_X_ONLY_BIT_NVX(0x00000002),
        VK_SUBPASS_DESCRIPTION_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        private final int value;

        SubpassDescriptionFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    enum PipelineBindPoint {
        VK_PIPELINE_BIND_POINT_GRAPHICS(0),
        VK_PIPELINE_BIND_POINT_COMPUTE(1),
        VK_PIPELINE_BIND_POINT_RAY_TRACING_NV(1000165000),
        VK_PIPELINE_BIND_POINT_MAX_ENUM(0x7FFFFFFF);

        public final int value;

        PipelineBindPoint(int value) {
            this.value = value;
        }

    }

    enum PipelineStageFlagBit implements BitFlag {
        VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT(0x00000001),
        VK_PIPELINE_STAGE_DRAW_INDIRECT_BIT(0x00000002),
        VK_PIPELINE_STAGE_VERTEX_INPUT_BIT(0x00000004),
        VK_PIPELINE_STAGE_VERTEX_SHADER_BIT(0x00000008),
        VK_PIPELINE_STAGE_TESSELLATION_CONTROL_SHADER_BIT(0x00000010),
        VK_PIPELINE_STAGE_TESSELLATION_EVALUATION_SHADER_BIT(0x00000020),
        VK_PIPELINE_STAGE_GEOMETRY_SHADER_BIT(0x00000040),
        VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT(0x00000080),
        VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT(0x00000100),
        VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT(0x00000200),
        VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT(0x00000400),
        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT(0x00000800),
        VK_PIPELINE_STAGE_TRANSFER_BIT(0x00001000),
        VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT(0x00002000),
        VK_PIPELINE_STAGE_HOST_BIT(0x00004000),
        VK_PIPELINE_STAGE_ALL_GRAPHICS_BIT(0x00008000),
        VK_PIPELINE_STAGE_ALL_COMMANDS_BIT(0x00010000),
        VK_PIPELINE_STAGE_TRANSFORM_FEEDBACK_BIT_EXT(0x01000000),
        VK_PIPELINE_STAGE_CONDITIONAL_RENDERING_BIT_EXT(0x00040000),
        VK_PIPELINE_STAGE_COMMAND_PROCESS_BIT_NVX(0x00020000),
        VK_PIPELINE_STAGE_SHADING_RATE_IMAGE_BIT_NV(0x00400000),
        VK_PIPELINE_STAGE_RAY_TRACING_SHADER_BIT_NV(0x00200000),
        VK_PIPELINE_STAGE_ACCELERATION_STRUCTURE_BUILD_BIT_NV(0x02000000),
        VK_PIPELINE_STAGE_TASK_SHADER_BIT_NV(0x00080000),
        VK_PIPELINE_STAGE_MESH_SHADER_BIT_NV(0x00100000),
        VK_PIPELINE_STAGE_FRAGMENT_DENSITY_PROCESS_BIT_EXT(0x00800000),
        VK_PIPELINE_STAGE_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        private final int value;

        PipelineStageFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

        public static PipelineStageFlagBit[] getBitFlags(PipelineStageFlagBit... bits) {
            return bits;
        }

    }

    enum AccessFlagBit implements BitFlag {
        VK_ACCESS_INDIRECT_COMMAND_READ_BIT(0x00000001),
        VK_ACCESS_INDEX_READ_BIT(0x00000002),
        VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT(0x00000004),
        VK_ACCESS_UNIFORM_READ_BIT(0x00000008),
        VK_ACCESS_INPUT_ATTACHMENT_READ_BIT(0x00000010),
        VK_ACCESS_SHADER_READ_BIT(0x00000020),
        VK_ACCESS_SHADER_WRITE_BIT(0x00000040),
        VK_ACCESS_COLOR_ATTACHMENT_READ_BIT(0x00000080),
        VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT(0x00000100),
        VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT(0x00000200),
        VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT(0x00000400),
        VK_ACCESS_TRANSFER_READ_BIT(0x00000800),
        VK_ACCESS_TRANSFER_WRITE_BIT(0x00001000),
        VK_ACCESS_HOST_READ_BIT(0x00002000),
        VK_ACCESS_HOST_WRITE_BIT(0x00004000),
        VK_ACCESS_MEMORY_READ_BIT(0x00008000),
        VK_ACCESS_MEMORY_WRITE_BIT(0x00010000),
        VK_ACCESS_TRANSFORM_FEEDBACK_WRITE_BIT_EXT(0x02000000),
        VK_ACCESS_TRANSFORM_FEEDBACK_COUNTER_READ_BIT_EXT(0x04000000),
        VK_ACCESS_TRANSFORM_FEEDBACK_COUNTER_WRITE_BIT_EXT(0x08000000),
        VK_ACCESS_CONDITIONAL_RENDERING_READ_BIT_EXT(0x00100000),
        VK_ACCESS_COMMAND_PROCESS_READ_BIT_NVX(0x00020000),
        VK_ACCESS_COMMAND_PROCESS_WRITE_BIT_NVX(0x00040000),
        VK_ACCESS_COLOR_ATTACHMENT_READ_NONCOHERENT_BIT_EXT(0x00080000),
        VK_ACCESS_SHADING_RATE_IMAGE_READ_BIT_NV(0x00800000),
        VK_ACCESS_ACCELERATION_STRUCTURE_READ_BIT_NV(0x00200000),
        VK_ACCESS_ACCELERATION_STRUCTURE_WRITE_BIT_NV(0x00400000),
        VK_ACCESS_FRAGMENT_DENSITY_MAP_READ_BIT_EXT(0x01000000),
        VK_ACCESS_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        private final int value;

        AccessFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

        public static AccessFlagBit[] getBitFlags(AccessFlagBit... bits) {
            return bits;
        }

    }

    enum DependencyFlagBit implements BitFlag {
        VK_DEPENDENCY_BY_REGION_BIT(0x00000001),
        VK_DEPENDENCY_DEVICE_GROUP_BIT(0x00000004),
        VK_DEPENDENCY_VIEW_LOCAL_BIT(0x00000002),
        VK_DEPENDENCY_VIEW_LOCAL_BIT_KHR(VK_DEPENDENCY_VIEW_LOCAL_BIT.value),
        VK_DEPENDENCY_DEVICE_GROUP_BIT_KHR(VK_DEPENDENCY_DEVICE_GROUP_BIT.value),
        VK_DEPENDENCY_FLAG_BITS_MAX_ENUM(0x7FFFFFFF);

        private final int value;

        DependencyFlagBit(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    enum QueueFlagBit implements BitFlag {
        VK_QUEUE_GRAPHICS_BIT(1),
        VK_QUEUE_COMPUTE_BIT(2),
        VK_QUEUE_TRANSFER_BIT(4),
        VK_QUEUE_SPARSE_BINDING_BIT(8),
        VK_QUEUE_PROTECTED_BIT(16);

        private final int value;

        QueueFlagBit(int value) {
            this.value = value;
        }

        public static String toString(int flags) {
            return BitFlags.toString(flags, QueueFlagBit.values());
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }

    };

    interface VulkanExtension {
        String getName();

        static VulkanExtension getExtension(String name) {
            VulkanExtension e = Extension.getExtension(name);
            if (e != null) {
                return e;
            }
            e = Vulkan11Extension.getExtension(name);
            if (e != null) {
                return e;
            }
            e = Vulkan12Extension.getExtension(name);
            if (e != null) {
                return e;
            }
            e = Vulkan13Extension.getExtension(name);
            if (e != null) {
                return e;
            }
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Unknown exception: " + name);
        }
    }

    enum Extension implements VulkanExtension {
        VK_KHR_acceleration_structure,
        VK_KHR_android_surface,
        VK_KHR_deferred_host_operations,
        VK_KHR_display,
        VK_KHR_display_swapchain,
        VK_KHR_external_fence_fd,
        VK_KHR_external_fence_win32,
        VK_KHR_external_memory_fd,
        VK_KHR_external_memory_win32,
        VK_KHR_external_semaphore_fd,
        VK_KHR_external_semaphore_win32,
        VK_KHR_fragment_shader_barycentric,
        VK_KHR_fragment_shading_rate,
        VK_KHR_get_display_properties2,
        VK_KHR_get_surface_capabilities2,
        VK_KHR_global_priority,
        VK_KHR_incremental_present,
        VK_KHR_performance_query,
        VK_KHR_pipeline_executable_properties,
        VK_KHR_pipeline_library,
        VK_KHR_portability_enumeration,
        VK_KHR_present_id,
        VK_KHR_present_wait,
        VK_KHR_push_descriptor,
        VK_KHR_ray_query,
        VK_KHR_ray_tracing_maintenance1,
        VK_KHR_ray_tracing_pipeline,
        VK_KHR_shader_clock,
        VK_KHR_shader_subgroup_uniform_control_flow,
        VK_KHR_shared_presentable_image,
        VK_KHR_surface,
        VK_KHR_surface_protected_capabilities,
        VK_KHR_swapchain,
        VK_KHR_swapchain_mutable_format,
        VK_KHR_wayland_surface,
        VK_KHR_win32_keyed_mutex,
        VK_KHR_win32_surface,
        VK_KHR_workgroup_memory_explicit_layout,
        VK_KHR_xcb_surface,
        VK_KHR_xlib_surface,
        VK_EXT_acquire_drm_display,
        VK_EXT_acquire_xlib_display,
        VK_EXT_astc_decode_mode,
        VK_EXT_attachment_feedback_loop_layout,
        VK_EXT_blend_operation_advanced,
        VK_EXT_border_color_swizzle,
        VK_EXT_calibrated_timestamps,
        VK_EXT_color_write_enable,
        VK_EXT_conditional_rendering,
        VK_EXT_conservative_rasterization,
        VK_EXT_custom_border_color,
        VK_EXT_debug_utils,
        VK_EXT_depth_clamp_zero_one,
        VK_EXT_depth_clip_control,
        VK_EXT_depth_clip_enable,
        VK_EXT_depth_range_unrestricted,
        VK_EXT_descriptor_buffer,
        VK_EXT_device_address_binding_report,
        VK_EXT_device_fault,
        VK_EXT_device_memory_report,
        VK_EXT_direct_mode_display,
        VK_EXT_directfb_surface,
        VK_EXT_discard_rectangles,
        VK_EXT_display_control,
        VK_EXT_display_surface_counter,
        VK_EXT_extended_dynamic_state3,
        VK_EXT_external_memory_dma_buf,
        VK_EXT_external_memory_host,
        VK_EXT_filter_cubic,
        VK_EXT_fragment_density_map,
        VK_EXT_fragment_density_map2,
        VK_EXT_fragment_shader_interlock,
        VK_EXT_full_screen_exclusive,
        VK_EXT_graphics_pipeline_library,
        VK_EXT_hdr_metadata,
        VK_EXT_headless_surface,
        VK_EXT_image_2d_view_of_3d,
        VK_EXT_image_compression_control,
        VK_EXT_image_compression_control_swapchain,
        VK_EXT_image_drm_format_modifier,
        VK_EXT_image_view_min_lod,
        VK_EXT_index_type_uint8,
        VK_EXT_legacy_dithering,
        VK_EXT_line_rasterization,
        VK_EXT_load_store_op_none,
        VK_EXT_memory_budget,
        VK_EXT_memory_priority,
        VK_EXT_mesh_shader,
        VK_EXT_metal_objects,
        VK_EXT_metal_surface,
        VK_EXT_multi_draw,
        VK_EXT_multisampled_render_to_single_sampled,
        VK_EXT_mutable_descriptor_type,
        VK_EXT_non_seamless_cube_map,
        VK_EXT_opacity_micromap,
        VK_EXT_pageable_device_local_memory,
        VK_EXT_pci_bus_info,
        VK_EXT_physical_device_drm,
        VK_EXT_pipeline_properties,
        VK_EXT_pipeline_protected_access,
        VK_EXT_pipeline_robustness,
        VK_EXT_post_depth_coverage,
        VK_EXT_primitive_topology_list_restart,
        VK_EXT_primitives_generated_query,
        VK_EXT_provoking_vertex,
        VK_EXT_queue_family_foreign,
        VK_EXT_rasterization_order_attachment_access,
        VK_EXT_rgba10x6_formats,
        VK_EXT_robustness2,
        VK_EXT_sample_locations,
        VK_EXT_shader_atomic_float,
        VK_EXT_shader_atomic_float2,
        VK_EXT_shader_image_atomic_int64,
        VK_EXT_shader_module_identifier,
        VK_EXT_shader_stencil_export,
        VK_EXT_subpass_merge_feedback,
        VK_EXT_swapchain_colorspace,
        VK_EXT_transform_feedback,
        VK_EXT_validation_cache,
        VK_EXT_validation_features,
        VK_EXT_vertex_attribute_divisor,
        VK_EXT_vertex_input_dynamic_state,
        VK_EXT_ycbcr_image_arrays,
        VK_AMD_buffer_marker,
        VK_AMD_device_coherent_memory,
        VK_AMD_display_native_hdr,
        VK_AMD_gcn_shader,
        VK_AMD_memory_overallocation_behavior,
        VK_AMD_mixed_attachment_samples,
        VK_AMD_pipeline_compiler_control,
        VK_AMD_rasterization_order,
        VK_AMD_shader_ballot,
        VK_AMD_shader_core_properties,
        VK_AMD_shader_core_properties2,
        VK_AMD_shader_early_and_late_fragment_tests,
        VK_AMD_shader_explicit_vertex_parameter,
        VK_AMD_shader_fragment_mask,
        VK_AMD_shader_image_load_store_lod,
        VK_AMD_shader_info,
        VK_AMD_shader_trinary_minmax,
        VK_AMD_texture_gather_bias_lod,
        VK_ANDROID_external_memory_android_hardware_buffer,
        VK_ARM_shader_core_builtins,
        VK_FUCHSIA_buffer_collection,
        VK_FUCHSIA_external_memory,
        VK_FUCHSIA_external_semaphore,
        VK_FUCHSIA_imagepipe_surface,
        VK_GGP_frame_token,
        VK_GGP_stream_descriptor_surface,
        VK_GOOGLE_decorate_string,
        VK_GOOGLE_display_timing,
        VK_GOOGLE_hlsl_functionality1,
        VK_GOOGLE_surfaceless_query,
        VK_GOOGLE_user_type,
        VK_HUAWEI_invocation_mask,
        VK_HUAWEI_subpass_shading,
        VK_IMG_filter_cubic,
        VK_INTEL_performance_query,
        VK_INTEL_shader_integer_functions2,
        VK_NN_vi_surface,
        VK_NV_acquire_winrt_display,
        VK_NV_clip_space_w_scaling,
        VK_NV_compute_shader_derivatives,
        VK_NV_cooperative_matrix,
        VK_NV_copy_memory_indirect,
        VK_NV_corner_sampled_image,
        VK_NV_coverage_reduction_mode,
        VK_NV_dedicated_allocation_image_aliasing,
        VK_NV_device_diagnostic_checkpoints,
        VK_NV_device_diagnostics_config,
        VK_NV_device_generated_commands,
        VK_NV_external_memory_rdma,
        VK_NV_fill_rectangle,
        VK_NV_fragment_coverage_to_color,
        VK_NV_fragment_shading_rate_enums,
        VK_NV_framebuffer_mixed_samples,
        VK_NV_geometry_shader_passthrough,
        VK_NV_inherited_viewport_scissor,
        VK_NV_linear_color_attachment,
        VK_NV_memory_decompression,
        VK_NV_mesh_shader,
        VK_NV_optical_flow,
        VK_NV_present_barrier,
        VK_NV_ray_tracing,
        VK_NV_ray_tracing_invocation_reorder,
        VK_NV_ray_tracing_motion_blur,
        VK_NV_representative_fragment_test,
        VK_NV_sample_mask_override_coverage,
        VK_NV_scissor_exclusive,
        VK_NV_shader_image_footprint,
        VK_NV_shader_sm_builtins,
        VK_NV_shader_subgroup_partitioned,
        VK_NV_shading_rate_image,
        VK_NV_viewport_array2,
        VK_NV_viewport_swizzle,
        VK_NVX_binary_import,
        VK_NVX_image_view_handle,
        VK_NVX_multiview_per_view_attributes,
        VK_QCOM_fragment_density_map_offset,
        VK_QCOM_image_processing,
        VK_QCOM_render_pass_shader_resolve,
        VK_QCOM_render_pass_store_ops,
        VK_QCOM_render_pass_transform,
        VK_QCOM_rotated_copy_commands,
        VK_QCOM_tile_properties,
        VK_QNX_screen_surface,
        VK_SEC_amigo_profiling,
        VK_VALVE_descriptor_set_host_mapping;

        /**
         * Returns the name of the extension in a null terminated byte buffer.
         * 
         * @return
         */
        public final ByteBuffer createByteBuffer() {
            return Buffers.createByteBuffer(name());
        }

        /**
         * Returns true if this extension is in the extensions array
         * 
         * @param extensions
         * @return True if this extension is present in the array
         */
        public static boolean contains(String name, VulkanExtension... extensions) {
            for (VulkanExtension e : extensions) {
                if (name.contentEquals(e.getName())) {
                    return true;
                }
            }
            return false;
        }

        public static Extension getExtension(String name) {
            for (Extension e : values()) {
                if (e.name().contentEquals(name)) {
                    return e;
                }
            }
            return null;
        }

        @Override
        public String getName() {
            return this.name();
        }
    }

    enum Vulkan11Extension implements VulkanExtension {
        VK_KHR_16bit_storage,
        VK_KHR_bind_memory2,
        VK_KHR_dedicated_allocation,
        VK_KHR_descriptor_update_template,
        VK_KHR_device_group,
        VK_KHR_device_group_creation,
        VK_KHR_external_fence,
        VK_KHR_external_fence_capabilities,
        VK_KHR_external_memory,
        VK_KHR_external_memory_capabilities,
        VK_KHR_external_semaphore,
        VK_KHR_external_semaphore_capabilities,
        VK_KHR_get_memory_requirements2,
        VK_KHR_get_physical_device_properties2,
        VK_KHR_maintenance1,
        VK_KHR_maintenance2,
        VK_KHR_maintenance3,
        VK_KHR_multiview,
        VK_KHR_relaxed_block_layout,
        VK_KHR_sampler_ycbcr_conversion,
        VK_KHR_shader_draw_parameters,
        VK_KHR_storage_buffer_storage_class,
        VK_KHR_variable_pointers;

        public static Vulkan11Extension getExtension(String name) {
            for (Vulkan11Extension e : values()) {
                if (e.name().contentEquals(name)) {
                    return e;
                }
            }
            return null;
        }

        @Override
        public String getName() {
            return this.name();
        }
    }

    enum Vulkan12Extension implements VulkanExtension {
        VK_KHR_8bit_storage,
        VK_KHR_buffer_device_address,
        VK_KHR_create_renderpass2,
        VK_KHR_depth_stencil_resolve,
        VK_KHR_draw_indirect_count,
        VK_KHR_driver_properties,
        VK_KHR_image_format_list,
        VK_KHR_imageless_framebuffer,
        VK_KHR_sampler_mirror_clamp_to_edge,
        VK_KHR_separate_depth_stencil_layouts,
        VK_KHR_shader_atomic_int64,
        VK_KHR_shader_float16_int8,
        VK_KHR_shader_float_controls,
        VK_KHR_shader_subgroup_extended_types,
        VK_KHR_spirv_1_4,
        VK_KHR_timeline_semaphore,
        VK_KHR_uniform_buffer_standard_layout,
        VK_KHR_vulkan_memory_model,
        VK_EXT_descriptor_indexing,
        VK_EXT_host_query_reset,
        VK_EXT_sampler_filter_minmax,
        VK_EXT_scalar_block_layout,
        VK_EXT_separate_stencil_usage,
        VK_EXT_shader_viewport_index_layer;

        @Override
        public String getName() {
            return this.name();
        }

        public static Vulkan12Extension getExtension(String name) {
            for (Vulkan12Extension e : values()) {
                if (e.name().contentEquals(name)) {
                    return e;
                }
            }
            return null;
        }

    }

    enum Vulkan13Extension implements VulkanExtension {
        VK_KHR_copy_commands2,
        VK_KHR_dynamic_rendering,
        VK_KHR_format_feature_flags2,
        VK_KHR_maintenance4,
        VK_KHR_shader_integer_dot_product,
        VK_KHR_shader_non_semantic_info,
        VK_KHR_shader_terminate_invocation,
        VK_KHR_synchronization2,
        VK_KHR_zero_initialize_workgroup_memory,
        VK_EXT_4444_formats,
        VK_EXT_extended_dynamic_state,
        VK_EXT_extended_dynamic_state2,
        VK_EXT_image_robustness,
        VK_EXT_inline_uniform_block,
        VK_EXT_pipeline_creation_cache_control,
        VK_EXT_pipeline_creation_feedback,
        VK_EXT_private_data,
        VK_EXT_shader_demote_to_helper_invocation,
        VK_EXT_subgroup_size_control,
        VK_EXT_texel_buffer_alignment,
        VK_EXT_texture_compression_astc_hdr,
        VK_EXT_tooling_info,
        VK_EXT_ycbcr_2plane_444_formats;

        @Override
        public String getName() {
            return this.name();
        }

        public static Vulkan13Extension getExtension(String name) {
            for (Vulkan13Extension e : values()) {
                if (e.name().contentEquals(name)) {
                    return e;
                }
            }
            return null;
        }

    }

}

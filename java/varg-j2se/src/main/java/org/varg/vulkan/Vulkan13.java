package org.varg.vulkan;

import org.gltfio.lib.BitFlag;

public interface Vulkan13 extends Vulkan12 {

    enum AccessFlagBits2 implements BitFlag {

        VK_ACCESS_2_NONE(0L),
        VK_ACCESS_2_NONE_KHR(0L),
        VK_ACCESS_2_INDIRECT_COMMAND_READ_BIT(0x00000001L),
        VK_ACCESS_2_INDIRECT_COMMAND_READ_BIT_KHR(0x00000001L),
        VK_ACCESS_2_INDEX_READ_BIT(0x00000002L),
        VK_ACCESS_2_INDEX_READ_BIT_KHR(0x00000002L),
        VK_ACCESS_2_VERTEX_ATTRIBUTE_READ_BIT(0x00000004L),
        VK_ACCESS_2_VERTEX_ATTRIBUTE_READ_BIT_KHR(0x00000004L),
        VK_ACCESS_2_UNIFORM_READ_BIT(0x00000008L),
        VK_ACCESS_2_UNIFORM_READ_BIT_KHR(0x00000008L),
        VK_ACCESS_2_INPUT_ATTACHMENT_READ_BIT(0x00000010L),
        VK_ACCESS_2_INPUT_ATTACHMENT_READ_BIT_KHR(0x00000010L),
        VK_ACCESS_2_SHADER_READ_BIT(0x00000020L),
        VK_ACCESS_2_SHADER_READ_BIT_KHR(0x00000020L),
        VK_ACCESS_2_SHADER_WRITE_BIT(0x00000040L),
        VK_ACCESS_2_SHADER_WRITE_BIT_KHR(0x00000040L),
        VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT(0x00000080L),
        VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT_KHR(0x00000080L),
        VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT(0x00000100L),
        VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT_KHR(0x00000100L),
        VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT(0x00000200L),
        VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT_KHR(0x00000200L),
        VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT(0x00000400L),
        VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT_KHR(0x00000400L),
        VK_ACCESS_2_TRANSFER_READ_BIT(0x00000800L),
        VK_ACCESS_2_TRANSFER_READ_BIT_KHR(0x00000800L),
        VK_ACCESS_2_TRANSFER_WRITE_BIT(0x00001000L),
        VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR(0x00001000L),
        VK_ACCESS_2_HOST_READ_BIT(0x00002000L),
        VK_ACCESS_2_HOST_READ_BIT_KHR(0x00002000L),
        VK_ACCESS_2_HOST_WRITE_BIT(0x00004000L),
        VK_ACCESS_2_HOST_WRITE_BIT_KHR(0x00004000L),
        VK_ACCESS_2_MEMORY_READ_BIT(0x00008000L),
        VK_ACCESS_2_MEMORY_READ_BIT_KHR(0x00008000L),
        VK_ACCESS_2_MEMORY_WRITE_BIT(0x00010000L),
        VK_ACCESS_2_MEMORY_WRITE_BIT_KHR(0x00010000L),
        VK_ACCESS_2_SHADER_SAMPLED_READ_BIT(0x100000000L),
        VK_ACCESS_2_SHADER_SAMPLED_READ_BIT_KHR(0x100000000L),
        VK_ACCESS_2_SHADER_STORAGE_READ_BIT(0x200000000L),
        VK_ACCESS_2_SHADER_STORAGE_READ_BIT_KHR(0x200000000L),
        VK_ACCESS_2_SHADER_STORAGE_WRITE_BIT(0x400000000L),
        VK_ACCESS_2_SHADER_STORAGE_WRITE_BIT_KHR(0x400000000L),
        // Provided by VK_KHR_video_decode_queue
        VK_ACCESS_2_VIDEO_DECODE_READ_BIT_KHR(0x800000000L),
        // Provided by VK_KHR_video_decode_queue
        VK_ACCESS_2_VIDEO_DECODE_WRITE_BIT_KHR(0x1000000000L),
        // Provided by VK_KHR_video_encode_queue
        VK_ACCESS_2_VIDEO_ENCODE_READ_BIT_KHR(0x2000000000L),
        // Provided by VK_KHR_video_encode_queue
        VK_ACCESS_2_VIDEO_ENCODE_WRITE_BIT_KHR(0x4000000000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_transform_feedback
        VK_ACCESS_2_TRANSFORM_FEEDBACK_WRITE_BIT_EXT(0x02000000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_transform_feedback
        VK_ACCESS_2_TRANSFORM_FEEDBACK_COUNTER_READ_BIT_EXT(0x04000000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_transform_feedback
        VK_ACCESS_2_TRANSFORM_FEEDBACK_COUNTER_WRITE_BIT_EXT(0x08000000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_conditional_rendering
        VK_ACCESS_2_CONDITIONAL_RENDERING_READ_BIT_EXT(0x00100000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_device_generated_commands
        VK_ACCESS_2_COMMAND_PREPROCESS_READ_BIT_NV(0x00020000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_device_generated_commands
        VK_ACCESS_2_COMMAND_PREPROCESS_WRITE_BIT_NV(0x00040000L),
        // Provided by VK_KHR_fragment_shading_rate with VK_KHR_synchronization2
        VK_ACCESS_2_FRAGMENT_SHADING_RATE_ATTACHMENT_READ_BIT_KHR(0x00800000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_shading_rate_image
        VK_ACCESS_2_SHADING_RATE_IMAGE_READ_BIT_NV(0x00800000L),
        // Provided by VK_KHR_acceleration_structure with VK_KHR_synchronization2
        VK_ACCESS_2_ACCELERATION_STRUCTURE_READ_BIT_KHR(0x00200000L),
        // Provided by VK_KHR_acceleration_structure with VK_KHR_synchronization2
        VK_ACCESS_2_ACCELERATION_STRUCTURE_WRITE_BIT_KHR(0x00400000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_ray_tracing
        VK_ACCESS_2_ACCELERATION_STRUCTURE_READ_BIT_NV(0x00200000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_ray_tracing
        VK_ACCESS_2_ACCELERATION_STRUCTURE_WRITE_BIT_NV(0x00400000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_fragment_density_map
        VK_ACCESS_2_FRAGMENT_DENSITY_MAP_READ_BIT_EXT(0x01000000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_blend_operation_advanced
        VK_ACCESS_2_COLOR_ATTACHMENT_READ_NONCOHERENT_BIT_EXT(0x00080000L),
        // Provided by VK_EXT_descriptor_buffer
        VK_ACCESS_2_DESCRIPTOR_BUFFER_READ_BIT_EXT(0x20000000000L),
        // Provided by VK_HUAWEI_invocation_mask
        VK_ACCESS_2_INVOCATION_MASK_READ_BIT_HUAWEI(0x8000000000L),
        // Provided by VK_KHR_ray_tracing_maintenance1 with VK_KHR_synchronization2 and VK_KHR_ray_tracing_pipeline
        VK_ACCESS_2_SHADER_BINDING_TABLE_READ_BIT_KHR(0x10000000000L),
        // Provided by VK_EXT_opacity_micromap
        VK_ACCESS_2_MICROMAP_READ_BIT_EXT(0x100000000000L),
        // Provided by VK_EXT_opacity_micromap
        VK_ACCESS_2_MICROMAP_WRITE_BIT_EXT(0x200000000000L),
        // Provided by VK_NV_optical_flow
        VK_ACCESS_2_OPTICAL_FLOW_READ_BIT_NV(0x40000000000L),
        // Provided by VK_NV_optical_flow
        VK_ACCESS_2_OPTICAL_FLOW_WRITE_BIT_NV(0x80000000000L);

        public final long value;

        AccessFlagBits2(long value) {
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

    enum PipelineStateFlagBits2 implements BitFlag {

        VK_PIPELINE_STAGE_2_NONE(0),
        VK_PIPELINE_STAGE_2_NONE_KHR(0),
        VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT(0x00000001),
        VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT_KHR(0x00000001),
        VK_PIPELINE_STAGE_2_DRAW_INDIRECT_BIT(0x00000002),
        VK_PIPELINE_STAGE_2_DRAW_INDIRECT_BIT_KHR(0x00000002),
        VK_PIPELINE_STAGE_2_VERTEX_INPUT_BIT(0x00000004),
        VK_PIPELINE_STAGE_2_VERTEX_INPUT_BIT_KHR(0x00000004),
        VK_PIPELINE_STAGE_2_VERTEX_SHADER_BIT(0x00000008),
        VK_PIPELINE_STAGE_2_VERTEX_SHADER_BIT_KHR(0x00000008),
        VK_PIPELINE_STAGE_2_TESSELLATION_CONTROL_SHADER_BIT(0x00000010),
        VK_PIPELINE_STAGE_2_TESSELLATION_CONTROL_SHADER_BIT_KHR(0x00000010),
        VK_PIPELINE_STAGE_2_TESSELLATION_EVALUATION_SHADER_BIT(0x00000020),
        VK_PIPELINE_STAGE_2_TESSELLATION_EVALUATION_SHADER_BIT_KHR(0x00000020),
        VK_PIPELINE_STAGE_2_GEOMETRY_SHADER_BIT(0x00000040),
        VK_PIPELINE_STAGE_2_GEOMETRY_SHADER_BIT_KHR(0x00000040),
        VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT(0x00000080),
        VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT_KHR(0x00000080),
        VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT(0x00000100),
        VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT_KHR(0x00000100),
        VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT(0x00000200),
        VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT_KHR(0x00000200),
        VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT(0x00000400),
        VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT_KHR(0x00000400),
        VK_PIPELINE_STAGE_2_COMPUTE_SHADER_BIT(0x00000800),
        VK_PIPELINE_STAGE_2_COMPUTE_SHADER_BIT_KHR(0x00000800),
        VK_PIPELINE_STAGE_2_ALL_TRANSFER_BIT(0x00001000),
        VK_PIPELINE_STAGE_2_ALL_TRANSFER_BIT_KHR(0x00001000),
        VK_PIPELINE_STAGE_2_TRANSFER_BIT(0x00001000),
        VK_PIPELINE_STAGE_2_TRANSFER_BIT_KHR(0x00001000),
        VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT(0x00002000),
        VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT_KHR(0x00002000),
        VK_PIPELINE_STAGE_2_HOST_BIT(0x00004000),
        VK_PIPELINE_STAGE_2_HOST_BIT_KHR(0x00004000),
        VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT(0x00008000),
        VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT_KHR(0x00008000),
        VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT(0x00010000),
        VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT_KHR(0x00010000),
        VK_PIPELINE_STAGE_2_COPY_BIT(0x100000000L),
        VK_PIPELINE_STAGE_2_COPY_BIT_KHR(0x100000000L),
        VK_PIPELINE_STAGE_2_RESOLVE_BIT(0x200000000L),
        VK_PIPELINE_STAGE_2_RESOLVE_BIT_KHR(0x200000000L),
        VK_PIPELINE_STAGE_2_BLIT_BIT(0x400000000L),
        VK_PIPELINE_STAGE_2_BLIT_BIT_KHR(0x400000000L),
        VK_PIPELINE_STAGE_2_CLEAR_BIT(0x800000000L),
        VK_PIPELINE_STAGE_2_CLEAR_BIT_KHR(0x800000000L),
        VK_PIPELINE_STAGE_2_INDEX_INPUT_BIT(0x1000000000L),
        VK_PIPELINE_STAGE_2_INDEX_INPUT_BIT_KHR(0x1000000000L),
        VK_PIPELINE_STAGE_2_VERTEX_ATTRIBUTE_INPUT_BIT(0x2000000000L),
        VK_PIPELINE_STAGE_2_VERTEX_ATTRIBUTE_INPUT_BIT_KHR(0x2000000000L),
        VK_PIPELINE_STAGE_2_PRE_RASTERIZATION_SHADERS_BIT(0x4000000000L),
        VK_PIPELINE_STAGE_2_PRE_RASTERIZATION_SHADERS_BIT_KHR(0x4000000000L),
        // Provided by VK_KHR_video_decode_queue
        VK_PIPELINE_STAGE_2_VIDEO_DECODE_BIT_KHR(0x04000000L),
        // Provided by VK_KHR_video_encode_queue
        VK_PIPELINE_STAGE_2_VIDEO_ENCODE_BIT_KHR(0x08000000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_transform_feedback
        VK_PIPELINE_STAGE_2_TRANSFORM_FEEDBACK_BIT_EXT(0x01000000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_conditional_rendering
        VK_PIPELINE_STAGE_2_CONDITIONAL_RENDERING_BIT_EXT(0x00040000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_device_generated_commands
        VK_PIPELINE_STAGE_2_COMMAND_PREPROCESS_BIT_NV(0x00020000L),
        // Provided by VK_KHR_fragment_shading_rate with VK_KHR_synchronization2
        VK_PIPELINE_STAGE_2_FRAGMENT_SHADING_RATE_ATTACHMENT_BIT_KHR(0x00400000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_shading_rate_image
        VK_PIPELINE_STAGE_2_SHADING_RATE_IMAGE_BIT_NV(0x00400000L),
        // Provided by VK_KHR_acceleration_structure with VK_KHR_synchronization2
        VK_PIPELINE_STAGE_2_ACCELERATION_STRUCTURE_BUILD_BIT_KHR(0x02000000L),
        // Provided by VK_KHR_ray_tracing_pipeline with VK_KHR_synchronization2
        VK_PIPELINE_STAGE_2_RAY_TRACING_SHADER_BIT_KHR(0x00200000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_ray_tracing
        VK_PIPELINE_STAGE_2_RAY_TRACING_SHADER_BIT_NV(0x00200000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_ray_tracing
        VK_PIPELINE_STAGE_2_ACCELERATION_STRUCTURE_BUILD_BIT_NV(0x02000000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_fragment_density_map
        VK_PIPELINE_STAGE_2_FRAGMENT_DENSITY_PROCESS_BIT_EXT(0x00800000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_mesh_shader
        VK_PIPELINE_STAGE_2_TASK_SHADER_BIT_NV(0x00080000L),
        // Provided by VK_KHR_synchronization2 with VK_NV_mesh_shader
        VK_PIPELINE_STAGE_2_MESH_SHADER_BIT_NV(0x00100000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_mesh_shader
        VK_PIPELINE_STAGE_2_TASK_SHADER_BIT_EXT(0x00080000L),
        // Provided by VK_KHR_synchronization2 with VK_EXT_mesh_shader
        VK_PIPELINE_STAGE_2_MESH_SHADER_BIT_EXT(0x00100000L),
        // Provided by VK_HUAWEI_subpass_shading
        VK_PIPELINE_STAGE_2_SUBPASS_SHADER_BIT_HUAWEI(0x8000000000L),
        // Provided by VK_HUAWEI_subpass_shading
        VK_PIPELINE_STAGE_2_SUBPASS_SHADING_BIT_HUAWEI(0x8000000000L),
        // Provided by VK_HUAWEI_invocation_mask
        VK_PIPELINE_STAGE_2_INVOCATION_MASK_BIT_HUAWEI(0x10000000000L),
        // Provided by VK_KHR_ray_tracing_maintenance1 with VK_KHR_synchronization2
        VK_PIPELINE_STAGE_2_ACCELERATION_STRUCTURE_COPY_BIT_KHR(0x10000000),
        // Provided by VK_EXT_opacity_micromap
        VK_PIPELINE_STAGE_2_MICROMAP_BUILD_BIT_EXT(0x40000000),
        // Provided by VK_HUAWEI_cluster_culling_shader
        VK_PIPELINE_STAGE_2_CLUSTER_CULLING_SHADER_BIT_HUAWEI(0x20000000000L),
        // Provided by VK_NV_optical_flow
        VK_PIPELINE_STAGE_2_OPTICAL_FLOW_BIT_NV(0x20000000);

        public final long value;

        PipelineStateFlagBits2(long value) {
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

}
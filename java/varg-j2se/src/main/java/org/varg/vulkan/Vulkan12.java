package org.varg.vulkan;

import org.gltfio.lib.BitFlag;

public interface Vulkan12 extends Vulkan10 {

    enum MemoryAllocateFlagBits implements BitFlag {
        VK_MEMORY_ALLOCATE_DEVICE_MASK_BIT(0x00000001),
        // Provided by VK_VERSION_1_2
        VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT(0x00000002),
        // Provided by VK_VERSION_1_2
        VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_CAPTURE_REPLAY_BIT(0x00000004),
        // Provided by VK_KHR_device_group
        VK_MEMORY_ALLOCATE_DEVICE_MASK_BIT_KHR(VK_MEMORY_ALLOCATE_DEVICE_MASK_BIT.value),
        // Provided by VK_KHR_buffer_device_address
        VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT_KHR(VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT.value),
        // Provided by VK_KHR_buffer_device_address
        VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_CAPTURE_REPLAY_BIT_KHR(VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_CAPTURE_REPLAY_BIT.value);

        public final int value;

        MemoryAllocateFlagBits(int value) {
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

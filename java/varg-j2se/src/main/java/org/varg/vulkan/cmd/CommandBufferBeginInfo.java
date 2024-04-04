
package org.varg.vulkan.cmd;

import org.varg.vulkan.Vulkan10.CommandBufferUsageFlagBits;

public class CommandBufferBeginInfo {

    final CommandBufferUsageFlagBits[] flags;
    final CommandBufferInheritanceInfo pInheritanceInfo;

    public CommandBufferBeginInfo(CommandBufferUsageFlagBits[] flags, CommandBufferInheritanceInfo inheritanceInfo) {
        this.flags = flags;
        this.pInheritanceInfo = inheritanceInfo;
    }

}

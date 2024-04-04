package org.varg.vulkan.structs;

/**
 * Wrapper for VkQueryPool
 */
public class QueryPool extends Handle<QueryPoolCreateInfo> {

    public QueryPool(long handle, QueryPoolCreateInfo createInfo) {
        super(handle, createInfo);
    }

}

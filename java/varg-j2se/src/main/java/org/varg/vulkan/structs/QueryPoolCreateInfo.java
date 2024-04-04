package org.varg.vulkan.structs;

import org.varg.vulkan.Vulkan10.QueryPipelineStatisticFlagBits;
import org.varg.vulkan.Vulkan10.QueryType;

/**
 * Wrapper for VkQueryPoolCreateInfo
 */
public class QueryPoolCreateInfo {

    public final QueryType queryType;
    public final int queryCount;
    public final QueryPipelineStatisticFlagBits[] pipelineStatistics;

    public QueryPoolCreateInfo(QueryType queryType, int queryCount, QueryPipelineStatisticFlagBits... pipelineStatistics) {
        this.queryType = queryType;
        this.queryCount = queryCount;
        this.pipelineStatistics = pipelineStatistics;
    }

}

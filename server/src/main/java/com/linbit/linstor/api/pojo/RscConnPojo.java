package com.linbit.linstor.api.pojo;

import java.util.Map;
import java.util.UUID;

import com.linbit.linstor.ResourceConnection;

public class RscConnPojo implements ResourceConnection.RscConnApi
{
    private final UUID uuid;
    private final String sourceNodeName;
    private final String targetNodeName;
    private final String rscName;
    private final Map<String, String> props;
    private final long flags;
    private final Integer port;

    public RscConnPojo(
        UUID uuidRef,
        String sourceNodeNameRef,
        String targetNodeNameRef,
        String rscNameRef,
        Map<String, String> propsRef,
        long flagRef,
        Integer portRef
    )
    {
        this.uuid = uuidRef;
        this.sourceNodeName = sourceNodeNameRef;
        this.targetNodeName = targetNodeNameRef;
        this.rscName = rscNameRef;
        this.props = propsRef;
        this.flags = flagRef;
        this.port = portRef;
    }

    @Override
    public UUID getUuid()
    {
        return uuid;
    }

    @Override
    public String getSourceNodeName()
    {
        return sourceNodeName;
    }

    @Override
    public String getTargetNodeName()
    {
        return targetNodeName;
    }

    @Override
    public String getResourceName()
    {
        return rscName;
    }

    @Override
    public Map<String, String> getProps()
    {
        return props;
    }

    @Override
    public long getFlags()
    {
        return flags;
    }

    @Override
    public Integer getPort()
    {
        return port;
    }
}

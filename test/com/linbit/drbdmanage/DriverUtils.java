package com.linbit.drbdmanage;

public class DriverUtils
{
    public static void clearCaches()
    {
        ConnectionDefinitionDataDerbyDriver.clearCache();
        NetInterfaceDataDerbyDriver.clearCache();
        NodeDataDerbyDriver.clearCache();
        ResourceDataDerbyDriver.clearCache();
        ResourceDefinitionDataDerbyDriver.clearCache();
        StorPoolDataDerbyDriver.clearCache();
        StorPoolDefinitionDataDerbyDriver.clearCache();
        VolumeDataDerbyDriver.clearCache();
        VolumeDefinitionDataDerbyDriver.clearCache();
    }
}
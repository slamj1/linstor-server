package com.linbit.linstor.storage;

import com.linbit.fsevent.FileSystemWatch;
import com.linbit.linstor.core.StltConfigAccessor;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.storage.utils.RestHttpClient;
import com.linbit.linstor.timer.CoreTimer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SwordfishTargetDriverKind implements StorageDriverKind
{
    @Override
    public String getDriverName()
    {
        return  SwordfishTargetDriver.class.getSimpleName();
    }

    @Override
    public StorageDriver makeStorageDriver(
        ErrorReporter errorReporter,
        FileSystemWatch fileSystemWatch,
        CoreTimer timer,
        StltConfigAccessor stltCfgAccessor
    )
    {
        return new SwordfishTargetDriver(
            errorReporter,
            this,
            new RestHttpClient(errorReporter),
            stltCfgAccessor
        );
    }

    @Override
    public Map<String, String> getStaticTraits()
    {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getConfigurationKeys()
    {
        final HashSet<String> keySet = new HashSet<>();

        keySet.add(StorageConstants.CONFIG_SF_URL_KEY);
        keySet.add(StorageConstants.CONFIG_SF_COMPOSED_NODE_NAME_KEY);
        keySet.add(StorageConstants.CONFIG_SF_STOR_POOL_KEY);
        keySet.add(StorageConstants.CONFIG_SF_POLL_TIMEOUT_VLM_CRT_KEY);
        keySet.add(StorageConstants.CONFIG_SF_POLL_RETRIES_VLM_CRT_KEY);
        keySet.add(StorageConstants.CONFIG_SF_POLL_TIMEOUT_ATTACH_VLM_KEY);
        keySet.add(StorageConstants.CONFIG_SF_POLL_RETRIES_ATTACH_VLM_KEY);
        keySet.add(StorageConstants.CONFIG_SF_POLL_TIMEOUT_GREP_NVME_UUID_KEY);
        keySet.add(StorageConstants.CONFIG_SF_POLL_RETRIES_GREP_NVME_UUID_KEY);
        keySet.add(StorageConstants.CONFIG_SF_STOR_SVC_KEY);
        keySet.add(StorageConstants.CONFIG_SF_USER_NAME_KEY);
        keySet.add(StorageConstants.CONFIG_SF_USER_PW_KEY);

        return keySet;
    }

    @Override
    public boolean isSnapshotSupported()
    {
        return false;
    }

    @Override
    public boolean hasBackingStorage()
    {
        return true;
    }

    @Override
    public boolean supportsDrbd()
    {
        return false;
    }
}

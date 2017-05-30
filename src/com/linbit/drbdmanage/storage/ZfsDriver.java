package com.linbit.drbdmanage.storage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.linbit.Checks;
import com.linbit.ChildProcessTimeoutException;
import com.linbit.InvalidNameException;
import com.linbit.extproc.ExtCmd;
import com.linbit.extproc.ExtCmd.OutputData;

public class ZfsDriver extends AbsStorageDriver
{
    public static final String ZFS_POOL_DEFAULT = "drbdpool";
    public static final String ZFS_COMMAND_DEFAULT = "zfs";

    protected String zfsCommand = ZFS_COMMAND_DEFAULT;

    protected String pool = ZFS_POOL_DEFAULT;

    public ZfsDriver()
    {
    }

    ZfsDriver(ExtCmd ec)
    {
        extCommand = ec;
    }

    @Override
    public Map<String, String> getTraits() throws StorageException
    {
        final HashMap<String, String> traits = new HashMap<>();
        traits.put(DriverTraits.KEY_PROV, DriverTraits.PROV_FAT);
        traits.put(DriverTraits.KEY_ALLOC_UNIT, String.valueOf(getExtentSize()));

        return traits;
    }

    @Override
    public Set<String> getConfigurationKeys()
    {
        HashSet<String> keys = new HashSet<>();
        keys.add(StorageConstants.CONFIG_ZFS_POOL_KEY);
        keys.add(StorageConstants.CONFIG_ZFS_COMMAND_KEY);
        keys.add(StorageConstants.CONFIG_SIZE_ALIGN_TOLERANCE_KEY);

        //TODO zfs offers a lot of editable properties, for example recordsize..

        return keys;
    }

    @Override
    protected String getExpectedVolumePath(String identifier)
    {
        return File.separator + "dev" +
            File.separator + "zvol" +
            File.separator + pool +
            File.separator + identifier;
    }

    @Override
    protected VolumeInfo getVolumeInfo(String identifier) throws StorageException
    {
        try
        {
            return ZfsVolumeInfo.getInfo(extCommand, zfsCommand, pool, identifier);
        }
        catch (ChildProcessTimeoutException | IOException e)
        {
            throw new StorageException(
                String.format("Could not get Volume info for pool [%s] and identifier [%s]",
                    pool,
                    identifier
                ),
                e
            );
        }
    }

    @Override
    protected long getExtentSize() throws StorageException
    {
        final String[] command = new String[]
        {
            zfsCommand,
            "get", "recordsize", //TODO check if recordsize really is the extent size
            "-o", "value",
            "-Hp",
            pool
        };

        try
        {
            OutputData outputData = extCommand.exec(command);

            checkExitCode(outputData, command);

            String strBlockSize = new String(outputData.stdoutData);
            return Long.parseLong(strBlockSize.trim()) >> 10; // we have to return extent size in KiB
        }
        catch (ChildProcessTimeoutException | IOException exc)
        {
            throw new StorageException(
                String.format("Could not determine block size. Command: %s",
                    glue(command, " ")
                ),
                exc
            );
        }
    }

    @Override
    protected void checkConfiguration(Map<String, String> config) throws StorageException
    {
        checkCommand(config, StorageConstants.CONFIG_ZFS_COMMAND_KEY);
        checkPool(config);
        checkToleranceFactor(config);
    }


    @Override
    protected void applyConfiguration(Map<String, String> config)
    {
        zfsCommand = getAsString(config, StorageConstants.CONFIG_ZFS_COMMAND_KEY, zfsCommand);
        pool = getAsString(config, StorageConstants.CONFIG_ZFS_POOL_KEY, pool);
        sizeAlignmentToleranceFactor = uncheckedGetAsInt(config, StorageConstants.CONFIG_SIZE_ALIGN_TOLERANCE_KEY, sizeAlignmentToleranceFactor);
    }

    @Override
    protected String[] getCreateCommand(String identifier, long size)
    {
        return new String[]
        {
            zfsCommand,
            "create",
            "-V", size + "KB",
            pool + File.separator + identifier
        };
    }

    @Override
    protected String[] getDeleteCommand(String identifier)
    {
        return new String[]
        {
            zfsCommand,
            "destroy",
            "-f",  // force
            "-r",  // also delete snapshots of this volume
            pool + File.separator + identifier
        };
    }

    @Override
    public boolean isSnapshotSupported()
    {
        return true;
    }

    @Override
    public void createSnapshot(String identifier, String snapshotName) throws StorageException
    {
        super.createSnapshot(identifier, snapshotName);
        super.cloneSnapshot(identifier + "@" + snapshotName, snapshotName);
    }

    @Override
    protected String[] getCreateSnapshotCommand(String identifier, String snapshotName)
    {
        final String zfsSnapName = pool + File.separator + identifier + "@" + snapshotName;
        final String[] command = new String[]
        {
            zfsCommand,
            "snapshot", zfsSnapName
        };
        return command;
    }

    @Override
    protected String[] getCloneSnapshotCommand(String snapshotSource, String snapshotTarget) throws StorageException
    {
        String origin;
        if (snapshotSource.contains("@"))
        {
            origin = pool + File.separator + snapshotSource;
        }
        else
        {
            final String[] getSnapshotsOriginCommand = new String[]
            {
                zfsCommand,
                "get", "origin",
                pool + File.separator + snapshotSource,
                "-o", "value",
                "-H"
            };

            try
            {
                OutputData originData = extCommand.exec(getSnapshotsOriginCommand);
                checkExitCode(originData, getSnapshotsOriginCommand);
                String[] lines = new String(originData.stdoutData).split("\n");
                if (lines.length > 1)
                {
                    throw new StorageException(String.format("Zfs snapshot [%s] has unexpectedly multiple origins...", snapshotSource));
                }
                origin = lines[0];
            }
            catch (ChildProcessTimeoutException | IOException exc)
            {
                throw new StorageException(
                    String.format("Could not determine snapshots origin. Command: %s",
                        glue(getSnapshotsOriginCommand, " ")
                    ),
                    exc
                );
            }
        }

        return new String[]
        {
            zfsCommand,
            "clone",
            origin,
            pool + File.separator + snapshotTarget
        };
    }

    @Override
    protected String[] getDeleteSnapshotCommand(String snapshotName)
    {
        return new String[]
        {
            zfsCommand,
            "destroy",
            pool + File.separator + snapshotName
        };
    }

    protected void checkPool(Map<String, String> config) throws StorageException
    {
        String value = config.get(StorageConstants.CONFIG_ZFS_POOL_KEY);
        if (value != null)
        {
            final String pool = value.trim();

            try
            {
                Checks.nameCheck(
                    pool,
                    1,
                    Integer.MAX_VALUE,
                    VALID_CHARS,
                    VALID_INNER_CHARS
                );
            }
            catch (InvalidNameException ine)
            {
                // TODO: Detailed error reporting
                throw new StorageException(
                    String.format("Invalid volumeName [%s]", pool),
                    ine
                );
            }
            try
            {
                final String[] volumeGroupCheckCommand = new String[]
                {
                    zfsCommand,
                    "list",
                    "-H", // no headers
                    "-o", "name", // name column only
                    pool
                };

                final OutputData output = extCommand.exec(volumeGroupCheckCommand);
                if (output.exitCode != 0)
                {
                    // TODO: Detailed error reporting
                    throw new StorageException(
                        String.format("Zfs pool [%s] not found.", pool)
                    );
                }
            }
            catch (ChildProcessTimeoutException | IOException exc)
            {
                // TODO: Detailed error reporting
                throw new StorageException(
                    String.format("Could not run zfs list %s", pool),
                    exc
                );
            }
        }
    }
}
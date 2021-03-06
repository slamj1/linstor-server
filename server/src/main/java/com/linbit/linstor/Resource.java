package com.linbit.linstor;

import com.linbit.linstor.propscon.Props;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.ObjectProtection;
import com.linbit.linstor.stateflags.Flags;
import com.linbit.linstor.stateflags.FlagsHelper;
import com.linbit.linstor.stateflags.StateFlags;
import com.linbit.linstor.transaction.TransactionObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface Resource extends TransactionObject, DbgInstanceUuid, Comparable<Resource>
{
    UUID getUuid();

    ObjectProtection getObjProt();

    ResourceDefinition getDefinition();

    Volume getVolume(VolumeNumber volNr);

    int getVolumeCount();

    Iterator<Volume> iterateVolumes();

    Stream<Volume> streamVolumes();

    Node getAssignedNode();

    NodeId getNodeId();

    Stream<ResourceConnection> streamResourceConnections(AccessContext accCtx)
        throws AccessDeniedException;

    ResourceConnection getResourceConnection(AccessContext accCtx, Resource otherResource)
        throws AccessDeniedException;

    void setResourceConnection(AccessContext accCtx, ResourceConnection resCon)
        throws AccessDeniedException;

    void removeResourceConnection(AccessContext accCtx, ResourceConnection resCon)
        throws AccessDeniedException;

    Props getProps(AccessContext accCtx)
        throws AccessDeniedException;

    StateFlags<RscFlags> getStateFlags();

    /**
     * Whether peers should treat this resource as diskless.
     */
    boolean disklessForPeers(AccessContext accCtx)
        throws AccessDeniedException;

    void markDeleted(AccessContext accCtx)
        throws AccessDeniedException, SQLException;

    void delete(AccessContext accCtx)
        throws AccessDeniedException, SQLException;

    boolean isDeleted();

    boolean supportsDrbd(AccessContext accCtx)
        throws AccessDeniedException;

    RscApi getApiData(AccessContext accCtx, Long fullSyncId, Long updateId)
        throws AccessDeniedException;

    boolean isCreatePrimary();

    boolean isDiskless(AccessContext accCtx)
        throws AccessDeniedException;

    /**
     * Returns the identification key without checking if "this" is already deleted
     */
    Key getKey();

    @Override
    default int compareTo(Resource otherRsc)
    {
        int eq = getAssignedNode().compareTo(otherRsc.getAssignedNode());
        if (eq == 0)
        {
            eq = getDefinition().compareTo(otherRsc.getDefinition());
        }
        return eq;
    }

    static String getStringId(Resource rsc)
    {
        return rsc.getAssignedNode().getName().value + "/" +
               rsc.getDefinition().getName().value;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    enum RscFlags implements Flags
    {
        CLEAN(1L << 0),
        DELETE(1L << 1),
        DISKLESS(1L << 2),
        DISK_ADD_REQUESTED(1L << 3),
        DISK_ADDING(1L << 4),
        DISK_REMOVE_REQUESTED(1L << 5),
        DISK_REMOVING(1L << 6);

        public final long flagValue;

        RscFlags(long value)
        {
            flagValue = value;
        }

        @Override
        public long getFlagValue()
        {
            return flagValue;
        }

        public static RscFlags[] restoreFlags(long rscFlags)
        {
            List<RscFlags> flagList = new ArrayList<>();
            for (RscFlags flag : RscFlags.values())
            {
                if ((rscFlags & flag.flagValue) == flag.flagValue)
                {
                    flagList.add(flag);
                }
            }
            return flagList.toArray(new RscFlags[flagList.size()]);
        }

        public static List<String> toStringList(long flagsMask)
        {
            return FlagsHelper.toStringList(RscFlags.class, flagsMask);
        }

        public static long fromStringList(List<String> listFlags)
        {
            return FlagsHelper.fromStringList(RscFlags.class, listFlags);
        }
    }

    public interface RscApi
    {
        UUID getUuid();
        String getName();
        UUID getNodeUuid();
        String getNodeName();
        UUID getRscDfnUuid();
        Map<String, String> getProps();
        long getFlags();
        List<? extends Volume.VlmApi> getVlmList();
        Integer getLocalRscNodeId();
    }

    /**
     * Identifies a resource globally.
     */
    class Key implements Comparable<Key>
    {
        private final ResourceName resourceName;

        private final NodeName nodeName;

        public Key(Resource resource)
        {
            this(resource.getDefinition().getName(), resource.getAssignedNode().getName());
        }

        public Key(ResourceName resourceNameRef, NodeName nodeNameRef)
        {
            resourceName = resourceNameRef;
            nodeName = nodeNameRef;
        }

        public ResourceName getResourceName()
        {
            return resourceName;
        }

        public NodeName getNodeName()
        {
            return nodeName;
        }

        @Override
        // Code style exception: Automatically generated code
        @SuppressWarnings({"DescendantToken", "ParameterName"})
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            Key that = (Key) o;
            return Objects.equals(resourceName, that.resourceName) &&
                Objects.equals(nodeName, that.nodeName);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(resourceName, nodeName);
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compareTo(Key other)
        {
            int eq = resourceName.compareTo(other.resourceName);
            if (eq == 0)
            {
                eq = nodeName.compareTo(other.nodeName);
            }
            return eq;
        }
    }

    public interface InitMaps
    {
        Map<Resource.Key, ResourceConnection> getRscConnMap();
        Map<VolumeNumber, Volume> getVlmMap();
    }
}
